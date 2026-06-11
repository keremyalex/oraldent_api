package com.example.odontologia_api.repository;

import com.example.odontologia_api.entity.FichaClinica;
import com.example.odontologia_api.entity.Radiografia;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RadiografiaRepository extends JpaRepository<Radiografia, Long> {

    List<Radiografia> findByFichaClinicaAndActivoTrueOrderByFechaEstudioDescFechaCreacionDescIdDesc(FichaClinica fichaClinica);
}
