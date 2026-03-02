package br.com.thomas.library.inventory_service.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class RentalOperationConverter implements AttributeConverter<RentalOperationEnum, Integer> {

    @Override
    public Integer convertToDatabaseColumn(RentalOperationEnum attribute) {
        return attribute == null ? null : attribute.getId();
    }

    @Override
    public RentalOperationEnum convertToEntityAttribute(Integer dbData) {
        return dbData == null ? null : RentalOperationEnum.fromId(dbData);
    }
}
