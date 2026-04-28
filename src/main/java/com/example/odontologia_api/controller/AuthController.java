package com.example.odontologia_api.controller;

import com.example.odontologia_api.dto.AuthResponse;
import com.example.odontologia_api.dto.ActualizarPerfilRequest;
import com.example.odontologia_api.dto.ActualizarFotoPerfilRequest;
import com.example.odontologia_api.dto.LoginRequest;
import com.example.odontologia_api.dto.RegisterPacienteRequest;
import com.example.odontologia_api.dto.RegisterStaffRequest;
import com.example.odontologia_api.dto.UsuarioAuthResponse;
import com.example.odontologia_api.security.UsuarioDetails;
import com.example.odontologia_api.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticacion", description = "Operaciones de login y registro")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register/staff")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar usuario interno del sistema")
    public AuthResponse registerStaff(@Valid @RequestBody RegisterStaffRequest request) {
        return authService.registerStaff(request);
    }

    @PostMapping("/register/paciente")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar paciente con cuenta para acceder al sistema")
    public AuthResponse registerPaciente(@Valid @RequestBody RegisterPacienteRequest request) {
        return authService.registerPaciente(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesion con correo o celular")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    @Operation(summary = "Obtener datos del usuario autenticado")
    public UsuarioAuthResponse me(@AuthenticationPrincipal UsuarioDetails usuarioDetails) {
        return authService.me(usuarioDetails.getId());
    }

    @PutMapping("/me")
    @Operation(summary = "Actualizar datos del usuario autenticado")
    public AuthResponse actualizarPerfil(
            @AuthenticationPrincipal UsuarioDetails usuarioDetails,
            @Valid @RequestBody ActualizarPerfilRequest request
    ) {
        return authService.actualizarPerfil(usuarioDetails.getId(), request);
    }

    @PutMapping("/me/foto-perfil")
    @Operation(summary = "Actualizar la foto de perfil del usuario autenticado")
    public UsuarioAuthResponse actualizarFotoPerfil(
            @AuthenticationPrincipal UsuarioDetails usuarioDetails,
            @Valid @RequestBody ActualizarFotoPerfilRequest request
    ) {
        return authService.actualizarFotoPerfil(usuarioDetails.getId(), request);
    }

    @PutMapping(value = "/me/foto-perfil", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Subir la foto de perfil del usuario autenticado a Cloudinary")
    public UsuarioAuthResponse actualizarFotoPerfilArchivo(
            @AuthenticationPrincipal UsuarioDetails usuarioDetails,
            @RequestPart("archivo") MultipartFile archivo
    ) {
        return authService.actualizarFotoPerfil(usuarioDetails.getId(), archivo);
    }
}
