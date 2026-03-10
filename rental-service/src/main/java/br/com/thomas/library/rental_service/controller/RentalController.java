package br.com.thomas.library.rental_service.controller;

import br.com.thomas.library.rental_service.dto.request.ReserveRequest;
import br.com.thomas.library.rental_service.dto.request.ReturnRequest;
import br.com.thomas.library.rental_service.dto.response.PagedRentalResponse;
import br.com.thomas.library.rental_service.dto.response.RentalResponse;
import br.com.thomas.library.rental_service.model.RentalStatus;
import br.com.thomas.library.rental_service.service.RentalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rentals")
@RequiredArgsConstructor
public class RentalController {

    private final RentalService rentalService;

    /**
     * Reserva um livro para o usuário. Retorna 202 Accepted com o Rental em status PENDING.
     * Valida que o userId existe e está ativo.
     */
    @PostMapping("/reserve")
    public ResponseEntity<RentalResponse> reserve(@Valid @RequestBody ReserveRequest request) {
        RentalResponse response = rentalService.createReserve(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RentalResponse> getById(@PathVariable Long id) {
        RentalResponse response = rentalService.getById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Registra devolução do aluguel. Rental deve existir, estar RESERVED e pertencer ao userId do body.
     */
    @PutMapping("/{id}/return")
    public ResponseEntity<RentalResponse> registerReturn(
            @PathVariable Long id,
            @Valid @RequestBody ReturnRequest request) {
        RentalResponse response = rentalService.registerReturn(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Meus alugueis / histórico: lista alugueis do usuário com paginação e ordenação.
     * userId é obrigatório. Filtro opcional por status. Ordenação: sort=status ou sort=reservedAt (ex.: sort=reservedAt,desc).
     */
    @GetMapping
    public ResponseEntity<PagedRentalResponse> listByUser(
            @RequestParam(required = true) Long userId,
            @RequestParam(required = false) List<RentalStatus> status,
            @PageableDefault(size = 25) Pageable pageable) {
        PagedRentalResponse response = rentalService.listByUser(userId, status, pageable);
        return ResponseEntity.ok(response);
    }
}
