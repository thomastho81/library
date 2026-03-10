package br.com.thomas.library.inventory_service.dto.rental;

import br.com.thomas.library.inventory_service.constants.DateFormatConstants;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Payload do evento de reserva de cópias (rental-service → inventory-service).
 * eventId garante idempotência no consumo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RentalReservePayload {

    private String eventId;
    private Long rentalId;
    private Long userId;
    private Long bookId;
    private Integer quantity;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateFormatConstants.LOCAL_DATE_TIME)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime eventDate;
}
