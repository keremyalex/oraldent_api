package com.example.odontologia_api.dto;

import com.example.odontologia_api.enums.ColorCaraOdontograma;
import com.example.odontologia_api.enums.TipoCaraOdontograma;

public record OdontogramaCaraResponse(
        Long id,
        TipoCaraOdontograma tipo,
        ColorCaraOdontograma color,
        String descripcion
) {
}
