package com.example.odontologia_api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.util.List;

public record OdontogramaBatchRequest(
        @Size(max = 500) String observaciones,
        @Valid List<OdontogramaDienteBatchRequest> dientes
) {
}
