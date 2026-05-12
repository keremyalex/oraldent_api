package com.example.odontologia_api.repository;

import com.example.odontologia_api.entity.Odontograma;
import com.example.odontologia_api.entity.Paciente;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OdontogramaRepository extends JpaRepository<Odontograma, Long> {

    boolean existsByPacienteAndActivoTrue(Paciente paciente);

    Optional<Odontograma> findFirstByPacienteAndActivoTrueOrderByIdDesc(Paciente paciente);
}
