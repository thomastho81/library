package br.com.thomas.library.search_service.service;

import br.com.thomas.library.search_service.document.BookDocument;
import br.com.thomas.library.search_service.dto.propagation.BookPropagationPayload;
import br.com.thomas.library.search_service.repository.BookDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


/**
 * Sincroniza o índice de busca a partir dos eventos do catalog-service (catalog.book.created/updated/deleted).
 * Apenas metadados do livro; disponibilidade é tratada por {@link InventoryAvailabilitySyncService}.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CatalogSyncService {

    private final BookDocumentRepository bookDocumentRepository;

    public void onBookCreated(BookPropagationPayload payload) {
        if (payload == null || payload.getId() == null) {
            log.warn("Evento book.created ignorado: payload ou id nulo");
            return;
        }
        bookDocumentRepository.save(toDocument(payload));
        log.info("Livro indexado: bookId={}, title={}", payload.getId(), payload.getTitle());
    }

    public void onBookUpdated(BookPropagationPayload payload) {
        if (payload == null || payload.getId() == null) {
            log.warn("Evento book.updated ignorado: payload ou id nulo");
            return;
        }
        bookDocumentRepository.save(toDocument(payload));
        log.debug("Livro atualizado no índice: bookId={}", payload.getId());
    }

    public void onBookDeleted(BookPropagationPayload payload) {
        if (payload == null || payload.getId() == null) {
            log.warn("Evento book.deleted ignorado: payload ou id nulo");
            return;
        }
        bookDocumentRepository.deleteById(payload.getId().toString());
        log.info("Livro removido do índice: bookId={}", payload.getId());
    }

    private static BookDocument toDocument(BookPropagationPayload payload) {
        return BookDocument.builder()
                .id(payload.getId().toString())
                .title(payload.getTitle())
                .author(payload.getAuthor())
                .category(payload.getCategory())
                .genre(payload.getGenre())
                .description(payload.getDescription())
                .isbn(payload.getIsbn())
                .publishedYear(payload.getPublishedYear())
                .active(payload.getActive())
                .createdAt(payload.getCreatedAt())
                .updatedAt(payload.getUpdatedAt())
                .totalCopies(null)
                .availableCopies(null)
                .inventoryUpdatedAt(null)
                .build();
    }
}
