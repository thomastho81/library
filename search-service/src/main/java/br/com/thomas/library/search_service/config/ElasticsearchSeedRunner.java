package br.com.thomas.library.search_service.config;

import br.com.thomas.library.search_service.document.BookDocument;
import br.com.thomas.library.search_service.repository.BookDocumentRepository;
import br.com.thomas.library.search_service.seed.SeedBooks;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Insere os 60 livros de seed no Elasticsearch na primeira subida (quando o índice está vazio).
 * Usa <b>bulk index</b> com documentos como Map e datas em <b>string</b> no formato "yyyy-MM-dd'T'HH:mm:ss",
 * igual ao scripts/bulk-books.ndjson, para que o ES armazene as datas corretamente (sem passar pela conversão
 * entidade → documento do repositório, que acabava gravando só "yyyy-MM-dd").
 */
@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class ElasticsearchSeedRunner implements ApplicationRunner {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final BookDocumentRepository bookDocumentRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public void run(ApplicationArguments args) {
        if (bookDocumentRepository.count() > 0) {
            log.debug("Índice books já contém documentos, seed ignorado.");
            return;
        }
        List<IndexQuery> queries = SeedBooks.ALL.stream()
                .map(this::toIndexQuery)
                .collect(Collectors.toList());
        // Usa IndexCoordinates para que o Map seja serializado como JSON sem conversão Map→BookDocument,
        // preservando as strings de data no formato "yyyy-MM-dd'T'HH:mm:ss".
        elasticsearchOperations.bulkIndex(queries, IndexCoordinates.of("books"));
        log.info("Seed do Elasticsearch concluído: {} livros indexados (bulk, datas em string).", queries.size());
    }

    /**
     * Monta um documento no mesmo formato do bulk NDJSON: campos de data como string "yyyy-MM-dd'T'HH:mm:ss".
     */
    private IndexQuery toIndexQuery(SeedBooks.SeedBook seed) {
        Map<String, Object> doc = new LinkedHashMap<>();
        doc.put("id", String.valueOf(seed.id()));
        doc.put("_class", BookDocument.class.getName());
        doc.put("title", seed.title());
        doc.put("author", seed.author());
        doc.put("category", seed.category());
        doc.put("genre", seed.genre());
        doc.put("description", seed.description());
        doc.put("isbn", seed.isbn());
        doc.put("publishedYear", seed.publishedYear());
        doc.put("active", true);
        doc.put("createdAt", seed.createdAt().format(DATE_FORMAT));
        doc.put("updatedAt", seed.updatedAt().format(DATE_FORMAT));
        doc.put("totalCopies", seed.totalCopies());
        doc.put("availableCopies", seed.availableCopies());
        doc.put("inventoryUpdatedAt", seed.inventoryUpdatedAt().format(DATE_FORMAT));

        return new IndexQueryBuilder()
                .withId(String.valueOf(seed.id()))
                .withObject(doc)
                .build();
    }
}
