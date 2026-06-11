package com.example.odontologia_api.service;
import com.example.odontologia_api.dto.CloudinaryUploadResult;
import com.example.odontologia_api.dto.PacienteRequest;
import com.example.odontologia_api.dto.PacienteResponse;
import com.example.odontologia_api.dto.RegisterPacienteRequest;
import com.example.odontologia_api.entity.Paciente;
import com.example.odontologia_api.exception.RecursoNoEncontradoException;
import com.example.odontologia_api.exception.ReglaNegocioException;
import com.example.odontologia_api.repository.PacienteRepository;
import java.util.List;
import java.util.UUID;
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
    @Transactional
    public List<PacienteResponse> listar() {
        return pacienteRepository.findByActivoTrueOrderByApellidoPaternoAscApellidoMaternoAscNombreAsc()
                .stream()
                .map(this::asegurarCodigoPaciente)
                .map(this::toResponse)
                .toList();
    }
    @Transactional
    public PacienteResponse obtener(Long id) {
        return toResponse(asegurarCodigoPaciente(buscarActivo(id)));
    }
    @Transactional
    public PacienteResponse crear(PacienteRequest request) {
        Paciente paciente = new Paciente();
        validarDocumentoDisponible(request.documentoIdentidad(), null);
        aplicarDatos(paciente, request);
        asegurarCodigoPaciente(paciente);
        paciente = pacienteRepository.save(paciente);
        return toResponse(paciente);
    }
    @Transactional
    public PacienteResponse actualizar(Long id, PacienteRequest request) {
        Paciente paciente = buscarActivo(id);
        validarDocumentoDisponible(request.documentoIdentidad(), id);
        aplicarDatos(paciente, request);
        asegurarCodigoPaciente(paciente);
        paciente = pacienteRepository.save(paciente);
        return toResponse(paciente);
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
        asegurarCodigoPaciente(paciente);
        paciente = pacienteRepository.save(paciente);
        return toResponse(paciente);
    }
    @Transactional(readOnly = true)
    public Paciente buscarActivo(Long id) {
        return pacienteRepository.findById(id)
                .filter(Paciente::getActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Paciente no encontrado."));
    }
    @Transactional
    public Paciente obtenerOCrearParaCita(PacienteRequest request) {
        Paciente paciente = pacienteRepository.findFirstByCelularAndActivoTrueOrderByIdDesc(request.celular())
                .map(existente -> {
                    validarDocumentoDisponible(request.documentoIdentidad(), existente.getId());
                    aplicarDatos(existente, request);
                    asegurarCodigoPaciente(existente);
                    return pacienteRepository.save(existente);
                })
                .orElseGet(() -> {
                    validarDocumentoDisponible(request.documentoIdentidad(), null);
                    Paciente nuevo = new Paciente();
                    aplicarDatos(nuevo, request);
                    asegurarCodigoPaciente(nuevo);
                    return pacienteRepository.save(nuevo);
                });
        return paciente;
    }
    @Transactional
    public Paciente obtenerOCrearParaUsuario(RegisterPacienteRequest request) {
        Paciente paciente = buscarPorCelularOCorreo(request.celular(), request.correo())
                .orElseGet(Paciente::new);
        validarDocumentoDisponible(request.documentoIdentidad(), paciente.getId());
        paciente.setNombre(request.nombre());
        paciente.setApellidoPaterno(request.apellidoPaterno());
        paciente.setApellidoMaterno(request.apellidoMaterno());
        paciente.setCelular(request.celular());
        paciente.setCorreo(request.correo());
        paciente.setDocumentoIdentidad(normalizarDocumento(request.documentoIdentidad()));
        paciente.setFechaNacimiento(request.fechaNacimiento());
        paciente.setDireccion(request.direccion());
        paciente.setFotoUrl(request.fotoUrl());
        paciente.setActivo(true);
        asegurarCodigoPaciente(paciente);
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
                paciente.getCodigoPaciente(),
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
        paciente.setDocumentoIdentidad(normalizarDocumento(request.documentoIdentidad()));
        paciente.setCorreo(request.correo());
        paciente.setFechaNacimiento(request.fechaNacimiento());
        paciente.setDireccion(request.direccion());
        paciente.setFotoUrl(request.fotoUrl());
    }
    private void validarDocumentoDisponible(String documentoIdentidad, Long pacienteIdActual) {
        String documento = normalizarDocumento(documentoIdentidad);
        if (documento == null) {
            throw new ReglaNegocioException("El documento de identidad del paciente es obligatorio.");
        }
        pacienteRepository.findFirstByDocumentoIdentidadIgnoreCase(documento)
                .filter(existente -> pacienteIdActual == null || !existente.getId().equals(pacienteIdActual))
                .ifPresent(existente -> {
                    throw new ReglaNegocioException("Ya existe un paciente con ese documento de identidad.");
                });
    }

    private String normalizarDocumento(String documentoIdentidad) {
        if (documentoIdentidad == null || documentoIdentidad.isBlank()) {
            return null;
        }
        return documentoIdentidad.trim().toUpperCase();
    }

    private Paciente asegurarCodigoPaciente(Paciente paciente) {
        if (paciente.getCodigoPaciente() != null && !paciente.getCodigoPaciente().isBlank()) {
            return paciente;
        }
        if (paciente.getId() != null) {
            String codigoPorId = formatearCodigoPaciente(paciente.getId());
            if (!pacienteRepository.existsByCodigoPacienteIgnoreCase(codigoPorId)) {
                paciente.setCodigoPaciente(codigoPorId);
                return paciente;
            }
        }
        paciente.setCodigoPaciente(generarCodigoPacienteTemporal());
        return paciente;
    }
    private String formatearCodigoPaciente(Long id) {
        return "PAC-" + String.format("%06d", id);
    }
    private String generarCodigoPacienteTemporal() {
        String codigo;
        do {
            codigo = "PAC-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        } while (pacienteRepository.existsByCodigoPacienteIgnoreCase(codigo));
        return codigo;
    }
}


