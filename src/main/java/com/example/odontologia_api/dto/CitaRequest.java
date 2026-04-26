package com.example.odontologia_api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record CitaRequest(
        Long pacienteId,
        @Valid PacienteRequest paciente,
        @NotNull Long servicioId,
        @NotNull @Future LocalDateTime fechaHoraInicio,
        @NotBlank @Size(max = 300) String motivo,
        @Size(max = 500) String notas
) {
}
