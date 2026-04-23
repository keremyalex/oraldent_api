package com.example.odontologia_api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record PacienteRequest(
        @NotBlank @Size(max = 80) String nombre,
        @NotBlank @Size(max = 80) String apellidoPaterno,
        @Size(max = 80) String apellidoMaterno,
        @NotBlank @Size(max = 30) String celular,
        @Size(max = 30) String documentoIdentidad,
        @Email @Size(max = 120) String correo,
        @Past LocalDate fechaNacimiento,
        @Size(max = 200) String direccion,
        @Size(max = 500) String fotoUrl
) {
}
