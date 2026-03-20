package br.com.thomas.library.rental_service.service;

import br.com.thomas.library.rental_service.dto.request.ReserveRequest;
import br.com.thomas.library.rental_service.dto.request.ReturnRequest;
import br.com.thomas.library.rental_service.dto.response.PagedRentalResponse;
import br.com.thomas.library.rental_service.dto.response.RentalResponse;
import br.com.thomas.library.rental_service.model.Rental;
import br.com.thomas.library.rental_service.model.RentalStatus;
import br.com.thomas.library.rental_service.model.UserProfile;
import br.com.thomas.library.rental_service.repository.RentalRepository;
import br.com.thomas.library.rental_service.repository.UserRepository;
import br.com.thomas.library.rental_service.service.propagation.PropagationService;
import br.com.thomas.library.rental_service.service.propagation.ReserveEventPayload;
import br.com.thomas.library.rental_service.service.propagation.ReturnEventPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RentalService {

    private final RentalRepository rentalRepository;
    private final UserRepository userRepository;
    private final PropagationService propagationService;

    /**
     * Valida que o usuário existe e está ativo. Lança IllegalArgumentException se não encontrado ou inativo.
     */
    private void validateUserExistsAndActive(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId é obrigatório");
        }
        if (userRepository.findByIdAndActiveTrue(userId).isEmpty()) {
            throw new IllegalArgumentException("Usuário não encontrado ou inativo: id=" + userId);
        }
    }

    /**
     * Cria reserva em estado PENDING, publica evento para o inventory e retorna o Rental.
     * Valida existência e ativação do usuário. Em caso de falha na publicação, compensa com CANCELLED.
     */
    @Transactional
    public RentalResponse createReserve(ReserveRequest request) {
        validateUserExistsAndActive(request.getUserId());

        String eventId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();

        Rental rental = Rental.builder()
                .userId(request.getUserId())
                .bookId(request.getBookId())
                .quantity(request.getQuantity())
                .status(RentalStatus.PENDING)
                .reservedAt(now)
                .build();
        rental = rentalRepository.save(rental);
        Long rentalId = rental.getId();

        try {
            ReserveEventPayload payload = buildReserveEventPayload(eventId, rentalId, request.getUserId(),
                    request.getBookId(), request.getQuantity(), now);
            propagationService.publishReserve(payload);
        } catch (Exception e) {
            log.error("Falha ao publicar evento de reserva para rentalId={}, compensando com CANCELLED", rentalId, e);
            compensateWithStatus(rental);
            throw e;
        }

        return toResponse(rental);
    }

    /**
     * Constrói o payload do evento de reserva (responsabilidade única: montar o DTO).
     */
    private ReserveEventPayload buildReserveEventPayload(String eventId, Long rentalId, Long userId,
                                                        Long bookId, Integer quantity, LocalDateTime eventDate) {
        return ReserveEventPayload.builder()
                .eventId(eventId)
                .rentalId(rentalId)
                .userId(userId)
                .bookId(bookId)
                .quantity(quantity)
                .eventDate(eventDate)
                .build();
    }

    /**
     * Compensa o Rental com o status indicado (ex.: CANCELLED em falha de publicação).
     */
    private void compensateWithStatus(Rental rental) {
        rental.setStatus(RentalStatus.CANCELLED);
        rentalRepository.save(rental);
    }

    public RentalResponse getById(Long id) {
        Rental rental = rentalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Rental não encontrado: id=" + id));
        return toResponse(rental);
    }

    /**
     * Lista alugueis do usuário (meus alugueis / histórico) com paginação, ordenação e filtros opcionais.
     * Ordenação: use sort=status ou sort=reservedAt (ex.: sort=reservedAt,desc).
     *
     * @param userId          obrigatório
     * @param statuses        opcional; se vazio, retorna todos os status
     * @param reservedAtFrom  opcional; data inicial (inclusive) do intervalo de reservedAt
     * @param reservedAtTo    opcional; data final (inclusive) do intervalo de reservedAt
     */
    public PagedRentalResponse listByUser(Long userId, List<RentalStatus> statuses,
                                          LocalDate reservedAtFrom, LocalDate reservedAtTo,
                                          Pageable pageable) {
        validateUserExistsAndActive(userId);

        // Usar limites concretos em vez de null para evitar "could not determine data type of parameter" no PostgreSQL.
        LocalDateTime from = reservedAtFrom != null ? reservedAtFrom.atStartOfDay() : LocalDateTime.MIN;
        LocalDateTime to = reservedAtTo != null ? reservedAtTo.atTime(LocalTime.MAX) : LocalDateTime.MAX;
        boolean filterByDate = reservedAtFrom != null || reservedAtTo != null;
        List<RentalStatus> statusList = (statuses != null && !statuses.isEmpty())
                ? statuses
                : Arrays.asList(RentalStatus.values());

        Page<Rental> page;
        if (filterByDate) {
            page = rentalRepository.findByUserIdAndReservedAtBetweenAndStatusIn(userId, from, to, statusList, pageable);
        } else if (statuses != null && !statuses.isEmpty()) {
            page = rentalRepository.findByUserIdAndStatusIn(userId, statuses, pageable);
        } else {
            page = rentalRepository.findByUserId(userId, pageable);
        }

        List<RentalResponse> content = page.getContent().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return PagedRentalResponse.builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }

    /**
     * Lista alugueis com devolução solicitada (RETURN_REQUESTED), para a tela do gestor.
     * Apenas usuário com perfil GESTOR pode chamar.
     */
    public PagedRentalResponse listPendingReturns(Long gestorUserId, Pageable pageable) {
        var gestor = userRepository.findById(gestorUserId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário gestor não encontrado: id=" + gestorUserId));
        if (!Boolean.TRUE.equals(gestor.getActive()) || gestor.getProfile() != UserProfile.GESTOR) {
            throw new IllegalArgumentException("Apenas usuário com perfil GESTOR pode listar devoluções pendentes");
        }
        Page<Rental> page = rentalRepository.findByStatus(RentalStatus.RETURN_REQUESTED, pageable);
        List<RentalResponse> content = page.getContent().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return PagedRentalResponse.builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }

    /**
     * Solicitação de devolução pelo usuário (ex.: entrega no balcão).
     * Rental deve estar RESERVED e pertencer ao userId. Altera para RETURN_REQUESTED.
     * Não publica evento para o inventory-service.
     */
    @Transactional
    public RentalResponse requestReturn(Long rentalId, Long userId) {
        validateUserExistsAndActive(userId);

        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new IllegalArgumentException("Rental não encontrado: id=" + rentalId));
        if (rental.getStatus() != RentalStatus.RESERVED) {
            throw new IllegalStateException(
                    "Rental não está reservado: id=" + rentalId + ", status=" + rental.getStatus());
        }
        if (!rental.getUserId().equals(userId)) {
            throw new IllegalStateException("Aluguel não pertence ao usuário informado");
        }

        rental.setStatus(RentalStatus.RETURN_REQUESTED);
        rentalRepository.save(rental);
        return toResponse(rental);
    }

    /**
     * Confirmação de recebimento da devolução pelo gestor.
     * Rental deve estar RETURN_REQUESTED. Valida que gestorUserId é usuário com perfil GESTOR.
     * Altera para RETURNED, preenche returnedAt e publica evento para o inventory-service.
     */
    @Transactional
    public RentalResponse confirmReturn(Long rentalId, Long gestorUserId) {
        var gestor = userRepository.findById(gestorUserId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário gestor não encontrado: id=" + gestorUserId));
        if (!Boolean.TRUE.equals(gestor.getActive())) {
            throw new IllegalArgumentException("Usuário gestor inativo: id=" + gestorUserId);
        }
        if (gestor.getProfile() != UserProfile.GESTOR) {
            throw new IllegalArgumentException("Apenas usuário com perfil GESTOR pode confirmar devolução");
        }

        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new IllegalArgumentException("Rental não encontrado: id=" + rentalId));
        if (rental.getStatus() != RentalStatus.RETURN_REQUESTED) {
            throw new IllegalStateException(
                    "Rental não está com devolução solicitada: id=" + rentalId + ", status=" + rental.getStatus());
        }

        LocalDateTime now = LocalDateTime.now();
        String eventId = UUID.randomUUID().toString();
        ReturnEventPayload payload = buildReturnEventPayload(eventId, rental.getId(), rental.getUserId(),
                rental.getBookId(), rental.getQuantity(), now);
        propagationService.publishReturn(payload);

        rental.setStatus(RentalStatus.RETURNED);
        rental.setReturnedAt(now);
        rentalRepository.save(rental);
        return toResponse(rental);
    }

    private ReturnEventPayload buildReturnEventPayload(String eventId, Long rentalId, Long userId,
                                                      Long bookId, Integer quantity, LocalDateTime eventDate) {
        return ReturnEventPayload.builder()
                .eventId(eventId)
                .rentalId(rentalId)
                .userId(userId)
                .bookId(bookId)
                .quantity(quantity)
                .eventDate(eventDate)
                .build();
    }

    private RentalResponse toResponse(Rental rental) {
        String selfLink = "/api/rentals/" + rental.getId();
        return RentalResponse.builder()
                .id(rental.getId())
                .userId(rental.getUserId())
                .bookId(rental.getBookId())
                .quantity(rental.getQuantity())
                .status(rental.getStatus())
                .reservedAt(rental.getReservedAt())
                .returnedAt(rental.getReturnedAt())
                ._links(Map.of("self", selfLink))
                .build();
    }
}
