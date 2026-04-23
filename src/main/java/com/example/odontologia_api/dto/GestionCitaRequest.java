package com.example.odontologia_api.dto;

import jakarta.validation.constraints.NotBlank;

public record GestionCitaRequest(
        @NotBlank String codigoGestion
) {
}
