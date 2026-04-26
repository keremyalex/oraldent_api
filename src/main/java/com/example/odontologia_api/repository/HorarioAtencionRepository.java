package com.example.odontologia_api.repository;

import com.example.odontologia_api.entity.HorarioAtencion;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HorarioAtencionRepository extends JpaRepository<HorarioAtencion, Long> {

    List<HorarioAtencion> findByActivoTrueOrderByDiaSemanaAscHoraInicioAsc();

    @Query(
            value = """
                    select *
                    from horarios
                    where activo = true
                      and dia_semana in (:diaSemanaValores)
                    order by hora_inicio asc
                    """,
            nativeQuery = true
    )
    List<HorarioAtencion> findActivosPorDiaSemanaValoresOrderByHoraInicioAsc(
            @Param("diaSemanaValores") Collection<String> diaSemanaValores
    );
}
