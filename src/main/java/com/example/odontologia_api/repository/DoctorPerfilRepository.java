package com.example.odontologia_api.repository;

import com.example.odontologia_api.entity.DoctorPerfil;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DoctorPerfilRepository extends JpaRepository<DoctorPerfil, Long> {

    List<DoctorPerfil> findByActivoTrueOrderByIdAsc();
}
