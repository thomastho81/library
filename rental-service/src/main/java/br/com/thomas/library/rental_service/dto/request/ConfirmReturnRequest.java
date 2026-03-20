package br.com.thomas.library.rental_service.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmReturnRequest {

    @NotNull(message = "gestorUserId é obrigatório")
    private Long gestorUserId;
}
