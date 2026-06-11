package com.example.odontologia_api.repository;

import com.example.odontologia_api.entity.FichaClinica;
import com.example.odontologia_api.entity.Receta;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecetaRepository extends JpaRepository<Receta, Long> {

    List<Receta> findByFichaClinicaAndActivoTrueOrderByFechaCreacionDescIdDesc(FichaClinica fichaClinica);
}
