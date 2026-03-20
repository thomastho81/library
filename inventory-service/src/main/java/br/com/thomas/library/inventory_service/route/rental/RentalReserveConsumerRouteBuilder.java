package br.com.thomas.library.inventory_service.route.rental;

import br.com.thomas.library.inventory_service.dto.rental.RentalReservePayload;
import br.com.thomas.library.inventory_service.service.RentalEventService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Consome eventos <strong>rental-service</strong>: rental.inventory.reserve (exchange rental.topic).
 * Processa a reserva no {@link RentalEventService}; a publicação do resultado (inventory.rental.reserve.result)
 * é feita pelo {@link br.com.thomas.library.inventory_service.service.propagation.InventoryPropagationService}.
 * <p>
 * Usa {@link JacksonDataFormat} com o {@link ObjectMapper} do Spring para {@link java.time.LocalDateTime}
 * e anotações Jackson (o {@code unmarshal().json(Class)} padrão pode falhar silenciosamente no consumo).
 */
@Component
@RequiredArgsConstructor
public class RentalReserveConsumerRouteBuilder extends RouteBuilder {

    private static final String ROUTING_KEY = "rental.inventory.reserve";
    private static final String QUEUE = "inventory.rental.reserve";

    private final ObjectMapper objectMapper;

    @Value("${rental.propagation.exchange:rental.topic}")
    private String exchangeName;

    @Override
    public void configure() {
        JacksonDataFormat rentalReserveJson = new JacksonDataFormat(objectMapper, RentalReservePayload.class);
        from(consumerUri())
                .routeId("RENTAL_RESERVE_ROUTE")
                .unmarshal(rentalReserveJson)
                .log("Evento rental.inventory.reserve recebido - eventId=${body.eventId}, bookId=${body.bookId}, quantity=${body.quantity}")
                .bean(RentalEventService.class, "processReserve");
    }

    private String consumerUri() {
        return String.format(
                "spring-rabbitmq:%s?exchangeType=topic&queues=%s&routingKey=%s&autoDeclare=true",
                exchangeName, QUEUE, ROUTING_KEY);
    }
}
