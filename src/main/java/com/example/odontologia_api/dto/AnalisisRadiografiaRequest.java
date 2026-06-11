package com.example.odontologia_api.dto;

import com.example.odontologia_api.enums.EstadoAnalisisRadiografia;
import com.example.odontologia_api.enums.SeveridadPerdidaOsea;
import com.example.odontologia_api.enums.TipoPerdidaOsea;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record AnalisisRadiografiaRequest(
        @Size(max = 120) String modelo,
        EstadoAnalisisRadiografia estado,
        Boolean perdidaOseaDetectada,
        TipoPerdidaOsea tipoPerdidaOsea,
        SeveridadPerdidaOsea severidad,
        @DecimalMin("0.00") @DecimalMax("100.00") BigDecimal porcentajePerdidaOsea,
        @DecimalMin("0.0000") @DecimalMax("1.0000") BigDecimal confianza,
        String resultadoJson,
        @Size(max = 1000) String recomendacion,
        @Size(max = 1000) String errorAnalisis,
        Boolean validado,
        @Size(max = 1000) String comentarioValidacion,
        SeveridadPerdidaOsea severidadFinal,
        TipoPerdidaOsea tipoPerdidaOseaFinal
) {
}
