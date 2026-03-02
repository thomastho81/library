package br.com.thomas.library.inventory_service.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Status do evento recebido: processado com sucesso ou rejeitado (ex.: validação de quantidade).
 * No banco persiste-se apenas o id (integer).
 */
@Getter
@RequiredArgsConstructor
public enum EventStatus {

    PROCESSADO(1, "Processado"),
    REJEITADO(2, "Rejeitado");

    private final int id;
    private final String descricao;

    public static EventStatus fromId(int id) {
        for (EventStatus s : values()) {
            if (s.id == id) return s;
        }
        throw new IllegalArgumentException("EventStatus inválido: " + id);
    }
}
