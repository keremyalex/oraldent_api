package com.example.odontologia_api.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PacienteResponse(
        Long id,
        String nombre,
        String apellidoPaterno,
        String apellidoMaterno,
        String celular,
        String documentoIdentidad,
        String correo,
        LocalDate fechaNacimiento,
        String direccion,
        String fotoUrl,
        Boolean activo,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaActualizacion
) {
}
