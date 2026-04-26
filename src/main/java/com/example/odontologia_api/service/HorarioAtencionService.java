package com.example.odontologia_api.service;

import com.example.odontologia_api.dto.HorarioAtencionRequest;
import com.example.odontologia_api.dto.HorarioAtencionResponse;
import com.example.odontologia_api.entity.HorarioAtencion;
import com.example.odontologia_api.enums.DiaSemana;
import com.example.odontologia_api.exception.RecursoNoEncontradoException;
import com.example.odontologia_api.exception.ReglaNegocioException;
import com.example.odontologia_api.repository.HorarioAtencionRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HorarioAtencionService {

    private final HorarioAtencionRepository horarioRepository;

    public HorarioAtencionService(HorarioAtencionRepository horarioRepository) {
        this.horarioRepository = horarioRepository;
    }

    @Transactional(readOnly = true)
    public List<HorarioAtencionResponse> listar() {
        return horarioRepository.findByActivoTrueOrderByDiaSemanaAscHoraInicioAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<HorarioAtencion> listarPorDia(DiaSemana diaSemana) {
        return horarioRepository.findActivosPorDiaSemanaValoresOrderByHoraInicioAsc(
                List.of(diaSemana.name(), diaSemana.legacyValue())
        );
    }

    @Transactional
    public HorarioAtencionResponse crear(HorarioAtencionRequest request) {
        HorarioAtencion horario = new HorarioAtencion();
        aplicarDatos(horario, request);
        return toResponse(horarioRepository.save(horario));
    }

    @Transactional
    public HorarioAtencionResponse actualizar(Long id, HorarioAtencionRequest request) {
        HorarioAtencion horario = buscarActivo(id);
        aplicarDatos(horario, request);
        return toResponse(horarioRepository.save(horario));
    }

    @Transactional
    public void desactivar(Long id) {
        HorarioAtencion horario = buscarActivo(id);
        horario.setActivo(false);
        horarioRepository.save(horario);
    }

    private HorarioAtencion buscarActivo(Long id) {
        return horarioRepository.findById(id)
                .filter(HorarioAtencion::getActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Horario de atención no encontrado."));
    }

    private void aplicarDatos(HorarioAtencion horario, HorarioAtencionRequest request) {
        if (!request.horaInicio().isBefore(request.horaFin())) {
            throw new ReglaNegocioException("La hora de inicio debe ser menor a la hora de fin.");
        }
        horario.setDiaSemana(request.diaSemana());
        horario.setHoraInicio(request.horaInicio());
        horario.setHoraFin(request.horaFin());
        horario.setDuracionCitaMinutos(request.duracionCitaMinutos());
        horario.setObservacion(request.observacion());
    }

    public HorarioAtencionResponse toResponse(HorarioAtencion horario) {
        return new HorarioAtencionResponse(
                horario.getId(),
                horario.getDiaSemana(),
                horario.getHoraInicio(),
                horario.getHoraFin(),
                horario.getDuracionCitaMinutos(),
                horario.getObservacion(),
                horario.getActivo(),
                horario.getFechaCreacion(),
                horario.getFechaActualizacion()
        );
    }
}
