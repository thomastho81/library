package br.com.thomas.library.rental_service.route;

import br.com.thomas.library.rental_service.dto.reserve_result.ReserveResultPayload;
import br.com.thomas.library.rental_service.service.ReserveResultConsumerService;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Consome evento de resultado da reserva (inventory-service → rental-service).
 * Exchange: rental.topic, routing key: inventory.rental.reserve.result
 */
@Component
public class ReserveResultConsumerRouteBuilder extends RouteBuilder {

    private static final String ROUTING_KEY = "inventory.rental.reserve.result";
    private static final String QUEUE = "rental.inventory.reserve.result";

    @Value("${rental.propagation.exchange:rental.topic}")
    private String exchangeName;

    @Override
    public void configure() {
        from(consumerUri())
                .routeId("RESERVE_RESULT_CONSUMER_ROUTE")
                .unmarshal().json(ReserveResultPayload.class)
                .log("Evento inventory.rental.reserve.result recebido - rentalId=${body.rentalId}, success=${body.success}")
                .bean(ReserveResultConsumerService.class, "processResult");
    }

    private String consumerUri() {
        return String.format(
                "spring-rabbitmq:%s?exchangeType=topic&queues=%s&routingKey=%s&autoDeclare=true",
                exchangeName, QUEUE, ROUTING_KEY);
    }
}
