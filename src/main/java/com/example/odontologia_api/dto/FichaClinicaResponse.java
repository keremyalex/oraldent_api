package com.example.odontologia_api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FichaClinicaResponse(
        Long id,
        PacienteResponse paciente,
        Long usuarioId,
        Long citaId,
        LocalDateTime fecha,
        Integer edad,
        String sexo,
        String procedencia,
        String ocupacion,
        String presionArterial,
        BigDecimal temperatura,
        Integer pulso,
        String motivoConsulta,
        String enfermedadActual,
        AnamnesisResponse anamnesis,
        String examenClinico,
        String examenRadiografico,
        String diagnostico,
        String tratamiento,
        String tecnicaAnestesia,
        String evolucion,
        Boolean activo,
        Long odontogramaId,
        Long periodontogramaId,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaActualizacion
) {
}
