package com.example.odontologia_api.repository;

import com.example.odontologia_api.entity.Anamnesis;
import com.example.odontologia_api.entity.FichaClinica;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnamnesisRepository extends JpaRepository<Anamnesis, Long> {
    Optional<Anamnesis> findByFichaClinica(FichaClinica fichaClinica);
}
