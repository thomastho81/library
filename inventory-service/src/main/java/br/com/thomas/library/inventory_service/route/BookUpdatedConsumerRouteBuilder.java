package br.com.thomas.library.inventory_service.route;

import br.com.thomas.library.inventory_service.dto.propagation.BookPropagationPayload;
import br.com.thomas.library.inventory_service.service.InventorySyncService;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Consome eventos catalog.book.updated da exchange topic (catalog.topic).
 */
@Component
public class BookUpdatedConsumerRouteBuilder extends RouteBuilder {

    private static final String ROUTING_KEY = "catalog.book.updated";
    private static final String QUEUE = "inventory.book.updated";

    @Value("${catalog.propagation.exchange:catalog.topic}")
    private String exchangeName;

    @Override
    public void configure() {
        from(consumerUri())
                .routeId("UPDATE_ROUTE")
                .unmarshal().json(BookPropagationPayload.class)
                .log("Evento book.updated recebido - bookId=${body.id}, title=${body.title}")
                .bean(InventorySyncService.class, "onBookUpdated");
    }

    private String consumerUri() {
        return String.format(
                "spring-rabbitmq:%s?exchangeType=topic&queues=%s&routingKey=%s&autoDeclare=true",
                exchangeName, QUEUE, ROUTING_KEY);
    }
}
