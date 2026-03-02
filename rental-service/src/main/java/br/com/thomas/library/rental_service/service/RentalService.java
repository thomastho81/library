package br.com.thomas.library.rental_service.service;

import br.com.thomas.library.rental_service.dto.request.ReserveRequest;
import br.com.thomas.library.rental_service.dto.response.RentalResponse;
import br.com.thomas.library.rental_service.model.Rental;
import br.com.thomas.library.rental_service.model.RentalStatus;
import br.com.thomas.library.rental_service.repository.RentalRepository;
import br.com.thomas.library.rental_service.service.propagation.PropagationService;
import br.com.thomas.library.rental_service.service.propagation.ReserveEventPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RentalService {

    private final RentalRepository rentalRepository;
    private final PropagationService propagationService;

    /**
     * Cria reserva em estado PENDING, publica evento para o inventory e retorna o Rental.
     * Em caso de falha na publicação, compensa marcando o Rental como CANCELLED.
     */
    @Transactional
    public RentalResponse createReserve(ReserveRequest request) {
        String eventId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();

        Rental rental = Rental.builder()
                .bookId(request.getBookId())
                .quantity(request.getQuantity())
                .status(RentalStatus.PENDING)
                .reservedAt(now)
                .build();
        rental = rentalRepository.save(rental);
        Long rentalId = rental.getId();

        try {
            ReserveEventPayload payload = buildReserveEventPayload(eventId, rentalId, request.getBookId(), request.getQuantity(), now);
            propagationService.publishReserve(payload);
        } catch (Exception e) {
            log.error("Falha ao publicar evento de reserva para rentalId={}, compensando com CANCELLED", rentalId, e);
            compensateWithStatus(rental, RentalStatus.CANCELLED);
            throw e;
        }

        return toResponse(rental);
    }

    /**
     * Constrói o payload do evento de reserva (responsabilidade única: montar o DTO).
     */
    private ReserveEventPayload buildReserveEventPayload(String eventId, Long rentalId, Long bookId, Integer quantity, LocalDateTime eventDate) {
        return ReserveEventPayload.builder()
                .eventId(eventId)
                .rentalId(rentalId)
                .bookId(bookId)
                .quantity(quantity)
                .eventDate(eventDate)
                .build();
    }

    /**
     * Compensa o Rental com o status indicado (ex.: CANCELLED em falha de publicação).
     */
    private void compensateWithStatus(Rental rental, RentalStatus status) {
        rental.setStatus(status);
        rentalRepository.save(rental);
    }

    public RentalResponse getById(Long id) {
        Rental rental = rentalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Rental não encontrado: id=" + id));
        return toResponse(rental);
    }

    private RentalResponse toResponse(Rental rental) {
        String selfLink = "/api/rentals/" + rental.getId();
        return RentalResponse.builder()
                .id(rental.getId())
                .bookId(rental.getBookId())
                .quantity(rental.getQuantity())
                .status(rental.getStatus())
                .reservedAt(rental.getReservedAt())
                .returnedAt(rental.getReturnedAt())
                ._links(Map.of("self", selfLink))
                .build();
    }
}
