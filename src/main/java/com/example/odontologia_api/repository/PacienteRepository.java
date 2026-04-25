package com.example.odontologia_api.repository;

import com.example.odontologia_api.entity.Paciente;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PacienteRepository extends JpaRepository<Paciente, Long> {

    List<Paciente> findByActivoTrueOrderByApellidoPaternoAscApellidoMaternoAscNombreAsc();

    Optional<Paciente> findFirstByCelularAndActivoTrueOrderByIdDesc(String celular);

    Optional<Paciente> findFirstByCorreoIgnoreCaseAndActivoTrueOrderByIdDesc(String correo);
}
