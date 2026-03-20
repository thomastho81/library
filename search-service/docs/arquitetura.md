# Arquitetura do search-service (em camadas)

O search-service segue a **mesma arquitetura em camadas** dos outros projetos (catalog-service, inventory-service, rental-service): controller → service → repository, com DTOs e documentos de persistência.

## Estrutura de pacotes

```
br.com.thomas.library.search_service
├── SearchServiceApplication.java
├── constants
│   └── DateFormatConstants.java
├── document
│   └── BookDocument.java              # Documento Elasticsearch (índice books)
├── dto
│   └── propagation
│       └── BookPropagationPayload.java # Payload dos eventos catalog
├── repository
│   └── BookDocumentRepository.java    # ElasticsearchRepository
├── route
│   ├── catalog
│   │   ├── BookCreatedConsumerRouteBuilder.java
│   │   ├── BookUpdatedConsumerRouteBuilder.java
│   │   └── BookDeletedConsumerRouteBuilder.java
│   └── inventory
│       └── InventoryAvailabilityConsumerRouteBuilder.java
└── service
    ├── CatalogSyncService.java              # Sincroniza índice com eventos catalog (created/updated/deleted)
    └── InventoryAvailabilitySyncService.java # Sincroniza disponibilidade com eventos inventory.book.availability
```

## Fluxo

1. **RabbitMQ** (exchange `catalog.topic`) publica eventos com routing keys `catalog.book.created`, `catalog.book.updated`, `catalog.book.deleted`.
2. **Rotas Camel** consomem as filas `search.book.*`, deserializam o JSON para `BookPropagationPayload` e chamam o método correspondente do `CatalogSyncService`.
3. **CatalogSyncService** converte o payload em `BookDocument` e usa o `BookDocumentRepository` para fazer `save` ou `deleteById` no Elasticsearch.
4. **Elasticsearch** roda em container Docker; o search-service conecta em `http://localhost:9200` (porta mapeada no compose).

## Consumo de eventos do catalog

| Evento        | Exchange       | Routing key           | Fila                 | Service método   |
|---------------|----------------|------------------------|----------------------|------------------|
| Livro criado  | `catalog.topic` | `catalog.book.created`  | `search.book.created`  | `onBookCreated`  |
| Livro atualizado | `catalog.topic` | `catalog.book.updated`  | `search.book.updated`  | `onBookUpdated`  |
| Livro excluído | `catalog.topic` | `catalog.book.deleted`  | `search.book.deleted`  | `onBookDeleted`  |
| Disponibilidade | `rental.topic`  | `inventory.book.availability` | `search.inventory.availability` | InventoryAvailabilitySyncService.onAvailability |

## Documentação adicional

- **Elasticsearch no Docker e explicação das camadas:** `docs/elasticsearch-e-camadas.md` (configuração do compose, conexão, papel de cada classe e referência ao que existia na Clean Architecture).
