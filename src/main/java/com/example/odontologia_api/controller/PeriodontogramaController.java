package com.example.odontologia_api.controller;

import com.example.odontologia_api.dto.PeriodontogramaDienteRequest;
import com.example.odontologia_api.dto.PeriodontogramaObservacionesRequest;
import com.example.odontologia_api.dto.PeriodontogramaResponse;
import com.example.odontologia_api.dto.PeriodontogramaSitioRequest;
import com.example.odontologia_api.enums.SitioPeriodontograma;
import com.example.odontologia_api.service.PeriodontogramaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
@Tag(name = "Periodontogramas", description = "Operaciones para gestionar periodontogramas")
public class PeriodontogramaController {

    private final PeriodontogramaService periodontogramaService;

    public PeriodontogramaController(PeriodontogramaService periodontogramaService) {
        this.periodontogramaService = periodontogramaService;
    }

    @GetMapping("/fichas/{fichaId}/periodontograma")
    @Operation(summary = "Obtener o crear el periodontograma de una ficha clinica")
    public PeriodontogramaResponse obtenerPorFicha(@PathVariable Long fichaId) {
        return periodontogramaService.obtenerOCrearPorFicha(fichaId);
    }

    @PostMapping("/fichas/{fichaId}/periodontogramas")
    @Operation(summary = "Crear un nuevo periodontograma para una ficha clinica")
    public PeriodontogramaResponse crearParaFicha(
            @PathVariable Long fichaId,
            @RequestParam(required = false) String observaciones
    ) {
        return periodontogramaService.crearParaFicha(fichaId, observaciones);
    }

    @PutMapping("/periodontogramas/{periodontogramaId}/observaciones")
    @Operation(summary = "Actualizar observaciones generales del periodontograma")
    public PeriodontogramaResponse actualizarObservaciones(
            @PathVariable Long periodontogramaId,
            @Valid @RequestBody PeriodontogramaObservacionesRequest request
    ) {
        return periodontogramaService.actualizarObservaciones(periodontogramaId, request);
    }

    @PutMapping("/periodontogramas/{periodontogramaId}/dientes/{numeroFdi}")
    @Operation(summary = "Actualizar datos generales de un diente periodontal")
    public PeriodontogramaResponse actualizarDiente(
            @PathVariable Long periodontogramaId,
            @PathVariable Integer numeroFdi,
            @Valid @RequestBody PeriodontogramaDienteRequest request
    ) {
        return periodontogramaService.actualizarDiente(periodontogramaId, numeroFdi, request);
    }

    @PutMapping("/periodontogramas/{periodontogramaId}/dientes/{numeroFdi}/sitios/{sitio}")
    @Operation(summary = "Actualizar un sitio periodontal de un diente")
    public PeriodontogramaResponse actualizarSitio(
            @PathVariable Long periodontogramaId,
            @PathVariable Integer numeroFdi,
            @PathVariable SitioPeriodontograma sitio,
            @Valid @RequestBody PeriodontogramaSitioRequest request
    ) {
        return periodontogramaService.actualizarSitio(periodontogramaId, numeroFdi, sitio, request);
    }
}
