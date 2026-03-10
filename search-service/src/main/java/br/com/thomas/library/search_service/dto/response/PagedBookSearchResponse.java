package br.com.thomas.library.search_service.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Resposta paginada da API de busca de livros.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedBookSearchResponse {

    private List<BookSearchResponse> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;

    @JsonProperty("numberOfElements")
    private int numberOfElements;
}
