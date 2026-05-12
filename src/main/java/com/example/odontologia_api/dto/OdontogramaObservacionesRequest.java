package com.example.odontologia_api.dto;

import jakarta.validation.constraints.Size;

public record OdontogramaObservacionesRequest(
        @Size(max = 500) String observaciones
) {
}
