package com.example.odontologia_api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "fichas_clinicas")
public class FichaClinica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cita_id", unique = true)
    private Cita cita;

    @Column(nullable = false)
    private LocalDateTime fecha;

    private Integer edad;

    @Column(length = 30)
    private String sexo;

    @Column(length = 120)
    private String procedencia;

    @Column(length = 120)
    private String ocupacion;

    @Column(length = 30)
    private String presionArterial;

    @Column(precision = 4, scale = 1)
    private BigDecimal temperatura;

    private Integer pulso;

    @Column(length = 500)
    private String motivoConsulta;

    @Column(length = 1000)
    private String enfermedadActual;

    @Column(length = 1000)
    private String anamnesis;

    private Boolean hemorragia = false;
    private Boolean diabetes = false;
    private Boolean hipertension = false;
    private Boolean epilepsia = false;
    private Boolean problemasCardiovasculares = false;
    private Boolean lipotimias = false;
    private Boolean tratamientoMedicoActual = false;

    @Column(length = 500)
    private String alergias;

    @Column(length = 500)
    private String medicamentoActual;

    @Column(length = 500)
    private String otrasPatologias;

    @Column(length = 1200)
    private String examenClinico;

    @Column(length = 1200)
    private String examenRadiografico;

    @Column(length = 1200)
    private String diagnostico;

    @Column(length = 1200)
    private String tratamiento;

    @Column(length = 800)
    private String tecnicaAnestesia;

    @Column(length = 1200)
    private String evolucion;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(nullable = false)
    private LocalDateTime fechaActualizacion;

    @PrePersist
    void prePersist() {
        if (fecha == null) {
            fecha = LocalDateTime.now();
        }
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = fechaCreacion;
    }

    @PreUpdate
    void preUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Paciente getPaciente() { return paciente; }
    public void setPaciente(Paciente paciente) { this.paciente = paciente; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public Cita getCita() { return cita; }
    public void setCita(Cita cita) { this.cita = cita; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public Integer getEdad() { return edad; }
    public void setEdad(Integer edad) { this.edad = edad; }
    public String getSexo() { return sexo; }
    public void setSexo(String sexo) { this.sexo = sexo; }
    public String getProcedencia() { return procedencia; }
    public void setProcedencia(String procedencia) { this.procedencia = procedencia; }
    public String getOcupacion() { return ocupacion; }
    public void setOcupacion(String ocupacion) { this.ocupacion = ocupacion; }
    public String getPresionArterial() { return presionArterial; }
    public void setPresionArterial(String presionArterial) { this.presionArterial = presionArterial; }
    public BigDecimal getTemperatura() { return temperatura; }
    public void setTemperatura(BigDecimal temperatura) { this.temperatura = temperatura; }
    public Integer getPulso() { return pulso; }
    public void setPulso(Integer pulso) { this.pulso = pulso; }
    public String getMotivoConsulta() { return motivoConsulta; }
    public void setMotivoConsulta(String motivoConsulta) { this.motivoConsulta = motivoConsulta; }
    public String getEnfermedadActual() { return enfermedadActual; }
    public void setEnfermedadActual(String enfermedadActual) { this.enfermedadActual = enfermedadActual; }
    public String getAnamnesis() { return anamnesis; }
    public void setAnamnesis(String anamnesis) { this.anamnesis = anamnesis; }
    public Boolean getHemorragia() { return hemorragia; }
    public void setHemorragia(Boolean hemorragia) { this.hemorragia = hemorragia; }
    public Boolean getDiabetes() { return diabetes; }
    public void setDiabetes(Boolean diabetes) { this.diabetes = diabetes; }
    public Boolean getHipertension() { return hipertension; }
    public void setHipertension(Boolean hipertension) { this.hipertension = hipertension; }
    public Boolean getEpilepsia() { return epilepsia; }
    public void setEpilepsia(Boolean epilepsia) { this.epilepsia = epilepsia; }
    public Boolean getProblemasCardiovasculares() { return problemasCardiovasculares; }
    public void setProblemasCardiovasculares(Boolean problemasCardiovasculares) { this.problemasCardiovasculares = problemasCardiovasculares; }
    public Boolean getLipotimias() { return lipotimias; }
    public void setLipotimias(Boolean lipotimias) { this.lipotimias = lipotimias; }
    public Boolean getTratamientoMedicoActual() { return tratamientoMedicoActual; }
    public void setTratamientoMedicoActual(Boolean tratamientoMedicoActual) { this.tratamientoMedicoActual = tratamientoMedicoActual; }
    public String getAlergias() { return alergias; }
    public void setAlergias(String alergias) { this.alergias = alergias; }
    public String getMedicamentoActual() { return medicamentoActual; }
    public void setMedicamentoActual(String medicamentoActual) { this.medicamentoActual = medicamentoActual; }
    public String getOtrasPatologias() { return otrasPatologias; }
    public void setOtrasPatologias(String otrasPatologias) { this.otrasPatologias = otrasPatologias; }
    public String getExamenClinico() { return examenClinico; }
    public void setExamenClinico(String examenClinico) { this.examenClinico = examenClinico; }
    public String getExamenRadiografico() { return examenRadiografico; }
    public void setExamenRadiografico(String examenRadiografico) { this.examenRadiografico = examenRadiografico; }
    public String getDiagnostico() { return diagnostico; }
    public void setDiagnostico(String diagnostico) { this.diagnostico = diagnostico; }
    public String getTratamiento() { return tratamiento; }
    public void setTratamiento(String tratamiento) { this.tratamiento = tratamiento; }
    public String getTecnicaAnestesia() { return tecnicaAnestesia; }
    public void setTecnicaAnestesia(String tecnicaAnestesia) { this.tecnicaAnestesia = tecnicaAnestesia; }
    public String getEvolucion() { return evolucion; }
    public void setEvolucion(String evolucion) { this.evolucion = evolucion; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
}
