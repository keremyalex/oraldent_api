package com.example.odontologia_api.dto;

public record AnamnesisResponse(
        Long id,
        String descripcion,
        Boolean hemorragia,
        Boolean diabetes,
        Boolean hipertension,
        Boolean epilepsia,
        Boolean problemasCardiovasculares,
        Boolean lipotimias,
        Boolean tratamientoMedicoActual,
        String alergias,
        String medicamentoActual,
        String otrasPatologias
) {
}
