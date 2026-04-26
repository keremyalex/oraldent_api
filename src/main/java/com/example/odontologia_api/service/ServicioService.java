package com.example.odontologia_api.service;

import com.example.odontologia_api.dto.ServicioRequest;
import com.example.odontologia_api.dto.ServicioResponse;
import com.example.odontologia_api.entity.Servicio;
import com.example.odontologia_api.exception.RecursoNoEncontradoException;
import com.example.odontologia_api.exception.ReglaNegocioException;
import com.example.odontologia_api.repository.ServicioRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServicioService {

    private final ServicioRepository servicioRepository;

    public ServicioService(ServicioRepository servicioRepository) {
        this.servicioRepository = servicioRepository;
    }

    @Transactional(readOnly = true)
    public List<ServicioResponse> listarActivos() {
        return servicioRepository.findByActivoTrueOrderByNombreAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ServicioResponse obtener(Long id) {
        return toResponse(buscarActivo(id));
    }

    @Transactional
    public ServicioResponse crear(ServicioRequest request) {
        validarNombreDuplicado(request.nombre(), null);

        Servicio servicio = new Servicio();
        aplicarDatos(servicio, request);
        return toResponse(servicioRepository.save(servicio));
    }

    @Transactional
    public ServicioResponse actualizar(Long id, ServicioRequest request) {
        Servicio servicio = buscarActivo(id);
        validarNombreDuplicado(request.nombre(), id);
        aplicarDatos(servicio, request);
        return toResponse(servicioRepository.save(servicio));
    }

    @Transactional
    public void desactivar(Long id) {
        Servicio servicio = buscarActivo(id);
        servicio.setActivo(false);
        servicioRepository.save(servicio);
    }

    @Transactional(readOnly = true)
    public Servicio buscarActivo(Long id) {
        return servicioRepository.findById(id)
                .filter(Servicio::getActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Servicio no encontrado."));
    }

    public ServicioResponse toResponse(Servicio servicio) {
        return new ServicioResponse(
                servicio.getId(),
                servicio.getNombre(),
                servicio.getDescripcion(),
                servicio.getActivo(),
                servicio.getFechaCreacion(),
                servicio.getFechaActualizacion()
        );
    }

    private void aplicarDatos(Servicio servicio, ServicioRequest request) {
        servicio.setNombre(request.nombre().trim());
        servicio.setDescripcion(request.descripcion() == null ? null : request.descripcion().trim());
    }

    private void validarNombreDuplicado(String nombre, Long servicioIdActual) {
        servicioRepository.findByNombreIgnoreCase(nombre.trim())
                .filter(Servicio::getActivo)
                .filter(servicio -> servicioIdActual == null || !servicio.getId().equals(servicioIdActual))
                .ifPresent(servicio -> {
                    throw new ReglaNegocioException("Ya existe un servicio activo con ese nombre.");
                });
    }
}
