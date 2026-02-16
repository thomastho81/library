package br.com.thomas.library.catalog_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookRequest {

    @NotBlank(message = "Título é obrigatório")
    private String title;

    @NotBlank(message = "Autor é obrigatório")
    private String author;

    private String category;

    private String genre;

    private String description;

    private String isbn;

    private Integer publishedYear;
}

