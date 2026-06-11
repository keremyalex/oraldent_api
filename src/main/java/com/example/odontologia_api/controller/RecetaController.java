package com.example.odontologia_api.controller;

import com.example.odontologia_api.dto.RecetaRequest;
import com.example.odontologia_api.dto.RecetaResponse;
import com.example.odontologia_api.security.UsuarioDetails;
import com.example.odontologia_api.service.RecetaPdfService;
import com.example.odontologia_api.service.RecetaService;
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
@Tag(name = "Recetas", description = "Operaciones para gestionar recetas por ficha clinica")
public class RecetaController {

    private final RecetaService recetaService;
    private final RecetaPdfService recetaPdfService;

    public RecetaController(RecetaService recetaService, RecetaPdfService recetaPdfService) {
        this.recetaService = recetaService;
        this.recetaPdfService = recetaPdfService;
    }

    @GetMapping("/fichas/{fichaId}/recetas")
    @Operation(summary = "Listar recetas de una ficha clinica")
    public List<RecetaResponse> listarPorFicha(@PathVariable Long fichaId) {
        return recetaService.listarPorFicha(fichaId);
    }

    @PostMapping("/fichas/{fichaId}/recetas")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear una receta para una ficha clinica")
    public RecetaResponse crear(
            @PathVariable Long fichaId,
            @Valid @RequestBody RecetaRequest request,
            @AuthenticationPrincipal UsuarioDetails usuarioDetails
    ) {
        return recetaService.crear(
                fichaId,
                usuarioDetails != null ? usuarioDetails.getId() : null,
                request
        );
    }

    @GetMapping("/recetas/{recetaId}")
    @Operation(summary = "Obtener una receta por id")
    public RecetaResponse obtener(@PathVariable Long recetaId) {
        return recetaService.obtener(recetaId);
    }


    @GetMapping(value = "/recetas/{recetaId}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Descargar una receta en PDF")
    public ResponseEntity<byte[]> descargarPdf(@PathVariable Long recetaId) {
        byte[] pdf = recetaPdfService.generarPdf(recetaId);
        String filename = "receta-" + recetaId + ".pdf";
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(filename).build().toString())
                .body(pdf);
    }
    @PutMapping("/recetas/{recetaId}")
    @Operation(summary = "Actualizar una receta")
    public RecetaResponse actualizar(
            @PathVariable Long recetaId,
            @Valid @RequestBody RecetaRequest request
    ) {
        return recetaService.actualizar(recetaId, request);
    }

    @DeleteMapping("/recetas/{recetaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Desactivar una receta")
    public void desactivar(@PathVariable Long recetaId) {
        recetaService.desactivar(recetaId);
    }
}



