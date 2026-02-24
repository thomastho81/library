# Docker – Biblioteca distribuída (centralizado)

Ambiente local centralizado: **PostgreSQL** do catalog-service, **PostgreSQL** do inventory-service e **RabbitMQ** compartilhado. Uso: catalog-service publica eventos; inventory-service consome.

## Pré-requisitos

- [Docker](https://docs.docker.com/get-docker/) e [Docker Compose](https://docs.docker.com/compose/install/) instalados.

## Subir os containers

Na pasta `docker/`:

```bash
docker compose up -d
```

Sobe:

- **PostgreSQL catalog** (porta 5432) – `catalog-postgres`
- **PostgreSQL inventory** (porta 5433) – `inventory-postgres`
- **RabbitMQ** (AMQP 5672, Management UI 15672) – `library-rabbitmq`

Para ver os logs:

```bash
docker compose logs -f postgres
docker compose logs -f inventory-postgres
docker compose logs -f rabbitmq
```

## Parar os containers

```bash
docker compose down
```

Para remover também os volumes (apaga os dados):

```bash
docker compose down -v
```

## Variáveis de ambiente (opcional)

Crie um arquivo `.env` na pasta `docker/` para sobrescrever valores padrão:

| Variável               | Padrão           | Descrição                    |
|------------------------|------------------|------------------------------|
| `POSTGRES_USER`        | `catalog_user`   | Usuário do PostgreSQL        |
| `POSTGRES_PASSWORD`    | `catalog_secret` | Senha do usuário             |
| `POSTGRES_DB`          | `catalog_db`     | Nome do banco de dados       |
| `POSTGRES_PORT`        | `5432`           | Porta exposta no host (mapear no compose se alterar) |

## Conexão no catalog-service (PostgreSQL)

Com os valores padrão do `docker-compose`, configure o datasource no `application.properties` (ou em variáveis de ambiente da aplicação):

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/catalog_db
spring.datasource.username=catalog_user
spring.datasource.password=catalog_secret
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA / Hibernate
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=validate
```

Ajuste host, porta, usuário e senha se usar `.env` ou outro ambiente.

## RabbitMQ – interface web (Management)

- **URL:** http://localhost:15672  
- **Usuário:** `library_user`  
- **Senha:** `library_secret`  

Na interface é possível ver exchanges, filas, bindings e mensagens. A exchange `catalog.topic` e as filas do inventory-service (`inventory.book.created`, etc.) são criadas automaticamente ao publicar/consumir.

## Conexão do inventory-service (PostgreSQL)

No `application.properties` do **inventory-service**:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5433/inventory_db
spring.datasource.username=inventory_user
spring.datasource.password=inventory_secret
spring.datasource.driver-class-name=org.postgresql.Driver
```

## Conexão do catalog-service ao RabbitMQ

No `application.properties` (para propagação e consumidores Camel):

```properties
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=library_user
spring.rabbitmq.password=library_secret
```

## Versões

- **PostgreSQL:** imagem **postgres:16-alpine** (PostgreSQL 16).
- **RabbitMQ:** imagem **rabbitmq:3.13-management** (RabbitMQ 3.13 com plugin de gerenciamento e interface web).
