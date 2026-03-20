package br.com.thomas.library.search_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Totais agregados do índice de livros (inventário informativo).
 * {@code booksWithReservedCopies}: quantidade de títulos (documentos) com {@code totalCopies > availableCopies}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventorySummaryResponse {

    private long totalBooks;
    private long totalAvailableCopies;
    private long booksWithReservedCopies;
}
