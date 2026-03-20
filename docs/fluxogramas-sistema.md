# Fluxogramas: sistema, broker e persistência

Documento com fluxogramas do sistema de biblioteca distribuída: visão por serviço e visão geral. Os diagramas usam **Mermaid** (renderizam no GitHub, GitLab e em editores com suporte).

---

## 1. Visão geral do sistema

```
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│                                    SISTEMA (Biblioteca)                                    │
├─────────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                           │
│   ┌──────────────┐     REST      ┌─────────────────┐     eventos      ┌──────────────┐   │
│   │   Cliente/   │ ────────────► │ catalog-service  │ ───────────────► │   RabbitMQ   │   │
│   │   API       │               │ (CRUD livros)    │  catalog.topic   │ (broker)     │   │
│   └──────────────┘               └────────┬────────┘                  └──────┬───────┘   │
│         │                                    │                                  │          │
│         │ REST                               │ PostgreSQL                       │          │
│         ▼                                    ▼                                  │          │
│   ┌──────────────┐               ┌─────────────────┐                            │          │
│   │ rental-      │◄─────────────│ inventory-      │◄── rental.topic ───────────┤          │
│   │ service      │  reserve/     │ service         │   (reserve/return/           │          │
│   │ (empréstimos)│  return      │ (estoque)       │    availability)            │          │
│   └──────┬───────┘               └────────┬────────┘                            │          │
│          │                                │                                        │          │
│          │ PostgreSQL                     │ PostgreSQL                             │          │
│          ▼                                ▼                                        │          │
│   ┌──────────────┐               ┌─────────────────┐     inventory.book.          │          │
│   │              │               │ search-service  │◄── availability ──────────────┤          │
│   │              │               │ (busca)         │     catalog.book.*           │          │
│   │              │               └────────┬────────┘◄─────────────────────────────┘          │
│   │              │                        │                                                  │
│   │              │                        ▼ Elasticsearch                                    │
│   │              │               ┌─────────────────┐                                        │
│   │              │               │   Elasticsearch │                                        │
│   │              │               │   (índice books)│                                        │
│   │              │               └─────────────────┘                                        │
│   └──────────────┘                                                                          │
└─────────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Broker de mensagens (RabbitMQ) – exchanges, routing keys e filas

### 2.1 Exchange **catalog.topic** (tipo topic)

| Quem publica    | Routing key            | Quem consome        | Fila                  |
|----------------|------------------------|---------------------|------------------------|
| catalog-service| catalog.book.created   | inventory-service   | inventory.book.created |
| catalog-service| catalog.book.updated   | inventory-service   | inventory.book.updated |
| catalog-service| catalog.book.deleted   | inventory-service   | inventory.book.deleted |
| catalog-service| catalog.book.created   | search-service      | search.book.created    |
| catalog-service| catalog.book.updated   | search-service      | search.book.updated    |
| catalog-service| catalog.book.deleted   | search-service      | search.book.deleted    |

### 2.2 Exchange **rental.topic** (tipo topic)

| Quem publica    | Routing key                     | Quem consome        | Fila                          |
|----------------|---------------------------------|---------------------|-------------------------------|
| rental-service | rental.inventory.reserve        | inventory-service   | inventory.rental.reserve      |
| rental-service | rental.inventory.return         | inventory-service   | inventory.rental.return       |
| inventory-svc  | inventory.rental.reserve.result | rental-service      | rental.inventory.reserve.result |
| inventory-svc  | inventory.rental.return.result   | rental-service      | rental.inventory.return.result  |
| inventory-svc  | inventory.book.availability     | search-service      | search.inventory.availability   |

### 2.3 Diagrama do broker (Mermaid)

```mermaid
flowchart LR
    subgraph catalog_topic["Exchange: catalog.topic"]
        c_created[catalog.book.created]
        c_updated[catalog.book.updated]
        c_deleted[catalog.book.deleted]
    end

    subgraph rental_topic["Exchange: rental.topic"]
        r_reserve[rental.inventory.reserve]
        r_return[rental.inventory.return]
        inv_result[inventory.rental.reserve.result]
        inv_return_result[inventory.rental.return.result]
        inv_avail[inventory.book.availability]
    end

    Catalog[("catalog-service")]
    Inventory[("inventory-service")]
    Rental[("rental-service")]
    Search[("search-service")]

    Catalog --> c_created
    Catalog --> c_updated
    Catalog --> c_deleted

    c_created --> Inventory
    c_updated --> Inventory
    c_deleted --> Inventory
    c_created --> Search
    c_updated --> Search
    c_deleted --> Search

    Rental --> r_reserve
    Rental --> r_return
    r_reserve --> Inventory
    r_return --> Inventory

    Inventory --> inv_result
    Inventory --> inv_return_result
    Inventory --> inv_avail
    inv_result --> Rental
    inv_return_result --> Rental
    inv_avail --> Search
```

---

## 3. Banco de dados e motor de busca por serviço

### 3.1 catalog-service

- **Persistência:** PostgreSQL (banco `catalog_db`).
- **Tabela principal:** `tb_livro` (livros: título, autor, categoria, ISBN, etc.).
- **Fluxo:** API REST → BookController → BookService → BookRepository (JPA) → PostgreSQL. Após create/update/delete, publica em `catalog.topic`.

```mermaid
flowchart LR
    API[REST API] --> Controller[BookController]
    Controller --> Service[BookService]
    Service --> Repo[BookRepository]
    Repo --> PG[(PostgreSQL\ncatalog_db)]
    Service --> Propagation[PropagationService]
    Propagation --> MQ[RabbitMQ\ncatalog.topic]
```

### 3.2 inventory-service

- **Persistência:** PostgreSQL (banco `inventory_db`).
- **Tabelas:** `tb_inventario` (por livro: total, disponíveis, reservadas), `tb_evento_processado` (idempotência de eventos de rental).
- **Fluxo:** Consome catalog.topic (cria/atualiza/inativa inventário) e rental.topic (reserva/devolução). Persiste no PostgreSQL e publica resultado (reserve.result) e disponibilidade (inventory.book.availability) em rental.topic.

```mermaid
flowchart LR
    MQ1[RabbitMQ\ncatalog.topic] --> Sync[InventorySyncService]
    MQ2[RabbitMQ\nrental.topic] --> RentalSvc[RentalEventService]
    Sync --> InvRepo[InventoryRepository]
    RentalSvc --> InvRepo
    RentalSvc --> EventRepo[ProcessedEventRepository]
    InvRepo --> PG[(PostgreSQL\ninventory_db)]
    EventRepo --> PG
    Sync --> Prop[InventoryPropagationService]
    RentalSvc --> Prop
    Prop --> MQ3[RabbitMQ\nrental.topic]
```

### 3.3 rental-service

- **Persistência:** PostgreSQL (banco `rental_db`).
- **Tabelas:** empréstimos/reservas (ex.: `tb_rental` ou equivalente).
- **Fluxo:** API REST para criar empréstimo → publica `rental.inventory.reserve` em rental.topic. Consome `inventory.rental.reserve.result`. Para devolução, publica `rental.inventory.return`.

```mermaid
flowchart LR
    API[REST API] --> RentalCtrl[RentalController]
    RentalCtrl --> RentalSvc[RentalService]
    RentalSvc --> Repo[RentalRepository]
    Repo --> PG[(PostgreSQL\nrental_db)]
    RentalSvc --> Prop[PropagationService]
    Prop --> MQ1[RabbitMQ\nrental.inventory.reserve\nrental.inventory.return]
    MQ2[RabbitMQ\ninventory.rental.reserve.result] --> ResultConsumer[ReserveResultConsumer]
    ResultConsumer --> RentalSvc
```

### 3.4 search-service

- **Persistência:** Elasticsearch (índice `books`).
- **Fluxo:** Consome catalog.topic (create/update/delete do livro) → atualiza documento completo no índice. Consome rental.topic (inventory.book.availability) → atualiza apenas totalCopies, availableCopies e inventoryUpdatedAt no documento.

```mermaid
flowchart LR
    MQ1[RabbitMQ\ncatalog.topic] --> CatalogRoutes[BookCreated/Updated/Deleted\nRouteBuilders]
    MQ2[RabbitMQ\nrental.topic\ninventory.book.availability] --> AvailRoute[InventoryAvailability\nRouteBuilder]
    CatalogRoutes --> Sync[CatalogSyncService]
    AvailRoute --> Sync
    Sync --> Repo[BookDocumentRepository]
    Repo --> ES[(Elasticsearch\níndice books)]
```

---

## 4. Visão integrada: banco de dados e motor de busca

```mermaid
flowchart TB
    subgraph Catalog["catalog-service"]
        C_DB[(PostgreSQL\ncatalog_db)]
    end

    subgraph Inventory["inventory-service"]
        I_DB[(PostgreSQL\ninventory_db)]
    end

    subgraph Rental["rental-service"]
        R_DB[(PostgreSQL\nrental_db)]
    end

    subgraph Search["search-service"]
        ES[(Elasticsearch\níndice books)]
    end

    Catalog -->|eventos catalog.book.*| Inventory
    Catalog -->|eventos catalog.book.*| Search
    Rental -->|rental.inventory.reserve/return| Inventory
    Inventory -->|inventory.rental.reserve.result| Rental
    Inventory -->|inventory.book.availability| Search

    C_DB -.->|fonte da verdade livros| Catalog
    I_DB -.->|estoque por livro| Inventory
    R_DB -.->|empréstimos| Rental
    ES -.->|índice de busca| Search
```

---

## 5. Resumo: mesma exchange para inventory → rental e inventory → search?

O inventory-service publica em **rental.topic** dois tipos de evento:

1. **inventory.rental.reserve.result** — consumido apenas pelo **rental-service** (resposta da reserva).
2. **inventory.book.availability** — consumido apenas pelo **search-service** (atualização de total/cópias disponíveis).

**É possível usar a mesma exchange?** Sim. Foi adotada a **rental.topic** para ambos: cada consumidor declara sua fila com o binding na routing key que lhe interessa (rental-service: `inventory.rental.reserve.result`; search-service: `inventory.book.availability`).

**É indicado?** Para este escopo, sim: uma exchange a menos, sem alterar o rental-service, e separação lógica por routing key. Em um sistema maior, pode-se criar uma exchange **inventory.topic** e o inventory publicar todos os seus eventos lá (incluindo reserve.result e availability), com o rental e o search consumindo dessa nova exchange — fica mais alinhado a “um contexto (inventory) uma exchange”, com pequeno refactor no rental.
