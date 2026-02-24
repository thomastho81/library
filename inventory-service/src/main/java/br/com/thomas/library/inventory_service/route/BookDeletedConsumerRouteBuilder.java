package br.com.thomas.library.inventory_service.route;

import br.com.thomas.library.inventory_service.dto.propagation.BookPropagationPayload;
import br.com.thomas.library.inventory_service.service.InventorySyncService;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Consome eventos catalog.book.deleted da exchange topic (catalog.topic).
 * Marca o registro de inventário como inativo (soft delete).
 */
@Component
public class BookDeletedConsumerRouteBuilder extends RouteBuilder {

    private static final String ROUTING_KEY = "catalog.book.deleted";
    private static final String QUEUE = "inventory.book.deleted";

    @Value("${catalog.propagation.exchange:catalog.topic}")
    private String exchangeName;

    @Override
    public void configure() {
        from(consumerUri())
                .routeId("DELETE_ROUTE")
                .unmarshal().json(BookPropagationPayload.class)
                .log("Evento book.deleted recebido - bookId=${body.id}, title=${body.title}")
                .bean(InventorySyncService.class, "onBookDeleted");
    }

    private String consumerUri() {
        return String.format(
                "spring-rabbitmq:%s?exchangeType=topic&queues=%s&routingKey=%s&autoDeclare=true",
                exchangeName, QUEUE, ROUTING_KEY);
    }
}
