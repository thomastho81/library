package br.com.thomas.library.inventory_service.controller;

import br.com.thomas.library.inventory_service.dto.request.InventoryQuantityChangeRequest;
import br.com.thomas.library.inventory_service.dto.request.InventoryRequest;
import br.com.thomas.library.inventory_service.dto.response.InventoryResponse;
import br.com.thomas.library.inventory_service.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping
    public ResponseEntity<InventoryResponse> create(@Valid @RequestBody InventoryRequest request) {
        InventoryResponse response = inventoryService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{bookId}/increase")
    public ResponseEntity<InventoryResponse> increase(
            @PathVariable Long bookId,
            @Valid @RequestBody InventoryQuantityChangeRequest request) {
        InventoryResponse response = inventoryService.increase(bookId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{bookId}/decrease")
    public ResponseEntity<InventoryResponse> decrease(
            @PathVariable Long bookId,
            @Valid @RequestBody InventoryQuantityChangeRequest request) {
        InventoryResponse response = inventoryService.decrease(bookId, request);
        return ResponseEntity.ok(response);
    }
}
