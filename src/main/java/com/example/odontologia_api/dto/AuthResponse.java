package com.example.odontologia_api.dto;

public record AuthResponse(
        String token,
        String tipoToken,
        UsuarioAuthResponse usuario
) {
}
