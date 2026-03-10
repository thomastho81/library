package br.com.thomas.library.rental_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Resposta paginada para listagem "meus alugueis" / histórico.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedRentalResponse {

    private List<RentalResponse> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;
}
