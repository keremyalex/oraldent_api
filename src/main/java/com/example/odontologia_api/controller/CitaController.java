package com.example.odontologia_api.controller;

import com.example.odontologia_api.dto.CitaRequest;
import com.example.odontologia_api.dto.CitaResponse;
import com.example.odontologia_api.dto.DisponibilidadResponse;
import com.example.odontologia_api.dto.GestionCitaRequest;
import com.example.odontologia_api.dto.ReprogramarCitaRequest;
import com.example.odontologia_api.security.UsuarioDetails;
import com.example.odontologia_api.service.CitaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@RequestMapping("/api/citas")
@Tag(name = "Citas", description = "Operaciones para gestionar citas odontologicas")
public class CitaController {

    private final CitaService citaService;

    public CitaController(CitaService citaService) {
        this.citaService = citaService;
    }

    @GetMapping
    @Operation(summary = "Listar citas por fecha")
    public List<CitaResponse> listar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha
    ) {
        return citaService.listar(fecha);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una cita por id")
    public CitaResponse obtener(@PathVariable Long id) {
        return citaService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar una cita")
    public CitaResponse crear(
            @Valid @RequestBody CitaRequest request,
            @AuthenticationPrincipal UsuarioDetails usuarioDetails
    ) {
        return citaService.crear(request, usuarioDetails != null ? usuarioDetails.getId() : null);
    }

    @PutMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar una cita usando codigo de gestion")
    public CitaResponse cancelar(@PathVariable Long id, @Valid @RequestBody GestionCitaRequest request) {
        return citaService.cancelar(id, request.codigoGestion());
    }

    @PutMapping("/{id}/reprogramar")
    @Operation(summary = "Reprogramar una cita usando codigo de gestion")
    public CitaResponse reprogramar(
            @PathVariable Long id,
            @RequestParam String codigoGestion,
            @Valid @RequestBody ReprogramarCitaRequest request
    ) {
        return citaService.reprogramar(id, codigoGestion, request.nuevaFechaHoraInicio());
    }

    @GetMapping("/disponibilidad")
    @Operation(summary = "Consultar horarios disponibles por fecha")
    public DisponibilidadResponse disponibilidad(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha
    ) {
        return citaService.disponibilidad(fecha);
    }
}
