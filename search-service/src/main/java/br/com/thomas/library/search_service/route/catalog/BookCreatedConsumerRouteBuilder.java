package br.com.thomas.library.search_service.route.catalog;

import br.com.thomas.library.search_service.dto.propagation.BookPropagationPayload;
import br.com.thomas.library.search_service.service.CatalogSyncService;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Consome eventos <strong>catalog-service</strong>: catalog.book.created (exchange catalog.topic).
 */
@Component
public class BookCreatedConsumerRouteBuilder extends RouteBuilder {

    private static final String ROUTING_KEY = "catalog.book.created";
    private static final String QUEUE = "search.book.created";

    @Value("${catalog.propagation.exchange:catalog.topic}")
    private String exchangeName;

    @Override
    public void configure() {
        from(consumerUri())
                .routeId("CATALOG_CREATE_ROUTE")
                .unmarshal().json(BookPropagationPayload.class)
                .log("Evento book.created recebido (catalog) - bookId=${body.id}, title=${body.title}")
                .bean(CatalogSyncService.class, "onBookCreated");
    }

    private String consumerUri() {
        return String.format(
                "spring-rabbitmq:%s?exchangeType=topic&queues=%s&routingKey=%s&autoDeclare=true",
                exchangeName, QUEUE, ROUTING_KEY);
    }
}
