package com.example.odontologia_api.dto;

import com.example.odontologia_api.enums.FurcacionPeriodontograma;
import java.util.List;

public record PeriodontogramaDienteResponse(
        Long id,
        Integer numeroFdi,
        Integer cuadrante,
        Integer posicion,
        Boolean ausente,
        Boolean implante,
        Integer movilidad,
        FurcacionPeriodontograma furcacionVestibular,
        FurcacionPeriodontograma furcacionPalatinaLingual,
        String observacion,
        List<PeriodontogramaSitioResponse> sitios
) {
}
