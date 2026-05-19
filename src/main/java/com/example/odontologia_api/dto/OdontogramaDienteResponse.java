package com.example.odontologia_api.dto;

import java.util.List;

public record OdontogramaDienteResponse(
        Long id,
        Integer numeroFdi,
        Integer cuadrante,
        Integer posicion,
        Boolean ausente,
        Boolean implante,
        Boolean corona,
        Boolean endodoncia,
        Boolean extraccionIndicada,
        String observacion,
        List<OdontogramaCaraResponse> caras
) {
}
