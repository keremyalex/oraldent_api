package com.example.odontologia_api.dto;

import java.time.LocalDateTime;
import java.util.List;

public record RecetaResponse(
        Long id,
        Long fichaClinicaId,
        Long usuarioId,
        String indicacionesGenerales,
        String observaciones,
        Boolean activo,
        List<RecetaDetalleResponse> detalles,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaActualizacion
) {
}
