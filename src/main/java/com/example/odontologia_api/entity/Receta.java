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
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "recetas")
public class Receta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ficha_clinica_id", nullable = false)
    private FichaClinica fichaClinica;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(length = 1200)
    private String indicacionesGenerales;

    @Column(length = 1200)
    private String observaciones;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(nullable = false)
    private LocalDateTime fechaActualizacion;

    @OneToMany(mappedBy = "receta", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orden asc, id asc")
    private List<RecetaDetalle> detalles = new ArrayList<>();

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

    public FichaClinica getFichaClinica() {
        return fichaClinica;
    }

    public void setFichaClinica(FichaClinica fichaClinica) {
        this.fichaClinica = fichaClinica;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getIndicacionesGenerales() {
        return indicacionesGenerales;
    }

    public void setIndicacionesGenerales(String indicacionesGenerales) {
        this.indicacionesGenerales = indicacionesGenerales;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
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

    public List<RecetaDetalle> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<RecetaDetalle> detalles) {
        this.detalles.clear();
        if (detalles == null) {
            return;
        }
        for (RecetaDetalle detalle : detalles) {
            addDetalle(detalle);
        }
    }

    public void addDetalle(RecetaDetalle detalle) {
        detalle.setReceta(this);
        this.detalles.add(detalle);
    }
}
