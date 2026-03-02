package br.com.thomas.library.inventory_service.route.catalog;

import br.com.thomas.library.inventory_service.dto.propagation.BookPropagationPayload;
import br.com.thomas.library.inventory_service.service.InventorySyncService;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Consome eventos <strong>catalog-service</strong>: catalog.book.updated (exchange catalog.topic).
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
                .routeId("CATALOG_UPDATE_ROUTE")
                .unmarshal().json(BookPropagationPayload.class)
                .log("Evento book.updated recebido (catalog) - bookId=${body.id}, title=${body.title}")
                .bean(InventorySyncService.class, "onBookUpdated");
    }

    private String consumerUri() {
        return String.format(
                "spring-rabbitmq:%s?exchangeType=topic&queues=%s&routingKey=%s&autoDeclare=true",
                exchangeName, QUEUE, ROUTING_KEY);
    }
}
