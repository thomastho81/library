package br.com.thomas.library.rental_service.controller;

import br.com.thomas.library.rental_service.dto.request.ConfirmReturnRequest;
import br.com.thomas.library.rental_service.dto.request.RequestReturnRequest;
import br.com.thomas.library.rental_service.dto.request.ReserveRequest;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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
     * Solicitação de devolução pelo usuário (pedido de devolução no balcão).
     * Rental deve estar RESERVED e pertencer ao userId. Status passa a RETURN_REQUESTED; não publica evento ao inventory.
     */
    @PostMapping("/{id}/request-return")
    public ResponseEntity<RentalResponse> requestReturn(
            @PathVariable Long id,
            @Valid @RequestBody RequestReturnRequest request) {
        RentalResponse response = rentalService.requestReturn(id, request.getUserId());
        return ResponseEntity.ok(response);
    }

    /**
     * Confirmação de recebimento da devolução pelo gestor.
     * Apenas usuário com perfil GESTOR. Rental deve estar RETURN_REQUESTED. Publica evento ao inventory e altera para RETURNED.
     */
    @PostMapping("/{id}/confirm-return")
    public ResponseEntity<RentalResponse> confirmReturn(
            @PathVariable Long id,
            @Valid @RequestBody ConfirmReturnRequest request) {
        RentalResponse response = rentalService.confirmReturn(id, request.getGestorUserId());
        return ResponseEntity.ok(response);
    }

    /**
     * Devoluções pendentes (apenas gestor). Lista alugueis com status RETURN_REQUESTED.
     */
    @GetMapping("/pending-returns")
    public ResponseEntity<PagedRentalResponse> listPendingReturns(
            @RequestParam(required = true) Long gestorUserId,
            @PageableDefault(size = 25) Pageable pageable) {
        PagedRentalResponse response = rentalService.listPendingReturns(gestorUserId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Meus alugueis / histórico: lista alugueis do usuário com paginação, ordenação e filtros.
     * userId é obrigatório. Filtros opcionais: status, reservedAtFrom, reservedAtTo (datas em ISO: yyyy-MM-dd).
     * Ordenação: sort=reservedAt,desc ou sort=status,asc.
     */
    @GetMapping
    public ResponseEntity<PagedRentalResponse> listByUser(
            @RequestParam(required = true) Long userId,
            @RequestParam(required = false) List<RentalStatus> status,
            @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate reservedAtFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate reservedAtTo,
            @PageableDefault(size = 25) Pageable pageable) {
        PagedRentalResponse response = rentalService.listByUser(userId, status, reservedAtFrom, reservedAtTo, pageable);
        return ResponseEntity.ok(response);
    }
}
