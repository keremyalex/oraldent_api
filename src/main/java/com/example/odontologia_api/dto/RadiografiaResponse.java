package com.example.odontologia_api.dto;

import com.example.odontologia_api.enums.SeveridadPerdidaOsea;
import com.example.odontologia_api.enums.TipoPerdidaOsea;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record RadiografiaResponse(
        Long id,
        Long fichaClinicaId,
        String titulo,
        String descripcion,
        String tipo,
        Integer numeroFdi,
        String zona,
        String imagenUrl,
        String imagenPublicId,
        String nombreArchivo,
        String formato,
        Long tamanoBytes,
        Integer anchoPx,
        Integer altoPx,
        LocalDate fechaEstudio,
        String diagnosticoRadiografico,
        Boolean perdidaOseaObservada,
        TipoPerdidaOsea tipoPerdidaOsea,
        SeveridadPerdidaOsea severidadPerdidaOsea,
        BigDecimal porcentajePerdidaOseaEstimado,
        BigDecimal nivelCrestaOseaMm,
        String observacionesPeriodontales,
        Boolean activo,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaActualizacion
) {
}
