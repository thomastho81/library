package br.com.thomas.library.inventory_service.service;

import br.com.thomas.library.inventory_service.dto.request.InventoryQuantityChangeRequest;
import br.com.thomas.library.inventory_service.dto.request.InventoryRequest;
import br.com.thomas.library.inventory_service.dto.response.InventoryResponse;
import br.com.thomas.library.inventory_service.mapper.InventoryMapper;
import br.com.thomas.library.inventory_service.model.Inventory;
import br.com.thomas.library.inventory_service.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Regras de negócio de inventário: criar estoque, aumentar e reduzir exemplares.
 * Operações só afetam registros ativos (active = true).
 * Disponibilidade: available = (availableCopies > 0).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryMapper inventoryMapper;

    @Transactional
    public InventoryResponse create(InventoryRequest request) {
        if (request == null || request.getBookId() == null) {
            throw new IllegalArgumentException("bookId é obrigatório");
        }
        if (inventoryRepository.findByBookIdAndActiveTrue(request.getBookId()).isPresent()) {
            throw new IllegalArgumentException("Já existe inventário ativo para o livro com id: " + request.getBookId());
        }
        int total = request.getTotalCopies() != null ? request.getTotalCopies() : 0;
        if (total < 0) {
            throw new IllegalArgumentException("Quantidade inicial não pode ser negativa");
        }
        var inventory = Inventory.builder()
                .bookId(request.getBookId())
                .totalCopies(total)
                .availableCopies(total)
                .reservedCopies(0)
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        inventory = inventoryRepository.save(inventory);
        log.info("Estoque inicial criado para bookId={}, totalCopies={}", request.getBookId(), total);
        return inventoryMapper.toResponse(inventory);
    }

    @Transactional
    public InventoryResponse increase(Long bookId, InventoryQuantityChangeRequest request) {
        Inventory inv = inventoryRepository.findByBookIdAndActiveTrue(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Inventário não encontrado ou inativo para o livro: " + bookId));
        int amount = request != null && request.getAmount() != null ? request.getAmount() : 0;
        if (amount < 1) {
            throw new IllegalArgumentException("Quantidade para aumento deve ser no mínimo 1");
        }
        inv.setTotalCopies(inv.getTotalCopies() + amount);
        inv.setAvailableCopies(inv.getAvailableCopies() + amount);
        inv.setUpdatedAt(LocalDateTime.now());
        inv = inventoryRepository.save(inv);
        log.info("Estoque aumentado para bookId={}, amount={}, novo total={}", bookId, amount, inv.getTotalCopies());
        return inventoryMapper.toResponse(inv);
    }

    @Transactional
    public InventoryResponse decrease(Long bookId, InventoryQuantityChangeRequest request) {
        Inventory inv = inventoryRepository.findByBookIdAndActiveTrue(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Inventário não encontrado ou inativo para o livro: " + bookId));
        int amount = request != null && request.getAmount() != null ? request.getAmount() : 0;
        if (amount < 1) {
            throw new IllegalArgumentException("Quantidade para redução deve ser no mínimo 1");
        }
        if (inv.getAvailableCopies() < amount) {
            throw new IllegalArgumentException(
                    "Cópias disponíveis (" + inv.getAvailableCopies() + ") insuficientes para reduzir " + amount + " exemplares.");
        }
        inv.setTotalCopies(inv.getTotalCopies() - amount);
        inv.setAvailableCopies(inv.getAvailableCopies() - amount);
        inv.setUpdatedAt(LocalDateTime.now());
        inv = inventoryRepository.save(inv);
        log.info("Estoque reduzido para bookId={}, amount={}, novo total={}", bookId, amount, inv.getTotalCopies());
        return inventoryMapper.toResponse(inv);
    }
}
