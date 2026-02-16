package br.com.thomas.library.catalog_service.repository;

import br.com.thomas.library.catalog_service.model.Book;

import java.util.List;

public interface BookRepositoryCustom {

    List<Book> findActiveByFilters(String title, String author, String category, String genre);
}
