package br.com.thomas.library.inventory_service.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request para criar estoque inicial de um livro.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryRequest {

    @NotNull(message = "ID do livro é obrigatório")
    private Long bookId;

    @NotNull(message = "Quantidade inicial é obrigatória")
    @Min(value = 0, message = "Quantidade inicial não pode ser negativa")
    private Integer totalCopies;
}
