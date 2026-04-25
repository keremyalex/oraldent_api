package com.example.odontologia_api.dto;

import com.example.odontologia_api.enums.RolUsuario;

public record UsuarioAuthResponse(
        Long id,
        String nombre,
        String apellidoPaterno,
        String apellidoMaterno,
        String correo,
        String celular,
        RolUsuario rol,
        String fotoPerfilUrl,
        Long pacienteId
) {
}
