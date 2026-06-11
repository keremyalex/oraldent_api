package com.example.odontologia_api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RecetaDetalleRequest(
        @NotBlank @Size(max = 160) String medicamento,
        @Size(max = 120) String dosis,
        @Size(max = 120) String frecuencia,
        @Size(max = 120) String duracion,
        @Size(max = 500) String indicaciones,
        @Min(0) Integer orden
) {
}
