package br.com.thomas.library.catalog_service.repository;

import br.com.thomas.library.catalog_service.model.Book;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class BookRepositoryImpl implements BookRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Override
    public List<Book> findActiveByFilters(String title, String author, String category, String genre) {
        Query query = new Query();
        query.addCriteria(Criteria.where("active").is(true));

        if (title != null && !title.isBlank()) {
            query.addCriteria(Criteria.where("title").regex(title, "i"));
        }
        if (author != null && !author.isBlank()) {
            query.addCriteria(Criteria.where("author").regex(author, "i"));
        }
        if (category != null && !category.isBlank()) {
            query.addCriteria(Criteria.where("category").is(category));
        }
        if (genre != null && !genre.isBlank()) {
            query.addCriteria(Criteria.where("genre").is(genre));
        }

        return mongoTemplate.find(query, Book.class);
    }
}
