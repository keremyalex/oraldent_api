package com.example.odontologia_api.service;

import com.example.odontologia_api.dto.DoctorPerfilRequest;
import com.example.odontologia_api.dto.DoctorPerfilResponse;
import com.example.odontologia_api.entity.DoctorPerfil;
import com.example.odontologia_api.exception.RecursoNoEncontradoException;
import com.example.odontologia_api.repository.DoctorPerfilRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DoctorPerfilService {

    private final DoctorPerfilRepository doctorPerfilRepository;

    public DoctorPerfilService(DoctorPerfilRepository doctorPerfilRepository) {
        this.doctorPerfilRepository = doctorPerfilRepository;
    }

    @Transactional(readOnly = true)
    public List<DoctorPerfilResponse> listar() {
        return doctorPerfilRepository.findByActivoTrueOrderByIdAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DoctorPerfilResponse obtener(Long id) {
        return toResponse(buscarActivo(id));
    }

    @Transactional
    public DoctorPerfilResponse crear(DoctorPerfilRequest request) {
        DoctorPerfil perfil = new DoctorPerfil();
        aplicarDatos(perfil, request);
        return toResponse(doctorPerfilRepository.save(perfil));
    }

    @Transactional
    public DoctorPerfilResponse actualizar(Long id, DoctorPerfilRequest request) {
        DoctorPerfil perfil = buscarActivo(id);
        aplicarDatos(perfil, request);
        return toResponse(doctorPerfilRepository.save(perfil));
    }

    @Transactional
    public void desactivar(Long id) {
        DoctorPerfil perfil = buscarActivo(id);
        perfil.setActivo(false);
        doctorPerfilRepository.save(perfil);
    }

    private DoctorPerfil buscarActivo(Long id) {
        return doctorPerfilRepository.findById(id)
                .filter(DoctorPerfil::getActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Perfil del doctor no encontrado."));
    }

    private void aplicarDatos(DoctorPerfil perfil, DoctorPerfilRequest request) {
        perfil.setNombre(request.nombre());
        perfil.setApellidoPaterno(request.apellidoPaterno());
        perfil.setApellidoMaterno(request.apellidoMaterno());
        perfil.setEspecialidad(request.especialidad());
        perfil.setTelefono(request.telefono());
        perfil.setCorreo(request.correo());
        perfil.setDireccionConsultorio(request.direccionConsultorio());
        perfil.setDescripcion(request.descripcion());
    }

    private DoctorPerfilResponse toResponse(DoctorPerfil perfil) {
        return new DoctorPerfilResponse(
                perfil.getId(),
                perfil.getNombre(),
                perfil.getApellidoPaterno(),
                perfil.getApellidoMaterno(),
                perfil.getEspecialidad(),
                perfil.getTelefono(),
                perfil.getCorreo(),
                perfil.getDireccionConsultorio(),
                perfil.getDescripcion(),
                perfil.getActivo(),
                perfil.getFechaCreacion(),
                perfil.getFechaActualizacion()
        );
    }
}
