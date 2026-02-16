package br.com.thomas.library.catalog_service.dto.propagation;

import java.io.Serializable;

/**
 * Marca DTOs que podem ser publicados como payload em eventos de propagação
 * (topic exchange RabbitMQ via Apache Camel).
 */
public interface PropagationPayload extends Serializable {
}
