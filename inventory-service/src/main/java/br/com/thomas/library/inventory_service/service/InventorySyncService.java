package br.com.thomas.library.inventory_service.service;

import br.com.thomas.library.inventory_service.dto.propagation.BookAvailabilityPayload;
import br.com.thomas.library.inventory_service.dto.propagation.BookPropagationPayload;
import br.com.thomas.library.inventory_service.model.Inventory;
import br.com.thomas.library.inventory_service.repository.InventoryRepository;
import br.com.thomas.library.inventory_service.service.propagation.InventoryPropagationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Sincroniza inventário a partir dos eventos (book.created, book.updated, book.deleted)
 * publicados pelo catalog-service na exchange topic.
 * Publica disponibilidade (inventory.book.availability) para o search-service.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InventorySyncService {

    private final InventoryRepository inventoryRepository;
    private final InventoryPropagationService propagationService;

    @Transactional
    public void onBookCreated(BookPropagationPayload payload) {
        if (payload == null || payload.getId() == null) {
            log.warn("Evento book.created ignorado: payload ou id nulo");
            return;
        }
        if (inventoryRepository.findByBookIdAndActiveTrue(payload.getId()).isPresent()) {
            log.debug("Inventário já existe para bookId={}, ignorando created", payload.getId());
            return;
        }
        var inventory = Inventory.builder()
                .bookId(payload.getId())
                .totalCopies(0)
                .availableCopies(0)
                .reservedCopies(0)
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        inventoryRepository.save(inventory);
        propagationService.publishAvailability(toAvailabilityPayload(inventory));
        log.info("Inventário criado para bookId={}", payload.getId());
    }

    @Transactional
    public void onBookUpdated(BookPropagationPayload payload) {
        if (payload == null || payload.getId() == null) {
            log.warn("Evento book.updated ignorado: payload ou id nulo");
            return;
        }
        inventoryRepository.findByBookIdAndActiveTrue(payload.getId())
                .ifPresentOrElse(
                        inv -> {
                            inv.setUpdatedAt(LocalDateTime.now());
                            inventoryRepository.save(inv);
                            propagationService.publishAvailability(toAvailabilityPayload(inv));
                            log.debug("Inventário atualizado para bookId={}", payload.getId());
                        },
                        () -> {
                            // livro pode ter sido criado antes do inventory-service existir
                            var inventory = Inventory.builder()
                                    .bookId(payload.getId())
                                    .totalCopies(0)
                                    .availableCopies(0)
                                    .reservedCopies(0)
                                    .active(true)
                                    .createdAt(LocalDateTime.now())
                                    .updatedAt(LocalDateTime.now())
                                    .build();
                            inventoryRepository.save(inventory);
                            propagationService.publishAvailability(toAvailabilityPayload(inventory));
                            log.info("Inventário criado (sync) para bookId={}", payload.getId());
                        }
                );
    }

    @Transactional
    public void onBookDeleted(BookPropagationPayload payload) {
        if (payload == null || payload.getId() == null) {
            log.warn("Evento book.deleted ignorado: payload ou id nulo");
            return;
        }
        inventoryRepository.findByBookIdAndActiveTrue(payload.getId())
                .ifPresent(inv -> {
                    inv.setActive(false);
                    inv.setUpdatedAt(LocalDateTime.now());
                    inventoryRepository.save(inv);
                    propagationService.publishAvailability(toAvailabilityPayload(inv));
                    log.info("Inventário marcado como inativo para bookId={}", payload.getId());
                });
    }

    private static BookAvailabilityPayload toAvailabilityPayload(Inventory inv) {
        return BookAvailabilityPayload.builder()
                .bookId(inv.getBookId())
                .totalCopies(inv.getTotalCopies())
                .availableCopies(inv.getAvailableCopies())
                .updatedAt(inv.getUpdatedAt())
                .build();
    }
}
