package br.com.thomas.library.rental_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Usuário que pode realizar alugueis. Cadastrado no rental-service.
 */
@Entity
@Table(name = "tb_usuarios")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotBlank
    @Column(name = "nome", nullable = false)
    private String name;

    @NotNull
    @Column(name = "idade", nullable = false)
    private Integer age;

    @NotBlank
    @Email
    @Column(name = "email", nullable = false)
    private String email;

    @NotNull
    @Column(name = "ativo", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @NotNull
    @Column(name = "data_cadastro", nullable = false)
    private LocalDateTime registeredAt;
}
