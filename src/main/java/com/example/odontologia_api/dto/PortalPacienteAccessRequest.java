package com.example.odontologia_api.dto;

import jakarta.validation.constraints.NotBlank;

public record PortalPacienteAccessRequest(
        @NotBlank String codigoPaciente,
        @NotBlank String documentoIdentidad
) {
}
