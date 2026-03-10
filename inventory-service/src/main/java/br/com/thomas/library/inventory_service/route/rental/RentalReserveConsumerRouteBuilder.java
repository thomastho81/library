package br.com.thomas.library.inventory_service.route.rental;

import br.com.thomas.library.inventory_service.dto.rental.RentalReservePayload;
import br.com.thomas.library.inventory_service.service.RentalEventService;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Consome eventos <strong>rental-service</strong>: rental.inventory.reserve (exchange rental.topic).
 * Processa a reserva no {@link RentalEventService}; a publicação do resultado (inventory.rental.reserve.result)
 * é feita pelo {@link br.com.thomas.library.inventory_service.service.propagation.InventoryPropagationService}.
 */
@Component
public class RentalReserveConsumerRouteBuilder extends RouteBuilder {

    private static final String ROUTING_KEY = "rental.inventory.reserve";
    private static final String QUEUE = "inventory.rental.reserve";

    @Value("${rental.propagation.exchange:rental.topic}")
    private String exchangeName;

    @Override
    public void configure() {
        from(consumerUri())
                .routeId("RENTAL_RESERVE_ROUTE")
                .unmarshal().json(RentalReservePayload.class)
                .log("Evento rental.inventory.reserve recebido - eventId=${body.eventId}, bookId=${body.bookId}, quantity=${body.quantity}")
                .bean(RentalEventService.class, "processReserve");
    }

    private String consumerUri() {
        return String.format(
                "spring-rabbitmq:%s?exchangeType=topic&queues=%s&routingKey=%s&autoDeclare=true",
                exchangeName, QUEUE, ROUTING_KEY);
    }
}
