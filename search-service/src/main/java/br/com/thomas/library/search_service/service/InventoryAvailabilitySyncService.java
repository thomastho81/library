package br.com.thomas.library.search_service.service;

import br.com.thomas.library.search_service.dto.propagation.BookAvailabilityPayload;
import br.com.thomas.library.search_service.repository.BookDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


/**
 * Sincroniza a disponibilidade (total e cópias disponíveis) no índice de busca
 * a partir dos eventos inventory.book.availability publicados pelo inventory-service.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryAvailabilitySyncService {

    private final BookDocumentRepository bookDocumentRepository;

    /**
     * Atualiza apenas totalCopies, availableCopies e inventoryUpdatedAt no documento do livro.
     * Chamado pela rota que consome inventory.book.availability (exchange rental.topic).
     */
    public void onAvailability(BookAvailabilityPayload payload) {
        if (payload == null || payload.getBookId() == null) {
            log.warn("Evento inventory.book.availability ignorado: payload ou bookId nulo");
            return;
        }
        String bookIdStr = payload.getBookId().toString();
        bookDocumentRepository.findById(bookIdStr).ifPresentOrElse(
                doc -> {
                    doc.setTotalCopies(payload.getTotalCopies());
                    doc.setAvailableCopies(payload.getAvailableCopies());
                    doc.setInventoryUpdatedAt(payload.getUpdatedAt());
                    bookDocumentRepository.save(doc);
                    log.debug("Disponibilidade atualizada no índice: bookId={}, available={}", payload.getBookId(), payload.getAvailableCopies());
                },
                () -> log.debug("Documento não encontrado no índice para bookId={}, ignorando disponibilidade", payload.getBookId())
        );
    }
}
