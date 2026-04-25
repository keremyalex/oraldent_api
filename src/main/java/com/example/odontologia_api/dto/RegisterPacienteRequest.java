package com.example.odontologia_api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record RegisterPacienteRequest(
        @NotBlank @Size(max = 80) String nombre,
        @NotBlank @Size(max = 80) String apellidoPaterno,
        @Size(max = 80) String apellidoMaterno,
        @Email @Size(max = 120) String correo,
        @NotBlank @Pattern(regexp = "^[0-9]{7,15}$") String celular,
        @NotBlank @Size(min = 8, max = 72) String password,
        @Size(max = 30) String documentoIdentidad,
        LocalDate fechaNacimiento,
        @Size(max = 200) String direccion,
        @Size(max = 500) String fotoUrl
) {
}
