package com.example.odontologia_api.repository;

import com.example.odontologia_api.entity.AnalisisRadiografia;
import com.example.odontologia_api.entity.Radiografia;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalisisRadiografiaRepository extends JpaRepository<AnalisisRadiografia, Long> {

    List<AnalisisRadiografia> findByRadiografiaAndActivoTrueOrderByFechaCreacionDescIdDesc(Radiografia radiografia);
}
