package com.example.odontologia_api.controller;

import com.example.odontologia_api.dto.PacienteRequest;
import com.example.odontologia_api.dto.PacienteResponse;
import com.example.odontologia_api.service.PacienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/api/pacientes")
@Tag(name = "Pacientes", description = "Operaciones para gestionar pacientes")
public class PacienteController {

    private final PacienteService pacienteService;

    public PacienteController(PacienteService pacienteService) {
        this.pacienteService = pacienteService;
    }

    @GetMapping
    @Operation(summary = "Listar pacientes activos")
    public List<PacienteResponse> listar() {
        return pacienteService.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un paciente por id")
    public PacienteResponse obtener(@PathVariable Long id) {
        return pacienteService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar un paciente")
    public PacienteResponse crear(@Valid @RequestBody PacienteRequest request) {
        return pacienteService.crear(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un paciente")
    public PacienteResponse actualizar(@PathVariable Long id, @Valid @RequestBody PacienteRequest request) {
        return pacienteService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Desactivar un paciente")
    public void desactivar(@PathVariable Long id) {
        pacienteService.desactivar(id);
    }
}
