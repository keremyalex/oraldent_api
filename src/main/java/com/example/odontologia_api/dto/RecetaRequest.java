package com.example.odontologia_api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.util.List;

public record RecetaRequest(
        @Size(max = 1200) String indicacionesGenerales,
        @Size(max = 1200) String observaciones,
        List<@Valid RecetaDetalleRequest> detalles
) {
}
