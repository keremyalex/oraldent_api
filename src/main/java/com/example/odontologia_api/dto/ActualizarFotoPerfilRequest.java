package com.example.odontologia_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ActualizarFotoPerfilRequest(
        @NotBlank @Size(max = 500) String fotoPerfilUrl
) {
}
