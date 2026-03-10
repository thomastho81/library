package br.com.thomas.library.rental_service.service.propagation;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Payload do evento de reserva publicado em rental.topic (routing key rental.inventory.reserve).
 * Contrato alinhado ao inventory-service (RentalReservePayload).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReserveEventPayload {

    private String eventId;
    private Long rentalId;
    private Long userId;
    private Long bookId;
    private Integer quantity;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime eventDate;
}
