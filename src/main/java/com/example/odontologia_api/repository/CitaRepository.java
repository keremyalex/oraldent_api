package com.example.odontologia_api.repository;

import com.example.odontologia_api.entity.Cita;
import com.example.odontologia_api.enums.EstadoCita;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CitaRepository extends JpaRepository<Cita, Long> {

    boolean existsByCodigoGestion(String codigoGestion);

    Optional<Cita> findByCodigoGestion(String codigoGestion);

    List<Cita> findAllByOrderByFechaHoraInicioAsc();

    List<Cita> findByFechaHoraInicioBetweenOrderByFechaHoraInicioAsc(LocalDateTime inicio, LocalDateTime fin);

    Optional<Cita> findByIdAndCodigoGestion(Long id, String codigoGestion);

    @Query("""
            select count(c) > 0
            from Cita c
            where c.estado not in :estadosExcluidos
              and (:citaIdIgnorada is null or c.id <> :citaIdIgnorada)
              and c.fechaHoraInicio < :fin
              and c.fechaHoraFin > :inicio
            """)
    boolean existsSolapamiento(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin,
            @Param("estadosExcluidos") Collection<EstadoCita> estadosExcluidos,
            @Param("citaIdIgnorada") Long citaIdIgnorada
    );
}
