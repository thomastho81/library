package br.com.thomas.library.rental_service.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Perfil do usuário: usuário comum (pode alugar) ou gestor (operações de inventário/devoluções).
 */
@Getter
@RequiredArgsConstructor
public enum UserProfile {

    USER(0, "Usuário"),
    GESTOR(1, "Gestor");

    private final int id;
    private final String descricao;

    public static UserProfile fromId(int id) {
        for (UserProfile p : values()) {
            if (p.id == id) return p;
        }
        throw new IllegalArgumentException("UserProfile inválido: " + id);
    }
}
