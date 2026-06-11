package com.example.odontologia_api.service;

import com.example.odontologia_api.dto.CitaRequest;
import com.example.odontologia_api.dto.CitaResponse;
import com.example.odontologia_api.dto.FichaClinicaResponse;
import com.example.odontologia_api.dto.PacienteResponse;
import com.example.odontologia_api.dto.PortalPacienteAccessRequest;
import com.example.odontologia_api.dto.PortalPacienteAccessResponse;
import com.example.odontologia_api.dto.RadiografiaResponse;
import com.example.odontologia_api.dto.RecetaResponse;
import com.example.odontologia_api.entity.FichaClinica;
import com.example.odontologia_api.entity.Odontograma;
import com.example.odontologia_api.entity.Paciente;
import com.example.odontologia_api.entity.Periodontograma;
import com.example.odontologia_api.entity.Radiografia;
import com.example.odontologia_api.entity.Receta;
import com.example.odontologia_api.exception.RecursoNoEncontradoException;
import com.example.odontologia_api.exception.ReglaNegocioException;
import com.example.odontologia_api.repository.CitaRepository;
import com.example.odontologia_api.repository.FichaClinicaRepository;
import com.example.odontologia_api.repository.PacienteRepository;
import com.example.odontologia_api.repository.RadiografiaRepository;
import com.example.odontologia_api.repository.RecetaRepository;
import com.example.odontologia_api.security.PortalPacienteTokenService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PortalPacienteService {

    private final PacienteRepository pacienteRepository;
    private final CitaRepository citaRepository;
    private final FichaClinicaRepository fichaClinicaRepository;
    private final RecetaRepository recetaRepository;
    private final RadiografiaRepository radiografiaRepository;
    private final PacienteService pacienteService;
    private final CitaService citaService;
    private final FichaClinicaService fichaClinicaService;
    private final RecetaService recetaService;
    private final RadiografiaService radiografiaService;
    private final RecetaPdfService recetaPdfService;
    private final OdontogramaPdfService odontogramaPdfService;
    private final PeriodontogramaPdfService periodontogramaPdfService;
    private final PortalPacienteTokenService tokenService;

    public PortalPacienteService(
            PacienteRepository pacienteRepository,
            CitaRepository citaRepository,
            FichaClinicaRepository fichaClinicaRepository,
            RecetaRepository recetaRepository,
            RadiografiaRepository radiografiaRepository,
            PacienteService pacienteService,
            CitaService citaService,
            FichaClinicaService fichaClinicaService,
            RecetaService recetaService,
            RadiografiaService radiografiaService,
            RecetaPdfService recetaPdfService,
            OdontogramaPdfService odontogramaPdfService,
            PeriodontogramaPdfService periodontogramaPdfService,
            PortalPacienteTokenService tokenService
    ) {
        this.pacienteRepository = pacienteRepository;
        this.citaRepository = citaRepository;
        this.fichaClinicaRepository = fichaClinicaRepository;
        this.recetaRepository = recetaRepository;
        this.radiografiaRepository = radiografiaRepository;
        this.pacienteService = pacienteService;
        this.citaService = citaService;
        this.fichaClinicaService = fichaClinicaService;
        this.recetaService = recetaService;
        this.radiografiaService = radiografiaService;
        this.recetaPdfService = recetaPdfService;
        this.odontogramaPdfService = odontogramaPdfService;
        this.periodontogramaPdfService = periodontogramaPdfService;
        this.tokenService = tokenService;
    }

    @Transactional
    public PortalPacienteAccessResponse acceder(PortalPacienteAccessRequest request) {
        Paciente paciente = pacienteRepository
                .findFirstByCodigoPacienteIgnoreCaseAndActivoTrue(request.codigoPaciente().trim())
                .orElseThrow(() -> new RecursoNoEncontradoException("Paciente no encontrado."));

        if (!normalizarAcceso(paciente.getDocumentoIdentidad()).equals(normalizarAcceso(request.documentoIdentidad()))) {
            throw new ReglaNegocioException("Los datos de acceso no coinciden.");
        }

        Paciente pacienteConCodigo = asegurarCodigo(paciente);
        String token = tokenService.generateToken(pacienteConCodigo.getId(), pacienteConCodigo.getCodigoPaciente());
        return new PortalPacienteAccessResponse(token, "Bearer", pacienteService.toResponse(pacienteConCodigo));
    }

    @Transactional(readOnly = true)
    public PacienteResponse perfil(Long pacienteId) {
        return pacienteService.toResponse(buscarPacienteActivo(pacienteId));
    }

    @Transactional
    public CitaResponse crearCita(Long pacienteId, CitaRequest request) {
        Paciente paciente = buscarPacienteActivo(pacienteId);
        CitaRequest requestDelPaciente = new CitaRequest(
                paciente.getId(),
                null,
                request.servicioId(),
                request.fechaHoraInicio(),
                request.motivo(),
                request.notas()
        );
        return citaService.crear(requestDelPaciente, null);
    }

    @Transactional(readOnly = true)
    public List<CitaResponse> citas(Long pacienteId) {
        Paciente paciente = buscarPacienteActivo(pacienteId);
        return citaRepository.findByPacienteOrderByFechaHoraInicioDescIdDesc(paciente)
                .stream()
                .map(citaService::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FichaClinicaResponse> fichas(Long pacienteId) {
        Paciente paciente = buscarPacienteActivo(pacienteId);
        return fichaClinicaRepository.findByPacienteAndActivoTrueOrderByFechaDescIdDesc(paciente)
                .stream()
                .map(ficha -> fichaClinicaService.obtener(ficha.getId()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RecetaResponse> recetasPorFicha(Long pacienteId, Long fichaId) {
        FichaClinica ficha = buscarFichaDelPaciente(pacienteId, fichaId);
        return recetaRepository.findByFichaClinicaAndActivoTrueOrderByFechaCreacionDescIdDesc(ficha)
                .stream()
                .map(receta -> recetaService.obtener(receta.getId()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RadiografiaResponse> radiografiasPorFicha(Long pacienteId, Long fichaId) {
        FichaClinica ficha = buscarFichaDelPaciente(pacienteId, fichaId);
        return radiografiaRepository.findByFichaClinicaAndActivoTrueOrderByFechaEstudioDescFechaCreacionDescIdDesc(ficha)
                .stream()
                .map(radiografia -> radiografiaService.obtener(radiografia.getId()))
                .toList();
    }

    @Transactional(readOnly = true)
    public byte[] recetaPdf(Long pacienteId, Long recetaId) {
        Receta receta = recetaService.buscarActiva(recetaId);
        Long recetaPacienteId = receta.getFichaClinica().getPaciente().getId();
        if (!recetaPacienteId.equals(pacienteId)) {
            throw new RecursoNoEncontradoException("Receta no encontrada.");
        }
        return recetaPdfService.generarPdf(recetaId);
    }

    @Transactional(readOnly = true)
    public byte[] odontogramaPdf(Long pacienteId, Long odontogramaId) {
        Odontograma odontograma = odontogramaPdfService.buscarActivo(odontogramaId);
        if (!odontograma.getFichaClinica().getPaciente().getId().equals(pacienteId)) {
            throw new RecursoNoEncontradoException("Odontograma no encontrado.");
        }
        return odontogramaPdfService.generarPdf(odontogramaId);
    }

    @Transactional(readOnly = true)
    public byte[] periodontogramaPdf(Long pacienteId, Long periodontogramaId) {
        Periodontograma periodontograma = periodontogramaPdfService.buscarActivo(periodontogramaId);
        if (!periodontograma.getFichaClinica().getPaciente().getId().equals(pacienteId)) {
            throw new RecursoNoEncontradoException("Periodontograma no encontrado.");
        }
        return periodontogramaPdfService.generarPdf(periodontogramaId);
    }

    private Paciente buscarPacienteActivo(Long pacienteId) {
        return pacienteRepository.findById(pacienteId)
                .filter(Paciente::getActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Paciente no encontrado."));
    }

    private FichaClinica buscarFichaDelPaciente(Long pacienteId, Long fichaId) {
        FichaClinica ficha = fichaClinicaService.buscarActiva(fichaId);
        if (!ficha.getPaciente().getId().equals(pacienteId)) {
            throw new RecursoNoEncontradoException("Ficha clinica no encontrada.");
        }
        return ficha;
    }

    private String normalizarAcceso(String value) {
        return value == null ? "" : value.trim().toUpperCase();
    }

    private Paciente asegurarCodigo(Paciente paciente) {
        if (paciente.getCodigoPaciente() != null && !paciente.getCodigoPaciente().isBlank()) {
            return paciente;
        }
        return pacienteRepository.save(paciente);
    }
}


