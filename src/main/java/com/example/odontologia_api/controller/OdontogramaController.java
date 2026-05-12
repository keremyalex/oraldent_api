package com.example.odontologia_api.controller;

import com.example.odontologia_api.dto.OdontogramaCaraRequest;
import com.example.odontologia_api.dto.OdontogramaDienteRequest;
import com.example.odontologia_api.dto.OdontogramaObservacionesRequest;
import com.example.odontologia_api.dto.OdontogramaResponse;
import com.example.odontologia_api.enums.TipoCaraOdontograma;
import com.example.odontologia_api.security.UsuarioDetails;
import com.example.odontologia_api.service.OdontogramaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@RequestMapping("/api")
@Tag(name = "Odontogramas", description = "Operaciones para gestionar odontogramas")
public class OdontogramaController {

    private final OdontogramaService odontogramaService;

    public OdontogramaController(OdontogramaService odontogramaService) {
        this.odontogramaService = odontogramaService;
    }

    @GetMapping("/pacientes/{pacienteId}/odontograma")
    @Operation(summary = "Obtener o crear el odontograma activo de un paciente")
    public OdontogramaResponse obtenerPorPaciente(
            @PathVariable Long pacienteId,
            @AuthenticationPrincipal UsuarioDetails usuarioDetails
    ) {
        return odontogramaService.obtenerOCrearPorPaciente(
                pacienteId,
                usuarioDetails != null ? usuarioDetails.getId() : null
        );
    }

    @PostMapping("/pacientes/{pacienteId}/odontogramas")
    @Operation(summary = "Crear un nuevo odontograma activo para un paciente")
    public OdontogramaResponse crear(
            @PathVariable Long pacienteId,
            @RequestParam(required = false) Long citaId,
            @RequestParam(required = false) String observaciones,
            @AuthenticationPrincipal UsuarioDetails usuarioDetails
    ) {
        return odontogramaService.crear(
                pacienteId,
                usuarioDetails != null ? usuarioDetails.getId() : null,
                citaId,
                observaciones
        );
    }

    @PutMapping("/odontogramas/{odontogramaId}/observaciones")
    @Operation(summary = "Actualizar observaciones generales del odontograma")
    public OdontogramaResponse actualizarObservaciones(
            @PathVariable Long odontogramaId,
            @Valid @RequestBody OdontogramaObservacionesRequest request
    ) {
        return odontogramaService.actualizarObservaciones(odontogramaId, request);
    }

    @PutMapping("/odontogramas/{odontogramaId}/dientes/{numeroFdi}")
    @Operation(summary = "Actualizar datos clinicos de un diente")
    public OdontogramaResponse actualizarDiente(
            @PathVariable Long odontogramaId,
            @PathVariable Integer numeroFdi,
            @Valid @RequestBody OdontogramaDienteRequest request
    ) {
        return odontogramaService.actualizarDiente(odontogramaId, numeroFdi, request);
    }

    @PutMapping("/odontogramas/{odontogramaId}/dientes/{numeroFdi}/caras/{tipo}")
    @Operation(summary = "Actualizar una cara de un diente")
    public OdontogramaResponse actualizarCara(
            @PathVariable Long odontogramaId,
            @PathVariable Integer numeroFdi,
            @PathVariable TipoCaraOdontograma tipo,
            @Valid @RequestBody OdontogramaCaraRequest request
    ) {
        return odontogramaService.actualizarCara(odontogramaId, numeroFdi, tipo, request);
    }
}
