package com.example.odontologia_api.dto;

import com.example.odontologia_api.enums.EstadoAnalisisRadiografia;
import com.example.odontologia_api.enums.SeveridadPerdidaOsea;
import com.example.odontologia_api.enums.TipoPerdidaOsea;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AnalisisRadiografiaResponse(
        Long id,
        Long radiografiaId,
        String modelo,
        EstadoAnalisisRadiografia estado,
        Boolean perdidaOseaDetectada,
        TipoPerdidaOsea tipoPerdidaOsea,
        SeveridadPerdidaOsea severidad,
        BigDecimal porcentajePerdidaOsea,
        BigDecimal confianza,
        String resultadoJson,
        String recomendacion,
        String errorAnalisis,
        Boolean validado,
        Long validadoPorUsuarioId,
        LocalDateTime fechaValidacion,
        String comentarioValidacion,
        SeveridadPerdidaOsea severidadFinal,
        TipoPerdidaOsea tipoPerdidaOseaFinal,
        Boolean activo,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaActualizacion
) {
}
