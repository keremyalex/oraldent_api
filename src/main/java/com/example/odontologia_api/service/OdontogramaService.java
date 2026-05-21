package com.example.odontologia_api.service;

import com.example.odontologia_api.dto.OdontogramaBatchRequest;
import com.example.odontologia_api.dto.OdontogramaCaraRequest;
import com.example.odontologia_api.dto.OdontogramaCaraResponse;
import com.example.odontologia_api.dto.OdontogramaDienteRequest;
import com.example.odontologia_api.dto.OdontogramaDienteResponse;
import com.example.odontologia_api.dto.OdontogramaObservacionesRequest;
import com.example.odontologia_api.dto.OdontogramaResponse;
import com.example.odontologia_api.entity.FichaClinica;
import com.example.odontologia_api.entity.Odontograma;
import com.example.odontologia_api.entity.OdontogramaCara;
import com.example.odontologia_api.entity.OdontogramaDiente;
import com.example.odontologia_api.enums.ColorCaraOdontograma;
import com.example.odontologia_api.enums.TipoCaraOdontograma;
import com.example.odontologia_api.exception.RecursoNoEncontradoException;
import com.example.odontologia_api.repository.OdontogramaRepository;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OdontogramaService {

    private final OdontogramaRepository odontogramaRepository;
    private final PacienteService pacienteService;
    private final FichaClinicaService fichaClinicaService;

    public OdontogramaService(
            OdontogramaRepository odontogramaRepository,
            PacienteService pacienteService,
            FichaClinicaService fichaClinicaService
    ) {
        this.odontogramaRepository = odontogramaRepository;
        this.pacienteService = pacienteService;
        this.fichaClinicaService = fichaClinicaService;
    }

    @Transactional
    public OdontogramaResponse obtenerOCrearPorFicha(Long fichaId) {
        FichaClinica ficha = fichaClinicaService.buscarActiva(fichaId);
        Odontograma odontograma = odontogramaRepository
                .findFirstByFichaClinicaAndActivoTrueOrderByIdDesc(ficha)
                .orElseGet(() -> odontogramaRepository.save(crearBase(ficha)));
        return toResponse(odontograma);
    }

    @Transactional
    public OdontogramaResponse crearParaFicha(Long fichaId, String observaciones) {
        FichaClinica ficha = fichaClinicaService.buscarActiva(fichaId);
        Odontograma anterior = odontogramaRepository
                .findFirstByFichaClinicaAndActivoTrueOrderByIdDesc(ficha)
                .orElse(null);
        if (anterior != null) {
            anterior.setActivo(false);
        }

        Odontograma odontograma = crearBase(ficha);
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

    @Transactional
    public OdontogramaResponse actualizarCompleto(Long odontogramaId, OdontogramaBatchRequest request) {
        Odontograma odontograma = buscarActivo(odontogramaId);
        odontograma.setObservaciones(request.observaciones());

        if (request.dientes() != null) {
            for (var dienteRequest : request.dientes()) {
                OdontogramaDiente diente = buscarDiente(odontograma, dienteRequest.numeroFdi());
                diente.setAusente(valor(dienteRequest.ausente(), diente.getAusente()));
                diente.setImplante(valor(dienteRequest.implante(), diente.getImplante()));
                diente.setCorona(valor(dienteRequest.corona(), diente.getCorona()));
                diente.setEndodoncia(valor(dienteRequest.endodoncia(), diente.getEndodoncia()));
                diente.setExtraccionIndicada(valor(
                        dienteRequest.extraccionIndicada(),
                        diente.getExtraccionIndicada()
                ));
                diente.setObservacion(dienteRequest.observacion());

                if (dienteRequest.caras() != null) {
                    for (var caraRequest : dienteRequest.caras()) {
                        OdontogramaCara cara = diente.getCaras().stream()
                                .filter(item -> item.getTipo() == caraRequest.tipo())
                                .findFirst()
                                .orElseThrow(() -> new RecursoNoEncontradoException("Cara del diente no encontrada."));
                        cara.setColor(caraRequest.color());
                        cara.setDescripcion(caraRequest.descripcion());
                    }
                }
            }
        }

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

    private Odontograma crearBase(FichaClinica ficha) {
        Odontograma odontograma = new Odontograma();
        odontograma.setFichaClinica(ficha);
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
                pacienteService.toResponse(odontograma.getFichaClinica().getPaciente()),
                odontograma.getFichaClinica().getUsuario() == null ? null : odontograma.getFichaClinica().getUsuario().getId(),
                odontograma.getFichaClinica().getCita() == null ? null : odontograma.getFichaClinica().getCita().getId(),
                odontograma.getFichaClinica().getId(),
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
