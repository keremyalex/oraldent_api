package com.example.odontologia_api.service;

import com.example.odontologia_api.dto.CloudinaryUploadResult;
import com.example.odontologia_api.dto.PacienteRequest;
import com.example.odontologia_api.dto.PacienteResponse;
import com.example.odontologia_api.dto.RegisterPacienteRequest;
import com.example.odontologia_api.entity.Paciente;
import com.example.odontologia_api.exception.RecursoNoEncontradoException;
import com.example.odontologia_api.repository.PacienteRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PacienteService {

    private final PacienteRepository pacienteRepository;
    private final CloudinaryService cloudinaryService;

    public PacienteService(PacienteRepository pacienteRepository, CloudinaryService cloudinaryService) {
        this.pacienteRepository = pacienteRepository;
        this.cloudinaryService = cloudinaryService;
    }

    @Transactional(readOnly = true)
    public List<PacienteResponse> listar() {
        return pacienteRepository.findByActivoTrueOrderByApellidoPaternoAscApellidoMaternoAscNombreAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PacienteResponse obtener(Long id) {
        return toResponse(buscarActivo(id));
    }

    @Transactional
    public PacienteResponse crear(PacienteRequest request) {
        Paciente paciente = new Paciente();
        aplicarDatos(paciente, request);
        return toResponse(pacienteRepository.save(paciente));
    }

    @Transactional
    public PacienteResponse actualizar(Long id, PacienteRequest request) {
        Paciente paciente = buscarActivo(id);
        aplicarDatos(paciente, request);
        return toResponse(pacienteRepository.save(paciente));
    }

    @Transactional
    public void desactivar(Long id) {
        Paciente paciente = buscarActivo(id);
        paciente.setActivo(false);
        pacienteRepository.save(paciente);
    }

    @Transactional
    public PacienteResponse actualizarFoto(Long id, MultipartFile archivo) {
        Paciente paciente = buscarActivo(id);
        CloudinaryUploadResult uploadResult = cloudinaryService.replaceImage(
                archivo,
                "oraldent/pacientes",
                paciente.getFotoPublicId()
        );
        paciente.setFotoUrl(uploadResult.url());
        paciente.setFotoPublicId(uploadResult.publicId());
        return toResponse(pacienteRepository.save(paciente));
    }

    @Transactional(readOnly = true)
    public Paciente buscarActivo(Long id) {
        return pacienteRepository.findById(id)
                .filter(Paciente::getActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Paciente no encontrado."));
    }

    @Transactional
    public Paciente obtenerOCrearParaCita(PacienteRequest request) {
        return pacienteRepository.findFirstByCelularAndActivoTrueOrderByIdDesc(request.celular())
                .map(paciente -> {
                    aplicarDatos(paciente, request);
                    return pacienteRepository.save(paciente);
                })
                .orElseGet(() -> {
                    Paciente paciente = new Paciente();
                    aplicarDatos(paciente, request);
                    return pacienteRepository.save(paciente);
                });
    }

    @Transactional
    public Paciente obtenerOCrearParaUsuario(RegisterPacienteRequest request) {
        Paciente paciente = buscarPorCelularOCorreo(request.celular(), request.correo())
                .orElseGet(Paciente::new);
        paciente.setNombre(request.nombre());
        paciente.setApellidoPaterno(request.apellidoPaterno());
        paciente.setApellidoMaterno(request.apellidoMaterno());
        paciente.setCelular(request.celular());
        paciente.setCorreo(request.correo());
        paciente.setDocumentoIdentidad(request.documentoIdentidad());
        paciente.setFechaNacimiento(request.fechaNacimiento());
        paciente.setDireccion(request.direccion());
        paciente.setFotoUrl(request.fotoUrl());
        paciente.setActivo(true);
        return pacienteRepository.save(paciente);
    }

    @Transactional(readOnly = true)
    public java.util.Optional<Paciente> buscarPorCelularOCorreo(String celular, String correo) {
        if (correo != null && !correo.isBlank()) {
            java.util.Optional<Paciente> porCorreo = pacienteRepository
                    .findFirstByCorreoIgnoreCaseAndActivoTrueOrderByIdDesc(correo);
            if (porCorreo.isPresent()) {
                return porCorreo;
            }
        }
        return pacienteRepository.findFirstByCelularAndActivoTrueOrderByIdDesc(celular);
    }

    public PacienteResponse toResponse(Paciente paciente) {
        return new PacienteResponse(
                paciente.getId(),
                paciente.getNombre(),
                paciente.getApellidoPaterno(),
                paciente.getApellidoMaterno(),
                paciente.getCelular(),
                paciente.getDocumentoIdentidad(),
                paciente.getCorreo(),
                paciente.getFechaNacimiento(),
                paciente.getDireccion(),
                paciente.getFotoUrl(),
                paciente.getActivo(),
                paciente.getFechaCreacion(),
                paciente.getFechaActualizacion()
        );
    }

    private void aplicarDatos(Paciente paciente, PacienteRequest request) {
        paciente.setNombre(request.nombre());
        paciente.setApellidoPaterno(request.apellidoPaterno());
        paciente.setApellidoMaterno(request.apellidoMaterno());
        paciente.setCelular(request.celular());
        paciente.setDocumentoIdentidad(request.documentoIdentidad());
        paciente.setCorreo(request.correo());
        paciente.setFechaNacimiento(request.fechaNacimiento());
        paciente.setDireccion(request.direccion());
        paciente.setFotoUrl(request.fotoUrl());
    }
}
