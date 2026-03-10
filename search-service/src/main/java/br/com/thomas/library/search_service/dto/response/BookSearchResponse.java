package br.com.thomas.library.search_service.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Resposta de um livro na API de busca (item da listagem ou visualização por id).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookSearchResponse {

    private String id;
    private String title;
    private String author;
    private String category;
    private String genre;
    private String description;
    private String isbn;
    private Integer publishedYear;
    private Boolean active;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant updatedAt;

    /** Quantidade total de cópias (inventory); null quando não informado. */
    private Integer totalCopies;
    /** Cópias disponíveis (inventory); null quando não informado. */
    private Integer availableCopies;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant inventoryUpdatedAt;
}
