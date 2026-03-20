package br.com.thomas.library.inventory_service.route.rental;

import br.com.thomas.library.inventory_service.dto.rental.RentalReturnPayload;
import br.com.thomas.library.inventory_service.service.RentalEventService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Consome eventos <strong>rental-service</strong>: rental.inventory.return (exchange rental.topic).
 * @see RentalReserveConsumerRouteBuilder Mesmo uso de Jackson + ObjectMapper do Spring.
 */
@Component
@RequiredArgsConstructor
public class RentalReturnConsumerRouteBuilder extends RouteBuilder {

    private static final String ROUTING_KEY = "rental.inventory.return";
    private static final String QUEUE = "inventory.rental.return";

    private final ObjectMapper objectMapper;

    @Value("${rental.propagation.exchange:rental.topic}")
    private String exchangeName;

    @Override
    public void configure() {
        JacksonDataFormat rentalReturnJson = new JacksonDataFormat(objectMapper, RentalReturnPayload.class);
        from(consumerUri())
                .routeId("RENTAL_RETURN_ROUTE")
                .unmarshal(rentalReturnJson)
                .log("Evento rental.inventory.return recebido - eventId=${body.eventId}, bookId=${body.bookId}, quantity=${body.quantity}")
                .bean(RentalEventService.class, "processReturn");
    }

    private String consumerUri() {
        return String.format(
                "spring-rabbitmq:%s?exchangeType=topic&queues=%s&routingKey=%s&autoDeclare=true",
                exchangeName, QUEUE, ROUTING_KEY);
    }
}
