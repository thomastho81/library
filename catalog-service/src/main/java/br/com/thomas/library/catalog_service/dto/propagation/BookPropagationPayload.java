package br.com.thomas.library.catalog_service.dto.propagation;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Payload de propagação para eventos de livro (exchange topic).
 * Campos alinhados ao documento Book; datas formatadas como no response.
 * <p>
 * {@link JsonFormat} é suficiente para serializar LocalDateTime em JSON;
 * {@link com.fasterxml.jackson.databind.annotation.JsonSerialize} não é necessário.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookPropagationPayload implements PropagationPayload {

    private Long id;
    private String title;
    private String author;
    private String category;
    private String genre;
    private String description;
    private String isbn;
    private Integer publishedYear;
    private Boolean active;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
