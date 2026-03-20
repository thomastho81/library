package br.com.thomas.library.rental_service.controller;

import br.com.thomas.library.rental_service.dto.response.PagedUserResponse;
import br.com.thomas.library.rental_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * API de usuários. GET /api/users lista clientes (usuários com perfil USER, não gestores).
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Lista clientes (usuários que não são gestores), paginado.
     * Uso: tela "Clientes" do perfil gestor.
     */
    @GetMapping
    public ResponseEntity<PagedUserResponse> listClients(
            @PageableDefault(size = 25) Pageable pageable) {
        PagedUserResponse response = userService.listClients(pageable);
        return ResponseEntity.ok(response);
    }
}
