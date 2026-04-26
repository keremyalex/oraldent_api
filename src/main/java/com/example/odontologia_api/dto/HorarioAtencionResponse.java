package com.example.odontologia_api.dto;

import com.example.odontologia_api.enums.DiaSemana;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record HorarioAtencionResponse(
        Long id,
        DiaSemana diaSemana,
        LocalTime horaInicio,
        LocalTime horaFin,
        Integer duracionCitaMinutos,
        String observacion,
        Boolean activo,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaActualizacion
) {
}
