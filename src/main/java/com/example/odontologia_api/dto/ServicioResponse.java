package com.example.odontologia_api.dto;

import java.time.LocalDateTime;

public record ServicioResponse(
        Long id,
        String nombre,
        String descripcion,
        Boolean activo,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaActualizacion
) {
}
