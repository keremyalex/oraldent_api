package com.example.odontologia_api.config;

import com.example.odontologia_api.entity.Cita;
import com.example.odontologia_api.entity.HorarioAtencion;
import com.example.odontologia_api.entity.Paciente;
import com.example.odontologia_api.entity.Usuario;
import com.example.odontologia_api.enums.EstadoCita;
import com.example.odontologia_api.enums.RolUsuario;
import com.example.odontologia_api.repository.CitaRepository;
import com.example.odontologia_api.repository.HorarioAtencionRepository;
import com.example.odontologia_api.repository.PacienteRepository;
import com.example.odontologia_api.repository.UsuarioRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
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
    private final CitaRepository citaRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.enabled:false}")
    private boolean seedEnabled;

    public DataSeeder(
            UsuarioRepository usuarioRepository,
            PacienteRepository pacienteRepository,
            HorarioAtencionRepository horarioRepository,
            CitaRepository citaRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.usuarioRepository = usuarioRepository;
        this.pacienteRepository = pacienteRepository;
        this.horarioRepository = horarioRepository;
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
        Paciente paciente1 = obtenerOCrearPaciente("María Fernanda", "Rojas", "Flores", "70010001", "maria@oraldent.demo");
        Paciente paciente2 = obtenerOCrearPaciente("Luis Alberto", "Mamani", "Quispe", "70010002", "luis@oraldent.demo");
        Paciente paciente3 = obtenerOCrearPaciente("Camila", "Pérez", "Vargas", "70010003", "camila@oraldent.demo");

        obtenerOCrearUsuario(
                "Andrea",
                "Suárez",
                "Mendoza",
                "admin@oraldent.demo",
                "70000001",
                "admin1234",
                RolUsuario.ADMIN,
                null
        );
        obtenerOCrearUsuario(
                "Valeria",
                "Soria",
                "López",
                "doctor@oraldent.demo",
                "70000002",
                "doctor1234",
                RolUsuario.ADMIN,
                null
        );
        obtenerOCrearUsuario(
                "Paola",
                "Rivas",
                "Torrez",
                "recepcion@oraldent.demo",
                "70000003",
                "recepcion1234",
                RolUsuario.RECEPCION,
                null
        );
        obtenerOCrearUsuario(
                paciente1.getNombre(),
                paciente1.getApellidoPaterno(),
                paciente1.getApellidoMaterno(),
                "paciente@oraldent.demo",
                "70000004",
                "paciente1234",
                RolUsuario.PACIENTE,
                paciente1
        );

        crearCitaSiNoExiste(paciente1, LocalTime.of(9, 0), "Limpieza dental", EstadoCita.CONFIRMADA, "SEEDA001");
        crearCitaSiNoExiste(paciente2, LocalTime.of(10, 0), "Evaluación general", EstadoCita.PENDIENTE, "SEEDA002");
        crearCitaSiNoExiste(paciente3, LocalTime.of(11, 30), "Control de ortodoncia", EstadoCita.CONFIRMADA, "SEEDA003");
        log.info("Seed de demo verificado correctamente.");
    }

    private void crearHorariosSiFaltan() {
        crearHorarioSiNoExiste(DayOfWeek.MONDAY);
        crearHorarioSiNoExiste(DayOfWeek.TUESDAY);
        crearHorarioSiNoExiste(DayOfWeek.WEDNESDAY);
        crearHorarioSiNoExiste(DayOfWeek.THURSDAY);
        crearHorarioSiNoExiste(DayOfWeek.FRIDAY);
    }

    private void crearHorarioSiNoExiste(DayOfWeek dayOfWeek) {
        if (!horarioRepository.findByDiaSemanaAndActivoTrueOrderByHoraInicioAsc(dayOfWeek).isEmpty()) {
            return;
        }

        HorarioAtencion horario = new HorarioAtencion();
        horario.setDiaSemana(dayOfWeek);
        horario.setHoraInicio(LocalTime.of(8, 30));
        horario.setHoraFin(LocalTime.of(18, 30));
        horario.setDuracionCitaMinutos(30);
        horario.setObservacion("Turnos habilitados");
        horario.setActivo(true);
        horarioRepository.save(horario);
    }

    private Paciente obtenerOCrearPaciente(String nombre, String paterno, String materno, String celular, String correo) {
        Optional<Paciente> porCelular = pacienteRepository.findFirstByCelularAndActivoTrueOrderByIdDesc(celular);
        if (porCelular.isPresent()) {
            return porCelular.get();
        }

        Optional<Paciente> porCorreo = pacienteRepository.findFirstByCorreoIgnoreCaseAndActivoTrueOrderByIdDesc(correo);
        if (porCorreo.isPresent()) {
            return porCorreo.get();
        }

        Paciente paciente = new Paciente();
        paciente.setNombre(nombre);
        paciente.setApellidoPaterno(paterno);
        paciente.setApellidoMaterno(materno);
        paciente.setCelular(celular);
        paciente.setCorreo(correo);
        paciente.setDocumentoIdentidad("CI-" + celular.substring(celular.length() - 4));
        paciente.setDireccion("Zona central");
        paciente.setActivo(true);
        return pacienteRepository.save(paciente);
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
            LocalTime horaInicio,
            String motivo,
            EstadoCita estado,
            String codigo
    ) {
        if (citaRepository.existsByCodigoGestion(codigo)) {
            return;
        }

        LocalDate fechaBase = LocalDate.now().plusDays(1);
        LocalDateTime fechaHoraInicio = LocalDateTime.of(fechaBase, horaInicio);
        LocalDateTime fechaHoraFin = LocalDateTime.of(fechaBase, horaInicio.plusMinutes(30));
        Cita cita = new Cita();
        cita.setPaciente(paciente);
        cita.setFechaHoraInicio(fechaHoraInicio);
        cita.setFechaHoraFin(fechaHoraFin);
        cita.setMotivo(motivo);
        cita.setEstado(estado);
        cita.setCodigoGestion(codigo);
        cita.setNotas("Registro inicial de demostración.");
        citaRepository.save(cita);
    }
}
