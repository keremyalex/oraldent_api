package com.example.odontologia_api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record OdontogramaDienteRequest(
        Boolean ausente,
        Boolean implante,
        Boolean corona,
        Boolean endodoncia,
        Boolean extraccionIndicada,
        @Min(0) @Max(3) Integer movilidad,
        @Size(max = 500) String observacion
) {
}
