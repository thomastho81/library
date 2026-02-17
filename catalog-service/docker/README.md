# Docker - Catalog Service

Ambiente local com **PostgreSQL** e **RabbitMQ** (management) para o **catalog-service**.

## Pré-requisitos

- [Docker](https://docs.docker.com/get-docker/) e [Docker Compose](https://docs.docker.com/compose/install/) instalados.

## Subir os containers

Na pasta `docker/`:

```bash
docker compose up -d
```

Sobe **PostgreSQL** (porta 5432) e **RabbitMQ** (AMQP 5672, Management UI 15672).

Para ver os logs:

```bash
docker compose logs -f postgres
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
- **Usuário:** `catalog_user`  
- **Senha:** `catalog_secret`  

Na interface é possível ver exchanges, filas, bindings e mensagens. A exchange `catalog.topic` e as filas são criadas automaticamente quando o catalog-service publica/consome.

## Conexão do catalog-service ao RabbitMQ

No `application.properties` (para propagação e consumidores Camel):

```properties
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=catalog_user
spring.rabbitmq.password=catalog_secret
```

## Versões

- **PostgreSQL:** imagem **postgres:16-alpine** (PostgreSQL 16).
- **RabbitMQ:** imagem **rabbitmq:3.13-management** (RabbitMQ 3.13 com plugin de gerenciamento e interface web).
