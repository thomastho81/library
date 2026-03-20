package br.com.thomas.library.search_service.service;

import br.com.thomas.library.search_service.document.BookDocument;
import br.com.thomas.library.search_service.dto.response.BookSearchResponse;
import br.com.thomas.library.search_service.dto.response.PagedBookSearchResponse;
import br.com.thomas.library.search_service.repository.BookDocumentRepository;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Serviço de busca de livros no Elasticsearch.
 * Oferece busca parametrizada (full-text + filtros) paginada e visualização por id.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BookSearchService {

    private static final int DEFAULT_PAGE_SIZE = 25;

    private final BookDocumentRepository bookDocumentRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    /**
     * Busca parametrizada de livros com paginação (padrão 25 por página).
     *
     * @param q                 texto livre (busca em title, author, description)
     * @param category         filtro exato categoria
     * @param genre            filtro exato gênero
     * @param publishedYearFrom ano mínimo (inclusive)
     * @param publishedYearTo   ano máximo (inclusive)
     * @param title            filtro por trecho no título
     * @param author           filtro por trecho no autor
     * @param isbn             filtro exato ISBN
     * @param active           filtro ativo (default true = apenas ativos; false = apenas inativos)
     * @param all              quando true, ignora active e retorna todos (ativos e inativos)
     * @param availableOnly    apenas com availableCopies >= 1
     * @param sortBy           campo de ordenação (title, author, publishedYear, updatedAt, availableCopies)
     * @param sortDir          direção: asc ou desc (opcional; default por campo)
     * @param pageable         paginação (default 25 por página, size máximo 100)
     */
    public PagedBookSearchResponse search(
            String q,
            String category,
            String genre,
            Integer publishedYearFrom,
            Integer publishedYearTo,
            String title,
            String author,
            String isbn,
            Boolean active,
            Boolean all,
            Boolean availableOnly,
            String sortBy,
            String sortDir,
            Pageable pageable) {

        int pageNumber = pageable.getPageNumber() >= 0 ? pageable.getPageNumber() : 0;
        int pageSize = pageable.getPageSize() > 0 && pageable.getPageSize() <= 100
                ? pageable.getPageSize()
                : DEFAULT_PAGE_SIZE;
        Sort sort = buildSort(sortBy, sortDir, q);
        Pageable effectivePageable = PageRequest.of(pageNumber, pageSize, sort);

        boolean includeAll = Boolean.TRUE.equals(all);
        boolean activeFilter = includeAll || (active != null ? active : true);
        boolean filterByActive = !includeAll;
        boolean availableFilter = Boolean.TRUE.equals(availableOnly);

        String requestId = UUID.randomUUID().toString().substring(0, 8);
        log.info("[requestId={}] Busca de livros | params: q={}, category={}, genre={}, publishedYearFrom={}, publishedYearTo={}, title={}, author={}, isbn={}, active={}, all={}, availableOnly={}, sortBy={}, sortDir={}, page={}, size={}",
                requestId, q, category, genre, publishedYearFrom, publishedYearTo, title, author, isbn, activeFilter, includeAll, availableFilter, sortBy, sortDir, pageNumber, pageSize);

        long totalElements;
        List<BookSearchResponse> content;

        Optional<Query> nativeQuery = buildNativeQuery(q, category, genre, publishedYearFrom, publishedYearTo, title, author, isbn, filterByActive, activeFilter, availableFilter);
        if (nativeQuery.isEmpty()) {
            Page<BookDocument> page = bookDocumentRepository.findAll(effectivePageable);
            totalElements = page.getTotalElements();
            content = page.getContent().stream().map(this::toResponse).toList();
        } else {
            org.springframework.data.elasticsearch.core.query.Query springQuery = NativeQuery.builder()
                    .withQuery(nativeQuery.get())
                    .withPageable(effectivePageable)
                    .build();
            SearchHits<BookDocument> searchHits = elasticsearchOperations.search(springQuery, BookDocument.class);
            totalElements = searchHits.getTotalHits();
            content = searchHits.getSearchHits().stream()
                    .map(SearchHit::getContent)
                    .map(this::toResponse)
                    .toList();
        }

        int totalPages = (int) Math.ceil((double) totalElements / pageSize);

        log.info("[requestId={}] Busca concluída | totalElements={}, page={}, size={}, numberOfElements={}, totalPages={}",
                requestId, totalElements, pageNumber, pageSize, content.size(), totalPages);

        return PagedBookSearchResponse.builder()
                .content(content)
                .page(pageNumber)
                .size(pageSize)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .first(pageNumber == 0)
                .last(pageNumber >= totalPages - 1)
                .numberOfElements(content.size())
                .build();
    }

    /**
     * Retorna um livro por id (visualização de único livro).
     */
    public Optional<BookSearchResponse> getById(String id) {
        if (id == null || id.isBlank()) {
            log.warn("getById ignorado: id nulo ou vazio");
            return Optional.empty();
        }
        Optional<BookDocument> doc = bookDocumentRepository.findById(id);
        if (doc.isEmpty()) {
            log.info("Livro não encontrado no índice: id={}", id);
            return Optional.empty();
        }
        log.debug("Livro encontrado: id={}, title={}", id, doc.get().getTitle());
        return Optional.of(toResponse(doc.get()));
    }

    /**
     * Constrói a query nativa (BoolQuery) com wildcards case-insensitive para genre, category, title, author
     * e com o parâmetro q buscando em title, author, description, genre e category.
     * Retorna empty quando não há filtros (match-all).
     */
    private Optional<Query> buildNativeQuery(String q, String category, String genre,
                                             Integer publishedYearFrom, Integer publishedYearTo,
                                             String title, String author, String isbn,
                                             boolean filterByActive, boolean active, boolean availableOnly) {
        List<Query> filterClauses = new ArrayList<>();

        // q: bool should com multi_match (title, author, description) + wildcard case_insensitive em genre e category
        Query qClause = null;
        if (q != null && !q.isBlank()) {
            String term = q.trim();
            String wildcardPattern = "*" + escapeWildcard(term) + "*";
            List<Query> shouldClauses = new ArrayList<>();
            shouldClauses.add(Query.of(qb -> qb.multiMatch(m -> m.query(term).fields("title", "author", "description"))));
            shouldClauses.add(Query.of(qb -> qb.wildcard(w -> w.field("genre").value(wildcardPattern).caseInsensitive(true))));
            shouldClauses.add(Query.of(qb -> qb.wildcard(w -> w.field("category").value(wildcardPattern).caseInsensitive(true))));
            qClause = Query.of(qb -> qb.bool(b -> b.should(shouldClauses).minimumShouldMatch("1")));
        }

        // Filtros: genre, category, title, author com wildcard case_insensitive
        if (genre != null && !genre.isBlank()) {
            String pattern = "*" + escapeWildcard(genre.trim()) + "*";
            filterClauses.add(Query.of(qb -> qb.wildcard(w -> w.field("genre").value(pattern).caseInsensitive(true))));
        }
        if (category != null && !category.isBlank()) {
            String pattern = "*" + escapeWildcard(category.trim()) + "*";
            filterClauses.add(Query.of(qb -> qb.wildcard(w -> w.field("category").value(pattern).caseInsensitive(true))));
        }
        if (title != null && !title.isBlank()) {
            String pattern = "*" + escapeWildcard(title.trim()) + "*";
            filterClauses.add(Query.of(qb -> qb.wildcard(w -> w.field("title.keyword").value(pattern).caseInsensitive(true))));
        }
        if (author != null && !author.isBlank()) {
            String pattern = "*" + escapeWildcard(author.trim()) + "*";
            filterClauses.add(Query.of(qb -> qb.wildcard(w -> w.field("author.keyword").value(pattern).caseInsensitive(true))));
        }
        if (isbn != null && !isbn.isBlank()) {
            String exact = isbn.trim();
            filterClauses.add(Query.of(qb -> qb.term(t -> t.field("isbn").value(exact))));
        }
        if (filterByActive) {
            boolean activeVal = active;
            filterClauses.add(Query.of(qb -> qb.term(t -> t.field("active").value(activeVal))));
        }
        if (publishedYearFrom != null) {
            int from = publishedYearFrom;
            filterClauses.add(Query.of(qb -> qb.range(RangeQuery.of(r -> r.number(n -> n.field("publishedYear").gte((double) from))))));
        }
        if (publishedYearTo != null) {
            int to = publishedYearTo;
            filterClauses.add(Query.of(qb -> qb.range(RangeQuery.of(r -> r.number(n -> n.field("publishedYear").lte((double) to))))));
        }
        if (availableOnly) {
            filterClauses.add(Query.of(qb -> qb.range(RangeQuery.of(r -> r.number(n -> n.field("availableCopies").gte(1.0))))));
        }

        if (qClause == null && filterClauses.isEmpty()) {
            return Optional.empty();
        }

        final Query qMust = qClause;
        BoolQuery boolQuery = BoolQuery.of(b -> {
            if (qMust != null) b.must(qMust);
            b.filter(filterClauses);
            return b;
        });
        return Optional.of(Query.of(qb -> qb.bool(boolQuery)));
    }

    /** Escapa caracteres especiais do wildcard do Elasticsearch: \\ * ? */
    private static String escapeWildcard(String value) {
        return value.replace("\\", "\\\\").replace("*", "\\*").replace("?", "\\?");
    }

    private Sort buildSort(String sortBy, String sortDir, String q) {
        Sort.Direction direction = (sortDir != null && sortDir.equalsIgnoreCase("desc"))
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        if (sortBy != null && !sortBy.isBlank()) {
            return switch (sortBy.toLowerCase()) {
                case "author" -> Sort.by(direction, "author.keyword");
                case "publishedyear" -> Sort.by(direction, "publishedYear");
                case "updatedat" -> Sort.by(direction, "updatedAt");
                case "availablecopies" -> Sort.by(direction, "availableCopies");
                default -> Sort.by(direction, "title.keyword");
            };
        }
        if (q != null && !q.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "_score").and(Sort.by(direction, "title.keyword"));
        }
        return Sort.by(direction, "title.keyword");
    }

    private BookSearchResponse toResponse(BookDocument doc) {
        return BookSearchResponse.builder()
                .id(doc.getId())
                .title(doc.getTitle())
                .author(doc.getAuthor())
                .category(doc.getCategory())
                .genre(doc.getGenre())
                .description(doc.getDescription())
                .isbn(doc.getIsbn())
                .publishedYear(doc.getPublishedYear())
                .active(doc.getActive())
                .createdAt(doc.getCreatedAt())
                .updatedAt(doc.getUpdatedAt())
                .totalCopies(doc.getTotalCopies())
                .availableCopies(doc.getAvailableCopies())
                .inventoryUpdatedAt(doc.getInventoryUpdatedAt())
                .build();
    }
}
