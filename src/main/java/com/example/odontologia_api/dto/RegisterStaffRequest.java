package com.example.odontologia_api.dto;

import com.example.odontologia_api.enums.RolUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterStaffRequest(
        @NotBlank @Size(max = 80) String nombre,
        @NotBlank @Size(max = 80) String apellidoPaterno,
        @Size(max = 80) String apellidoMaterno,
        @NotBlank @Email @Size(max = 120) String correo,
        @NotBlank @Pattern(regexp = "^[0-9]{7,15}$") String celular,
        @NotBlank @Size(min = 8, max = 72) String password,
        @Size(max = 500) String fotoPerfilUrl,
        @NotNull RolUsuario rol
) {
}
