package br.com.thomas.library.rental_service.controller;

import br.com.thomas.library.rental_service.dto.request.ReserveRequest;
import br.com.thomas.library.rental_service.dto.response.RentalResponse;
import br.com.thomas.library.rental_service.service.RentalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rentals")
@RequiredArgsConstructor
public class RentalController {

    private final RentalService rentalService;

    /**
     * Reserva um livro. Retorna 202 Accepted com o Rental em status PENDING.
     * O cliente pode consultar GET /api/rentals/{id} para acompanhar até RESERVED, RESERVE_FAILED ou CANCELLED.
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
     * Registra devolução do aluguel. Rental deve existir e estar com status RESERVED.
     * Publica evento para o inventory e atualiza para RETURNED + data_devolucao.
     */
    @PutMapping("/{id}/return")
    public ResponseEntity<RentalResponse> registerReturn(@PathVariable Long id) {
        RentalResponse response = rentalService.registerReturn(id);
        return ResponseEntity.ok(response);
    }
}
