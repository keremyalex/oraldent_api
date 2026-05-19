package com.example.odontologia_api.dto;

import com.example.odontologia_api.enums.ColorCaraOdontograma;
import com.example.odontologia_api.enums.TipoCaraOdontograma;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OdontogramaCaraBatchRequest(
        @NotNull TipoCaraOdontograma tipo,
        @NotNull ColorCaraOdontograma color,
        @Size(max = 500) String descripcion
) {
}
