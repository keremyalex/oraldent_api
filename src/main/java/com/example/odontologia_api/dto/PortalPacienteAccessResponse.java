package com.example.odontologia_api.dto;

public record PortalPacienteAccessResponse(
        String token,
        String tipoToken,
        PacienteResponse paciente
) {
}
