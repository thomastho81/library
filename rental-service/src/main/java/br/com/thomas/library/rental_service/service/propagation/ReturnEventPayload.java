package br.com.thomas.library.rental_service.service.propagation;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Payload do evento de devolução publicado em rental.topic (routing key rental.inventory.return).
 * Contrato alinhado ao inventory-service (RentalReturnPayload).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReturnEventPayload {

    private String eventId;
    private Long rentalId;
    private Long bookId;
    private Integer quantity;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime eventDate;
}
