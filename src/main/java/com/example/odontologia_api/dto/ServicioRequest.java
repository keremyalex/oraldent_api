package com.example.odontologia_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ServicioRequest(
        @NotBlank @Size(max = 150) String nombre,
        @Size(max = 500) String descripcion
) {
}
