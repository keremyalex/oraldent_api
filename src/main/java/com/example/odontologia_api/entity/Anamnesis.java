package com.example.odontologia_api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "anamnesis")
public class Anamnesis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ficha_clinica_id", nullable = false, unique = true)
    private FichaClinica fichaClinica;

    @Column(length = 1000)
    private String descripcion;

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

    public Long getId() { return id; }
    public FichaClinica getFichaClinica() { return fichaClinica; }
    public void setFichaClinica(FichaClinica fichaClinica) { this.fichaClinica = fichaClinica; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
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
}
