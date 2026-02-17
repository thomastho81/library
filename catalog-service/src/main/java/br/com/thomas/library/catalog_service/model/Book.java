package br.com.thomas.library.catalog_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_livro")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotBlank(message = "Título é obrigatório")
    @Column(name = "titulo", nullable = false)
    private String title;

    @NotBlank(message = "Autor é obrigatório")
    @Column(name = "autor", nullable = false)
    private String author;

    @Column(name = "categoria")
    private String category;

    @Column(name = "genero")
    private String genre;

    @Column(name = "descricao", columnDefinition = "TEXT")
    private String description;

    @Column(name = "isbn", length = 50)
    private String isbn;

    @Column(name = "ano_publicacao")
    private Integer publishedYear;

    @NotNull
    @Builder.Default
    @Column(name = "ativo", nullable = false)
    private Boolean active = true;

    @Column(name = "data_criacao")
    private LocalDateTime createdAt;

    @Column(name = "data_atualizacao")
    private LocalDateTime updatedAt;
}
