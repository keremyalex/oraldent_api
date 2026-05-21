package com.example.odontologia_api.dto;

import jakarta.validation.constraints.Size;

public record AnamnesisRequest(
        @Size(max = 1000) String descripcion,
        Boolean hemorragia,
        Boolean diabetes,
        Boolean hipertension,
        Boolean epilepsia,
        Boolean problemasCardiovasculares,
        Boolean lipotimias,
        Boolean tratamientoMedicoActual,
        @Size(max = 500) String alergias,
        @Size(max = 500) String medicamentoActual,
        @Size(max = 500) String otrasPatologias
) {
}
