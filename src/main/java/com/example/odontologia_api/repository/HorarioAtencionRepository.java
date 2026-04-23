package com.example.odontologia_api.repository;

import com.example.odontologia_api.entity.HorarioAtencion;
import java.time.DayOfWeek;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HorarioAtencionRepository extends JpaRepository<HorarioAtencion, Long> {

    List<HorarioAtencion> findByActivoTrueOrderByDiaSemanaAscHoraInicioAsc();

    List<HorarioAtencion> findByDiaSemanaAndActivoTrueOrderByHoraInicioAsc(DayOfWeek diaSemana);
}
