package com.example.odontologia_api.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank String identificador,
        @NotBlank String password
) {
}
