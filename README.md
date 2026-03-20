# Library — sistema distribuído (TCC)

Monorepo com **catalog-service**, **inventory-service**, **rental-service**, **search-service** (Spring Boot) e **web** (Next.js).

## Pré-requisitos

- **Docker Desktop** (ou Docker Engine + Compose v2) para a opção com containers.
- **Java 21** e **Maven** (ou `./mvnw` em cada serviço) para rodar os backends na máquina.
- **Node.js 22+** e **npm** para rodar o front localmente.

Credenciais e portas padrão estão alinhadas com `docker/docker-compose-network.yml` e com os `application.properties` de cada serviço.

---

## 1. Subir tudo localmente com Docker

Infraestrutura (bancos, RabbitMQ, Elasticsearch e rede `library_network`) e, em seguida, os cinco containers da aplicação.

Na pasta **`docker/`**:

```bash
# 1) Rede + serviços complementares (aguarde o Elasticsearch ficar saudável, ~40s ou mais)
docker compose -f docker-compose-network.yml up -d

# 2) APIs + front (build na primeira vez)
docker compose -f docker-compose-app.yml up -d --build
```

- **Front:** http://localhost:3000  
- **APIs:** catalog `8080`, inventory `8081`, rental `8082`, search `8083`  
- **RabbitMQ Management:** http://localhost:15672 (usuário `library_user`, senha `library_secret`)  
- **Elasticsearch:** http://localhost:9200  

Para encerrar os containers da aplicação (mantendo a infra):

```bash
docker compose -f docker-compose-app.yml down
```

Para encerrar infra + rede (volumes persistem, salvo uso de `-v`):

```bash
docker compose -f docker-compose-network.yml down
```

> **Linux:** se o Elasticsearch não subir, tente: `sudo sysctl -w vm.max_map_count=262144`.

### Imagens pré-buildadas (GHCR)

Em cada push na branch `main`, o workflow [.github/workflows/publish-container-images.yml](.github/workflows/publish-container-images.yml) faz build e push para o **GitHub Container Registry**. Para subir só com `pull` (sem `--build` local), use `docker/docker-compose-app.registry.yml` e um ficheiro `docker/.env.registry` (ex.: copiar [docker/.env.registry.example](docker/.env.registry.example)). Passo a passo completo: [docs/ghcr-e-github-actions.md](docs/ghcr-e-github-actions.md).

### Dados `data.sql` e reinícios

Com `spring.sql.init.mode=always`, ao **subir de novo** um serviço contra o **mesmo volume** do Postgres, o Spring tenta executar de novo os scripts `schema.sql` / `data.sql`. Os `INSERT` sem `ON CONFLICT` tendem a **falhar** (chave duplicada), não a duplicar linhas silenciosamente.  
**Subir o compose duas vezes** sem apagar volumes **não** dobra os dados se o primeiro start já populou o banco e o segundo só **reinicia** o container (os dados já estão lá). O problema aparece se você **forçar** reexecução dos scripts no mesmo banco (reinstalar app com `always` sem limpar tabelas). Para ambiente limpo: `docker compose ... down -v` e subir de novo.

---

## 2. Aplicações na máquina + só Docker para infra

Use o mesmo compose de **rede e serviços complementares**; rode catalog, inventory, rental, search e web **fora** do Docker (IDE ou terminal).

### 2.1 Subir apenas infra no Docker

Na pasta **`docker/`**:

```bash
docker compose -f docker-compose-network.yml up -d
```

Os `application.properties` já apontam para **localhost** nos hosts esperados:

| Serviço            | Host no `application.properties` | Porta no host |
|--------------------|-------------------------------------|---------------|
| Catalog DB         | `localhost`                         | `5432`        |
| Inventory DB       | `localhost`                         | `5433`        |
| Rental DB          | `localhost`                         | `5434`        |
| RabbitMQ           | `localhost`                         | `5672`        |
| Elasticsearch      | `localhost`                         | `9200`        |

### 2.2 Ordem sugerida para subir os backends

1. Aguarde os healthchecks (principalmente o Elasticsearch).  
2. Suba **catalog-service** (porta **8080**).  
3. Suba **inventory-service** (**8081**) e **rental-service** (**8082**).  
4. Suba **search-service** (**8083**) — indexa/consome eventos; o índice pode ser preenchido pelo seed na primeira subida se o índice estiver vazio.

Em cada pasta `*-service/`:

```bash
./mvnw spring-boot:run
```

(Windows: `mvnw.cmd spring-boot:run`.)

### 2.3 Front (Next.js)

Na pasta **`web/`**:

```bash
npm install
npm run dev
```

Abra http://localhost:3000. As variáveis `NEXT_PUBLIC_*_API_URL` podem ser omitidas em desenvolvimento se os defaults do código apontarem para `localhost` nas portas corretas; caso contrário, crie `.env.local` com:

```env
NEXT_PUBLIC_CATALOG_API_URL=http://localhost:8080
NEXT_PUBLIC_INVENTORY_API_URL=http://localhost:8081
NEXT_PUBLIC_RENTAL_API_URL=http://localhost:8082
NEXT_PUBLIC_SEARCH_API_URL=http://localhost:8083
```

### 2.4 Parar só a infra

```bash
cd docker
docker compose -f docker-compose-network.yml down
```

---

## Estrutura útil

| Pasta              | Conteúdo                          |
|--------------------|-----------------------------------|
| `docker/`          | `docker-compose-network.yml`, `docker-compose-app.yml` |
| `catalog-service/` | API do catálogo de livros         |
| `inventory-service/` | Cópias / disponibilidade      |
| `rental-service/`  | Aluguéis / usuários               |
| `search-service/`  | Busca (Elasticsearch)             |
| `web/`             | Interface Next.js                 |

Documentação adicional costuma estar em `docs/` ou em `docs/` dentro de cada serviço.
