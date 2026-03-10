package br.com.thomas.library.inventory_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Registro de evento de rental recebido (idempotência).
 * PK = id_evento. Status: PROCESSADO (1) ou REJEITADO (2). id_usuario propagado do rental-service.
 */
@Entity
@Table(name = "tb_evento_processado")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedEvent {

    @Id
    @Column(name = "id_evento", length = 36)
    private String eventId;

    @NotNull
    @Column(name = "id_aluguel", nullable = false)
    private Long rentalId;

    @NotNull
    @Column(name = "id_usuario", nullable = false)
    private Long userId;

    @NotNull
    @Column(name = "id_livro", nullable = false)
    private Long bookId;

    @NotNull
    @Convert(converter = RentalOperationConverter.class)
    @Column(name = "operacao", nullable = false)
    private RentalOperationEnum operation;

    @NotNull
    @Column(name = "quantidade", nullable = false)
    private Integer quantity;

    @NotNull
    @Convert(converter = EventStatusConverter.class)
    @Column(name = "status", nullable = false)
    private EventStatus status;

    @NotNull
    @Column(name = "processado_em", nullable = false)
    private LocalDateTime processedAt;
}
