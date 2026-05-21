package com.example.odontologia_api.config;

import com.example.odontologia_api.entity.Cita;
import com.example.odontologia_api.entity.FichaClinica;
import com.example.odontologia_api.entity.HorarioAtencion;
import com.example.odontologia_api.entity.Odontograma;
import com.example.odontologia_api.entity.OdontogramaCara;
import com.example.odontologia_api.entity.OdontogramaDiente;
import com.example.odontologia_api.entity.Anamnesis;
import com.example.odontologia_api.entity.Paciente;
import com.example.odontologia_api.entity.Periodontograma;
import com.example.odontologia_api.entity.PeriodontogramaDiente;
import com.example.odontologia_api.entity.PeriodontogramaSitio;
import com.example.odontologia_api.entity.Servicio;
import com.example.odontologia_api.entity.Usuario;
import com.example.odontologia_api.enums.ColorCaraOdontograma;
import com.example.odontologia_api.enums.DiaSemana;
import com.example.odontologia_api.enums.EstadoCita;
import com.example.odontologia_api.enums.FurcacionPeriodontograma;
import com.example.odontologia_api.enums.RolUsuario;
import com.example.odontologia_api.enums.SitioPeriodontograma;
import com.example.odontologia_api.enums.TipoCaraOdontograma;
import com.example.odontologia_api.repository.CitaRepository;
import com.example.odontologia_api.repository.FichaClinicaRepository;
import com.example.odontologia_api.repository.HorarioAtencionRepository;
import com.example.odontologia_api.repository.OdontogramaRepository;
import com.example.odontologia_api.repository.PacienteRepository;
import com.example.odontologia_api.repository.PeriodontogramaRepository;
import com.example.odontologia_api.repository.ServicioRepository;
import com.example.odontologia_api.repository.UsuarioRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UsuarioRepository usuarioRepository;
    private final PacienteRepository pacienteRepository;
    private final HorarioAtencionRepository horarioRepository;
    private final ServicioRepository servicioRepository;
    private final CitaRepository citaRepository;
    private final FichaClinicaRepository fichaClinicaRepository;
    private final OdontogramaRepository odontogramaRepository;
    private final PeriodontogramaRepository periodontogramaRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.enabled:false}")
    private boolean seedEnabled;

    public DataSeeder(
            UsuarioRepository usuarioRepository,
            PacienteRepository pacienteRepository,
            HorarioAtencionRepository horarioRepository,
            ServicioRepository servicioRepository,
            CitaRepository citaRepository,
            FichaClinicaRepository fichaClinicaRepository,
            OdontogramaRepository odontogramaRepository,
            PeriodontogramaRepository periodontogramaRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.usuarioRepository = usuarioRepository;
        this.pacienteRepository = pacienteRepository;
        this.horarioRepository = horarioRepository;
        this.servicioRepository = servicioRepository;
        this.citaRepository = citaRepository;
        this.fichaClinicaRepository = fichaClinicaRepository;
        this.odontogramaRepository = odontogramaRepository;
        this.periodontogramaRepository = periodontogramaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!seedEnabled) {
            log.info("Seed de demo deshabilitado por configuración.");
            return;
        }

        crearHorariosSiFaltan();
        Servicio implantologia = obtenerOCrearServicio("Implantologia Dental", "Tratamientos relacionados con implantes dentales.");
        Servicio rayoX = obtenerOCrearServicio("Rayo X", "Toma y apoyo diagnostico radiografico.");
        Servicio blanqueamiento = obtenerOCrearServicio("Blanqueamiento Dental Laser", "Tratamiento estetico para aclaramiento dental.");
        Servicio ortodoncia = obtenerOCrearServicio("Ortodoncia", "Correccion de alineacion y mordida.");
        Servicio protesis = obtenerOCrearServicio("Protesis Fija y Removible", "Rehabilitacion oral con protesis parciales o completas.");

        Paciente paciente1 = obtenerOCrearPaciente("Maria Fernanda", "Rojas", "Flores", "70010001", "maria@oraldent.com", "1234567", LocalDate.of(1997, 3, 12), "Zona Central");
        Paciente paciente2 = obtenerOCrearPaciente("Luis Alberto", "Mamani", "Quispe", "70010002", "luis@oraldent.com", "2345678", LocalDate.of(1992, 8, 21), "Av. Busch");
        Paciente paciente3 = obtenerOCrearPaciente("Camila", "Perez", "Vargas", "70010003", "camila@oraldent.com", "3456789", LocalDate.of(2001, 1, 5), "Zona Norte");

        obtenerOCrearUsuario(
                "Valeria",
                "Soria",
                "Lopez",
                "admin@oraldent.com",
                "70000001",
                "admin1234",
                RolUsuario.ADMIN
        );
        Usuario admin = usuarioRepository.findByCorreoIgnoreCase("admin@oraldent.com").orElseThrow();

        Cita cita1 = crearCitaSiNoExiste(
                paciente1,
                admin,
                implantologia,
                LocalDateTime.of(2026, 4, 27, 9, 0),
                "Valoracion inicial para implante",
                EstadoCita.CONFIRMADA,
                "ORALA001",
                "Primera valoracion de la semana."
        );
        Cita cita2 = crearCitaSiNoExiste(
                paciente2,
                admin,
                rayoX,
                LocalDateTime.of(2026, 4, 27, 15, 30),
                "Radiografia panoramica de control",
                EstadoCita.PENDIENTE,
                "ORALA002",
                "Paciente referido para apoyo diagnostico."
        );
        Cita cita3 = crearCitaSiNoExiste(
                paciente3,
                null,
                ortodoncia,
                LocalDateTime.of(2026, 4, 28, 10, 0),
                "Control de ortodoncia",
                EstadoCita.CONFIRMADA,
                "ORALA003",
                "Ajuste mensual."
        );
        crearCitaSiNoExiste(
                paciente1,
                null,
                blanqueamiento,
                LocalDateTime.of(2026, 4, 29, 16, 0),
                "Evaluacion para blanqueamiento",
                EstadoCita.PENDIENTE,
                "ORALA004",
                "Paciente solicita tratamiento estetico."
        );
        crearCitaSiNoExiste(
                paciente2,
                admin,
                protesis,
                LocalDateTime.of(2026, 5, 1, 11, 0),
                "Revision de protesis",
                EstadoCita.REPROGRAMADA,
                "ORALA005",
                "Cita reprogramada por disponibilidad del paciente."
        );
        crearCitaSiNoExiste(
                paciente3,
                admin,
                rayoX,
                LocalDateTime.of(2026, 5, 2, 9, 0),
                "Rayo X de seguimiento",
                EstadoCita.CONFIRMADA,
                "ORALA006",
                "Control sabatino."
        );

        FichaClinica ficha1 = crearFichaClinicaSiNoExiste(
                paciente1,
                admin,
                cita1,
                "Valoracion inicial para implante",
                "Paciente refiere ausencia de pieza posterior y desea evaluar rehabilitacion con implante.",
                "Sin antecedentes sistemicos relevantes. Refiere sensibilidad ocasional en sector posterior.",
                true
        );
        FichaClinica ficha2 = crearFichaClinicaSiNoExiste(
                paciente2,
                admin,
                cita2,
                "Radiografia panoramica de control",
                "Control por molestia al masticar y revision de pieza ausente.",
                "Paciente refiere tratamiento medico anterior por hipertension controlada.",
                false
        );
        FichaClinica ficha3 = crearFichaClinicaSiNoExiste(
                paciente3,
                admin,
                cita3,
                "Control de ortodoncia",
                "Ajuste mensual y evaluacion de higiene durante tratamiento ortodontico.",
                "Paciente sin alergias conocidas. Buen estado general.",
                false
        );

        crearOdontogramaSiNoExiste(ficha1, paciente1, admin, cita1, "Odontograma inicial con hallazgos de valoracion.");
        crearOdontogramaSiNoExiste(ficha2, paciente2, admin, cita2, "Odontograma inicial para apoyo diagnostico.");
        crearOdontogramaSiNoExiste(ficha3, paciente3, admin, cita3, "Odontograma inicial de control de ortodoncia.");
        crearPeriodontogramaSiNoExiste(ficha1, paciente1, admin, cita1, "Registro periodontal inicial sin compromiso severo.");
        crearPeriodontogramaSiNoExiste(ficha2, paciente2, admin, cita2, "Registro periodontal de control con sangrado localizado.");
        crearPeriodontogramaSiNoExiste(ficha3, paciente3, admin, cita3, "Registro periodontal para control preventivo.");
        log.info("Seed de demo verificado correctamente.");
    }

    private void crearHorariosSiFaltan() {
        crearHorarioSiNoExiste(DiaSemana.LUNES, LocalTime.of(8, 30), LocalTime.of(12, 30), "Turno manana");
        crearHorarioSiNoExiste(DiaSemana.LUNES, LocalTime.of(15, 0), LocalTime.of(22, 30), "Turno tarde");
        crearHorarioSiNoExiste(DiaSemana.MARTES, LocalTime.of(8, 30), LocalTime.of(12, 30), "Turno manana");
        crearHorarioSiNoExiste(DiaSemana.MARTES, LocalTime.of(15, 0), LocalTime.of(22, 30), "Turno tarde");
        crearHorarioSiNoExiste(DiaSemana.MIERCOLES, LocalTime.of(8, 30), LocalTime.of(12, 30), "Turno manana");
        crearHorarioSiNoExiste(DiaSemana.MIERCOLES, LocalTime.of(15, 0), LocalTime.of(22, 30), "Turno tarde");
        crearHorarioSiNoExiste(DiaSemana.JUEVES, LocalTime.of(8, 30), LocalTime.of(12, 30), "Turno manana");
        crearHorarioSiNoExiste(DiaSemana.JUEVES, LocalTime.of(15, 0), LocalTime.of(22, 30), "Turno tarde");
        crearHorarioSiNoExiste(DiaSemana.VIERNES, LocalTime.of(8, 30), LocalTime.of(12, 30), "Turno manana");
        crearHorarioSiNoExiste(DiaSemana.VIERNES, LocalTime.of(15, 0), LocalTime.of(22, 30), "Turno tarde");
        crearHorarioSiNoExiste(DiaSemana.SABADO, LocalTime.of(9, 0), LocalTime.of(22, 30), "Turno continuo");
    }

    private void crearHorarioSiNoExiste(DiaSemana dayOfWeek, LocalTime horaInicio, LocalTime horaFin, String observacion) {
        boolean existe = horarioRepository.findActivosPorDiaSemanaValoresOrderByHoraInicioAsc(
                        java.util.List.of(dayOfWeek.name(), dayOfWeek.legacyValue())
                ).stream()
                .anyMatch(horario -> horario.getHoraInicio().equals(horaInicio) && horario.getHoraFin().equals(horaFin));
        if (existe) {
            return;
        }

        HorarioAtencion horario = new HorarioAtencion();
        horario.setDiaSemana(dayOfWeek);
        horario.setHoraInicio(horaInicio);
        horario.setHoraFin(horaFin);
        horario.setDuracionCitaMinutos(30);
        horario.setObservacion(observacion);
        horario.setActivo(true);
        horarioRepository.save(horario);
    }

    private Servicio obtenerOCrearServicio(String nombre, String descripcion) {
        return servicioRepository.findByNombreIgnoreCase(nombre)
                .orElseGet(() -> {
                    Servicio servicio = new Servicio();
                    servicio.setNombre(nombre);
                    servicio.setDescripcion(descripcion);
                    servicio.setActivo(true);
                    return servicioRepository.save(servicio);
                });
    }

    private Paciente obtenerOCrearPaciente(
            String nombre,
            String paterno,
            String materno,
            String celular,
            String correo,
            String documentoIdentidad,
            LocalDate fechaNacimiento,
            String direccion
    ) {
        Optional<Paciente> porCelular = pacienteRepository.findFirstByCelularAndActivoTrueOrderByIdDesc(celular);
        if (porCelular.isPresent()) {
            Paciente paciente = porCelular.get();
            actualizarPaciente(paciente, nombre, paterno, materno, celular, correo, documentoIdentidad, fechaNacimiento, direccion);
            return pacienteRepository.save(paciente);
        }

        Optional<Paciente> porCorreo = pacienteRepository.findFirstByCorreoIgnoreCaseAndActivoTrueOrderByIdDesc(correo);
        if (porCorreo.isPresent()) {
            Paciente paciente = porCorreo.get();
            actualizarPaciente(paciente, nombre, paterno, materno, celular, correo, documentoIdentidad, fechaNacimiento, direccion);
            return pacienteRepository.save(paciente);
        }

        Paciente paciente = new Paciente();
        actualizarPaciente(paciente, nombre, paterno, materno, celular, correo, documentoIdentidad, fechaNacimiento, direccion);
        return pacienteRepository.save(paciente);
    }

    private void actualizarPaciente(
            Paciente paciente,
            String nombre,
            String paterno,
            String materno,
            String celular,
            String correo,
            String documentoIdentidad,
            LocalDate fechaNacimiento,
            String direccion
    ) {
        paciente.setNombre(nombre);
        paciente.setApellidoPaterno(paterno);
        paciente.setApellidoMaterno(materno);
        paciente.setCelular(celular);
        paciente.setCorreo(correo);
        paciente.setDocumentoIdentidad(documentoIdentidad);
        paciente.setFechaNacimiento(fechaNacimiento);
        paciente.setDireccion(direccion);
        paciente.setActivo(true);
    }

    private Usuario obtenerOCrearUsuario(
            String nombre,
            String paterno,
            String materno,
            String correo,
            String celular,
            String password,
            RolUsuario rol
    ) {
        Optional<Usuario> usuarioExistente = usuarioRepository.findByCorreoIgnoreCase(correo);
        if (usuarioExistente.isPresent()) {
            return usuarioExistente.get();
        }

        Optional<Usuario> usuarioPorCelular = usuarioRepository.findByCelular(celular);
        if (usuarioPorCelular.isPresent()) {
            return usuarioPorCelular.get();
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setApellidoPaterno(paterno);
        usuario.setApellidoMaterno(materno);
        usuario.setCorreo(correo);
        usuario.setCelular(celular);
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setRol(rol);
        usuario.setActivo(true);
        usuario.setVerificado(true);
        return usuarioRepository.save(usuario);
    }

    private Cita crearCitaSiNoExiste(
            Paciente paciente,
            Usuario usuarioRegistrador,
            Servicio servicio,
            LocalDateTime fechaHoraInicio,
            String motivo,
            EstadoCita estado,
            String codigo,
            String notas
    ) {
        Optional<Cita> citaExistente = citaRepository.findByCodigoGestion(codigo);
        if (citaExistente.isPresent()) {
            return citaExistente.get();
        }

        LocalDateTime fechaHoraFin = fechaHoraInicio.plusMinutes(30);
        Cita cita = new Cita();
        cita.setPaciente(paciente);
        cita.setUsuario(usuarioRegistrador);
        cita.setServicio(servicio);
        cita.setFechaHoraInicio(fechaHoraInicio);
        cita.setFechaHoraFin(fechaHoraFin);
        cita.setMotivo(motivo);
        cita.setEstado(estado);
        cita.setCodigoGestion(codigo);
        cita.setNotas(notas);
        return citaRepository.save(cita);
    }

    private FichaClinica crearFichaClinicaSiNoExiste(
            Paciente paciente,
            Usuario usuario,
            Cita cita,
            String motivoConsulta,
            String enfermedadActual,
            String anamnesis,
            boolean implantologia
    ) {
        Optional<FichaClinica> fichaExistente = fichaClinicaRepository
                .findByPacienteAndActivoTrueOrderByFechaDescIdDesc(paciente)
                .stream()
                .filter(ficha -> ficha.getCita() != null && ficha.getCita().getId().equals(cita.getId()))
                .findFirst();
        if (fichaExistente.isPresent()) {
            return fichaExistente.get();
        }

        FichaClinica ficha = new FichaClinica();
        ficha.setPaciente(paciente);
        ficha.setUsuario(usuario);
        ficha.setCita(cita);
        ficha.setFecha(cita.getFechaHoraInicio());
        ficha.setEdad(calcularEdad(paciente.getFechaNacimiento(), cita.getFechaHoraInicio().toLocalDate()));
        ficha.setSexo("No especificado");
        ficha.setProcedencia("Cochabamba");
        ficha.setOcupacion(implantologia ? "Comerciante" : "Estudiante");
        ficha.setPresionArterial(implantologia ? "120/80" : "115/75");
        ficha.setTemperatura(BigDecimal.valueOf(36.7));
        ficha.setPulso(implantologia ? 78 : 74);
        ficha.setMotivoConsulta(motivoConsulta);
        ficha.setEnfermedadActual(enfermedadActual);
        Anamnesis datosAnamnesis = new Anamnesis();
        datosAnamnesis.setDescripcion(anamnesis);
        datosAnamnesis.setHemorragia(false);
        datosAnamnesis.setDiabetes(false);
        datosAnamnesis.setHipertension(!implantologia && "70010002".equals(paciente.getCelular()));
        datosAnamnesis.setEpilepsia(false);
        datosAnamnesis.setProblemasCardiovasculares(false);
        datosAnamnesis.setLipotimias(false);
        datosAnamnesis.setTratamientoMedicoActual("70010002".equals(paciente.getCelular()));
        datosAnamnesis.setAlergias("70010001".equals(paciente.getCelular()) ? "Niega alergias medicamentosas." : null);
        datosAnamnesis.setMedicamentoActual("70010002".equals(paciente.getCelular()) ? "Losartan 50 mg indicado por medico tratante." : null);
        datosAnamnesis.setOtrasPatologias("Sin otras patologias referidas.");
        ficha.setAnamnesis(datosAnamnesis);
        ficha.setExamenClinico(implantologia
                ? "Ausencia de pieza posterior y mucosa sin lesiones aparentes."
                : "Encías con inflamacion leve localizada y control de placa indicado.");
        ficha.setExamenRadiografico(implantologia
                ? "Se solicita radiografia panoramica para evaluar disponibilidad osea."
                : "Radiografia de control sin lesiones periapicales evidentes.");
        ficha.setDiagnostico(implantologia
                ? "Edentulismo parcial con indicacion de evaluacion para implante."
                : "Control odontologico con hallazgos leves.");
        ficha.setTratamiento(implantologia
                ? "Planificar fase diagnostica para rehabilitacion implantosoportada."
                : "Profilaxis, control de higiene y seguimiento.");
        ficha.setTecnicaAnestesia("No aplicada en esta consulta.");
        ficha.setEvolucion("Pendiente de control segun plan de tratamiento.");
        ficha.setActivo(true);
        return fichaClinicaRepository.save(ficha);
    }

    private Integer calcularEdad(LocalDate fechaNacimiento, LocalDate fechaReferencia) {
        if (fechaNacimiento == null) {
            return null;
        }
        return Period.between(fechaNacimiento, fechaReferencia).getYears();
    }

    private void crearOdontogramaSiNoExiste(
            FichaClinica ficha,
            Paciente paciente,
            Usuario usuario,
            Cita cita,
            String observaciones
    ) {
        Optional<Odontograma> porFicha = odontogramaRepository.findFirstByFichaClinicaAndActivoTrueOrderByIdDesc(ficha);
        if (porFicha.isPresent()) {
            return;
        }

        Optional<Odontograma> activoPaciente = odontogramaRepository.findFirstByPacienteAndActivoTrueOrderByIdDesc(paciente);
        if (activoPaciente.isPresent()) {
            Odontograma odontograma = activoPaciente.get();
            if (odontograma.getFichaClinica() == null) {
                odontograma.setFichaClinica(ficha);
                odontogramaRepository.save(odontograma);
            }
            return;
        }

        Odontograma odontograma = new Odontograma();
        odontograma.setPaciente(paciente);
        odontograma.setUsuario(usuario);
        odontograma.setCita(cita);
        odontograma.setFichaClinica(ficha);
        odontograma.setObservaciones(observaciones);
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

        aplicarHallazgosDemo(odontograma, paciente.getCelular());
        odontogramaRepository.save(odontograma);
    }

    private void crearPeriodontogramaSiNoExiste(
            FichaClinica ficha,
            Paciente paciente,
            Usuario usuario,
            Cita cita,
            String observaciones
    ) {
        if (periodontogramaRepository.findFirstByFichaClinicaAndActivoTrueOrderByIdDesc(ficha).isPresent()) {
            return;
        }

        Periodontograma periodontograma = new Periodontograma();
        periodontograma.setPaciente(paciente);
        periodontograma.setUsuario(usuario);
        periodontograma.setCita(cita);
        periodontograma.setFichaClinica(ficha);
        periodontograma.setObservaciones(observaciones);
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

        aplicarPeriodontoDemo(periodontograma, paciente.getCelular());
        periodontogramaRepository.save(periodontograma);
    }

    private void aplicarPeriodontoDemo(Periodontograma periodontograma, String celularPaciente) {
        if ("70010001".equals(celularPaciente)) {
            marcarSitioPeriodontal(periodontograma, 16, SitioPeriodontograma.MESIOVESTIBULAR, 3, true, false);
            marcarSitioPeriodontal(periodontograma, 36, SitioPeriodontograma.VESTIBULAR, 4, false, true);
            return;
        }

        if ("70010002".equals(celularPaciente)) {
            marcarSitioPeriodontal(periodontograma, 24, SitioPeriodontograma.DISTOVESTIBULAR, 4, true, true);
            marcarSitioPeriodontal(periodontograma, 25, SitioPeriodontograma.PALATINO, 5, true, false);
            periodontograma.getDientes().stream()
                    .filter(diente -> diente.getNumeroFdi() == 46)
                    .findFirst()
                    .ifPresent(diente -> {
                        diente.setAusente(true);
                        diente.setObservacion("Pieza ausente en control periodontal.");
                    });
            return;
        }

        if ("70010003".equals(celularPaciente)) {
            marcarSitioPeriodontal(periodontograma, 31, SitioPeriodontograma.VESTIBULAR, 3, true, true);
            marcarSitioPeriodontal(periodontograma, 41, SitioPeriodontograma.VESTIBULAR, 3, false, true);
        }
    }

    private void marcarSitioPeriodontal(
            Periodontograma periodontograma,
            int numeroFdi,
            SitioPeriodontograma sitio,
            int profundidad,
            boolean sangrado,
            boolean placa
    ) {
        periodontograma.getDientes().stream()
                .filter(diente -> diente.getNumeroFdi() == numeroFdi)
                .findFirst()
                .flatMap(diente -> diente.getSitios().stream()
                        .filter(registro -> registro.getSitio() == sitio)
                        .findFirst())
                .ifPresent(registro -> {
                    registro.setProfundidadSondajeMm(profundidad);
                    registro.setMargenGingivalMm(0);
                    registro.setSangradoSondaje(sangrado);
                    registro.setPlaca(placa);
                });
    }

    private void aplicarHallazgosDemo(Odontograma odontograma, String celularPaciente) {
        if ("70010001".equals(celularPaciente)) {
            marcarCara(odontograma, 16, TipoCaraOdontograma.MESIAL, ColorCaraOdontograma.ROJO, "Caries proximal pendiente de tratamiento.");
            marcarCara(odontograma, 36, TipoCaraOdontograma.OCLUSAL, ColorCaraOdontograma.AZUL, "Restauracion existente en buen estado.");
            return;
        }

        if ("70010002".equals(celularPaciente)) {
            marcarDienteAusente(odontograma, 46, "Pieza ausente referida por el paciente.");
            marcarCara(odontograma, 24, TipoCaraOdontograma.DISTAL, ColorCaraOdontograma.ROJO, "Lesion cariosa a evaluar.");
            return;
        }

        if ("70010003".equals(celularPaciente)) {
            marcarCara(odontograma, 11, TipoCaraOdontograma.VESTIBULAR, ColorCaraOdontograma.AZUL, "Sellado/restauracion estetica existente.");
            marcarCara(odontograma, 31, TipoCaraOdontograma.OCLUSAL, ColorCaraOdontograma.ROJO, "Controlar desgaste incisal.");
        }
    }

    private void marcarDienteAusente(Odontograma odontograma, int numeroFdi, String observacion) {
        odontograma.getDientes().stream()
                .filter(diente -> diente.getNumeroFdi() == numeroFdi)
                .findFirst()
                .ifPresent(diente -> {
                    diente.setAusente(true);
                    diente.setObservacion(observacion);
                });
    }

    private void marcarCara(
            Odontograma odontograma,
            int numeroFdi,
            TipoCaraOdontograma tipo,
            ColorCaraOdontograma color,
            String descripcion
    ) {
        odontograma.getDientes().stream()
                .filter(diente -> diente.getNumeroFdi() == numeroFdi)
                .findFirst()
                .flatMap(diente -> diente.getCaras().stream()
                        .filter(cara -> cara.getTipo() == tipo)
                        .findFirst())
                .ifPresent(cara -> {
                    cara.setColor(color);
                    cara.setDescripcion(descripcion);
                });
    }
}
