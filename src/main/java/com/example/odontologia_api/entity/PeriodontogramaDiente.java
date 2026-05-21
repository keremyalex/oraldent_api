package com.example.odontologia_api.entity;

import com.example.odontologia_api.enums.FurcacionPeriodontograma;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "periodontograma_dientes")
public class PeriodontogramaDiente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "periodontograma_id", nullable = false)
    private Periodontograma periodontograma;

    @Column(nullable = false)
    private Integer numeroFdi;

    @Column(nullable = false)
    private Integer cuadrante;

    @Column(nullable = false)
    private Integer posicion;

    @Column(nullable = false)
    private Boolean ausente = false;

    @Column(nullable = false)
    private Boolean implante = false;

    private Integer movilidad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FurcacionPeriodontograma furcacionVestibular = FurcacionPeriodontograma.NINGUNA;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FurcacionPeriodontograma furcacionPalatinaLingual = FurcacionPeriodontograma.NINGUNA;

    @Column(length = 500)
    private String observacion;

    @OneToMany(mappedBy = "diente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PeriodontogramaSitio> sitios = new ArrayList<>();

    public void addSitio(PeriodontogramaSitio sitio) {
        sitios.add(sitio);
        sitio.setDiente(this);
    }

    public Long getId() { return id; }
    public Periodontograma getPeriodontograma() { return periodontograma; }
    public void setPeriodontograma(Periodontograma periodontograma) { this.periodontograma = periodontograma; }
    public Integer getNumeroFdi() { return numeroFdi; }
    public void setNumeroFdi(Integer numeroFdi) { this.numeroFdi = numeroFdi; }
    public Integer getCuadrante() { return cuadrante; }
    public void setCuadrante(Integer cuadrante) { this.cuadrante = cuadrante; }
    public Integer getPosicion() { return posicion; }
    public void setPosicion(Integer posicion) { this.posicion = posicion; }
    public Boolean getAusente() { return ausente; }
    public void setAusente(Boolean ausente) { this.ausente = ausente; }
    public Boolean getImplante() { return implante; }
    public void setImplante(Boolean implante) { this.implante = implante; }
    public Integer getMovilidad() { return movilidad; }
    public void setMovilidad(Integer movilidad) { this.movilidad = movilidad; }
    public FurcacionPeriodontograma getFurcacionVestibular() { return furcacionVestibular; }
    public void setFurcacionVestibular(FurcacionPeriodontograma furcacionVestibular) { this.furcacionVestibular = furcacionVestibular; }
    public FurcacionPeriodontograma getFurcacionPalatinaLingual() { return furcacionPalatinaLingual; }
    public void setFurcacionPalatinaLingual(FurcacionPeriodontograma furcacionPalatinaLingual) { this.furcacionPalatinaLingual = furcacionPalatinaLingual; }
    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
    public List<PeriodontogramaSitio> getSitios() { return sitios; }
}
