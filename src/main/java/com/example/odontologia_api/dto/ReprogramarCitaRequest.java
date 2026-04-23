package com.example.odontologia_api.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record ReprogramarCitaRequest(
        @NotNull @Future LocalDateTime nuevaFechaHoraInicio
) {
}
