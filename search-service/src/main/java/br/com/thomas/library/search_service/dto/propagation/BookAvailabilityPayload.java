package br.com.thomas.library.search_service.dto.propagation;

import br.com.thomas.library.search_service.constants.DateFormatConstants;
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
 * Payload consumido dos eventos de disponibilidade (inventory.book.availability).
 * Publicado pelo inventory-service na exchange rental.topic.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookAvailabilityPayload {

    private Long bookId;
    private Integer totalCopies;
    private Integer availableCopies;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateFormatConstants.LOCAL_DATE_TIME)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime updatedAt;
}
