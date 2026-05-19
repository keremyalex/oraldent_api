package com.example.odontologia_api.repository;

import com.example.odontologia_api.entity.FichaClinica;
import com.example.odontologia_api.entity.Paciente;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FichaClinicaRepository extends JpaRepository<FichaClinica, Long> {

    List<FichaClinica> findByPacienteAndActivoTrueOrderByFechaDescIdDesc(Paciente paciente);
}
