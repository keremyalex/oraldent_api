package com.example.odontologia_api.dto;

import com.example.odontologia_api.enums.SitioPeriodontograma;

public record PeriodontogramaSitioResponse(
        Long id,
        SitioPeriodontograma sitio,
        Boolean sangradoSondaje,
        Boolean placa,
        Boolean supuracion,
        Integer margenGingivalMm,
        Integer profundidadSondajeMm,
        Integer nivelInsercionMm,
        String observacion
) {
}
