package com.example.odontologia_api.dto;

import java.time.LocalDateTime;
import java.util.List;

public record OdontogramaResponse(
        Long id,
        PacienteResponse paciente,
        Long usuarioId,
        Long citaId,
        String observaciones,
        Boolean activo,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaActualizacion,
        List<OdontogramaDienteResponse> dientes
) {
}
