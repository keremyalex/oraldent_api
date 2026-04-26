package com.example.odontologia_api.repository;

import com.example.odontologia_api.entity.Servicio;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServicioRepository extends JpaRepository<Servicio, Long> {

    List<Servicio> findByActivoTrueOrderByNombreAsc();

    Optional<Servicio> findByNombreIgnoreCase(String nombre);
}
