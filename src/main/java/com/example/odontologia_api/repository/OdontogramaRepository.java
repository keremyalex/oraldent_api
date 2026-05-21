package com.example.odontologia_api.repository;

import com.example.odontologia_api.entity.Odontograma;
import com.example.odontologia_api.entity.FichaClinica;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OdontogramaRepository extends JpaRepository<Odontograma, Long> {

    Optional<Odontograma> findFirstByFichaClinicaAndActivoTrueOrderByIdDesc(FichaClinica fichaClinica);
}
