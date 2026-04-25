package com.example.odontologia_api.service;

import com.example.odontologia_api.dto.CitaRequest;
import com.example.odontologia_api.dto.CitaResponse;
import com.example.odontologia_api.dto.DisponibilidadResponse;
import com.example.odontologia_api.entity.Cita;
import com.example.odontologia_api.entity.HorarioAtencion;
import com.example.odontologia_api.entity.Paciente;
import com.example.odontologia_api.enums.EstadoCita;
import com.example.odontologia_api.exception.RecursoNoEncontradoException;
import com.example.odontologia_api.exception.ReglaNegocioException;
import com.example.odontologia_api.repository.CitaRepository;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CitaService {

    private static final EnumSet<EstadoCita> ESTADOS_LIBERAN_HORARIO =
            EnumSet.of(EstadoCita.CANCELADA, EstadoCita.NO_ASISTIO);
    private static final String CODIGO_CARACTERES = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final CitaRepository citaRepository;
    private final PacienteService pacienteService;
    private final HorarioAtencionService horarioAtencionService;

    public CitaService(
            CitaRepository citaRepository,
            PacienteService pacienteService,
            HorarioAtencionService horarioAtencionService
    ) {
        this.citaRepository = citaRepository;
        this.pacienteService = pacienteService;
        this.horarioAtencionService = horarioAtencionService;
    }

    @Transactional(readOnly = true)
    public List<CitaResponse> listar(LocalDate fecha) {
        if (fecha == null) {
            return citaRepository.findAllByOrderByFechaHoraInicioAsc()
                    .stream()
                    .map(this::toResponse)
                    .toList();
        }

        LocalDateTime inicio = fecha.atStartOfDay();
        LocalDateTime fin = fecha.plusDays(1).atStartOfDay();
        return citaRepository.findByFechaHoraInicioBetweenOrderByFechaHoraInicioAsc(inicio, fin)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CitaResponse obtener(Long id) {
        return toResponse(buscar(id));
    }

    @Transactional
    public CitaResponse crear(CitaRequest request) {
        Paciente paciente = resolverPaciente(request);
        HorarioAtencion horario = buscarHorarioCompatible(request.fechaHoraInicio());
        LocalDateTime fin = request.fechaHoraInicio().plusMinutes(horario.getDuracionCitaMinutos());
        validarRangoDentroDelHorario(request.fechaHoraInicio(), fin, horario);
        validarDisponibilidad(request.fechaHoraInicio(), fin, null);

        Cita cita = new Cita();
        cita.setPaciente(paciente);
        cita.setFechaHoraInicio(request.fechaHoraInicio());
        cita.setFechaHoraFin(fin);
        cita.setMotivo(request.motivo());
        cita.setNotas(request.notas());
        cita.setEstado(EstadoCita.PENDIENTE);
        cita.setCodigoGestion(generarCodigoGestion());
        return toResponse(citaRepository.save(cita));
    }

    @Transactional
    public CitaResponse cancelar(Long id, String codigoGestion) {
        Cita cita = buscarConCodigo(id, codigoGestion);
        if (cita.getEstado() == EstadoCita.ATENDIDA) {
            throw new ReglaNegocioException("No se puede cancelar una cita que ya fue atendida.");
        }
        cita.setEstado(EstadoCita.CANCELADA);
        return toResponse(citaRepository.save(cita));
    }

    @Transactional
    public CitaResponse reprogramar(Long id, String codigoGestion, LocalDateTime nuevaFechaHoraInicio) {
        Cita cita = buscarConCodigo(id, codigoGestion);
        if (cita.getEstado() == EstadoCita.ATENDIDA) {
            throw new ReglaNegocioException("No se puede reprogramar una cita que ya fue atendida.");
        }

        HorarioAtencion horario = buscarHorarioCompatible(nuevaFechaHoraInicio);
        LocalDateTime nuevoFin = nuevaFechaHoraInicio.plusMinutes(horario.getDuracionCitaMinutos());
        validarRangoDentroDelHorario(nuevaFechaHoraInicio, nuevoFin, horario);
        validarDisponibilidad(nuevaFechaHoraInicio, nuevoFin, cita.getId());

        cita.setFechaHoraInicio(nuevaFechaHoraInicio);
        cita.setFechaHoraFin(nuevoFin);
        cita.setEstado(EstadoCita.REPROGRAMADA);
        return toResponse(citaRepository.save(cita));
    }

    @Transactional(readOnly = true)
    public DisponibilidadResponse disponibilidad(LocalDate fecha) {
        List<LocalTime> disponibles = horarioAtencionService.listarPorDia(fecha.getDayOfWeek())
                .stream()
                .flatMap(horario -> generarBloquesDisponibles(fecha, horario).stream())
                .sorted()
                .toList();
        return new DisponibilidadResponse(fecha, disponibles);
    }

    private Paciente resolverPaciente(CitaRequest request) {
        if (request.pacienteId() != null) {
            return pacienteService.buscarActivo(request.pacienteId());
        }
        if (request.paciente() == null) {
            throw new ReglaNegocioException("Debe enviar pacienteId o los datos del paciente.");
        }
        return pacienteService.obtenerOCrearParaCita(request.paciente());
    }

    private HorarioAtencion buscarHorarioCompatible(LocalDateTime inicio) {
        return horarioAtencionService.listarPorDia(inicio.getDayOfWeek())
                .stream()
                .filter(horario -> !inicio.toLocalTime().isBefore(horario.getHoraInicio()))
                .filter(horario -> inicio.toLocalTime().isBefore(horario.getHoraFin()))
                .findFirst()
                .orElseThrow(() -> new ReglaNegocioException("No existe horario de atención para la fecha y hora solicitada."));
    }

    private void validarRangoDentroDelHorario(LocalDateTime inicio, LocalDateTime fin, HorarioAtencion horario) {
        if (fin.toLocalTime().isAfter(horario.getHoraFin())) {
            throw new ReglaNegocioException("La cita termina fuera del horario de atención.");
        }
    }

    private void validarDisponibilidad(LocalDateTime inicio, LocalDateTime fin, Long citaIdIgnorada) {
        boolean ocupado = citaRepository.existsSolapamiento(inicio, fin, ESTADOS_LIBERAN_HORARIO, citaIdIgnorada);
        if (ocupado) {
            throw new ReglaNegocioException("El horario solicitado ya se encuentra ocupado.");
        }
    }

    private List<LocalTime> generarBloquesDisponibles(LocalDate fecha, HorarioAtencion horario) {
        LocalDateTime cursor = LocalDateTime.of(fecha, horario.getHoraInicio());
        LocalDateTime limite = LocalDateTime.of(fecha, horario.getHoraFin());
        List<LocalTime> bloques = new java.util.ArrayList<>();

        while (!cursor.plusMinutes(horario.getDuracionCitaMinutos()).isAfter(limite)) {
            LocalDateTime finBloque = cursor.plusMinutes(horario.getDuracionCitaMinutos());
            if (!citaRepository.existsSolapamiento(cursor, finBloque, ESTADOS_LIBERAN_HORARIO, null)) {
                bloques.add(cursor.toLocalTime());
            }
            cursor = finBloque;
        }
        return bloques;
    }

    private Cita buscar(Long id) {
        return citaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cita no encontrada."));
    }

    private Cita buscarConCodigo(Long id, String codigoGestion) {
        return citaRepository.findByIdAndCodigoGestion(id, codigoGestion)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cita no encontrada o código de gestión inválido."));
    }

    private String generarCodigoGestion() {
        StringBuilder codigo = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            codigo.append(CODIGO_CARACTERES.charAt(RANDOM.nextInt(CODIGO_CARACTERES.length())));
        }
        return codigo.toString();
    }

    private CitaResponse toResponse(Cita cita) {
        return new CitaResponse(
                cita.getId(),
                pacienteService.toResponse(cita.getPaciente()),
                cita.getFechaHoraInicio(),
                cita.getFechaHoraFin(),
                cita.getMotivo(),
                cita.getEstado(),
                cita.getCodigoGestion(),
                cita.getNotas(),
                cita.getFechaCreacion(),
                cita.getFechaActualizacion()
        );
    }
}
