package com.example.odontologia_api.entity;

import com.example.odontologia_api.enums.SitioPeriodontograma;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "periodontograma_sitios")
public class PeriodontogramaSitio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "diente_id", nullable = false)
    private PeriodontogramaDiente diente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SitioPeriodontograma sitio;

    @Column(nullable = false)
    private Boolean sangradoSondaje = false;

    @Column(nullable = false)
    private Boolean placa = false;

    @Column(nullable = false)
    private Boolean supuracion = false;

    @Column(nullable = false)
    private Integer margenGingivalMm = 0;

    @Column(nullable = false)
    private Integer profundidadSondajeMm = 0;

    @Column(length = 500)
    private String observacion;

    public Long getId() { return id; }
    public PeriodontogramaDiente getDiente() { return diente; }
    public void setDiente(PeriodontogramaDiente diente) { this.diente = diente; }
    public SitioPeriodontograma getSitio() { return sitio; }
    public void setSitio(SitioPeriodontograma sitio) { this.sitio = sitio; }
    public Boolean getSangradoSondaje() { return sangradoSondaje; }
    public void setSangradoSondaje(Boolean sangradoSondaje) { this.sangradoSondaje = sangradoSondaje; }
    public Boolean getPlaca() { return placa; }
    public void setPlaca(Boolean placa) { this.placa = placa; }
    public Boolean getSupuracion() { return supuracion; }
    public void setSupuracion(Boolean supuracion) { this.supuracion = supuracion; }
    public Integer getMargenGingivalMm() { return margenGingivalMm; }
    public void setMargenGingivalMm(Integer margenGingivalMm) { this.margenGingivalMm = margenGingivalMm; }
    public Integer getProfundidadSondajeMm() { return profundidadSondajeMm; }
    public void setProfundidadSondajeMm(Integer profundidadSondajeMm) { this.profundidadSondajeMm = profundidadSondajeMm; }
    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
}
