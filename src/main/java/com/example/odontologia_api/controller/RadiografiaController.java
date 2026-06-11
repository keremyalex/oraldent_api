package com.example.odontologia_api.controller;

import com.example.odontologia_api.dto.AnalisisRadiografiaRequest;
import com.example.odontologia_api.dto.AnalisisRadiografiaResponse;
import com.example.odontologia_api.dto.RadiografiaRequest;
import com.example.odontologia_api.dto.RadiografiaResponse;
import com.example.odontologia_api.enums.SeveridadPerdidaOsea;
import com.example.odontologia_api.enums.TipoPerdidaOsea;
import com.example.odontologia_api.security.UsuarioDetails;
import com.example.odontologia_api.service.RadiografiaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin
@RestController
@RequestMapping("/api")
@Tag(name = "Radiografias", description = "Operaciones para gestionar radiografias por ficha clinica")
public class RadiografiaController {

    private final RadiografiaService radiografiaService;

    public RadiografiaController(RadiografiaService radiografiaService) {
        this.radiografiaService = radiografiaService;
    }

    @GetMapping("/fichas/{fichaId}/radiografias")
    @Operation(summary = "Listar radiografias de una ficha clinica")
    public List<RadiografiaResponse> listarPorFicha(@PathVariable Long fichaId) {
        return radiografiaService.listarPorFicha(fichaId);
    }

    @PostMapping(value = "/fichas/{fichaId}/radiografias", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear una radiografia con imagen")
    public RadiografiaResponse crear(
            @PathVariable Long fichaId,
            @RequestPart("archivo") MultipartFile archivo,
            @RequestParam String titulo,
            @RequestParam(required = false) String descripcion,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaEstudio,
            @RequestParam(required = false) Integer numeroFdi,
            @RequestParam(required = false) String zona,
            @RequestParam(required = false) String diagnosticoRadiografico,
            @RequestParam(required = false) Boolean perdidaOseaObservada,
            @RequestParam(required = false) TipoPerdidaOsea tipoPerdidaOsea,
            @RequestParam(required = false) SeveridadPerdidaOsea severidadPerdidaOsea,
            @RequestParam(required = false) BigDecimal porcentajePerdidaOseaEstimado,
            @RequestParam(required = false) BigDecimal nivelCrestaOseaMm,
            @RequestParam(required = false) String observacionesPeriodontales
    ) {
        return radiografiaService.crear(
                fichaId,
                new RadiografiaRequest(
                        titulo,
                        descripcion,
                        tipo,
                        fechaEstudio,
                        numeroFdi,
                        zona,
                        diagnosticoRadiografico,
                        perdidaOseaObservada,
                        tipoPerdidaOsea,
                        severidadPerdidaOsea,
                        porcentajePerdidaOseaEstimado,
                        nivelCrestaOseaMm,
                        observacionesPeriodontales
                ),
                archivo
        );
    }

    @GetMapping("/radiografias/{radiografiaId}")
    @Operation(summary = "Obtener una radiografia por id")
    public RadiografiaResponse obtener(@PathVariable Long radiografiaId) {
        return radiografiaService.obtener(radiografiaId);
    }

    @PutMapping("/radiografias/{radiografiaId}")
    @Operation(summary = "Actualizar datos de una radiografia")
    public RadiografiaResponse actualizar(
            @PathVariable Long radiografiaId,
            @Valid @RequestBody RadiografiaRequest request
    ) {
        return radiografiaService.actualizar(radiografiaId, request);
    }

    @PutMapping(value = "/radiografias/{radiografiaId}/imagen", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Reemplazar la imagen de una radiografia")
    public RadiografiaResponse reemplazarImagen(
            @PathVariable Long radiografiaId,
            @RequestPart("archivo") MultipartFile archivo
    ) {
        return radiografiaService.reemplazarImagen(radiografiaId, archivo);
    }

    @DeleteMapping("/radiografias/{radiografiaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Desactivar una radiografia")
    public void desactivar(@PathVariable Long radiografiaId) {
        radiografiaService.desactivar(radiografiaId);
    }

    @GetMapping("/radiografias/{radiografiaId}/analisis")
    @Operation(summary = "Listar analisis de una radiografia")
    public List<AnalisisRadiografiaResponse> listarAnalisis(@PathVariable Long radiografiaId) {
        return radiografiaService.listarAnalisis(radiografiaId);
    }

    @PostMapping("/radiografias/{radiografiaId}/analisis")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear analisis de IA o validacion para una radiografia")
    public AnalisisRadiografiaResponse crearAnalisis(
            @PathVariable Long radiografiaId,
            @Valid @RequestBody AnalisisRadiografiaRequest request,
            @AuthenticationPrincipal UsuarioDetails usuarioDetails
    ) {
        return radiografiaService.crearAnalisis(
                radiografiaId,
                usuarioDetails == null ? null : usuarioDetails.getId(),
                request
        );
    }

    @GetMapping("/analisis-radiografias/{analisisId}")
    @Operation(summary = "Obtener un analisis radiografico")
    public AnalisisRadiografiaResponse obtenerAnalisis(@PathVariable Long analisisId) {
        return radiografiaService.obtenerAnalisis(analisisId);
    }

    @PutMapping("/analisis-radiografias/{analisisId}")
    @Operation(summary = "Actualizar un analisis radiografico")
    public AnalisisRadiografiaResponse actualizarAnalisis(
            @PathVariable Long analisisId,
            @Valid @RequestBody AnalisisRadiografiaRequest request,
            @AuthenticationPrincipal UsuarioDetails usuarioDetails
    ) {
        return radiografiaService.actualizarAnalisis(
                analisisId,
                usuarioDetails == null ? null : usuarioDetails.getId(),
                request
        );
    }

    @DeleteMapping("/analisis-radiografias/{analisisId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Desactivar un analisis radiografico")
    public void desactivarAnalisis(@PathVariable Long analisisId) {
        radiografiaService.desactivarAnalisis(analisisId);
    }
}
