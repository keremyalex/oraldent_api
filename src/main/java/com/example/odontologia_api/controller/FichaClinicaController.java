package com.example.odontologia_api.controller;

import com.example.odontologia_api.dto.FichaClinicaRequest;
import com.example.odontologia_api.dto.FichaClinicaResponse;
import com.example.odontologia_api.security.UsuarioDetails;
import com.example.odontologia_api.service.FichaClinicaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@RequestMapping("/api")
@Tag(name = "Fichas clinicas", description = "Historial clinico de pacientes")
public class FichaClinicaController {

    private final FichaClinicaService fichaClinicaService;

    public FichaClinicaController(FichaClinicaService fichaClinicaService) {
        this.fichaClinicaService = fichaClinicaService;
    }

    @GetMapping("/pacientes/{pacienteId}/fichas")
    @Operation(summary = "Listar fichas clinicas de un paciente")
    public List<FichaClinicaResponse> listarPorPaciente(@PathVariable Long pacienteId) {
        return fichaClinicaService.listarPorPaciente(pacienteId);
    }

    @PostMapping("/pacientes/{pacienteId}/fichas")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear una ficha clinica para un paciente")
    public FichaClinicaResponse crear(
            @PathVariable Long pacienteId,
            @Valid @RequestBody FichaClinicaRequest request,
            @AuthenticationPrincipal UsuarioDetails usuarioDetails
    ) {
        return fichaClinicaService.crear(
                pacienteId,
                usuarioDetails != null ? usuarioDetails.getId() : null,
                request
        );
    }

    @GetMapping("/fichas/{fichaId}")
    @Operation(summary = "Obtener una ficha clinica")
    public FichaClinicaResponse obtener(@PathVariable Long fichaId) {
        return fichaClinicaService.obtener(fichaId);
    }

    @PutMapping("/fichas/{fichaId}")
    @Operation(summary = "Actualizar una ficha clinica")
    public FichaClinicaResponse actualizar(
            @PathVariable Long fichaId,
            @Valid @RequestBody FichaClinicaRequest request
    ) {
        return fichaClinicaService.actualizar(fichaId, request);
    }

    @DeleteMapping("/fichas/{fichaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Desactivar una ficha clinica")
    public void desactivar(@PathVariable Long fichaId) {
        fichaClinicaService.desactivar(fichaId);
    }
}
