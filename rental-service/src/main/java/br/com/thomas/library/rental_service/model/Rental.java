package br.com.thomas.library.rental_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Registro de aluguel: uma única entidade para o ciclo reserva → devolução.
 * Ao reservar: cria-se com status RESERVED e data_reserva. Ao devolver: atualiza-se para RETURNED e data_devolucao.
 * Usuário e preço ficam de fora do escopo inicial.
 */
@Entity
@Table(name = "tb_aluguel")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Rental {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "id_livro", nullable = false)
    private Long bookId;

    @NotNull
    @Min(1)
    @Column(name = "quantidade", nullable = false)
    private Integer quantity;

    @NotNull
    @Convert(converter = RentalStatusConverter.class)
    @Column(name = "status", nullable = false)
    private RentalStatus status;

    @NotNull
    @Column(name = "data_reserva", nullable = false)
    private LocalDateTime reservedAt;

    @Column(name = "data_devolucao")
    private LocalDateTime returnedAt;
}
