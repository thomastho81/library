package br.com.thomas.library.inventory_service.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request para aumentar ou reduzir quantidade de exemplares.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryQuantityChangeRequest {

    @NotNull(message = "Quantidade é obrigatória")
    @Min(value = 1, message = "Quantidade deve ser no mínimo 1")
    private Integer amount;
}
