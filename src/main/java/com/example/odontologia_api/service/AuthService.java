package com.example.odontologia_api.service;

import com.example.odontologia_api.dto.AuthResponse;
import com.example.odontologia_api.dto.ActualizarFotoPerfilRequest;
import com.example.odontologia_api.dto.CloudinaryUploadResult;
import com.example.odontologia_api.dto.LoginRequest;
import com.example.odontologia_api.dto.RegisterPacienteRequest;
import com.example.odontologia_api.dto.RegisterStaffRequest;
import com.example.odontologia_api.dto.UsuarioAuthResponse;
import com.example.odontologia_api.entity.Paciente;
import com.example.odontologia_api.entity.Usuario;
import com.example.odontologia_api.enums.RolUsuario;
import com.example.odontologia_api.exception.RecursoNoEncontradoException;
import com.example.odontologia_api.exception.ReglaNegocioException;
import com.example.odontologia_api.repository.UsuarioRepository;
import com.example.odontologia_api.security.JwtService;
import com.example.odontologia_api.security.UsuarioDetails;
import java.util.HashMap;
import java.util.Map;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PacienteService pacienteService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CloudinaryService cloudinaryService;

    public AuthService(
            UsuarioRepository usuarioRepository,
            PacienteService pacienteService,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            CloudinaryService cloudinaryService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.pacienteService = pacienteService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.cloudinaryService = cloudinaryService;
    }

    @Transactional
    public AuthResponse registerStaff(RegisterStaffRequest request) {
        if (request.rol() == RolUsuario.PACIENTE) {
            throw new ReglaNegocioException("Use el registro de paciente para cuentas con rol PACIENTE.");
        }
        validarDuplicados(request.correo(), request.celular());

        Usuario usuario = new Usuario();
        usuario.setNombre(request.nombre());
        usuario.setApellidoPaterno(request.apellidoPaterno());
        usuario.setApellidoMaterno(request.apellidoMaterno());
        usuario.setCorreo(normalizarCorreo(request.correo()));
        usuario.setCelular(request.celular());
        usuario.setPassword(passwordEncoder.encode(request.password()));
        usuario.setFotoPerfilUrl(request.fotoPerfilUrl());
        usuario.setRol(request.rol());
        usuario.setActivo(true);
        usuario.setVerificado(true);

        Usuario saved = usuarioRepository.save(usuario);
        return construirAuthResponse(saved);
    }

    @Transactional
    public AuthResponse registerPaciente(RegisterPacienteRequest request) {
        validarDuplicados(request.correo(), request.celular());
        Paciente paciente = pacienteService.obtenerOCrearParaUsuario(request);

        Usuario usuario = new Usuario();
        usuario.setNombre(request.nombre());
        usuario.setApellidoPaterno(request.apellidoPaterno());
        usuario.setApellidoMaterno(request.apellidoMaterno());
        usuario.setCorreo(normalizarCorreo(request.correo()));
        usuario.setCelular(request.celular());
        usuario.setPassword(passwordEncoder.encode(request.password()));
        usuario.setRol(RolUsuario.PACIENTE);
        usuario.setActivo(true);
        usuario.setVerificado(true);

        Usuario saved = usuarioRepository.save(usuario);
        return construirAuthResponse(saved);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.identificador(), request.password())
        );

        Usuario usuario = usuarioRepository.findByCorreoIgnoreCaseOrCelular(request.identificador(), request.identificador())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado."));

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new ReglaNegocioException("El usuario está inactivo.");
        }

        return construirAuthResponse(usuario);
    }

    @Transactional(readOnly = true)
    public UsuarioAuthResponse me(Long userId) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado."));
        return toUsuarioResponse(usuario);
    }

    @Transactional
    public UsuarioAuthResponse actualizarFotoPerfil(Long userId, ActualizarFotoPerfilRequest request) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado."));
        usuario.setFotoPerfilUrl(request.fotoPerfilUrl().trim());
        return toUsuarioResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioAuthResponse actualizarFotoPerfil(Long userId, MultipartFile archivo) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado."));

        CloudinaryUploadResult uploadResult = cloudinaryService.replaceImage(
                archivo,
                "oraldent/usuarios",
                usuario.getFotoPerfilPublicId()
        );

        usuario.setFotoPerfilUrl(uploadResult.url());
        usuario.setFotoPerfilPublicId(uploadResult.publicId());
        return toUsuarioResponse(usuarioRepository.save(usuario));
    }

    private void validarDuplicados(String correo, String celular) {
        String correoNormalizado = normalizarCorreo(correo);
        if (correoNormalizado != null && usuarioRepository.existsByCorreoIgnoreCase(correoNormalizado)) {
            throw new ReglaNegocioException("Ya existe un usuario registrado con ese correo.");
        }
        if (usuarioRepository.existsByCelular(celular)) {
            throw new ReglaNegocioException("Ya existe un usuario registrado con ese celular.");
        }
    }

    private AuthResponse construirAuthResponse(Usuario usuario) {
        UsuarioDetails userDetails = new UsuarioDetails(usuario);
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", usuario.getId());
        claims.put("rol", usuario.getRol().name());
        String token = jwtService.generateToken(userDetails, claims);
        return new AuthResponse(token, "Bearer", toUsuarioResponse(usuario));
    }

    private UsuarioAuthResponse toUsuarioResponse(Usuario usuario) {
        return new UsuarioAuthResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getApellidoPaterno(),
                usuario.getApellidoMaterno(),
                usuario.getCorreo(),
                usuario.getCelular(),
                usuario.getRol(),
                usuario.getFotoPerfilUrl()
        );
    }

    private String normalizarCorreo(String correo) {
        if (correo == null || correo.isBlank()) {
            return null;
        }
        return correo.trim().toLowerCase();
    }
}
