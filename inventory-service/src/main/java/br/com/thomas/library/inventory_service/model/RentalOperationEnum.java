package br.com.thomas.library.inventory_service.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Operações de rental consumidas pelo inventory-service.
 * No banco persiste-se apenas o id (integer).
 */
@Getter
@RequiredArgsConstructor
public enum RentalOperationEnum {

    RESERVA(1, "Reserva"),
    DEVOLUCAO(2, "Devolução");

    private final int id;
    private final String descricao;

    public static RentalOperationEnum fromId(int id) {
        for (RentalOperationEnum op : values()) {
            if (op.id == id) return op;
        }
        throw new IllegalArgumentException("RentalOperation inválido: " + id);
    }
}
