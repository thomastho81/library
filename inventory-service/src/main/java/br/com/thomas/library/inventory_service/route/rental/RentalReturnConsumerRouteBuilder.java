package br.com.thomas.library.inventory_service.route.rental;

import br.com.thomas.library.inventory_service.dto.rental.RentalReturnPayload;
import br.com.thomas.library.inventory_service.service.RentalEventService;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Consome eventos <strong>rental-service</strong>: rental.inventory.return (exchange rental.topic).
 */
@Component
public class RentalReturnConsumerRouteBuilder extends RouteBuilder {

    private static final String ROUTING_KEY = "rental.inventory.return";
    private static final String QUEUE = "inventory.rental.return";

    @Value("${rental.propagation.exchange:rental.topic}")
    private String exchangeName;

    @Override
    public void configure() {
        from(consumerUri())
                .routeId("RENTAL_RETURN_ROUTE")
                .unmarshal().json(RentalReturnPayload.class)
                .log("Evento rental.inventory.return recebido - eventId=${body.eventId}, bookId=${body.bookId}, quantity=${body.quantity}")
                .bean(RentalEventService.class, "processReturn");
    }

    private String consumerUri() {
        return String.format(
                "spring-rabbitmq:%s?exchangeType=topic&queues=%s&routingKey=%s&autoDeclare=true",
                exchangeName, QUEUE, ROUTING_KEY);
    }
}
