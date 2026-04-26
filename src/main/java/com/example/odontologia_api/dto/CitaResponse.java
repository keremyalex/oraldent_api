package com.example.odontologia_api.dto;

import com.example.odontologia_api.enums.EstadoCita;
import java.time.LocalDateTime;

public record CitaResponse(
        Long id,
        PacienteResponse paciente,
        ServicioResponse servicio,
        LocalDateTime fechaHoraInicio,
        LocalDateTime fechaHoraFin,
        String motivo,
        EstadoCita estado,
        String codigoGestion,
        String notas,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaActualizacion
) {
}
