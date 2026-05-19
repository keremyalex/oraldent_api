package com.example.odontologia_api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record OdontogramaDienteBatchRequest(
        @NotNull Integer numeroFdi,
        Boolean ausente,
        Boolean implante,
        Boolean corona,
        Boolean endodoncia,
        Boolean extraccionIndicada,
        @Size(max = 500) String observacion,
        @Valid List<OdontogramaCaraBatchRequest> caras
) {
}
