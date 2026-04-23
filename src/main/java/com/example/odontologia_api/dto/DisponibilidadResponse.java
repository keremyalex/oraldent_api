package com.example.odontologia_api.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record DisponibilidadResponse(
        LocalDate fecha,
        List<LocalTime> horariosDisponibles
) {
}
