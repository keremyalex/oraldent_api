package com.example.odontologia_api.dto;

public record RecetaDetalleResponse(
        Long id,
        String medicamento,
        String dosis,
        String frecuencia,
        String duracion,
        String indicaciones,
        Integer orden
) {
}
