package com.example.odontologia_api.dto;

import java.time.LocalDateTime;

public record DoctorPerfilResponse(
        Long id,
        String nombre,
        String apellidoPaterno,
        String apellidoMaterno,
        String especialidad,
        String telefono,
        String correo,
        String direccionConsultorio,
        String descripcion,
        Boolean activo,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaActualizacion
) {
}
