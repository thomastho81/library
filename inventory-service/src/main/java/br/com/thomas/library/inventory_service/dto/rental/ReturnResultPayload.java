package br.com.thomas.library.inventory_service.dto.rental;

import br.com.thomas.library.inventory_service.constants.DateFormatConstants;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Payload do evento de resultado da devolução (inventory-service → rental-service).
 * Publicado em rental.topic com routing key inventory.rental.return.result.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReturnResultPayload {

    private String eventId;
    private Long rentalId;
    private Boolean success;
    private String reason;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateFormatConstants.LOCAL_DATE_TIME)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime processedAt;
}
