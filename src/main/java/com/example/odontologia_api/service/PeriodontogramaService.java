package com.example.odontologia_api.service;

import com.example.odontologia_api.dto.PeriodontogramaDienteRequest;
import com.example.odontologia_api.dto.PeriodontogramaDienteResponse;
import com.example.odontologia_api.dto.PeriodontogramaObservacionesRequest;
import com.example.odontologia_api.dto.PeriodontogramaResponse;
import com.example.odontologia_api.dto.PeriodontogramaSitioRequest;
import com.example.odontologia_api.dto.PeriodontogramaSitioResponse;
import com.example.odontologia_api.entity.FichaClinica;
import com.example.odontologia_api.entity.Periodontograma;
import com.example.odontologia_api.entity.PeriodontogramaDiente;
import com.example.odontologia_api.entity.PeriodontogramaSitio;
import com.example.odontologia_api.enums.FurcacionPeriodontograma;
import com.example.odontologia_api.enums.SitioPeriodontograma;
import com.example.odontologia_api.exception.RecursoNoEncontradoException;
import com.example.odontologia_api.repository.PeriodontogramaRepository;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PeriodontogramaService {

    private final PeriodontogramaRepository periodontogramaRepository;
    private final PacienteService pacienteService;
    private final FichaClinicaService fichaClinicaService;

    public PeriodontogramaService(
            PeriodontogramaRepository periodontogramaRepository,
            PacienteService pacienteService,
            FichaClinicaService fichaClinicaService
    ) {
        this.periodontogramaRepository = periodontogramaRepository;
        this.pacienteService = pacienteService;
        this.fichaClinicaService = fichaClinicaService;
    }

    @Transactional
    public PeriodontogramaResponse obtenerOCrearPorFicha(Long fichaId) {
        FichaClinica ficha = fichaClinicaService.buscarActiva(fichaId);
        Periodontograma periodontograma = periodontogramaRepository
                .findFirstByFichaClinicaAndActivoTrueOrderByIdDesc(ficha)
                .orElseGet(() -> periodontogramaRepository.save(crearBase(ficha)));
        return toResponse(periodontograma);
    }

    @Transactional
    public PeriodontogramaResponse crearParaFicha(Long fichaId, String observaciones) {
        FichaClinica ficha = fichaClinicaService.buscarActiva(fichaId);
        Periodontograma anterior = periodontogramaRepository
                .findFirstByFichaClinicaAndActivoTrueOrderByIdDesc(ficha)
                .orElse(null);
        if (anterior != null) {
            anterior.setActivo(false);
        }

        Periodontograma periodontograma = crearBase(ficha);
        periodontograma.setObservaciones(observaciones);
        return toResponse(periodontogramaRepository.save(periodontograma));
    }

    @Transactional
    public PeriodontogramaResponse actualizarObservaciones(
            Long periodontogramaId,
            PeriodontogramaObservacionesRequest request
    ) {
        Periodontograma periodontograma = buscarActivo(periodontogramaId);
        periodontograma.setObservaciones(request.observaciones());
        return toResponse(periodontogramaRepository.save(periodontograma));
    }

    @Transactional
    public PeriodontogramaResponse actualizarDiente(
            Long periodontogramaId,
            Integer numeroFdi,
            PeriodontogramaDienteRequest request
    ) {
        Periodontograma periodontograma = buscarActivo(periodontogramaId);
        PeriodontogramaDiente diente = buscarDiente(periodontograma, numeroFdi);
        diente.setAusente(valor(request.ausente(), diente.getAusente()));
        diente.setImplante(valor(request.implante(), diente.getImplante()));
        diente.setMovilidad(request.movilidad());
        diente.setFurcacionVestibular(resolverFurcacion(
                diente.getNumeroFdi(),
                request.furcacionVestibular(),
                diente.getFurcacionVestibular()
        ));
        diente.setFurcacionPalatinaLingual(resolverFurcacion(
                diente.getNumeroFdi(),
                request.furcacionPalatinaLingual(),
                diente.getFurcacionPalatinaLingual()
        ));
        diente.setObservacion(request.observacion());
        return toResponse(periodontogramaRepository.save(periodontograma));
    }

    @Transactional
    public PeriodontogramaResponse actualizarSitio(
            Long periodontogramaId,
            Integer numeroFdi,
            SitioPeriodontograma sitio,
            PeriodontogramaSitioRequest request
    ) {
        Periodontograma periodontograma = buscarActivo(periodontogramaId);
        PeriodontogramaDiente diente = buscarDiente(periodontograma, numeroFdi);
        PeriodontogramaSitio registro = diente.getSitios().stream()
                .filter(item -> item.getSitio() == sitio)
                .findFirst()
                .orElseThrow(() -> new RecursoNoEncontradoException("Sitio periodontal no encontrado."));
        registro.setSangradoSondaje(valor(request.sangradoSondaje(), registro.getSangradoSondaje()));
        registro.setPlaca(valor(request.placa(), registro.getPlaca()));
        registro.setSupuracion(valor(request.supuracion(), registro.getSupuracion()));
        registro.setMargenGingivalMm(valor(request.margenGingivalMm(), registro.getMargenGingivalMm()));
        registro.setProfundidadSondajeMm(valor(request.profundidadSondajeMm(), registro.getProfundidadSondajeMm()));
        registro.setObservacion(request.observacion());
        return toResponse(periodontogramaRepository.save(periodontograma));
    }

    private Periodontograma buscarActivo(Long id) {
        return periodontogramaRepository.findById(id)
                .filter(Periodontograma::getActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Periodontograma no encontrado."));
    }

    private PeriodontogramaDiente buscarDiente(Periodontograma periodontograma, Integer numeroFdi) {
        return periodontograma.getDientes().stream()
                .filter(diente -> diente.getNumeroFdi().equals(numeroFdi))
                .findFirst()
                .orElseThrow(() -> new RecursoNoEncontradoException("Diente periodontal no encontrado."));
    }

    private Periodontograma crearBase(FichaClinica ficha) {
        Periodontograma periodontograma = new Periodontograma();
        periodontograma.setFichaClinica(ficha);
        periodontograma.setActivo(true);

        for (int cuadrante = 1; cuadrante <= 4; cuadrante++) {
            for (int posicion = 1; posicion <= 8; posicion++) {
                PeriodontogramaDiente diente = new PeriodontogramaDiente();
                diente.setCuadrante(cuadrante);
                diente.setPosicion(posicion);
                diente.setNumeroFdi(cuadrante * 10 + posicion);
                diente.setFurcacionVestibular(FurcacionPeriodontograma.NINGUNA);
                diente.setFurcacionPalatinaLingual(FurcacionPeriodontograma.NINGUNA);
                for (SitioPeriodontograma sitio : SitioPeriodontograma.values()) {
                    PeriodontogramaSitio registro = new PeriodontogramaSitio();
                    registro.setSitio(sitio);
                    diente.addSitio(registro);
                }
                periodontograma.addDiente(diente);
            }
        }
        return periodontograma;
    }

    private Boolean valor(Boolean nuevoValor, Boolean valorActual) {
        return nuevoValor == null ? valorActual : nuevoValor;
    }

    private Integer valor(Integer nuevoValor, Integer valorActual) {
        return nuevoValor == null ? valorActual : nuevoValor;
    }

    private FurcacionPeriodontograma resolverFurcacion(
            Integer numeroFdi,
            FurcacionPeriodontograma nuevaFurcacion,
            FurcacionPeriodontograma furcacionActual
    ) {
        if (!permiteFurcacion(numeroFdi)) {
            return FurcacionPeriodontograma.NINGUNA;
        }
        return nuevaFurcacion == null ? furcacionActual : nuevaFurcacion;
    }

    private boolean permiteFurcacion(Integer numeroFdi) {
        return switch (numeroFdi) {
            case 16, 17, 18, 26, 27, 28, 36, 37, 38, 46, 47, 48 -> true;
            default -> false;
        };
    }

    private PeriodontogramaResponse toResponse(Periodontograma periodontograma) {
        List<PeriodontogramaDienteResponse> dientes = periodontograma.getDientes().stream()
                .sorted(Comparator.comparing(PeriodontogramaDiente::getNumeroFdi))
                .map(this::toDienteResponse)
                .toList();
        return new PeriodontogramaResponse(
                periodontograma.getId(),
                pacienteService.toResponse(periodontograma.getFichaClinica().getPaciente()),
                periodontograma.getFichaClinica().getUsuario() == null ? null : periodontograma.getFichaClinica().getUsuario().getId(),
                periodontograma.getFichaClinica().getCita() == null ? null : periodontograma.getFichaClinica().getCita().getId(),
                periodontograma.getFichaClinica().getId(),
                periodontograma.getObservaciones(),
                periodontograma.getActivo(),
                periodontograma.getFechaCreacion(),
                periodontograma.getFechaActualizacion(),
                dientes
        );
    }

    private PeriodontogramaDienteResponse toDienteResponse(PeriodontogramaDiente diente) {
        List<PeriodontogramaSitioResponse> sitios = diente.getSitios().stream()
                .sorted(Comparator.comparing(registro -> registro.getSitio().ordinal()))
                .map(this::toSitioResponse)
                .toList();
        return new PeriodontogramaDienteResponse(
                diente.getId(),
                diente.getNumeroFdi(),
                diente.getCuadrante(),
                diente.getPosicion(),
                diente.getAusente(),
                diente.getImplante(),
                diente.getMovilidad(),
                permiteFurcacion(diente.getNumeroFdi())
                        ? diente.getFurcacionVestibular()
                        : FurcacionPeriodontograma.NINGUNA,
                permiteFurcacion(diente.getNumeroFdi())
                        ? diente.getFurcacionPalatinaLingual()
                        : FurcacionPeriodontograma.NINGUNA,
                diente.getObservacion(),
                sitios
        );
    }

    private PeriodontogramaSitioResponse toSitioResponse(PeriodontogramaSitio sitio) {
        int nivelInsercion = sitio.getProfundidadSondajeMm() - sitio.getMargenGingivalMm();
        return new PeriodontogramaSitioResponse(
                sitio.getId(),
                sitio.getSitio(),
                sitio.getSangradoSondaje(),
                sitio.getPlaca(),
                sitio.getSupuracion(),
                sitio.getMargenGingivalMm(),
                sitio.getProfundidadSondajeMm(),
                nivelInsercion,
                sitio.getObservacion()
        );
    }
}
