package br.com.thomas.library.rental_service.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class RentalStatusConverter implements AttributeConverter<RentalStatus, Integer> {

    @Override
    public Integer convertToDatabaseColumn(RentalStatus attribute) {
        return attribute == null ? null : attribute.getId();
    }

    @Override
    public RentalStatus convertToEntityAttribute(Integer dbData) {
        return dbData == null ? null : RentalStatus.fromId(dbData);
    }
}
