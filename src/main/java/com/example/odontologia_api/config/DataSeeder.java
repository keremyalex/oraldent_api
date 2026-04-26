package com.example.odontologia_api.config;

import com.example.odontologia_api.entity.Cita;
import com.example.odontologia_api.entity.HorarioAtencion;
import com.example.odontologia_api.entity.Paciente;
import com.example.odontologia_api.entity.Servicio;
import com.example.odontologia_api.entity.Usuario;
import com.example.odontologia_api.enums.DiaSemana;
import com.example.odontologia_api.enums.EstadoCita;
import com.example.odontologia_api.enums.RolUsuario;
import com.example.odontologia_api.repository.CitaRepository;
import com.example.odontologia_api.repository.HorarioAtencionRepository;
import com.example.odontologia_api.repository.PacienteRepository;
import com.example.odontologia_api.repository.ServicioRepository;
import com.example.odontologia_api.repository.UsuarioRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.enabled:false}")
    private boolean seedEnabled;

    public DataSeeder(
            UsuarioRepository usuarioRepository,
            PacienteRepository pacienteRepository,
            HorarioAtencionRepository horarioRepository,
            ServicioRepository servicioRepository,
            CitaRepository citaRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.usuarioRepository = usuarioRepository;
        this.pacienteRepository = pacienteRepository;
        this.horarioRepository = horarioRepository;
        this.servicioRepository = servicioRepository;
        this.citaRepository = citaRepository;
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
                RolUsuario.ADMIN,
                null
        );

        crearCitaSiNoExiste(
                paciente1,
                implantologia,
                LocalDateTime.of(2026, 4, 27, 9, 0),
                "Valoracion inicial para implante",
                EstadoCita.CONFIRMADA,
                "ORALA001",
                "Primera valoracion de la semana."
        );
        crearCitaSiNoExiste(
                paciente2,
                rayoX,
                LocalDateTime.of(2026, 4, 27, 15, 30),
                "Radiografia panoramica de control",
                EstadoCita.PENDIENTE,
                "ORALA002",
                "Paciente referido para apoyo diagnostico."
        );
        crearCitaSiNoExiste(
                paciente3,
                ortodoncia,
                LocalDateTime.of(2026, 4, 28, 10, 0),
                "Control de ortodoncia",
                EstadoCita.CONFIRMADA,
                "ORALA003",
                "Ajuste mensual."
        );
        crearCitaSiNoExiste(
                paciente1,
                blanqueamiento,
                LocalDateTime.of(2026, 4, 29, 16, 0),
                "Evaluacion para blanqueamiento",
                EstadoCita.PENDIENTE,
                "ORALA004",
                "Paciente solicita tratamiento estetico."
        );
        crearCitaSiNoExiste(
                paciente2,
                protesis,
                LocalDateTime.of(2026, 5, 1, 11, 0),
                "Revision de protesis",
                EstadoCita.REPROGRAMADA,
                "ORALA005",
                "Cita reprogramada por disponibilidad del paciente."
        );
        crearCitaSiNoExiste(
                paciente3,
                rayoX,
                LocalDateTime.of(2026, 5, 2, 9, 0),
                "Rayo X de seguimiento",
                EstadoCita.CONFIRMADA,
                "ORALA006",
                "Control sabatino."
        );
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
            RolUsuario rol,
            Paciente paciente
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
        usuario.setPaciente(paciente);
        return usuarioRepository.save(usuario);
    }

    private void crearCitaSiNoExiste(
            Paciente paciente,
            Servicio servicio,
            LocalDateTime fechaHoraInicio,
            String motivo,
            EstadoCita estado,
            String codigo,
            String notas
    ) {
        if (citaRepository.existsByCodigoGestion(codigo)) {
            return;
        }

        LocalDateTime fechaHoraFin = fechaHoraInicio.plusMinutes(30);
        Cita cita = new Cita();
        cita.setPaciente(paciente);
        cita.setServicio(servicio);
        cita.setFechaHoraInicio(fechaHoraInicio);
        cita.setFechaHoraFin(fechaHoraFin);
        cita.setMotivo(motivo);
        cita.setEstado(estado);
        cita.setCodigoGestion(codigo);
        cita.setNotas(notas);
        citaRepository.save(cita);
    }
}
