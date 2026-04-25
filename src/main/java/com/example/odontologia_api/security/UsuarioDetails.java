package com.example.odontologia_api.security;

import com.example.odontologia_api.entity.Usuario;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class UsuarioDetails implements UserDetails {

    private final Long id;
    private final Long pacienteId;
    private final String username;
    private final String password;
    private final boolean activo;
    private final List<GrantedAuthority> authorities;

    public UsuarioDetails(Usuario usuario) {
        this.id = usuario.getId();
        this.pacienteId = usuario.getPaciente() != null ? usuario.getPaciente().getId() : null;
        this.username = usuario.getCorreo() != null ? usuario.getCorreo() : usuario.getCelular();
        this.password = usuario.getPassword();
        this.activo = Boolean.TRUE.equals(usuario.getActivo());
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name()));
    }

    public Long getId() {
        return id;
    }

    public Long getPacienteId() {
        return pacienteId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isEnabled() {
        return activo;
    }
}
