package br.com.thomas.library.rental_service.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Status do aluguel no ciclo reserva → devolução.
 * No banco persiste-se apenas o id (integer).
 */
@Getter
@RequiredArgsConstructor
public enum RentalStatus {

    PENDING(0, "Em processamento"),
    RESERVED(1, "Reservado"),
    RETURNED(2, "Devolvido"),
    CANCELLED(3, "Cancelado"),
    RESERVE_FAILED(4, "Reserva processada com erro"),
    /** Usuário solicitou devolução; aguardando gestor confirmar recebimento. */
    RETURN_REQUESTED(5, "Devolução solicitada (aguardando gestor)");

    private final int id;
    private final String descricao;

    public static RentalStatus fromId(int id) {
        for (RentalStatus s : values()) {
            if (s.id == id) return s;
        }
        throw new IllegalArgumentException("RentalStatus inválido: " + id);
    }
}
