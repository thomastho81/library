package br.com.thomas.library.rental_service.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class UserProfileConverter implements AttributeConverter<UserProfile, Integer> {

    @Override
    public Integer convertToDatabaseColumn(UserProfile attribute) {
        return attribute == null ? null : attribute.getId();
    }

    @Override
    public UserProfile convertToEntityAttribute(Integer dbData) {
        return dbData == null ? null : UserProfile.fromId(dbData);
    }
}
