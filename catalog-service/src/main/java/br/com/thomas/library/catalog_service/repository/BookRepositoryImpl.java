package br.com.thomas.library.catalog_service.repository;

import br.com.thomas.library.catalog_service.model.Book;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class BookRepositoryImpl implements BookRepositoryCustom {

    private final EntityManager entityManager;

    /**
     * Busca parametrizada usando EntityManager + Criteria API (JPA).
     * Filtros opcionais: active=true sempre; title/author (LIKE case-insensitive); category/genre (igual).
     */
    @Override
    public List<Book> findActiveByFilters(String title, String author, String category, String genre) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Book> cq = cb.createQuery(Book.class);
        Root<Book> root = cq.from(Book.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.isTrue(root.get("active")));

        if (title != null && !title.isBlank()) {
            predicates.add(cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%"));
        }
        if (author != null && !author.isBlank()) {
            predicates.add(cb.like(cb.lower(root.get("author")), "%" + author.toLowerCase() + "%"));
        }
        if (category != null && !category.isBlank()) {
            predicates.add(cb.equal(root.get("category"), category));
        }
        if (genre != null && !genre.isBlank()) {
            predicates.add(cb.equal(root.get("genre"), genre));
        }

        cq.where(predicates.toArray(new Predicate[0]));

        TypedQuery<Book> query = entityManager.createQuery(cq);
        return query.getResultList();
    }
}
