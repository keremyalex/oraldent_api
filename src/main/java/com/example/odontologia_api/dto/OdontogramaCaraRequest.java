package com.example.odontologia_api.dto;

import com.example.odontologia_api.enums.ColorCaraOdontograma;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OdontogramaCaraRequest(
        @NotNull ColorCaraOdontograma color,
        @Size(max = 500) String descripcion
) {
}
