package br.com.thomas.library.catalog_service.repository;

import br.com.thomas.library.catalog_service.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long>, BookRepositoryCustom {

    List<Book> findByActiveTrue();

    Optional<Book> findByIdAndActiveTrue(Long id);

    boolean existsByIsbnAndActiveTrue(String isbn);
}
