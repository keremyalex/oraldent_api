package com.example.odontologia_api.entity;

import com.example.odontologia_api.enums.SeveridadPerdidaOsea;
import com.example.odontologia_api.enums.TipoPerdidaOsea;
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "radiografias")
public class Radiografia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ficha_clinica_id", nullable = false)
    private FichaClinica fichaClinica;

    @Column(nullable = false, length = 160)
    private String titulo;

    @Column(length = 500)
    private String descripcion;

    @Column(length = 80)
    private String tipo;

    @Column(name = "numero_fdi")
    private Integer numeroFdi;

    @Column(length = 80)
    private String zona;

    @Column(nullable = false, length = 500)
    private String imagenUrl;

    @Column(nullable = false, length = 255)
    private String imagenPublicId;

    @Column(length = 255)
    private String nombreArchivo;

    @Column(length = 80)
    private String formato;

    private Long tamanoBytes;

    private Integer anchoPx;

    private Integer altoPx;

    private LocalDate fechaEstudio;

    @Column(length = 1000)
    private String diagnosticoRadiografico;

    private Boolean perdidaOseaObservada = false;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private TipoPerdidaOsea tipoPerdidaOsea;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private SeveridadPerdidaOsea severidadPerdidaOsea;

    @Column(precision = 5, scale = 2)
    private BigDecimal porcentajePerdidaOseaEstimado;

    @Column(precision = 6, scale = 2)
    private BigDecimal nivelCrestaOseaMm;

    @Column(length = 1000)
    private String observacionesPeriodontales;

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

    public Long getId() { return id; }
    public FichaClinica getFichaClinica() { return fichaClinica; }
    public void setFichaClinica(FichaClinica fichaClinica) { this.fichaClinica = fichaClinica; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public Integer getNumeroFdi() { return numeroFdi; }
    public void setNumeroFdi(Integer numeroFdi) { this.numeroFdi = numeroFdi; }
    public String getZona() { return zona; }
    public void setZona(String zona) { this.zona = zona; }
    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }
    public String getImagenPublicId() { return imagenPublicId; }
    public void setImagenPublicId(String imagenPublicId) { this.imagenPublicId = imagenPublicId; }
    public String getNombreArchivo() { return nombreArchivo; }
    public void setNombreArchivo(String nombreArchivo) { this.nombreArchivo = nombreArchivo; }
    public String getFormato() { return formato; }
    public void setFormato(String formato) { this.formato = formato; }
    public Long getTamanoBytes() { return tamanoBytes; }
    public void setTamanoBytes(Long tamanoBytes) { this.tamanoBytes = tamanoBytes; }
    public Integer getAnchoPx() { return anchoPx; }
    public void setAnchoPx(Integer anchoPx) { this.anchoPx = anchoPx; }
    public Integer getAltoPx() { return altoPx; }
    public void setAltoPx(Integer altoPx) { this.altoPx = altoPx; }
    public LocalDate getFechaEstudio() { return fechaEstudio; }
    public void setFechaEstudio(LocalDate fechaEstudio) { this.fechaEstudio = fechaEstudio; }
    public String getDiagnosticoRadiografico() { return diagnosticoRadiografico; }
    public void setDiagnosticoRadiografico(String diagnosticoRadiografico) { this.diagnosticoRadiografico = diagnosticoRadiografico; }
    public Boolean getPerdidaOseaObservada() { return perdidaOseaObservada; }
    public void setPerdidaOseaObservada(Boolean perdidaOseaObservada) { this.perdidaOseaObservada = perdidaOseaObservada; }
    public TipoPerdidaOsea getTipoPerdidaOsea() { return tipoPerdidaOsea; }
    public void setTipoPerdidaOsea(TipoPerdidaOsea tipoPerdidaOsea) { this.tipoPerdidaOsea = tipoPerdidaOsea; }
    public SeveridadPerdidaOsea getSeveridadPerdidaOsea() { return severidadPerdidaOsea; }
    public void setSeveridadPerdidaOsea(SeveridadPerdidaOsea severidadPerdidaOsea) { this.severidadPerdidaOsea = severidadPerdidaOsea; }
    public BigDecimal getPorcentajePerdidaOseaEstimado() { return porcentajePerdidaOseaEstimado; }
    public void setPorcentajePerdidaOseaEstimado(BigDecimal porcentajePerdidaOseaEstimado) { this.porcentajePerdidaOseaEstimado = porcentajePerdidaOseaEstimado; }
    public BigDecimal getNivelCrestaOseaMm() { return nivelCrestaOseaMm; }
    public void setNivelCrestaOseaMm(BigDecimal nivelCrestaOseaMm) { this.nivelCrestaOseaMm = nivelCrestaOseaMm; }
    public String getObservacionesPeriodontales() { return observacionesPeriodontales; }
    public void setObservacionesPeriodontales(String observacionesPeriodontales) { this.observacionesPeriodontales = observacionesPeriodontales; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
}
