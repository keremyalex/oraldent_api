package com.example.odontologia_api.controller;

import com.example.odontologia_api.dto.CitaRequest;
import com.example.odontologia_api.dto.CitaResponse;
import com.example.odontologia_api.dto.DisponibilidadResponse;
import com.example.odontologia_api.dto.GestionCitaRequest;
import com.example.odontologia_api.dto.ReprogramarCitaRequest;
import com.example.odontologia_api.service.CitaService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
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
public class CitaController {

    private final CitaService citaService;

    public CitaController(CitaService citaService) {
        this.citaService = citaService;
    }

    @GetMapping
    public List<CitaResponse> listar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha
    ) {
        return citaService.listar(fecha);
    }

    @GetMapping("/{id}")
    public CitaResponse obtener(@PathVariable Long id) {
        return citaService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CitaResponse crear(@Valid @RequestBody CitaRequest request) {
        return citaService.crear(request);
    }

    @PutMapping("/{id}/cancelar")
    public CitaResponse cancelar(@PathVariable Long id, @Valid @RequestBody GestionCitaRequest request) {
        return citaService.cancelar(id, request.codigoGestion());
    }

    @PutMapping("/{id}/reprogramar")
    public CitaResponse reprogramar(
            @PathVariable Long id,
            @RequestParam String codigoGestion,
            @Valid @RequestBody ReprogramarCitaRequest request
    ) {
        return citaService.reprogramar(id, codigoGestion, request.nuevaFechaHoraInicio());
    }

    @GetMapping("/disponibilidad")
    public DisponibilidadResponse disponibilidad(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha
    ) {
        return citaService.disponibilidad(fecha);
    }
}
