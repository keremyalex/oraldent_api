package com.example.odontologia_api.service;

import com.example.odontologia_api.dto.OdontogramaCaraRequest;
import com.example.odontologia_api.dto.OdontogramaCaraResponse;
import com.example.odontologia_api.dto.OdontogramaDienteRequest;
import com.example.odontologia_api.dto.OdontogramaDienteResponse;
import com.example.odontologia_api.dto.OdontogramaObservacionesRequest;
import com.example.odontologia_api.dto.OdontogramaResponse;
import com.example.odontologia_api.entity.Cita;
import com.example.odontologia_api.entity.Odontograma;
import com.example.odontologia_api.entity.OdontogramaCara;
import com.example.odontologia_api.entity.OdontogramaDiente;
import com.example.odontologia_api.entity.Paciente;
import com.example.odontologia_api.entity.Usuario;
import com.example.odontologia_api.enums.ColorCaraOdontograma;
import com.example.odontologia_api.enums.TipoCaraOdontograma;
import com.example.odontologia_api.exception.RecursoNoEncontradoException;
import com.example.odontologia_api.repository.CitaRepository;
import com.example.odontologia_api.repository.OdontogramaRepository;
import com.example.odontologia_api.repository.UsuarioRepository;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OdontogramaService {

    private final OdontogramaRepository odontogramaRepository;
    private final UsuarioRepository usuarioRepository;
    private final CitaRepository citaRepository;
    private final PacienteService pacienteService;

    public OdontogramaService(
            OdontogramaRepository odontogramaRepository,
            UsuarioRepository usuarioRepository,
            CitaRepository citaRepository,
            PacienteService pacienteService
    ) {
        this.odontogramaRepository = odontogramaRepository;
        this.usuarioRepository = usuarioRepository;
        this.citaRepository = citaRepository;
        this.pacienteService = pacienteService;
    }

    @Transactional
    public OdontogramaResponse obtenerOCrearPorPaciente(Long pacienteId, Long usuarioId) {
        Paciente paciente = pacienteService.buscarActivo(pacienteId);
        Odontograma odontograma = odontogramaRepository
                .findFirstByPacienteAndActivoTrueOrderByIdDesc(paciente)
                .orElseGet(() -> odontogramaRepository.save(crearBase(paciente, resolverUsuario(usuarioId), null)));
        return toResponse(odontograma);
    }

    @Transactional
    public OdontogramaResponse crear(Long pacienteId, Long usuarioId, Long citaId, String observaciones) {
        Paciente paciente = pacienteService.buscarActivo(pacienteId);
        Odontograma anterior = odontogramaRepository
                .findFirstByPacienteAndActivoTrueOrderByIdDesc(paciente)
                .orElse(null);
        if (anterior != null) {
            anterior.setActivo(false);
        }

        Odontograma odontograma = crearBase(paciente, resolverUsuario(usuarioId), resolverCita(citaId));
        odontograma.setObservaciones(observaciones);
        return toResponse(odontogramaRepository.save(odontograma));
    }

    @Transactional
    public OdontogramaResponse actualizarObservaciones(Long odontogramaId, OdontogramaObservacionesRequest request) {
        Odontograma odontograma = buscarActivo(odontogramaId);
        odontograma.setObservaciones(request.observaciones());
        return toResponse(odontogramaRepository.save(odontograma));
    }

    @Transactional
    public OdontogramaResponse actualizarDiente(
            Long odontogramaId,
            Integer numeroFdi,
            OdontogramaDienteRequest request
    ) {
        Odontograma odontograma = buscarActivo(odontogramaId);
        OdontogramaDiente diente = buscarDiente(odontograma, numeroFdi);
        diente.setAusente(valor(request.ausente(), diente.getAusente()));
        diente.setImplante(valor(request.implante(), diente.getImplante()));
        diente.setCorona(valor(request.corona(), diente.getCorona()));
        diente.setEndodoncia(valor(request.endodoncia(), diente.getEndodoncia()));
        diente.setExtraccionIndicada(valor(request.extraccionIndicada(), diente.getExtraccionIndicada()));
        diente.setMovilidad(request.movilidad());
        diente.setObservacion(request.observacion());
        return toResponse(odontogramaRepository.save(odontograma));
    }

    @Transactional
    public OdontogramaResponse actualizarCara(
            Long odontogramaId,
            Integer numeroFdi,
            TipoCaraOdontograma tipo,
            OdontogramaCaraRequest request
    ) {
        Odontograma odontograma = buscarActivo(odontogramaId);
        OdontogramaDiente diente = buscarDiente(odontograma, numeroFdi);
        OdontogramaCara cara = diente.getCaras().stream()
                .filter(item -> item.getTipo() == tipo)
                .findFirst()
                .orElseThrow(() -> new RecursoNoEncontradoException("Cara del diente no encontrada."));
        cara.setColor(request.color());
        cara.setDescripcion(request.descripcion());
        return toResponse(odontogramaRepository.save(odontograma));
    }

    private Odontograma buscarActivo(Long id) {
        return odontogramaRepository.findById(id)
                .filter(Odontograma::getActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Odontograma no encontrado."));
    }

    private OdontogramaDiente buscarDiente(Odontograma odontograma, Integer numeroFdi) {
        return odontograma.getDientes().stream()
                .filter(diente -> diente.getNumeroFdi().equals(numeroFdi))
                .findFirst()
                .orElseThrow(() -> new RecursoNoEncontradoException("Diente no encontrado."));
    }

    private Odontograma crearBase(Paciente paciente, Usuario usuario, Cita cita) {
        Odontograma odontograma = new Odontograma();
        odontograma.setPaciente(paciente);
        odontograma.setUsuario(usuario);
        odontograma.setCita(cita);
        odontograma.setActivo(true);

        for (int cuadrante = 1; cuadrante <= 4; cuadrante++) {
            for (int posicion = 1; posicion <= 8; posicion++) {
                OdontogramaDiente diente = new OdontogramaDiente();
                diente.setCuadrante(cuadrante);
                diente.setPosicion(posicion);
                diente.setNumeroFdi(cuadrante * 10 + posicion);
                for (TipoCaraOdontograma tipo : TipoCaraOdontograma.values()) {
                    OdontogramaCara cara = new OdontogramaCara();
                    cara.setTipo(tipo);
                    cara.setColor(ColorCaraOdontograma.NINGUNO);
                    diente.addCara(cara);
                }
                odontograma.addDiente(diente);
            }
        }

        return odontograma;
    }

    private Usuario resolverUsuario(Long usuarioId) {
        if (usuarioId == null) {
            return null;
        }
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado."));
    }

    private Cita resolverCita(Long citaId) {
        if (citaId == null) {
            return null;
        }
        return citaRepository.findById(citaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cita no encontrada."));
    }

    private Boolean valor(Boolean nuevoValor, Boolean valorActual) {
        return nuevoValor == null ? valorActual : nuevoValor;
    }

    private OdontogramaResponse toResponse(Odontograma odontograma) {
        List<OdontogramaDienteResponse> dientes = odontograma.getDientes().stream()
                .sorted(Comparator.comparing(OdontogramaDiente::getNumeroFdi))
                .map(this::toDienteResponse)
                .toList();
        return new OdontogramaResponse(
                odontograma.getId(),
                pacienteService.toResponse(odontograma.getPaciente()),
                odontograma.getUsuario() == null ? null : odontograma.getUsuario().getId(),
                odontograma.getCita() == null ? null : odontograma.getCita().getId(),
                odontograma.getObservaciones(),
                odontograma.getActivo(),
                odontograma.getFechaCreacion(),
                odontograma.getFechaActualizacion(),
                dientes
        );
    }

    private OdontogramaDienteResponse toDienteResponse(OdontogramaDiente diente) {
        List<OdontogramaCaraResponse> caras = diente.getCaras().stream()
                .sorted(Comparator.comparing(cara -> cara.getTipo().ordinal()))
                .map(this::toCaraResponse)
                .toList();
        return new OdontogramaDienteResponse(
                diente.getId(),
                diente.getNumeroFdi(),
                diente.getCuadrante(),
                diente.getPosicion(),
                diente.getAusente(),
                diente.getImplante(),
                diente.getCorona(),
                diente.getEndodoncia(),
                diente.getExtraccionIndicada(),
                diente.getMovilidad(),
                diente.getObservacion(),
                caras
        );
    }

    private OdontogramaCaraResponse toCaraResponse(OdontogramaCara cara) {
        return new OdontogramaCaraResponse(
                cara.getId(),
                cara.getTipo(),
                cara.getColor(),
                cara.getDescripcion()
        );
    }
}
