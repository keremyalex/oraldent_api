package com.example.odontologia_api.dto;

import com.example.odontologia_api.enums.DiaSemana;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalTime;

public record HorarioAtencionRequest(
        @NotNull DiaSemana diaSemana,
        @NotNull LocalTime horaInicio,
        @NotNull LocalTime horaFin,
        @NotNull @Min(10) @Max(240) Integer duracionCitaMinutos,
        @Size(max = 200) String observacion
) {
}
