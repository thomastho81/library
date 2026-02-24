package br.com.thomas.library.inventory_service.mapper;

import br.com.thomas.library.inventory_service.dto.response.InventoryResponse;
import br.com.thomas.library.inventory_service.model.Inventory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InventoryMapper {

    @Mapping(target = "available", expression = "java(entity.getAvailableCopies() != null && entity.getAvailableCopies() > 0)")
    InventoryResponse toResponse(Inventory entity);
}
