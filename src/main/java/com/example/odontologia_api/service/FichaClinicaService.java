package com.example.odontologia_api.service;

import com.example.odontologia_api.dto.FichaClinicaRequest;
import com.example.odontologia_api.dto.FichaClinicaResponse;
import com.example.odontologia_api.entity.Cita;
import com.example.odontologia_api.entity.FichaClinica;
import com.example.odontologia_api.entity.Odontograma;
import com.example.odontologia_api.entity.Paciente;
import com.example.odontologia_api.entity.Periodontograma;
import com.example.odontologia_api.entity.Usuario;
import com.example.odontologia_api.exception.RecursoNoEncontradoException;
import com.example.odontologia_api.exception.ReglaNegocioException;
import com.example.odontologia_api.repository.CitaRepository;
import com.example.odontologia_api.repository.FichaClinicaRepository;
import com.example.odontologia_api.repository.OdontogramaRepository;
import com.example.odontologia_api.repository.PeriodontogramaRepository;
import com.example.odontologia_api.repository.UsuarioRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FichaClinicaService {

    private final FichaClinicaRepository fichaClinicaRepository;
    private final UsuarioRepository usuarioRepository;
    private final CitaRepository citaRepository;
    private final OdontogramaRepository odontogramaRepository;
    private final PeriodontogramaRepository periodontogramaRepository;
    private final PacienteService pacienteService;

    public FichaClinicaService(
            FichaClinicaRepository fichaClinicaRepository,
            UsuarioRepository usuarioRepository,
            CitaRepository citaRepository,
            OdontogramaRepository odontogramaRepository,
            PeriodontogramaRepository periodontogramaRepository,
            PacienteService pacienteService
    ) {
        this.fichaClinicaRepository = fichaClinicaRepository;
        this.usuarioRepository = usuarioRepository;
        this.citaRepository = citaRepository;
        this.odontogramaRepository = odontogramaRepository;
        this.periodontogramaRepository = periodontogramaRepository;
        this.pacienteService = pacienteService;
    }

    @Transactional(readOnly = true)
    public List<FichaClinicaResponse> listarPorPaciente(Long pacienteId) {
        Paciente paciente = pacienteService.buscarActivo(pacienteId);
        return fichaClinicaRepository.findByPacienteAndActivoTrueOrderByFechaDescIdDesc(paciente).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public FichaClinicaResponse obtener(Long fichaId) {
        return toResponse(buscarActiva(fichaId));
    }

    @Transactional
    public FichaClinicaResponse crear(Long pacienteId, Long usuarioId, FichaClinicaRequest request) {
        Paciente paciente = pacienteService.buscarActivo(pacienteId);
        FichaClinica ficha = new FichaClinica();
        ficha.setPaciente(paciente);
        ficha.setUsuario(resolverUsuario(usuarioId));
        aplicarDatos(ficha, request);
        return toResponse(fichaClinicaRepository.save(ficha));
    }

    @Transactional
    public FichaClinicaResponse actualizar(Long fichaId, FichaClinicaRequest request) {
        FichaClinica ficha = buscarActiva(fichaId);
        aplicarDatos(ficha, request);
        return toResponse(fichaClinicaRepository.save(ficha));
    }

    @Transactional
    public void desactivar(Long fichaId) {
        FichaClinica ficha = buscarActiva(fichaId);
        ficha.setActivo(false);
        fichaClinicaRepository.save(ficha);
    }

    @Transactional(readOnly = true)
    public FichaClinica buscarActiva(Long fichaId) {
        return fichaClinicaRepository.findById(fichaId)
                .filter(FichaClinica::getActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Ficha clinica no encontrada."));
    }

    private void aplicarDatos(FichaClinica ficha, FichaClinicaRequest request) {
        ficha.setFecha(request.fecha() == null
                ? (ficha.getFecha() == null ? LocalDateTime.now() : ficha.getFecha())
                : request.fecha());
        ficha.setCita(resolverCita(request.citaId(), ficha.getPaciente()));
        ficha.setEdad(request.edad());
        ficha.setSexo(normalizar(request.sexo()));
        ficha.setProcedencia(normalizar(request.procedencia()));
        ficha.setOcupacion(normalizar(request.ocupacion()));
        ficha.setPresionArterial(normalizar(request.presionArterial()));
        ficha.setTemperatura(request.temperatura());
        ficha.setPulso(request.pulso());
        ficha.setMotivoConsulta(normalizar(request.motivoConsulta()));
        ficha.setEnfermedadActual(normalizar(request.enfermedadActual()));
        ficha.setAnamnesis(normalizar(request.anamnesis()));
        ficha.setHemorragia(valor(request.hemorragia()));
        ficha.setDiabetes(valor(request.diabetes()));
        ficha.setHipertension(valor(request.hipertension()));
        ficha.setEpilepsia(valor(request.epilepsia()));
        ficha.setProblemasCardiovasculares(valor(request.problemasCardiovasculares()));
        ficha.setLipotimias(valor(request.lipotimias()));
        ficha.setTratamientoMedicoActual(valor(request.tratamientoMedicoActual()));
        ficha.setAlergias(normalizar(request.alergias()));
        ficha.setMedicamentoActual(normalizar(request.medicamentoActual()));
        ficha.setOtrasPatologias(normalizar(request.otrasPatologias()));
        ficha.setExamenClinico(normalizar(request.examenClinico()));
        ficha.setExamenRadiografico(normalizar(request.examenRadiografico()));
        ficha.setDiagnostico(normalizar(request.diagnostico()));
        ficha.setTratamiento(normalizar(request.tratamiento()));
        ficha.setTecnicaAnestesia(normalizar(request.tecnicaAnestesia()));
        ficha.setEvolucion(normalizar(request.evolucion()));
    }

    private Usuario resolverUsuario(Long usuarioId) {
        if (usuarioId == null) {
            return null;
        }
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado."));
    }

    private Cita resolverCita(Long citaId, Paciente paciente) {
        if (citaId == null) {
            return null;
        }
        Cita cita = citaRepository.findById(citaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cita no encontrada."));
        if (!cita.getPaciente().getId().equals(paciente.getId())) {
            throw new ReglaNegocioException("La cita no pertenece al paciente de la ficha.");
        }
        return cita;
    }

    private String normalizar(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private Boolean valor(Boolean value) {
        return Boolean.TRUE.equals(value);
    }

    private FichaClinicaResponse toResponse(FichaClinica ficha) {
        Long odontogramaId = odontogramaRepository
                .findFirstByFichaClinicaAndActivoTrueOrderByIdDesc(ficha)
                .map(Odontograma::getId)
                .orElse(null);
        Long periodontogramaId = periodontogramaRepository
                .findFirstByFichaClinicaAndActivoTrueOrderByIdDesc(ficha)
                .map(Periodontograma::getId)
                .orElse(null);

        return new FichaClinicaResponse(
                ficha.getId(),
                pacienteService.toResponse(ficha.getPaciente()),
                ficha.getUsuario() == null ? null : ficha.getUsuario().getId(),
                ficha.getCita() == null ? null : ficha.getCita().getId(),
                ficha.getFecha(),
                ficha.getEdad(),
                ficha.getSexo(),
                ficha.getProcedencia(),
                ficha.getOcupacion(),
                ficha.getPresionArterial(),
                ficha.getTemperatura(),
                ficha.getPulso(),
                ficha.getMotivoConsulta(),
                ficha.getEnfermedadActual(),
                ficha.getAnamnesis(),
                ficha.getHemorragia(),
                ficha.getDiabetes(),
                ficha.getHipertension(),
                ficha.getEpilepsia(),
                ficha.getProblemasCardiovasculares(),
                ficha.getLipotimias(),
                ficha.getTratamientoMedicoActual(),
                ficha.getAlergias(),
                ficha.getMedicamentoActual(),
                ficha.getOtrasPatologias(),
                ficha.getExamenClinico(),
                ficha.getExamenRadiografico(),
                ficha.getDiagnostico(),
                ficha.getTratamiento(),
                ficha.getTecnicaAnestesia(),
                ficha.getEvolucion(),
                ficha.getActivo(),
                odontogramaId,
                periodontogramaId,
                ficha.getFechaCreacion(),
                ficha.getFechaActualizacion()
        );
    }
}
