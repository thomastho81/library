package br.com.thomas.library.inventory_service.dto.propagation;

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
 * Payload consumido dos eventos de livro (exchange topic catalog.topic).
 * Estrutura alinhada ao publicado pelo catalog-service.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookPropagationPayload {

    private Long id;
    private String title;
    private String author;
    private String category;
    private String genre;
    private String description;
    private String isbn;
    private Integer publishedYear;
    private Boolean active;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateFormatConstants.LOCAL_DATE_TIME)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateFormatConstants.LOCAL_DATE_TIME)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime updatedAt;
}
