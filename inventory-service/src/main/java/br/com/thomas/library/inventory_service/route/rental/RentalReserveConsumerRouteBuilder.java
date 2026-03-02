package br.com.thomas.library.inventory_service.route.rental;

import br.com.thomas.library.inventory_service.dto.rental.RentalReservePayload;
import br.com.thomas.library.inventory_service.service.RentalEventService;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.springrabbit.SpringRabbitMQConstants;

import static org.apache.camel.builder.Builder.constant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Consome eventos <strong>rental-service</strong>: rental.inventory.reserve (exchange rental.topic).
 * Após processar, publica resultado em rental.topic com routing key inventory.rental.reserve.result.
 */
@Component
public class RentalReserveConsumerRouteBuilder extends RouteBuilder {

    private static final String ROUTING_KEY = "rental.inventory.reserve";
    private static final String QUEUE = "inventory.rental.reserve";
    private static final String RESULT_ROUTING_KEY = "inventory.rental.reserve.result";

    @Value("${rental.propagation.exchange:rental.topic}")
    private String exchangeName;

    @Override
    public void configure() {
        from(consumerUri())
                .routeId("RENTAL_RESERVE_ROUTE")
                .unmarshal().json(RentalReservePayload.class)
                .log("Evento rental.inventory.reserve recebido - eventId=${body.eventId}, bookId=${body.bookId}, quantity=${body.quantity}")
                .bean(RentalEventService.class, "processReserve")
                .filter(simple("${body} != null"))
                .setHeader(SpringRabbitMQConstants.ROUTING_OVERRIDE_KEY, constant(RESULT_ROUTING_KEY))
                .setHeader(SpringRabbitMQConstants.CONTENT_TYPE, constant("application/json"))
                .marshal().json()
                .to(producerUri());
    }

    private String consumerUri() {
        return String.format(
                "spring-rabbitmq:%s?exchangeType=topic&queues=%s&routingKey=%s&autoDeclare=true",
                exchangeName, QUEUE, ROUTING_KEY);
    }

    private String producerUri() {
        return String.format("spring-rabbitmq:%s?exchangeType=topic", exchangeName);
    }
}
