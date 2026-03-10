package br.com.thomas.library.search_service.repository;

import br.com.thomas.library.search_service.document.BookDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookDocumentRepository extends ElasticsearchRepository<BookDocument, String> {
}
