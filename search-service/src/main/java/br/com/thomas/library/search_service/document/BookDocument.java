package br.com.thomas.library.search_service.document;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;

import java.time.LocalDateTime;

/**
 * Documento de livro no Elasticsearch (índice de busca).
 * Metadados vindos do catalog-service; quantidade/disponibilidade do inventory-service (quando houver).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "books")
public class BookDocument {

    @Id
    private String id;

    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "standard"),
            otherFields = { @InnerField(suffix = "keyword", type = FieldType.Keyword) })
    private String title;

    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "standard"),
            otherFields = { @InnerField(suffix = "keyword", type = FieldType.Keyword) })
    private String author;

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

    /**
     * Armazenado como Keyword para preservar o formato "yyyy-MM-dd'T'HH:mm:ss" no _source.
     * Conversão LocalDateTime ↔ String feita por ElasticsearchDateTimeConverters.
     */
    @Field(type = FieldType.Keyword)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @Field(type = FieldType.Keyword)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    @Field(type = FieldType.Integer)
    private Integer totalCopies;

    @Field(type = FieldType.Integer)
    private Integer availableCopies;

    @Field(type = FieldType.Keyword)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime inventoryUpdatedAt;
}
