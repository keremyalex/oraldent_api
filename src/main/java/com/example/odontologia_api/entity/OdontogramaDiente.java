package com.example.odontologia_api.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "odontograma_dientes")
public class OdontogramaDiente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "odontograma_id", nullable = false)
    private Odontograma odontograma;

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

    @Column(nullable = false)
    private Boolean corona = false;

    @Column(nullable = false)
    private Boolean endodoncia = false;

    @Column(nullable = false)
    private Boolean extraccionIndicada = false;

    private Integer movilidad;

    @Column(length = 500)
    private String observacion;

    @OneToMany(mappedBy = "diente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OdontogramaCara> caras = new ArrayList<>();

    public void addCara(OdontogramaCara cara) {
        caras.add(cara);
        cara.setDiente(this);
    }

    public Long getId() {
        return id;
    }

    public Odontograma getOdontograma() {
        return odontograma;
    }

    public void setOdontograma(Odontograma odontograma) {
        this.odontograma = odontograma;
    }

    public Integer getNumeroFdi() {
        return numeroFdi;
    }

    public void setNumeroFdi(Integer numeroFdi) {
        this.numeroFdi = numeroFdi;
    }

    public Integer getCuadrante() {
        return cuadrante;
    }

    public void setCuadrante(Integer cuadrante) {
        this.cuadrante = cuadrante;
    }

    public Integer getPosicion() {
        return posicion;
    }

    public void setPosicion(Integer posicion) {
        this.posicion = posicion;
    }

    public Boolean getAusente() {
        return ausente;
    }

    public void setAusente(Boolean ausente) {
        this.ausente = ausente;
    }

    public Boolean getImplante() {
        return implante;
    }

    public void setImplante(Boolean implante) {
        this.implante = implante;
    }

    public Boolean getCorona() {
        return corona;
    }

    public void setCorona(Boolean corona) {
        this.corona = corona;
    }

    public Boolean getEndodoncia() {
        return endodoncia;
    }

    public void setEndodoncia(Boolean endodoncia) {
        this.endodoncia = endodoncia;
    }

    public Boolean getExtraccionIndicada() {
        return extraccionIndicada;
    }

    public void setExtraccionIndicada(Boolean extraccionIndicada) {
        this.extraccionIndicada = extraccionIndicada;
    }

    public Integer getMovilidad() {
        return movilidad;
    }

    public void setMovilidad(Integer movilidad) {
        this.movilidad = movilidad;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public List<OdontogramaCara> getCaras() {
        return caras;
    }
}
