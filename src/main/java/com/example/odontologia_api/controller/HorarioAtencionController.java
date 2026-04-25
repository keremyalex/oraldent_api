package com.example.odontologia_api.controller;

import com.example.odontologia_api.dto.HorarioAtencionRequest;
import com.example.odontologia_api.dto.HorarioAtencionResponse;
import com.example.odontologia_api.service.HorarioAtencionService;
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
@RequestMapping("/api/horarios")
@Tag(name = "Horarios", description = "Operaciones para gestionar horarios de atencion")
public class HorarioAtencionController {

    private final HorarioAtencionService horarioAtencionService;

    public HorarioAtencionController(HorarioAtencionService horarioAtencionService) {
        this.horarioAtencionService = horarioAtencionService;
    }

    @GetMapping
    @Operation(summary = "Listar horarios activos")
    public List<HorarioAtencionResponse> listar() {
        return horarioAtencionService.listar();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar un horario de atencion")
    public HorarioAtencionResponse crear(@Valid @RequestBody HorarioAtencionRequest request) {
        return horarioAtencionService.crear(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un horario de atencion")
    public HorarioAtencionResponse actualizar(@PathVariable Long id, @Valid @RequestBody HorarioAtencionRequest request) {
        return horarioAtencionService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Desactivar un horario de atencion")
    public void desactivar(@PathVariable Long id) {
        horarioAtencionService.desactivar(id);
    }
}
