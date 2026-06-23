package com.example.odontologia_api.controller;

import com.example.odontologia_api.dto.CitaRequest;
import com.example.odontologia_api.dto.CitaResponse;
import com.example.odontologia_api.dto.FichaClinicaResponse;
import com.example.odontologia_api.dto.PacienteResponse;
import com.example.odontologia_api.dto.PortalPacienteAccessRequest;
import com.example.odontologia_api.dto.PortalPacienteAccessResponse;
import com.example.odontologia_api.dto.RadiografiaResponse;
import com.example.odontologia_api.dto.RecetaResponse;
import com.example.odontologia_api.security.PortalPacienteDetails;
import com.example.odontologia_api.service.PortalPacienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@RequestMapping("/api/portal-paciente")
@Tag(name = "Portal paciente", description = "Acceso de pacientes para consultar fichas y reportes")
public class PortalPacienteController {

    private final PortalPacienteService portalPacienteService;

    public PortalPacienteController(PortalPacienteService portalPacienteService) {
        this.portalPacienteService = portalPacienteService;
    }

    @PostMapping("/acceso")
    @Operation(summary = "Generar token temporal de acceso al portal paciente")
    public PortalPacienteAccessResponse acceder(@Valid @RequestBody PortalPacienteAccessRequest request) {
        return portalPacienteService.acceder(request);
    }

    @GetMapping("/perfil")
    @Operation(summary = "Obtener perfil del paciente autenticado")
    public PacienteResponse perfil(@AuthenticationPrincipal PortalPacienteDetails pacienteDetails) {
        return portalPacienteService.perfil(pacienteDetails.pacienteId());
    }

    @PostMapping("/citas")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar cita para el paciente autenticado")
    public CitaResponse crearCita(
            @AuthenticationPrincipal PortalPacienteDetails pacienteDetails,
            @Valid @RequestBody CitaRequest request
    ) {
        return portalPacienteService.crearCita(pacienteDetails.pacienteId(), request);
    }

    @GetMapping("/citas")
    @Operation(summary = "Listar citas del paciente autenticado")
    public List<CitaResponse> citas(@AuthenticationPrincipal PortalPacienteDetails pacienteDetails) {
        return portalPacienteService.citas(pacienteDetails.pacienteId());
    }

    @GetMapping("/fichas")
    @Operation(summary = "Listar fichas clinicas del paciente autenticado")
    public List<FichaClinicaResponse> fichas(@AuthenticationPrincipal PortalPacienteDetails pacienteDetails) {
        return portalPacienteService.fichas(pacienteDetails.pacienteId());
    }

    @GetMapping("/fichas/{fichaId}/recetas")
    @Operation(summary = "Listar recetas de una ficha del paciente autenticado")
    public List<RecetaResponse> recetasPorFicha(
            @AuthenticationPrincipal PortalPacienteDetails pacienteDetails,
            @PathVariable Long fichaId
    ) {
        return portalPacienteService.recetasPorFicha(pacienteDetails.pacienteId(), fichaId);
    }

    @GetMapping("/fichas/{fichaId}/radiografias")
    @Operation(summary = "Listar radiografias de una ficha del paciente autenticado")
    public List<RadiografiaResponse> radiografiasPorFicha(
            @AuthenticationPrincipal PortalPacienteDetails pacienteDetails,
            @PathVariable Long fichaId
    ) {
        return portalPacienteService.radiografiasPorFicha(pacienteDetails.pacienteId(), fichaId);
    }

    @GetMapping(value = "/radiografias/{radiografiaId}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Descargar reporte PDF de radiografia del paciente autenticado")
    public ResponseEntity<byte[]> descargarRadiografiaPdf(
            @AuthenticationPrincipal PortalPacienteDetails pacienteDetails,
            @PathVariable Long radiografiaId
    ) {
        byte[] pdf = portalPacienteService.radiografiaPdf(pacienteDetails.pacienteId(), radiografiaId);
        String filename = "radiografia-" + radiografiaId + ".pdf";
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(filename).build().toString())
                .body(pdf);
    }

    @GetMapping(value = "/odontogramas/{odontogramaId}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Descargar odontograma PDF del paciente autenticado")
    public ResponseEntity<byte[]> descargarOdontogramaPdf(
            @AuthenticationPrincipal PortalPacienteDetails pacienteDetails,
            @PathVariable Long odontogramaId
    ) {
        byte[] pdf = portalPacienteService.odontogramaPdf(pacienteDetails.pacienteId(), odontogramaId);
        String filename = "odontograma-" + odontogramaId + ".pdf";
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(filename).build().toString())
                .body(pdf);
    }

    @GetMapping(value = "/periodontogramas/{periodontogramaId}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Descargar periodontograma PDF del paciente autenticado")
    public ResponseEntity<byte[]> descargarPeriodontogramaPdf(
            @AuthenticationPrincipal PortalPacienteDetails pacienteDetails,
            @PathVariable Long periodontogramaId
    ) {
        byte[] pdf = portalPacienteService.periodontogramaPdf(pacienteDetails.pacienteId(), periodontogramaId);
        String filename = "periodontograma-" + periodontogramaId + ".pdf";
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(filename).build().toString())
                .body(pdf);
    }

    @GetMapping(value = "/recetas/{recetaId}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Descargar receta PDF del paciente autenticado")
    public ResponseEntity<byte[]> descargarRecetaPdf(
            @AuthenticationPrincipal PortalPacienteDetails pacienteDetails,
            @PathVariable Long recetaId
    ) {
        byte[] pdf = portalPacienteService.recetaPdf(pacienteDetails.pacienteId(), recetaId);
        String filename = "receta-" + recetaId + ".pdf";
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(filename).build().toString())
                .body(pdf);
    }
}

