package com.example.odontologia_api.dto;

import jakarta.validation.constraints.Size;

public record OdontogramaDienteRequest(
        Boolean ausente,
        Boolean implante,
        Boolean corona,
        Boolean endodoncia,
        Boolean extraccionIndicada,
        @Size(max = 500) String observacion
) {
}
