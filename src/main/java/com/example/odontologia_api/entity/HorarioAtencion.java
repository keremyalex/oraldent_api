package com.example.odontologia_api.entity;

import com.example.odontologia_api.enums.DiaSemana;
import com.example.odontologia_api.persistence.DiaSemanaConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "horarios")
public class HorarioAtencion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Convert(converter = DiaSemanaConverter.class)
    @Column(nullable = false, length = 20)
    private DiaSemana diaSemana;

    @Column(nullable = false)
    private LocalTime horaInicio;

    @Column(nullable = false)
    private LocalTime horaFin;

    @Column(nullable = false)
    private Integer duracionCitaMinutos;

    @Column(length = 200)
    private String observacion;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(nullable = false)
    private LocalDateTime fechaActualizacion;

    @PrePersist
    void prePersist() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = fechaCreacion;
    }

    @PreUpdate
    void preUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public DiaSemana getDiaSemana() {
        return diaSemana;
    }

    public void setDiaSemana(DiaSemana diaSemana) {
        this.diaSemana = diaSemana;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(LocalTime horaFin) {
        this.horaFin = horaFin;
    }

    public Integer getDuracionCitaMinutos() {
        return duracionCitaMinutos;
    }

    public void setDuracionCitaMinutos(Integer duracionCitaMinutos) {
        this.duracionCitaMinutos = duracionCitaMinutos;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }
}
