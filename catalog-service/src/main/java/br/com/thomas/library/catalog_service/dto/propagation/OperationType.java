package br.com.thomas.library.catalog_service.dto.propagation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Tipo da operação para eventos de propagação.
 * Usado na routing key do topic exchange (ex.: catalog.book.created).
 */
@Getter
@RequiredArgsConstructor
public enum OperationType {

    CREATE("criacao", "created"),
    UPDATE("atualizacao", "updated"),
    DELETE("exclusao", "deleted");

    /**
     * Nome da operação em português (uso em logs/mensagens).
     */
    private final String operation;

    /**
     * Sufixo da routing key em inglês (ex.: catalog.book. + created).
     */
    private final String routingKeySuffix;

    /**
     * Retorna a routing key completa no padrão catalog.book.&lt;suffix&gt;.
     */
    public String getRoutingKey() {
        return "catalog.book." + routingKeySuffix;
    }
}
