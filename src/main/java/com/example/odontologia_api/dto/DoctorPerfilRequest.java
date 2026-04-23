package com.example.odontologia_api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DoctorPerfilRequest(
        @NotBlank @Size(max = 80) String nombre,
        @NotBlank @Size(max = 80) String apellidoPaterno,
        @Size(max = 80) String apellidoMaterno,
        @NotBlank @Size(max = 120) String especialidad,
        @Size(max = 30) String telefono,
        @Email @Size(max = 120) String correo,
        @Size(max = 250) String direccionConsultorio,
        @Size(max = 1000) String descripcion
) {
}
