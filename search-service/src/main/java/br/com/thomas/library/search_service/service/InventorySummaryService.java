package br.com.thomas.library.search_service.service;

import br.com.thomas.library.search_service.document.BookDocument;
import br.com.thomas.library.search_service.dto.response.InventorySummaryResponse;
import br.com.thomas.library.search_service.query.BookIndexQueryFactory;
import co.elastic.clients.elasticsearch._types.Script;
import co.elastic.clients.elasticsearch._types.ScriptLanguage;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregation;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregations;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Agregações no índice de livros para totais informativos na tela de inventário.
 * Mesmos filtros opcionais da busca paginada; pode divergir do PostgreSQL por atraso de eventos.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InventorySummaryService {

    private static final String AGG_SUM_AVAILABLE = "sum_available_copies";
    private static final String AGG_BOOKS_WITH_RESERVED = "books_with_reserved_copies";

    /**
     * 1 por documento com estoque parcialmente indisponível (total &gt; disponível), 0 caso contrário.
     */
    private static final String SCRIPT_COUNT_RESERVED_BOOKS =
            "if (doc['totalCopies'].size() == 0 || doc['availableCopies'].size() == 0) { return 0; } "
                    + "return doc['totalCopies'].value > doc['availableCopies'].value ? 1 : 0;";

    private final ElasticsearchOperations elasticsearchOperations;
    private final BookIndexQueryFactory bookIndexQueryFactory;

    public InventorySummaryResponse summarize(
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
            Boolean availableOnly) {

        boolean includeAll = Boolean.TRUE.equals(all);
        boolean activeFilter = includeAll || (active != null ? active : true);
        boolean filterByActive = !includeAll;
        boolean availableFilter = Boolean.TRUE.equals(availableOnly);

        Optional<Query> optionalQuery = bookIndexQueryFactory.buildBookFilterQuery(
                q, category, genre, publishedYearFrom, publishedYearTo,
                title, author, isbn, filterByActive, activeFilter, availableFilter);

        Query rootQuery = optionalQuery.orElseGet(() -> Query.of(qb -> qb.matchAll(m -> m)));

        Script countReservedBooksScript =
                Script.of(sc -> sc.source(SCRIPT_COUNT_RESERVED_BOOKS).lang(ScriptLanguage.Painless));

        // size da página não pode ser 0 (Pageable do Spring exige >= 1); agregações não dependem dos hits.
        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(rootQuery)
                .withPageable(PageRequest.of(0, 1))
                .withMaxResults(0)
                .withAggregation(AGG_SUM_AVAILABLE, Aggregation.of(a -> a.sum(s -> s.field("availableCopies").missing(0.0))))
                .withAggregation(
                        AGG_BOOKS_WITH_RESERVED,
                        Aggregation.of(a -> a.sum(s -> s.script(countReservedBooksScript))))
                .build();

        SearchHits<BookDocument> hits = elasticsearchOperations.search(nativeQuery, BookDocument.class);
        long totalBooks = hits.getTotalHits();

        long sumAvailable = 0L;
        long booksWithReserved = 0L;

        if (hits.getAggregations() instanceof ElasticsearchAggregations elasticsearchAggregations) {
            sumAvailable = Math.round(sumAggregationValue(elasticsearchAggregations, AGG_SUM_AVAILABLE));
            booksWithReserved = Math.round(sumAggregationValue(elasticsearchAggregations, AGG_BOOKS_WITH_RESERVED));
        } else {
            log.warn("Agregações não disponíveis no formato esperado (ElasticsearchAggregations); retornando zeros parciais.");
        }

        return InventorySummaryResponse.builder()
                .totalBooks(totalBooks)
                .totalAvailableCopies(sumAvailable)
                .booksWithReservedCopies(booksWithReserved)
                .build();
    }

    private static double sumAggregationValue(ElasticsearchAggregations aggregations, String name) {
        ElasticsearchAggregation container = aggregations.get(name);
        if (container == null) {
            return 0.0;
        }
        Aggregate aggregate = container.aggregation().getAggregate();
        if (!aggregate.isSum()) {
            return 0.0;
        }
        return aggregate.sum().value();
    }

}
