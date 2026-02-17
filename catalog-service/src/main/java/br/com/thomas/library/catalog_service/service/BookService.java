package br.com.thomas.library.catalog_service.service;

import br.com.thomas.library.catalog_service.dto.propagation.OperationType;
import br.com.thomas.library.catalog_service.dto.request.BookRequest;
import br.com.thomas.library.catalog_service.dto.response.BookResponse;
import br.com.thomas.library.catalog_service.mapper.BookMapper;
import br.com.thomas.library.catalog_service.model.Book;
import br.com.thomas.library.catalog_service.repository.BookRepository;
import br.com.thomas.library.catalog_service.service.propagation.PropagationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;
    private final PropagationService propagationService;

    @Transactional
    public BookResponse createBook(BookRequest request) {
        log.info("Criando novo livro: {}", request.getTitle());

        if (request.getIsbn() != null && !request.getIsbn().isBlank()) {
            if (bookRepository.existsByIsbnAndActiveTrue(request.getIsbn())) {
                throw new IllegalArgumentException("Já existe um livro ativo com o ISBN: " + request.getIsbn());
            }
        }

        Book book = bookMapper.toEntity(request);
        book.setActive(true);
        book.setCreatedAt(LocalDateTime.now());

        Book savedBook = bookRepository.save(book);
        log.info("Livro {} criado com sucesso: {}", savedBook.getTitle(), savedBook.getId());

        propagationService.publish(bookMapper.toPropagationPayload(savedBook), OperationType.CREATE);

        return bookMapper.toResponse(savedBook);
    }

    public List<BookResponse> getAllActiveBooks(String title, String author, String category, String genre) {
        log.info("Buscando livros ativos com filtros - title: {}, author: {}, category: {}, genre: {}", title, author, category, genre);
        List<Book> books = hasAnyFilter(title, author, category, genre)
                ? bookRepository.findActiveByFilters(title, author, category, genre)
                : bookRepository.findByActiveTrue();
        return books.stream()
                .map(bookMapper::toResponse)
                .collect(Collectors.toList());
    }

    private boolean hasAnyFilter(String title, String author, String category, String genre) {
        return (title != null && !title.isBlank())
                || (author != null && !author.isBlank())
                || (category != null && !category.isBlank())
                || (genre != null && !genre.isBlank());
    }

    public BookResponse getBookById(Long id) {
        log.info("Buscando livro por ID: {}", id);
        Book book = bookRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new IllegalArgumentException("Livro não encontrado ou inativo: " + id));
        return bookMapper.toResponse(book);
    }

    @Transactional
    public BookResponse updateBook(Long id, BookRequest request) {
        log.info("Atualizando livro: {}", id);

        Book book = bookRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new IllegalArgumentException("Livro não encontrado ou inativo: " + id));

        if (request.getIsbn() != null && !request.getIsbn().isBlank()
                && !request.getIsbn().equals(book.getIsbn())) {
            if (bookRepository.existsByIsbnAndActiveTrue(request.getIsbn())) {
                throw new IllegalArgumentException("Já existe um livro ativo com o ISBN: " + request.getIsbn());
            }
        }

        book.setUpdatedAt(LocalDateTime.now());
        bookMapper.updateEntity(request, book);

        Book updatedBook = bookRepository.save(book);
        log.info("Livro atualizado com sucesso: {}", updatedBook.getId());

        propagationService.publish(bookMapper.toPropagationPayload(updatedBook), OperationType.UPDATE);

        return bookMapper.toResponse(updatedBook);
    }

    @Transactional
    public void deleteBook(Long id) {
        log.info("Soft delete do livro: {}", id);

        Book book = bookRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new IllegalArgumentException("Livro não encontrado ou já inativo: " + id));

        book.setActive(false);
        book.setUpdatedAt(LocalDateTime.now());

        Book savedBook = bookRepository.save(book);
        log.info("Livro removido logicamente com sucesso: {}", id);

        propagationService.publish(bookMapper.toPropagationPayload(savedBook), OperationType.DELETE);
    }
}
