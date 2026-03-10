package br.com.thomas.library.search_service.route.inventory;

import br.com.thomas.library.search_service.dto.propagation.BookAvailabilityPayload;
import br.com.thomas.library.search_service.service.InventoryAvailabilitySyncService;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Consome eventos <strong>inventory-service</strong>: inventory.book.availability (exchange rental.topic).
 * Atualiza totalCopies, availableCopies e inventoryUpdatedAt no documento do livro no Elasticsearch.
 */
@Component
public class InventoryAvailabilityConsumerRouteBuilder extends RouteBuilder {
//TODO: fazer entryPoints de SAGA nos services de consumo do processo
    private static final String ROUTING_KEY = "inventory.book.availability";
    private static final String QUEUE = "search.inventory.availability";

    @Value("${inventory.propagation.exchange:rental.topic}")
    private String exchangeName;

    @Override
    public void configure() {
        from(consumerUri())
                .routeId("INVENTORY_AVAILABILITY_ROUTE")
                .unmarshal().json(BookAvailabilityPayload.class)
                .log("Evento inventory.book.availability recebido - bookId=${body.bookId}, available=${body.availableCopies}")
                .bean(InventoryAvailabilitySyncService.class, "onAvailability");
    }

    private String consumerUri() {
        return String.format(
                "spring-rabbitmq:%s?exchangeType=topic&queues=%s&routingKey=%s&autoDeclare=true",
                exchangeName, QUEUE, ROUTING_KEY);
    }
}
