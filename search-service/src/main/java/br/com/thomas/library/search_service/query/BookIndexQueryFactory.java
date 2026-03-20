package br.com.thomas.library.search_service.query;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Monta a query Bool do Elasticsearch para filtros de livro, alinhada à busca paginada.
 * Reutilizada pelo serviço de busca e pelo resumo agregado do inventário (mesmos filtros, sem paginação).
 */
@Component
public class BookIndexQueryFactory {

    /**
     * @param filterByActive quando false, não aplica filtro de ativo (equivale a all=true no controller de busca)
     * @param active           valor de active quando filterByActive é true
     */
    public Optional<Query> buildBookFilterQuery(
            String q,
            String category,
            String genre,
            Integer publishedYearFrom,
            Integer publishedYearTo,
            String title,
            String author,
            String isbn,
            boolean filterByActive,
            boolean active,
            boolean availableOnly) {

        List<Query> filterClauses = new ArrayList<>();

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
            filterClauses.add(Query.of(qb -> qb.term(t -> t.field("active").value(active))));
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
            if (qMust != null) {
                b.must(qMust);
            }
            b.filter(filterClauses);
            return b;
        });
        return Optional.of(Query.of(qb -> qb.bool(boolQuery)));
    }

    /** Escapa caracteres especiais do wildcard do Elasticsearch: \\ * ? */
    public static String escapeWildcard(String value) {
        return value.replace("\\", "\\\\").replace("*", "\\*").replace("?", "\\?");
    }
}
