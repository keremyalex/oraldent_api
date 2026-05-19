package com.example.odontologia_api.dto;

import java.time.LocalDateTime;
import java.util.List;

public record PeriodontogramaResponse(
        Long id,
        PacienteResponse paciente,
        Long usuarioId,
        Long citaId,
        Long fichaClinicaId,
        String observaciones,
        Boolean activo,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaActualizacion,
        List<PeriodontogramaDienteResponse> dientes
) {
}
