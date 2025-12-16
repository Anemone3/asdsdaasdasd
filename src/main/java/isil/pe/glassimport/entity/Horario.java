package isil.pe.glassimport.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "horarios")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Horario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Formato: "YYYY-MM-DD"
    @Column(nullable = false, length = 10)
    private String fecha;

    // Formato: "HH:mm" (ej: "09:00", "10:30")
    @Column(nullable = false, length = 5)
    private String hora;

    // Valores: LIBRE, EN_PROCESO, OCUPADO
    @Column(nullable = false, length = 20)
    private String estado;

    // Relación bidireccional con Reserva
    @OneToOne(mappedBy = "horario")
    private Reserva reserva;
}
