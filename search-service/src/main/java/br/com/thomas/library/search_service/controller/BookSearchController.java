package br.com.thomas.library.search_service.controller;

import br.com.thomas.library.search_service.dto.response.BookSearchResponse;
import br.com.thomas.library.search_service.dto.response.PagedBookSearchResponse;
import br.com.thomas.library.search_service.service.BookSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * API de busca e visualização de livros no índice Elasticsearch.
 * Busca parametrizada com filtros e paginação (padrão 25 por página via {@link PageableDefault}).
 */
@RestController
@RequestMapping("/api/search/books")
@RequiredArgsConstructor
@Slf4j
public class BookSearchController {

    private final BookSearchService bookSearchService;

    /**
     * Busca parametrizada de livros com filtros e paginação.
     * <p>
     * <b>Quando nenhum parâmetro é passado:</b> retorna a primeira página (25 itens) de <b>livros ativos</b>,
     * ordenados por título. Equivale a active=true, page=0, size=25, sortBy=title.
     * </p>
     * <ul>
     *   <li><code>active</code> default=true (apenas ativos); false = apenas inativos.</li>
     *   <li><code>all</code> default=false; quando true, ignora active e retorna todos (ativos e inativos).</li>
     *   <li>Paginação: parâmetros padrão Spring <code>page</code> e <code>size</code>; {@link PageableDefault} size=25, page=0; size máximo 100.</li>
     *   <li>Ordenação: sortBy = title, author, publishedYear, updatedAt, availableCopies; com <code>q</code> o default é relevância + título.</li>
     * </ul>
     */
    @GetMapping
    public ResponseEntity<PagedBookSearchResponse> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) Integer publishedYearFrom,
            @RequestParam(required = false) Integer publishedYearTo,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String isbn,
            @RequestParam(required = false, defaultValue = "true") Boolean active,
            @RequestParam(required = false, defaultValue = "false") Boolean all,
            @RequestParam(required = false, defaultValue = "false") Boolean availableOnly,
            @RequestParam(required = false) String sortBy,
            @PageableDefault(size = 25) Pageable pageable) {

        PagedBookSearchResponse response = bookSearchService.search(
                q, category, genre, publishedYearFrom, publishedYearTo,
                title, author, isbn, active, all, availableOnly, sortBy, pageable);

        return ResponseEntity.ok(response);
    }

    /**
     * Visualização de um único livro por id.
     */
    @GetMapping("/{id}")
    public ResponseEntity<BookSearchResponse> getById(@PathVariable String id) {
        return bookSearchService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
