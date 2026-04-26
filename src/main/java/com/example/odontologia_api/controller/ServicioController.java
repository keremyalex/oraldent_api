package com.example.odontologia_api.controller;

import com.example.odontologia_api.dto.ServicioRequest;
import com.example.odontologia_api.dto.ServicioResponse;
import com.example.odontologia_api.service.ServicioService;
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
@RequestMapping("/api/servicios")
@Tag(name = "Servicios", description = "Operaciones para gestionar servicios odontológicos")
public class ServicioController {

    private final ServicioService servicioService;

    public ServicioController(ServicioService servicioService) {
        this.servicioService = servicioService;
    }

    @GetMapping
    @Operation(summary = "Listar servicios activos")
    public List<ServicioResponse> listar() {
        return servicioService.listarActivos();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un servicio por id")
    public ServicioResponse obtener(@PathVariable Long id) {
        return servicioService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar un servicio")
    public ServicioResponse crear(@Valid @RequestBody ServicioRequest request) {
        return servicioService.crear(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un servicio")
    public ServicioResponse actualizar(@PathVariable Long id, @Valid @RequestBody ServicioRequest request) {
        return servicioService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Desactivar un servicio")
    public void desactivar(@PathVariable Long id) {
        servicioService.desactivar(id);
    }
}
