package com.example.odontologia_api.dto;

import com.example.odontologia_api.enums.FurcacionPeriodontograma;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record PeriodontogramaDienteRequest(
        Boolean ausente,
        Boolean implante,
        @Min(0) @Max(3) Integer movilidad,
        FurcacionPeriodontograma furcacion,
        @Size(max = 500) String observacion
) {
}
