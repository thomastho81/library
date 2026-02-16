package br.com.thomas.library.catalog_service.repository;

import br.com.thomas.library.catalog_service.model.Book;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends MongoRepository<Book, String>, BookRepositoryCustom {

    List<Book> findByActiveTrue();

    Optional<Book> findByIdAndActiveTrue(String id);

    boolean existsByIsbnAndActiveTrue(String isbn);
}
