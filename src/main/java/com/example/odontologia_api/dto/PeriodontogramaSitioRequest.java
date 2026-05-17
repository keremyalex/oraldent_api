package com.example.odontologia_api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record PeriodontogramaSitioRequest(
        Boolean sangradoSondaje,
        Boolean placa,
        Boolean supuracion,
        @Min(-20) @Max(20) Integer margenGingivalMm,
        @Min(0) @Max(20) Integer profundidadSondajeMm,
        @Size(max = 500) String observacion
) {
}
