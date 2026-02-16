package br.com.thomas.library.catalog_service.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "books")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Book {

    @Id
    private String id;

    @NotBlank(message = "Título é obrigatório")
    private String title;

    @NotBlank(message = "Autor é obrigatório")
    private String author;

    private String category;

    private String genre;

    private String description;

    private String isbn;

    private Integer publishedYear;

    @NotNull
    @Builder.Default
    private Boolean active = true;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}

