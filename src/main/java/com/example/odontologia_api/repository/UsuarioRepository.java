package com.example.odontologia_api.repository;

import com.example.odontologia_api.entity.Usuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    boolean existsByCorreoIgnoreCase(String correo);

    boolean existsByCelular(String celular);

    Optional<Usuario> findByCorreoIgnoreCase(String correo);

    Optional<Usuario> findByCelular(String celular);

    Optional<Usuario> findByCorreoIgnoreCaseOrCelular(String correo, String celular);
}
