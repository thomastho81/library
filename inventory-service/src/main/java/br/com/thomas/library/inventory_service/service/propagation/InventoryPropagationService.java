package br.com.thomas.library.inventory_service.service.propagation;

import br.com.thomas.library.inventory_service.dto.propagation.BookAvailabilityPayload;
import br.com.thomas.library.inventory_service.dto.rental.ReserveResultPayload;
import br.com.thomas.library.inventory_service.dto.rental.ReturnResultPayload;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.FluentProducerTemplate;
import org.apache.camel.component.springrabbit.SpringRabbitMQConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Componente único de publicação de eventos do inventory-service na exchange (rental.topic).
 * Centraliza todas as mensagens outbound: resultado de reserva, resultado de devolução e disponibilidade.
 * SOLID: responsabilidade única (publicar); um método por tipo de evento (type-safe, Open/Closed).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryPropagationService {

    private static final String EXCHANGE_TYPE_TOPIC = "topic";

    /** Routing key: resultado da reserva (rental-service consome). */
    public static final String ROUTING_KEY_RESERVE_RESULT = "inventory.rental.reserve.result";
    /** Routing key: resultado da devolução (rental-service consome). */
    public static final String ROUTING_KEY_RETURN_RESULT = "inventory.rental.return.result";
    /** Routing key: disponibilidade do livro (search-service consome). */
    public static final String ROUTING_KEY_AVAILABILITY = "inventory.book.availability";

    private final FluentProducerTemplate fluentProducerTemplate;
    private final ObjectMapper objectMapper;

    @Value("${rental.propagation.exchange:rental.topic}")
    private String exchangeName;

    public void publishReserveResult(ReserveResultPayload payload) {
        if (payload != null) {
            publish(ROUTING_KEY_RESERVE_RESULT, payload);
            log.debug("Evento reserve.result publicado: exchange={}, rentalId={}, success={}", exchangeName, payload.getRentalId(), payload.getSuccess());
        }
    }

    public void publishReturnResult(ReturnResultPayload payload) {
        if (payload != null) {
            publish(ROUTING_KEY_RETURN_RESULT, payload);
            log.debug("Evento return.result publicado: exchange={}, rentalId={}, success={}", exchangeName, payload.getRentalId(), payload.getSuccess());
        }
    }
    /**
     * Publica o estado atual de disponibilidade do livro (total e cópias disponíveis) para o search-service.
     * O search-service <b>não precisa saber</b> se foi aumento, redução, reserva ou devolução: ele apenas
     * <b>substitui</b> totalCopies e availableCopies no documento pelo valor recebido (atualização por estado,
     * não por delta). Enviamos <b>totalCopies</b> (total de exemplares) e <b>availableCopies</b> (exemplares
     * disponíveis para empréstimo) para que a busca possa exibir ambos e filtrar/ordenar por disponibilidade.
     */
    public void publishAvailability(BookAvailabilityPayload payload) {
        if (payload != null) {
            publish(ROUTING_KEY_AVAILABILITY, payload);
            log.debug("Evento disponibilidade publicado: exchange={}, bookId={}", exchangeName, payload.getBookId());
        }
    }

    /**
     * Publica um payload na exchange com a routing key informada (DRY; detalhe de serialização e envio em um só lugar).
     */
    private void publish(String routingKey, Object payload) {
        String body = toJson(payload);
        String endpointUri = String.format("spring-rabbitmq:%s?exchangeType=%s", exchangeName, EXCHANGE_TYPE_TOPIC);
        fluentProducerTemplate
                .to(endpointUri)
                .withHeader(SpringRabbitMQConstants.ROUTING_OVERRIDE_KEY, routingKey)
                .withHeader(SpringRabbitMQConstants.CONTENT_TYPE, "application/json")
                .withBody(body)
                .send();
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
