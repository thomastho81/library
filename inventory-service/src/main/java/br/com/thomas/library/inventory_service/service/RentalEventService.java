package br.com.thomas.library.inventory_service.service;

import br.com.thomas.library.inventory_service.dto.rental.ReserveResultPayload;
import br.com.thomas.library.inventory_service.dto.rental.RentalReservePayload;
import br.com.thomas.library.inventory_service.dto.rental.RentalReturnPayload;
import br.com.thomas.library.inventory_service.model.EventStatus;
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

    public static final String REJECT_REASON_INVENTORY_NOT_FOUND = "INVENTORY_NOT_FOUND";
    public static final String REJECT_REASON_INSUFFICIENT_STOCK = "INSUFFICIENT_STOCK";
    public static final String REJECT_REASON_MISSING_EVENT_ID = "MISSING_EVENT_ID";
    public static final String REJECT_REASON_INVALID_BOOK_OR_QUANTITY = "INVALID_BOOK_OR_QUANTITY";

    @Transactional
    public ReserveResultPayload processReserve(RentalReservePayload payload) {
        LocalDateTime processedAt = LocalDateTime.now();
        if (payload == null) {
            log.warn("Evento de reserva ignorado: payload nulo");
            return null;
        }
        if (payload.getEventId() == null || payload.getEventId().isBlank()) {
            log.warn("Evento de reserva ignorado: eventId nulo ou vazio");
            return buildReserveResultPayload(payload, false, REJECT_REASON_MISSING_EVENT_ID, processedAt);
        }
        if (processedEventRepository.findByEventId(payload.getEventId()).isPresent()) {
            log.debug("Evento de reserva já processado (idempotência): eventId={}", payload.getEventId());
            return null;
        }
        if (payload.getBookId() == null || payload.getQuantity() == null || payload.getQuantity() < 1) {
            log.warn("Evento de reserva ignorado: bookId ou quantity inválido");
            return buildReserveResultPayload(payload, false, REJECT_REASON_INVALID_BOOK_OR_QUANTITY, processedAt);
        }
        Inventory inv = inventoryRepository.findByBookIdAndActiveTrue(payload.getBookId()).orElse(null);
        if (inv == null) {
            saveEvent(payload.getEventId(), payload.getRentalId(), payload.getBookId(), RentalOperationEnum.RESERVA, payload.getQuantity(), EventStatus.REJEITADO);
            log.warn("Evento de reserva rejeitado: inventário não encontrado ou inativo para bookId={}", payload.getBookId());
            return buildReserveResultPayload(payload, false, REJECT_REASON_INVENTORY_NOT_FOUND, processedAt);
        }
        if (inv.getAvailableCopies() < payload.getQuantity()) {
            saveEvent(payload.getEventId(), payload.getRentalId(), payload.getBookId(), RentalOperationEnum.RESERVA, payload.getQuantity(), EventStatus.REJEITADO);
            log.warn("Evento de reserva rejeitado: cópias disponíveis ({}) insuficientes para reservar {}", inv.getAvailableCopies(), payload.getQuantity());
            return buildReserveResultPayload(payload, false, REJECT_REASON_INSUFFICIENT_STOCK, processedAt);
        }
        inv.reserve(payload.getQuantity());
        inventoryRepository.save(inv);
        saveEvent(payload.getEventId(), payload.getRentalId(), payload.getBookId(), RentalOperationEnum.RESERVA, payload.getQuantity(), EventStatus.PROCESSADO);
        log.info("Reserva processada: eventId={}, bookId={}, quantity={}", payload.getEventId(), payload.getBookId(), payload.getQuantity());
        return buildReserveResultPayload(payload, true, null, processedAt);
    }

    @Transactional
    public void processReturn(RentalReturnPayload payload) {
        if (payload == null || payload.getEventId() == null) {
            log.warn("Evento de devolução ignorado: payload ou eventId nulo");
            return;
        }
        if (processedEventRepository.findByEventId(payload.getEventId()).isPresent()) {
            log.debug("Evento de devolução já processado (idempotência): eventId={}", payload.getEventId());
            return;
        }
        if (payload.getBookId() == null || payload.getQuantity() == null || payload.getQuantity() < 1) {
            log.warn("Evento de devolução ignorado: bookId ou quantity inválido");
            return;
        }
        Inventory inv = inventoryRepository.findByBookIdAndActiveTrue(payload.getBookId()).orElse(null);
        if (inv == null) {
            saveEvent(payload.getEventId(), payload.getRentalId(), payload.getBookId(), RentalOperationEnum.DEVOLUCAO, payload.getQuantity(), EventStatus.REJEITADO);
            log.warn("Evento de devolução rejeitado: inventário não encontrado ou inativo para bookId={}", payload.getBookId());
            return;
        }
        if (inv.getReservedCopies() < payload.getQuantity()) {
            saveEvent(payload.getEventId(), payload.getRentalId(), payload.getBookId(), RentalOperationEnum.DEVOLUCAO, payload.getQuantity(), EventStatus.REJEITADO);
            log.warn("Evento de devolução rejeitado: cópias reservadas ({}) insuficientes para devolver {}", inv.getReservedCopies(), payload.getQuantity());
            return;
        }
        inv.release(payload.getQuantity());
        inventoryRepository.save(inv);

        saveEvent(payload.getEventId(), payload.getRentalId(), payload.getBookId(), RentalOperationEnum.DEVOLUCAO, payload.getQuantity(), EventStatus.PROCESSADO);
        log.info("Devolução processada: eventId={}, bookId={}, quantity={}", payload.getEventId(), payload.getBookId(), payload.getQuantity());
    }

    private void saveEvent(String eventId, Long rentalId, Long bookId, RentalOperationEnum operation, int quantity, EventStatus status) {
        processedEventRepository.save(ProcessedEvent.builder()
                .eventId(eventId)
                .rentalId(rentalId)
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
