package br.com.thomas.library.inventory_service.service;

import br.com.thomas.library.inventory_service.dto.propagation.BookAvailabilityPayload;
import br.com.thomas.library.inventory_service.dto.rental.ReserveResultPayload;
import br.com.thomas.library.inventory_service.dto.rental.RentalReservePayload;
import br.com.thomas.library.inventory_service.dto.rental.RentalReturnPayload;
import br.com.thomas.library.inventory_service.dto.rental.ReturnResultPayload;
import br.com.thomas.library.inventory_service.model.EventStatus;
import br.com.thomas.library.inventory_service.service.propagation.InventoryPropagationService;
import br.com.thomas.library.inventory_service.model.Inventory;
import br.com.thomas.library.inventory_service.model.ProcessedEvent;
import br.com.thomas.library.inventory_service.model.RentalOperationEnum;
import br.com.thomas.library.inventory_service.repository.InventoryRepository;
import br.com.thomas.library.inventory_service.repository.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Processa eventos de rental (reserva e devolução) com idempotência por eventId.
 * <p>
 * Bloqueio otimista: a entidade Inventory possui @Version; no save() o JPA inclui a versão no UPDATE.
 * Se outra transação alterou o mesmo registro, ocorre ObjectOptimisticLockingFailureException (já tratada
 * no GlobalExceptionHandler para REST; no consumo RabbitMQ a exceção propaga e a mensagem pode ser reentregue).
 * <p>
 * Coluna mensagem de erro em tb_evento_processado: sim, vale a pena. Permite auditoria e diagnóstico
 * (ex.: "Cópias disponíveis (3) insuficientes para reservar 5") sem depender só de logs; útil para o
 * rental-service ou operação investigar rejeições. Não implementada neste momento por decisão de escopo.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RentalEventService {
//TODO: fazer um template method porque os metodos são todos iguais, só a validação de disponibilidade e a operação que mudam

    private final InventoryRepository inventoryRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final InventoryPropagationService propagationService;

    public static final String REJECT_REASON_INVENTORY_NOT_FOUND = "INVENTORY_NOT_FOUND";
    public static final String REJECT_REASON_INSUFFICIENT_STOCK = "INSUFFICIENT_STOCK";
    public static final String REJECT_REASON_MISSING_EVENT_ID = "MISSING_EVENT_ID";
    public static final String REJECT_REASON_MISSING_USER_ID = "MISSING_USER_ID";
    public static final String REJECT_REASON_INVALID_BOOK_OR_QUANTITY = "INVALID_BOOK_OR_QUANTITY";
    public static final String REJECT_REASON_INSUFFICIENT_RESERVED = "INSUFFICIENT_RESERVED";

    @Transactional
    public void processReserve(RentalReservePayload payload) {
        LocalDateTime processedAt = LocalDateTime.now();
        if (payload == null) {
            log.warn("Evento de reserva ignorado: payload nulo");
            return;
        }
        if (payload.getEventId() == null || payload.getEventId().isBlank()) {
            log.warn("Evento de reserva ignorado: eventId nulo ou vazio");
            publishReserveResultIfPresent(buildReserveResultPayload(payload, false, REJECT_REASON_MISSING_EVENT_ID, processedAt));
            return;
        }
        if (processedEventRepository.findByEventId(payload.getEventId()).isPresent()) {
            log.debug("Evento de reserva já processado (idempotência): eventId={}", payload.getEventId());
            return;
        }
        if (payload.getUserId() == null) {
            log.warn("Evento de reserva rejeitado: userId nulo (todo evento de aluguel deve vir com userId)");
            publishReserveResultIfPresent(buildReserveResultPayload(payload, false, REJECT_REASON_MISSING_USER_ID, processedAt));
            return;
        }
        if (payload.getBookId() == null || payload.getQuantity() == null || payload.getQuantity() < 1) {
            log.warn("Evento de reserva ignorado: bookId ou quantity inválido");
            publishReserveResultIfPresent(buildReserveResultPayload(payload, false, REJECT_REASON_INVALID_BOOK_OR_QUANTITY, processedAt));
            return;
        }
        Inventory inv = inventoryRepository.findByBookIdAndActiveTrue(payload.getBookId()).orElse(null);
        if (inv == null) {
            saveEvent(payload.getEventId(), payload.getRentalId(), payload.getUserId(), payload.getBookId(), RentalOperationEnum.RESERVA, payload.getQuantity(), EventStatus.REJEITADO);
            log.warn("Evento de reserva rejeitado: inventário não encontrado ou inativo para bookId={}", payload.getBookId());
            publishReserveResultIfPresent(buildReserveResultPayload(payload, false, REJECT_REASON_INVENTORY_NOT_FOUND, processedAt));
            return;
        }
        if (inv.getAvailableCopies() < payload.getQuantity()) {
            saveEvent(payload.getEventId(), payload.getRentalId(), payload.getUserId(), payload.getBookId(), RentalOperationEnum.RESERVA, payload.getQuantity(), EventStatus.REJEITADO);
            log.warn("Evento de reserva rejeitado: cópias disponíveis ({}) insuficientes para reservar {}", inv.getAvailableCopies(), payload.getQuantity());
            publishReserveResultIfPresent(buildReserveResultPayload(payload, false, REJECT_REASON_INSUFFICIENT_STOCK, processedAt));
            return;
        }
        inv.reserve(payload.getQuantity());
        inventoryRepository.save(inv);
        saveEvent(payload.getEventId(), payload.getRentalId(), payload.getUserId(), payload.getBookId(), RentalOperationEnum.RESERVA, payload.getQuantity(), EventStatus.PROCESSADO);
        propagationService.publishAvailability(toAvailabilityPayload(inv));
        log.info("Reserva processada: eventId={}, bookId={}, quantity={}", payload.getEventId(), payload.getBookId(), payload.getQuantity());
        publishReserveResultIfPresent(buildReserveResultPayload(payload, true, null, processedAt));
    }

    private void publishReserveResultIfPresent(ReserveResultPayload result) {
        if (result != null) {
            propagationService.publishReserveResult(result);
        }
    }

    @Transactional
    public void processReturn(RentalReturnPayload payload) {
        LocalDateTime processedAt = LocalDateTime.now();
        if (payload == null || payload.getEventId() == null) {
            log.warn("Evento de devolução ignorado: payload ou eventId nulo");
            return;
        }
        if (processedEventRepository.findByEventId(payload.getEventId()).isPresent()) {
            log.debug("Evento de devolução já processado (idempotência): eventId={}", payload.getEventId());
            return;
        }
        if (payload.getUserId() == null) {
            log.warn("Evento de devolução rejeitado: userId nulo (todo evento de aluguel deve vir com userId)");
            publishReturnResultIfPresent(buildReturnResultPayload(payload, false, REJECT_REASON_MISSING_USER_ID, processedAt));
            return;
        }
        if (payload.getBookId() == null || payload.getQuantity() == null || payload.getQuantity() < 1) {
            log.warn("Evento de devolução ignorado: bookId ou quantity inválido");
            publishReturnResultIfPresent(buildReturnResultPayload(payload, false, REJECT_REASON_INVALID_BOOK_OR_QUANTITY, processedAt));
            return;
        }
        Inventory inv = inventoryRepository.findByBookIdAndActiveTrue(payload.getBookId()).orElse(null);
        if (inv == null) {
            saveEvent(payload.getEventId(), payload.getRentalId(), payload.getUserId(), payload.getBookId(), RentalOperationEnum.DEVOLUCAO, payload.getQuantity(), EventStatus.REJEITADO);
            log.warn("Evento de devolução rejeitado: inventário não encontrado ou inativo para bookId={}", payload.getBookId());
            publishReturnResultIfPresent(buildReturnResultPayload(payload, false, REJECT_REASON_INVENTORY_NOT_FOUND, processedAt));
            return;
        }
        if (inv.getReservedCopies() < payload.getQuantity()) {
            saveEvent(payload.getEventId(), payload.getRentalId(), payload.getUserId(), payload.getBookId(), RentalOperationEnum.DEVOLUCAO, payload.getQuantity(), EventStatus.REJEITADO);
            log.warn("Evento de devolução rejeitado: cópias reservadas ({}) insuficientes para devolver {}", inv.getReservedCopies(), payload.getQuantity());
            publishReturnResultIfPresent(buildReturnResultPayload(payload, false, REJECT_REASON_INSUFFICIENT_RESERVED, processedAt));
            return;
        }
        inv.release(payload.getQuantity());
        inventoryRepository.save(inv);
        saveEvent(payload.getEventId(), payload.getRentalId(), payload.getUserId(), payload.getBookId(), RentalOperationEnum.DEVOLUCAO, payload.getQuantity(), EventStatus.PROCESSADO);
        propagationService.publishAvailability(toAvailabilityPayload(inv));
        log.info("Devolução processada: eventId={}, bookId={}, quantity={}", payload.getEventId(), payload.getBookId(), payload.getQuantity());
        publishReturnResultIfPresent(buildReturnResultPayload(payload, true, null, processedAt));
    }

    private void publishReturnResultIfPresent(ReturnResultPayload result) {
        if (result != null) {
            propagationService.publishReturnResult(result);
        }
    }

    private static ReturnResultPayload buildReturnResultPayload(RentalReturnPayload payload, boolean success, String reason, LocalDateTime processedAt) {
        if (payload == null) {
            return null;
        }
        if (!success && payload.getRentalId() == null) {
            return null;
        }
        return ReturnResultPayload.builder()
                .eventId(payload.getEventId() != null ? payload.getEventId() : "")
                .rentalId(payload.getRentalId())
                .success(success)
                .reason(reason)
                .processedAt(processedAt)
                .build();
    }

    private static BookAvailabilityPayload toAvailabilityPayload(Inventory inv) {
        return BookAvailabilityPayload.builder()
                .bookId(inv.getBookId())
                .totalCopies(inv.getTotalCopies())
                .availableCopies(inv.getAvailableCopies())
                .updatedAt(inv.getUpdatedAt())
                .build();
    }

    private void saveEvent(String eventId, Long rentalId, Long userId, Long bookId, RentalOperationEnum operation, int quantity, EventStatus status) {
        processedEventRepository.save(ProcessedEvent.builder()
                .eventId(eventId)
                .rentalId(rentalId)
                .userId(userId)
                .bookId(bookId)
                .operation(operation)
                .quantity(quantity)
                .status(status)
                .processedAt(LocalDateTime.now())
                .build());
    }

    /**
     * Constrói o payload de resultado da reserva (sucesso ou falha) conforme a situação.
     * Responsabilidade única: montar o DTO com success e reason parametrizados.
     * Em falha sem rentalId não há como notificar o rental, retorna null.
     */
    private ReserveResultPayload buildReserveResultPayload(RentalReservePayload payload, boolean success, String reason, LocalDateTime processedAt) {
        if (payload == null) {
            return null;
        }
        if (!success && payload.getRentalId() == null) {
            return null;
        }
        return ReserveResultPayload.builder()
                .eventId(payload.getEventId() != null ? payload.getEventId() : "")
                .rentalId(payload.getRentalId())
                .success(success)
                .reason(reason)
                .processedAt(processedAt)
                .build();
    }
}
