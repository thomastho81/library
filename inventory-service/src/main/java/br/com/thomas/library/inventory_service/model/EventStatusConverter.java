package br.com.thomas.library.inventory_service.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class EventStatusConverter implements AttributeConverter<EventStatus, Integer> {

    @Override
    public Integer convertToDatabaseColumn(EventStatus attribute) {
        return attribute == null ? null : attribute.getId();
    }

    @Override
    public EventStatus convertToEntityAttribute(Integer dbData) {
        return dbData == null ? null : EventStatus.fromId(dbData);
    }
}
