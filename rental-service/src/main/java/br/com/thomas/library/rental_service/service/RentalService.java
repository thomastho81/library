package br.com.thomas.library.rental_service.service;

import br.com.thomas.library.rental_service.dto.request.ReserveRequest;
import br.com.thomas.library.rental_service.dto.request.ReturnRequest;
import br.com.thomas.library.rental_service.dto.response.PagedRentalResponse;
import br.com.thomas.library.rental_service.dto.response.RentalResponse;
import br.com.thomas.library.rental_service.model.Rental;
import br.com.thomas.library.rental_service.model.RentalStatus;
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

import java.time.LocalDateTime;
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
     * Lista alugueis do usuário (meus alugueis / histórico) com paginação e ordenação.
     * Ordenação: use sort=status ou sort=reservedAt (ex.: sort=reservedAt,desc).
     *
     * @param userId   obrigatório
     * @param statuses opcional; se vazio, retorna todos os status
     */
    public PagedRentalResponse listByUser(Long userId, List<RentalStatus> statuses, Pageable pageable) {
        validateUserExistsAndActive(userId);

        Page<Rental> page = statuses != null && !statuses.isEmpty()
                ? rentalRepository.findByUserIdAndStatusIn(userId, statuses, pageable)
                : rentalRepository.findByUserId(userId, pageable);

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
     * Registra devolução: valida que o rental existe, está RESERVED e pertence ao userId.
     * Publica evento rental.inventory.return e coloca o rental em RETURNING.
     */
    @Transactional
    public RentalResponse registerReturn(Long rentalId, ReturnRequest request) {
        validateUserExistsAndActive(request.getUserId());

        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new IllegalArgumentException("Rental não encontrado: id=" + rentalId));
        if (rental.getStatus() != RentalStatus.RESERVED) {
            throw new IllegalStateException(
                    "Rental não está reservado: id=" + rentalId + ", status=" + rental.getStatus());
        }
        if (!rental.getUserId().equals(request.getUserId())) {
            throw new IllegalStateException("Aluguel não pertence ao usuário informado");
        }

        String eventId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        ReturnEventPayload payload = buildReturnEventPayload(eventId, rental.getId(), rental.getUserId(),
                rental.getBookId(), rental.getQuantity(), now);
        propagationService.publishReturn(payload);

        rental.setStatus(RentalStatus.RETURNING);
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
