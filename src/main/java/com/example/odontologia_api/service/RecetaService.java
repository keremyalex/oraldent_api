package com.example.odontologia_api.service;

import com.example.odontologia_api.dto.RecetaDetalleRequest;
import com.example.odontologia_api.dto.RecetaDetalleResponse;
import com.example.odontologia_api.dto.RecetaRequest;
import com.example.odontologia_api.dto.RecetaResponse;
import com.example.odontologia_api.entity.FichaClinica;
import com.example.odontologia_api.entity.Receta;
import com.example.odontologia_api.entity.RecetaDetalle;
import com.example.odontologia_api.entity.Usuario;
import com.example.odontologia_api.exception.RecursoNoEncontradoException;
import com.example.odontologia_api.repository.RecetaRepository;
import com.example.odontologia_api.repository.UsuarioRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecetaService {

    private final RecetaRepository recetaRepository;
    private final FichaClinicaService fichaClinicaService;
    private final UsuarioRepository usuarioRepository;

    public RecetaService(
            RecetaRepository recetaRepository,
            FichaClinicaService fichaClinicaService,
            UsuarioRepository usuarioRepository
    ) {
        this.recetaRepository = recetaRepository;
        this.fichaClinicaService = fichaClinicaService;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<RecetaResponse> listarPorFicha(Long fichaId) {
        FichaClinica ficha = fichaClinicaService.buscarActiva(fichaId);
        return recetaRepository.findByFichaClinicaAndActivoTrueOrderByFechaCreacionDescIdDesc(ficha)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public RecetaResponse obtener(Long recetaId) {
        return toResponse(buscarActiva(recetaId));
    }

    @Transactional
    public RecetaResponse crear(Long fichaId, Long usuarioId, RecetaRequest request) {
        FichaClinica ficha = fichaClinicaService.buscarActiva(fichaId);
        Receta receta = new Receta();
        receta.setFichaClinica(ficha);
        receta.setUsuario(resolverUsuario(usuarioId));
        aplicarDatos(receta, request);
        return toResponse(recetaRepository.save(receta));
    }

    @Transactional
    public RecetaResponse actualizar(Long recetaId, RecetaRequest request) {
        Receta receta = buscarActiva(recetaId);
        aplicarDatos(receta, request);
        return toResponse(recetaRepository.save(receta));
    }

    @Transactional
    public void desactivar(Long recetaId) {
        Receta receta = buscarActiva(recetaId);
        receta.setActivo(false);
        recetaRepository.save(receta);
    }

    @Transactional(readOnly = true)
    public Receta buscarActiva(Long recetaId) {
        return recetaRepository.findById(recetaId)
                .filter(Receta::getActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Receta no encontrada."));
    }

    private void aplicarDatos(Receta receta, RecetaRequest request) {
        receta.setIndicacionesGenerales(normalizar(request.indicacionesGenerales()));
        receta.setObservaciones(normalizar(request.observaciones()));
        receta.setDetalles(mapearDetalles(request.detalles()));
    }

    private List<RecetaDetalle> mapearDetalles(List<RecetaDetalleRequest> detalles) {
        List<RecetaDetalle> items = new ArrayList<>();
        if (detalles == null) {
            return items;
        }
        int ordenPorDefecto = 0;
        for (RecetaDetalleRequest detalleRequest : detalles) {
            RecetaDetalle detalle = new RecetaDetalle();
            detalle.setMedicamento(normalizar(detalleRequest.medicamento()));
            detalle.setDosis(normalizar(detalleRequest.dosis()));
            detalle.setFrecuencia(normalizar(detalleRequest.frecuencia()));
            detalle.setDuracion(normalizar(detalleRequest.duracion()));
            detalle.setIndicaciones(normalizar(detalleRequest.indicaciones()));
            detalle.setOrden(detalleRequest.orden() == null ? ordenPorDefecto : detalleRequest.orden());
            items.add(detalle);
            ordenPorDefecto++;
        }
        return items;
    }

    private Usuario resolverUsuario(Long usuarioId) {
        if (usuarioId == null) {
            return null;
        }
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado."));
    }

    private String normalizar(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private RecetaResponse toResponse(Receta receta) {
        return new RecetaResponse(
                receta.getId(),
                receta.getFichaClinica().getId(),
                receta.getUsuario() == null ? null : receta.getUsuario().getId(),
                receta.getIndicacionesGenerales(),
                receta.getObservaciones(),
                receta.getActivo(),
                receta.getDetalles().stream()
                        .map(detalle -> new RecetaDetalleResponse(
                                detalle.getId(),
                                detalle.getMedicamento(),
                                detalle.getDosis(),
                                detalle.getFrecuencia(),
                                detalle.getDuracion(),
                                detalle.getIndicaciones(),
                                detalle.getOrden()
                        ))
                        .toList(),
                receta.getFechaCreacion(),
                receta.getFechaActualizacion()
        );
    }
}
