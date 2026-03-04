# Design do search-service: documento, eventos e funcionalidades

Este documento descreve o **BookDocument** no Elasticsearch, a adequação dos eventos do catalog-service, a necessidade de consumir eventos do inventory-service e as funcionalidades que o search-service deve oferecer.

---

## 1. BookDocument (Elasticsearch)

O documento de livro no índice `books` contém:

| Campo | Tipo ES | Fonte | Uso |
|-------|---------|-------|-----|
| `id` | — (document id) | catalog (`BookPropagationPayload.id`) | Identificador do livro; usado como `_id` no índice. |
| `title` | Text (analyzer standard) | catalog | Busca full-text, relevância. |
| `author` | Text (analyzer standard) | catalog | Busca full-text. |
| `category` | Keyword | catalog | Filtro exato, facetas. |
| `genre` | Keyword | catalog | Filtro exato, facetas. |
| `description` | Text (analyzer standard) | catalog | Busca full-text. |
| `isbn` | Keyword | catalog | Filtro exato, busca por ISBN. |
| `publishedYear` | Integer | catalog | Filtro (faixa de ano), facetas. |
| `active` | Boolean | catalog | Filtro (ex.: só livros ativos). |
| `createdAt` | Date | catalog | Ordenação, filtro por período. |
| `updatedAt` | Date | catalog | Ordenação, sincronia. |
| `totalCopies` | Integer | **inventory** | Exibição, filtro (ex.: “tem pelo menos 1 cópia”). |
| `availableCopies` | Integer | **inventory** | **Disponibilidade**, filtro “só disponíveis”, ordenação. |
| `inventoryUpdatedAt` | Date | inventory | Auditoria da última atualização de estoque no documento. |

- **Metadados (título, autor, categoria, etc.):** preenchidos e atualizados a partir dos eventos do **catalog-service**.
- **Quantidade e disponibilidade:** preenchidos a partir dos eventos do **inventory-service**. Enquanto o inventory não publicar eventos, esses campos podem ficar `null`; a API de busca pode indicar “disponibilidade sob consulta” ou enriquecer com chamada ao inventory (híbrido).

Classe Java: `br.com.thomas.library.search_service.document.BookDocument`.

---

## 2. Eventos do catalog-service: são suficientes?

**Sim.** O catalog-service já publica tudo que o search-service precisa para **metadados** do livro.

| Evento | Exchange | Routing key | Payload | Ação no search-service |
|--------|----------|-------------|---------|-------------------------|
| Criação | `catalog.topic` | `catalog.book.created` | `BookPropagationPayload` | Indexar novo documento (sem disponibilidade até haver evento de inventory). |
| Atualização | `catalog.topic` | `catalog.book.updated` | `BookPropagationPayload` | Atualizar documento existente (merge por `id`). |
| Exclusão | `catalog.topic` | `catalog.book.deleted` | `BookPropagationPayload` | Remover documento do índice. |

O **BookPropagationPayload** contém: `id`, `title`, `author`, `category`, `genre`, `description`, `isbn`, `publishedYear`, `active`, `createdAt`, `updatedAt`. Esses campos mapeiam diretamente para o `BookDocument` (conversão de `LocalDateTime` para `Instant` se necessário).

**Conclusão:** não é necessário alterar o contrato de eventos do catalog-service para o search-service. Basta o search-service consumir as três routing keys (`catalog.book.created`, `catalog.book.updated`, `catalog.book.deleted`) e manter o índice de livros em sincronia com o catálogo.

---

## 3. O search-service deve consumir eventos do inventory-service?

**Sim.** Para oferecer **quantidade e disponibilidade** no resultado de busca (e filtros como “apenas livros disponíveis”), o search-service precisa manter esses dados no documento. A forma recomendada é o **inventory-service publicar eventos** quando a disponibilidade mudar; o search-service consome e atualiza apenas os campos de inventário do documento (partial update).

### Situação atual do inventory-service

- **Consome:** eventos do catalog (`catalog.book.created/updated/deleted`) e do rental (`rental.inventory.reserve`, `rental.inventory.return`).
- **Publica:** apenas `inventory.rental.reserve.result` (resposta ao rental-service). **Não publica** eventos de “inventário atualizado” ou “disponibilidade alterada”.

### Recomendação: novo evento de inventário

Introduzir um evento publicado pelo **inventory-service** sempre que o inventário de um livro for criado ou alterado (ex.: após processar reserve/return ou ao criar/atualizar registro de inventário):

- **Exchange:** por exemplo `inventory.topic` (ou reutilizar uma exchange existente, desde que o padrão fique claro).
- **Routing key:** ex.: `inventory.book.availability` ou `inventory.book.updated`.
- **Payload sugerido:**  
  `bookId` (Long), `totalCopies` (Integer), `availableCopies` (Integer), opcionalmente `updatedAt` (Instant/LocalDateTime).

O **search-service** passaria a:

- Consumir esse evento.
- Fazer **partial update** no documento do livro no Elasticsearch (atualizar apenas `totalCopies`, `availableCopies` e `inventoryUpdatedAt`), sem sobrescrever título, autor, etc.

Assim, o search-service não precisa chamar a API do inventory em tempo de busca; a disponibilidade fica no próprio índice e permite:

- Filtrar por “apenas com disponibilidade > 0”.
- Ordenar por disponibilidade.
- Exibir quantidade total e disponível na listagem de busca.

**Alternativa (híbrida):** se não for possível publicar eventos do inventory a curto prazo, o search-service pode manter só metadados no índice e, na API de busca, enriquecer com uma chamada ao inventory-service (por livro ou em lote). Isso adiciona latência e acoplamento; consumir eventos é a solução mais escalável e alinhada ao desenho atual (catalog → eventos → search).

---

## 4. Funcionalidades que o search-service deve ter

Lista objetiva do que o search-service deve oferecer:

1. **Busca full-text por livros**  
   Parâmetros: texto livre (ex.: `q`), com busca em título, autor, descrição, etc., e suporte a paginação.

2. **Filtros**  
   - Por categoria, gênero, ano de publicação (ou faixa), livro ativo.  
   - Por disponibilidade: “apenas disponíveis” (ex.: `availableCopies > 0`) quando o índice tiver dados de inventory.

3. **Ordenação**  
   Por relevância (default), título, autor, ano, data de atualização e, quando houver dados de inventory, por disponibilidade (ex.: mais disponíveis primeiro).

4. **Sincronização do índice com o catálogo**  
   Consumir `catalog.book.created`, `catalog.book.updated`, `catalog.book.deleted` e manter o índice `books` consistente (create/update/delete de documentos).

5. **Sincronização de quantidade e disponibilidade**  
   Consumir eventos do inventory (quando existirem) e fazer partial update de `totalCopies`, `availableCopies` e `inventoryUpdatedAt` no documento.

6. **Exposição de quantidade e disponibilidade na resposta**  
   Incluir `totalCopies` e `availableCopies` nos resultados de busca (e indicar “não informado” quando forem `null`).

7. **API REST de busca**  
   Endpoint(s) estável(is), por exemplo `GET /api/search/books`, com parâmetros de query, filtros e paginação.

8. **Cache (opcional)**  
   Se houver Redis, cachear resultados de buscas frequentes e invalidar (ou atualizar) quando eventos do catalog/inventory forem processados.

9. **Resiliência e idempotência**  
   Consumo de mensagens com tratamento de erro e, se aplicável, idempotência (ex.: usar `bookId` + versão ou timestamp para evitar aplicar o mesmo evento duas vezes).

10. **Health e operação**  
    Health check do Elasticsearch (e do RabbitMQ/Redis, se usados) para monitoramento e deploy.

---

## 5. Resumo

| Pergunta | Resposta |
|----------|----------|
| **BookDocument** | Definido em `BookDocument.java`: metadados (catalog) + totalCopies, availableCopies, inventoryUpdatedAt (inventory). |
| **Eventos do catalog são suficientes?** | Sim, para metadados. Criated/updated/deleted cobrem criação, atualização e remoção no índice. |
| **Consumir eventos do inventory?** | Sim, recomendado. Permite manter quantidade e disponibilidade no índice e oferecer filtros/ordenação por disponibilidade. |
| **O que o inventory precisa fazer?** | Passar a publicar um evento (ex.: `inventory.book.availability`) com bookId, totalCopies, availableCopies quando o inventário for criado ou alterado. |
| **Funcionalidades do search-service** | Busca full-text, filtros (incl. disponibilidade), ordenação, sincronia com catalog e inventory via eventos, API REST, opcionalmente cache e health. |

Com isso, o documento BookDocument fica desenhado, os eventos do catalog são suficientes para metadados, e fica explícito que o search-service deve consumir também eventos do inventory para quantidade e disponibilidade, assim que o inventory-service passar a publicá-los.
