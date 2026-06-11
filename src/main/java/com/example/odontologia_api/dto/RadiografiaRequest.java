package com.example.odontologia_api.dto;

import com.example.odontologia_api.enums.SeveridadPerdidaOsea;
import com.example.odontologia_api.enums.TipoPerdidaOsea;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record RadiografiaRequest(
        @Size(max = 160) String titulo,
        @Size(max = 500) String descripcion,
        @Size(max = 80) String tipo,
        LocalDate fechaEstudio,
        @Min(11) @Max(48) Integer numeroFdi,
        @Size(max = 80) String zona,
        @Size(max = 1000) String diagnosticoRadiografico,
        Boolean perdidaOseaObservada,
        TipoPerdidaOsea tipoPerdidaOsea,
        SeveridadPerdidaOsea severidadPerdidaOsea,
        @DecimalMin("0.00") @DecimalMax("100.00") BigDecimal porcentajePerdidaOseaEstimado,
        @DecimalMin("0.00") BigDecimal nivelCrestaOseaMm,
        @Size(max = 1000) String observacionesPeriodontales
) {
}
