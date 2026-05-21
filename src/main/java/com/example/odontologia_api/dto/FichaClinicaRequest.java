package com.example.odontologia_api.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FichaClinicaRequest(
        Long citaId,
        LocalDateTime fecha,
        @Min(0) @Max(130) Integer edad,
        @Size(max = 30) String sexo,
        @Size(max = 120) String procedencia,
        @Size(max = 120) String ocupacion,
        @Size(max = 30) String presionArterial,
        @DecimalMin("30.0") @DecimalMax("45.0") BigDecimal temperatura,
        @Min(0) @Max(260) Integer pulso,
        @Size(max = 500) String motivoConsulta,
        @Size(max = 1000) String enfermedadActual,
        AnamnesisRequest anamnesis,
        @Size(max = 1200) String examenClinico,
        @Size(max = 1200) String examenRadiografico,
        @Size(max = 1200) String diagnostico,
        @Size(max = 1200) String tratamiento,
        @Size(max = 800) String tecnicaAnestesia,
        @Size(max = 1200) String evolucion
) {
}
