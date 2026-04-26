package com.example.odontologia_api.persistence;

import com.example.odontologia_api.enums.DiaSemana;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class DiaSemanaConverter implements AttributeConverter<DiaSemana, String> {

    @Override
    public String convertToDatabaseColumn(DiaSemana attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public DiaSemana convertToEntityAttribute(String dbData) {
        return DiaSemana.fromPersistedValue(dbData);
    }
}
