package br.com.thomas.library.catalog_service.route;

import br.com.thomas.library.catalog_service.dto.propagation.BookPropagationPayload;
import br.com.thomas.library.catalog_service.dto.propagation.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Rotas Camel para teste: consumidores da exchange topic com uma fila por
 * OperationType. Apenas consome a mensagem e loga id, título do livro e operação.
 * <p>
 * Requer RabbitMQ configurado (spring.rabbitmq.* ou connectionFactory no
 * componente Camel). Para desabilitar em produção, remova @Component ou use um
 * profile.
 */
@Component
@Slf4j
public class BookPropagationConsumerRouteBuilder extends RouteBuilder {

    private static final String EXCHANGE_TYPE_TOPIC = "topic";

    @Value("${catalog.propagation.exchange:catalog.topic}")
    private String exchangeName;

    @Override
    public void configure() {
        consumeAndLog(OperationType.CREATE);
        consumeAndLog(OperationType.UPDATE);
        consumeAndLog(OperationType.DELETE);
    }

    private void consumeAndLog(OperationType operationType) {
        String routingKey = operationType.getRoutingKey();
        String queueName = "queue." + routingKey.replace('.', '-');

        String endpointUri = String.format(
                "spring-rabbitmq:%s?queues=%s&routingKey=%s&exchangeType=%s",
                exchangeName, queueName, routingKey, EXCHANGE_TYPE_TOPIC
        );

        from(endpointUri)
                .autoStartup(false)
                .unmarshal().json(BookPropagationPayload.class)

                .process(exchange -> {
                    BookPropagationPayload payload = exchange.getMessage().getBody(BookPropagationPayload.class);
                    if (payload != null) {
                        log.info("[Propagação] operação={} | id={} | livro={}",
                                operationType.getOperation(),
                                payload.getId(),
                                payload.getTitle());
                    }
                });
    }
}
