package com.example.odontologia_api.repository;

import com.example.odontologia_api.entity.FichaClinica;
import com.example.odontologia_api.entity.Paciente;
import com.example.odontologia_api.entity.Periodontograma;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PeriodontogramaRepository extends JpaRepository<Periodontograma, Long> {

    Optional<Periodontograma> findFirstByPacienteAndActivoTrueOrderByIdDesc(Paciente paciente);

    Optional<Periodontograma> findFirstByFichaClinicaAndActivoTrueOrderByIdDesc(FichaClinica fichaClinica);
}
