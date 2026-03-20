package br.com.thomas.library.rental_service.service.propagation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.FluentProducerTemplate;
import org.apache.camel.component.springrabbit.SpringRabbitMQConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PropagationService {

    private static final String EXCHANGE_TYPE_TOPIC = "topic";
    private static final String ROUTING_KEY_RESERVE = "rental.inventory.reserve";
    private static final String ROUTING_KEY_RETURN = "rental.inventory.return";

    private final FluentProducerTemplate fluentProducerTemplate;
    private final ObjectMapper objectMapper;

    @Value("${rental.propagation.exchange:rental.topic}")
    private String exchangeName;

    /**
     * Publica evento de reserva para o inventory-service (rental.topic, rental.inventory.reserve).
     */
    public void publishReserve(ReserveEventPayload payload) {
        String body = toJson(payload);
        // routingKey + skipDeclareQueue: publicação só na topic exchange (sem declarar fila no producer)
        String endpointUri = String.format(
                "spring-rabbitmq:%s?exchangeType=%s&routingKey=%s&skipDeclareQueue=true",
                exchangeName, EXCHANGE_TYPE_TOPIC, ROUTING_KEY_RESERVE);

        fluentProducerTemplate
                .to(endpointUri)
                .withHeader(SpringRabbitMQConstants.ROUTING_OVERRIDE_KEY, ROUTING_KEY_RESERVE)
                .withHeader(SpringRabbitMQConstants.CONTENT_TYPE, "application/json")
                .withBody(body)
                .send();

        log.debug("Evento rental.inventory.reserve publicado: eventId={}, rentalId={}", payload.getEventId(), payload.getRentalId());
    }

    /**
     * Publica evento de devolução para o inventory-service (rental.topic, rental.inventory.return).
     */
    public void publishReturn(ReturnEventPayload payload) {
        String body = toJson(payload);
        String endpointUri = String.format(
                "spring-rabbitmq:%s?exchangeType=%s&routingKey=%s&skipDeclareQueue=true",
                exchangeName, EXCHANGE_TYPE_TOPIC, ROUTING_KEY_RETURN);

        fluentProducerTemplate
                .to(endpointUri)
                .withHeader(SpringRabbitMQConstants.ROUTING_OVERRIDE_KEY, ROUTING_KEY_RETURN)
                .withHeader(SpringRabbitMQConstants.CONTENT_TYPE, "application/json")
                .withBody(body)
                .send();

        log.debug("Evento rental.inventory.return publicado: eventId={}, rentalId={}", payload.getEventId(), payload.getRentalId());
    }

    private String toJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            log.error("Falha ao serializar payload: {}", e.getMessage());
            throw new IllegalArgumentException("Payload não pôde ser serializado para JSON", e);
        }
    }
}
