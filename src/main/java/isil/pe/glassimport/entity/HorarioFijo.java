package isil.pe.glassimport.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "horarios_fijos")
public class HorarioFijo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Formato "HH:MM"
    @Column(nullable = false, unique = true, length = 5)
    private String hora;

    @Column(nullable = false)
    private Boolean habilitado = true;

    public Long getId() { return id; }

    public String getHora() { return hora; }
    public void setHora(String hora) { this.hora = hora; }

    public Boolean getHabilitado() { return habilitado; }
    public void setHabilitado(Boolean habilitado) { this.habilitado = habilitado; }
}
