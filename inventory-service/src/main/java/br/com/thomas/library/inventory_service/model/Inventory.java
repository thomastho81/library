package br.com.thomas.library.inventory_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Registro de inventário por livro (bookId vem do catalog-service).
 * Total de cópias, cópias disponíveis, cópias reservadas e controle de versão (bloqueio otimista).
 * <p>
 * {@code uniqueConstraints = @UniqueConstraint(columnNames = "id_livro")} declara na tabela
 * uma constraint UNIQUE sobre a coluna id_livro: só pode existir um registro de inventário por livro.
 */
@Entity
@Table(name = "tb_inventario", uniqueConstraints = @UniqueConstraint(columnNames = "id_livro"))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "id_livro", nullable = false)
    private Long bookId;

    @NotNull
    @Column(name = "total_copias", nullable = false)
    private Integer totalCopies;

    @NotNull
    @Column(name = "copias_disponiveis", nullable = false)
    private Integer availableCopies;

    @NotNull
    @Column(name = "copias_reservadas", nullable = false)
    private Integer reservedCopies;

    @NotNull
    @Builder.Default
    @Column(name = "ativo", nullable = false)
    private Boolean active = true;

    @Version
    @Column(name = "versao")
    private Long version;

    @Column(name = "data_criacao")
    private LocalDateTime createdAt;

    @Column(name = "data_atualizacao")
    private LocalDateTime updatedAt;

    /**
     * Reserves quantity of copies (available → reserved) and updates updatedAt.
     * Caller must validate availability before calling.
     */
    public void reserve(int quantity) {
        this.availableCopies = this.availableCopies - quantity;
        this.reservedCopies = this.reservedCopies + quantity;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Releases quantity of copies (reserved → available) and updates updatedAt.
     * Caller must validate reserved copies before calling.
     */
    public void release(int quantity) {
        this.reservedCopies = this.reservedCopies - quantity;
        this.availableCopies = this.availableCopies + quantity;
        this.updatedAt = LocalDateTime.now();
    }
}
