package br.com.thomas.library.rental_service.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReserveRequest {

    @NotNull(message = "bookId é obrigatório")
    private Long bookId;

    @NotNull(message = "quantity é obrigatório")
    @Min(value = 1, message = "quantity deve ser pelo menos 1")
    private Integer quantity;
}
