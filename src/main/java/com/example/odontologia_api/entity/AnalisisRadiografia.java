package com.example.odontologia_api.entity;

import com.example.odontologia_api.enums.EstadoAnalisisRadiografia;
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
import java.time.LocalDateTime;

@Entity
@Table(name = "analisis_radiografias")
public class AnalisisRadiografia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "radiografia_id", nullable = false)
    private Radiografia radiografia;

    @Column(length = 120)
    private String modelo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoAnalisisRadiografia estado = EstadoAnalisisRadiografia.PENDIENTE;

    private Boolean perdidaOseaDetectada = false;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private TipoPerdidaOsea tipoPerdidaOsea;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private SeveridadPerdidaOsea severidad;

    @Column(precision = 5, scale = 2)
    private BigDecimal porcentajePerdidaOsea;

    @Column(precision = 5, scale = 4)
    private BigDecimal confianza;

    @Column(columnDefinition = "text")
    private String resultadoJson;

    @Column(length = 1000)
    private String recomendacion;

    @Column(length = 1000)
    private String errorAnalisis;

    private Boolean validado = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "validado_por_usuario_id")
    private Usuario validadoPorUsuario;

    private LocalDateTime fechaValidacion;

    @Column(length = 1000)
    private String comentarioValidacion;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private SeveridadPerdidaOsea severidadFinal;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private TipoPerdidaOsea tipoPerdidaOseaFinal;

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
    public Radiografia getRadiografia() { return radiografia; }
    public void setRadiografia(Radiografia radiografia) { this.radiografia = radiografia; }
    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }
    public EstadoAnalisisRadiografia getEstado() { return estado; }
    public void setEstado(EstadoAnalisisRadiografia estado) { this.estado = estado; }
    public Boolean getPerdidaOseaDetectada() { return perdidaOseaDetectada; }
    public void setPerdidaOseaDetectada(Boolean perdidaOseaDetectada) { this.perdidaOseaDetectada = perdidaOseaDetectada; }
    public TipoPerdidaOsea getTipoPerdidaOsea() { return tipoPerdidaOsea; }
    public void setTipoPerdidaOsea(TipoPerdidaOsea tipoPerdidaOsea) { this.tipoPerdidaOsea = tipoPerdidaOsea; }
    public SeveridadPerdidaOsea getSeveridad() { return severidad; }
    public void setSeveridad(SeveridadPerdidaOsea severidad) { this.severidad = severidad; }
    public BigDecimal getPorcentajePerdidaOsea() { return porcentajePerdidaOsea; }
    public void setPorcentajePerdidaOsea(BigDecimal porcentajePerdidaOsea) { this.porcentajePerdidaOsea = porcentajePerdidaOsea; }
    public BigDecimal getConfianza() { return confianza; }
    public void setConfianza(BigDecimal confianza) { this.confianza = confianza; }
    public String getResultadoJson() { return resultadoJson; }
    public void setResultadoJson(String resultadoJson) { this.resultadoJson = resultadoJson; }
    public String getRecomendacion() { return recomendacion; }
    public void setRecomendacion(String recomendacion) { this.recomendacion = recomendacion; }
    public String getErrorAnalisis() { return errorAnalisis; }
    public void setErrorAnalisis(String errorAnalisis) { this.errorAnalisis = errorAnalisis; }
    public Boolean getValidado() { return validado; }
    public void setValidado(Boolean validado) { this.validado = validado; }
    public Usuario getValidadoPorUsuario() { return validadoPorUsuario; }
    public void setValidadoPorUsuario(Usuario validadoPorUsuario) { this.validadoPorUsuario = validadoPorUsuario; }
    public LocalDateTime getFechaValidacion() { return fechaValidacion; }
    public void setFechaValidacion(LocalDateTime fechaValidacion) { this.fechaValidacion = fechaValidacion; }
    public String getComentarioValidacion() { return comentarioValidacion; }
    public void setComentarioValidacion(String comentarioValidacion) { this.comentarioValidacion = comentarioValidacion; }
    public SeveridadPerdidaOsea getSeveridadFinal() { return severidadFinal; }
    public void setSeveridadFinal(SeveridadPerdidaOsea severidadFinal) { this.severidadFinal = severidadFinal; }
    public TipoPerdidaOsea getTipoPerdidaOseaFinal() { return tipoPerdidaOseaFinal; }
    public void setTipoPerdidaOseaFinal(TipoPerdidaOsea tipoPerdidaOseaFinal) { this.tipoPerdidaOseaFinal = tipoPerdidaOseaFinal; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
}
