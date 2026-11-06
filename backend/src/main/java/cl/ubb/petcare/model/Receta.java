package cl.ubb.petcare.model;

import jakarta.persistence.*;

@Entity
@Table(name = "receta")
public class Receta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "atencion_id", nullable = false)
    private Atencion atencion;

    // Nombre obtenido desde la API externa de medicamentos
    @Column(nullable = false, length = 100)
    private String medicamento;

    @Column(nullable = false, length = 60)
    private String dosis;

    @Column(nullable = false, length = 60)
    private String frecuencia;

    @Column(nullable = false)
    private Integer dias;

    public Receta() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Atencion getAtencion() { return atencion; }
    public void setAtencion(Atencion atencion) { this.atencion = atencion; }

    public String getMedicamento() { return medicamento; }
    public void setMedicamento(String medicamento) { this.medicamento = medicamento; }

    public String getDosis() { return dosis; }
    public void setDosis(String dosis) { this.dosis = dosis; }

    public String getFrecuencia() { return frecuencia; }
    public void setFrecuencia(String frecuencia) { this.frecuencia = frecuencia; }

    public Integer getDias() { return dias; }
    public void setDias(Integer dias) { this.dias = dias; }
}
