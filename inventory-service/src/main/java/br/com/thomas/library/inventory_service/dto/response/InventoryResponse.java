package br.com.thomas.library.inventory_service.dto.response;

import br.com.thomas.library.inventory_service.constants.DateFormatConstants;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Resposta de inventário (estoque de um livro).
 * available = há cópias disponíveis para empréstimo (availableCopies > 0).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryResponse {

    private Long id;
    private Long bookId;
    private Integer totalCopies;
    private Integer availableCopies;
    private Integer reservedCopies;

    /**
     * Indica se há pelo menos uma cópia disponível (availableCopies > 0).
     */
    private Boolean available;

    private Boolean active;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateFormatConstants.LOCAL_DATE_TIME)
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateFormatConstants.LOCAL_DATE_TIME)
    private LocalDateTime updatedAt;
}
