package br.com.thomas.library.catalog_service.mapper;

import br.com.thomas.library.catalog_service.dto.propagation.BookPropagationPayload;
import br.com.thomas.library.catalog_service.dto.request.BookRequest;
import br.com.thomas.library.catalog_service.dto.response.BookResponse;
import br.com.thomas.library.catalog_service.model.Book;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface BookMapper {

    BookResponse toResponse(Book book);

    BookPropagationPayload toPropagationPayload(Book book);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Book toEntity(BookRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(BookRequest request, @MappingTarget Book book);
}
