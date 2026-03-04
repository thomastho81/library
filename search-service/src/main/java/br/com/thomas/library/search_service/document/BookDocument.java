package br.com.thomas.library.search_service.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.Instant;

/**
 * Documento de livro no Elasticsearch (índice de busca).
 * <p>
 * Metadados vêm dos eventos do <strong>catalog-service</strong> (BookPropagationPayload).
 * Quantidade e disponibilidade vêm dos eventos do <strong>inventory-service</strong> (quando publicados).
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "books")
public class BookDocument {

    /**
     * ID do livro no catalog-service (usado como document id no Elasticsearch).
     */
    @Id
    private String id;

    // ---- Metadados (catalog) ----
    @Field(type = FieldType.Text, analyzer = "standard")
    private String title;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String author;

    /** Categoria para filtro e busca. */
    @Field(type = FieldType.Keyword)
    private String category;

    @Field(type = FieldType.Keyword)
    private String genre;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String description;

    @Field(type = FieldType.Keyword)
    private String isbn;

    @Field(type = FieldType.Integer)
    private Integer publishedYear;

    @Field(type = FieldType.Boolean)
    private Boolean active;

    @Field(type = FieldType.Date)
    private Instant createdAt;

    @Field(type = FieldType.Date)
    private Instant updatedAt;

    // ---- Inventário (inventory-service) ----
    /** Total de cópias do livro. Null até o inventory publicar evento. */
    @Field(type = FieldType.Integer)
    private Integer totalCopies;

    /** Cópias disponíveis para empréstimo. Null até o inventory publicar evento. */
    @Field(type = FieldType.Integer)
    private Integer availableCopies;

    /** Momento da última atualização de inventário no documento (opcional, para auditoria). */
    @Field(type = FieldType.Date)
    private Instant inventoryUpdatedAt;
}
