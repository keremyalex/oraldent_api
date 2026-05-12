package com.example.odontologia_api.entity;

import com.example.odontologia_api.enums.ColorCaraOdontograma;
import com.example.odontologia_api.enums.TipoCaraOdontograma;
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
@Table(name = "odontograma_caras")
public class OdontogramaCara {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "diente_id", nullable = false)
    private OdontogramaDiente diente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoCaraOdontograma tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ColorCaraOdontograma color = ColorCaraOdontograma.NINGUNO;

    @Column(length = 500)
    private String descripcion;

    public Long getId() {
        return id;
    }

    public OdontogramaDiente getDiente() {
        return diente;
    }

    public void setDiente(OdontogramaDiente diente) {
        this.diente = diente;
    }

    public TipoCaraOdontograma getTipo() {
        return tipo;
    }

    public void setTipo(TipoCaraOdontograma tipo) {
        this.tipo = tipo;
    }

    public ColorCaraOdontograma getColor() {
        return color;
    }

    public void setColor(ColorCaraOdontograma color) {
        this.color = color;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
