package br.com.thomas.library.catalog_service.service.propagation;

import br.com.thomas.library.catalog_service.dto.propagation.OperationType;
import br.com.thomas.library.catalog_service.dto.propagation.PropagationPayload;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.FluentProducerTemplate;
import org.apache.camel.component.springrabbit.SpringRabbitMQConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Publica eventos em uma exchange topic do RabbitMQ via Apache Camel
 * (FluentProducerTemplate).
 * <p>
 * Consumidores podem escutar por routing key exata (ex.: catalog.book.update)
 * ou por padrão (ex.: catalog.book.*).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PropagationService {

    private static final String EXCHANGE_TYPE_TOPIC = "topic";

    private final FluentProducerTemplate fluentProducerTemplate;
    private final ObjectMapper objectMapper;

    @Value("${catalog.propagation.exchange:catalog.topic}")
    private String exchangeName;

    /**
     * Publica um payload em uma exchange topic com routing key derivada do tipo da operação.
     *
     * @param payload   objeto que estende PropagationPayload (será serializado em JSON)
     * @param operation tipo da operação (CREATE, UPDATE, DELETE) — define a routing key
     * @param <T>       tipo que estende PropagationPayload
     */
    public <T extends PropagationPayload> void publish(T payload, OperationType operation) {
        String routingKey = operation.getRoutingKey();
        var body = parsePayload(payload);

        String endpointUri = String.format("spring-rabbitmq:%s?exchangeType=%s", exchangeName, EXCHANGE_TYPE_TOPIC);

        fluentProducerTemplate
                .to(endpointUri)
                .withHeader(SpringRabbitMQConstants.ROUTING_OVERRIDE_KEY, routingKey)
                .withHeader(SpringRabbitMQConstants.CONTENT_TYPE, "application/json")
                .withBody(body)
                .send();

        log.debug("Evento publicado: exchange={}, routingKey={}, operation={}", exchangeName, routingKey, operation.getOperation());
    }

    private <T> String parsePayload(T payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            log.error("Falha ao serializar payload para JSON: {}", e.getMessage());
            throw new IllegalArgumentException("Payload não pôde ser serializado para JSON", e);
        }
    }
}
