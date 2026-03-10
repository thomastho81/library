package br.com.thomas.library.rental_service.route;

import br.com.thomas.library.rental_service.dto.return_result.ReturnResultPayload;
import br.com.thomas.library.rental_service.service.ReturnResultConsumerService;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Consome evento de resultado da devolução (inventory-service → rental-service).
 * Exchange: rental.topic, routing key: inventory.rental.return.result
 */
@Component
public class ReturnResultConsumerRouteBuilder extends RouteBuilder {

    private static final String ROUTING_KEY = "inventory.rental.return.result";
    private static final String QUEUE = "rental.inventory.return.result";

    @Value("${rental.propagation.exchange:rental.topic}")
    private String exchangeName;

    @Override
    public void configure() {
        from(consumerUri())
                .routeId("RETURN_RESULT_CONSUMER_ROUTE")
                .unmarshal().json(ReturnResultPayload.class)
                .log("Evento inventory.rental.return.result recebido - rentalId=${body.rentalId}, success=${body.success}")
                .bean(ReturnResultConsumerService.class, "processResult");
    }

    private String consumerUri() {
        return String.format(
                "spring-rabbitmq:%s?exchangeType=topic&queues=%s&routingKey=%s&autoDeclare=true",
                exchangeName, QUEUE, ROUTING_KEY);
    }
}
