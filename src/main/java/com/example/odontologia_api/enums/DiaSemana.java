package com.example.odontologia_api.enums;

import java.time.DayOfWeek;
import java.util.Arrays;

public enum DiaSemana {
    LUNES(DayOfWeek.MONDAY, "MONDAY"),
    MARTES(DayOfWeek.TUESDAY, "TUESDAY"),
    MIERCOLES(DayOfWeek.WEDNESDAY, "WEDNESDAY"),
    JUEVES(DayOfWeek.THURSDAY, "THURSDAY"),
    VIERNES(DayOfWeek.FRIDAY, "FRIDAY"),
    SABADO(DayOfWeek.SATURDAY, "SATURDAY"),
    DOMINGO(DayOfWeek.SUNDAY, "SUNDAY");

    private final DayOfWeek dayOfWeek;
    private final String legacyValue;

    DiaSemana(DayOfWeek dayOfWeek, String legacyValue) {
        this.dayOfWeek = dayOfWeek;
        this.legacyValue = legacyValue;
    }

    public DayOfWeek toDayOfWeek() {
        return dayOfWeek;
    }

    public String legacyValue() {
        return legacyValue;
    }

    public static DiaSemana fromDayOfWeek(DayOfWeek dayOfWeek) {
        return Arrays.stream(values())
                .filter(value -> value.dayOfWeek == dayOfWeek)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Día de semana no soportado: " + dayOfWeek));
    }

    public static DiaSemana fromPersistedValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value) || item.legacyValue.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Valor de día de semana no soportado: " + value));
    }
}
