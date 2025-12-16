package isil.pe.glassimport.entity;

import jakarta.persistence.*;
import lombok.*;
import java.sql.Timestamp;

@Entity
@Table(name = "reservas")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reserva {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String estado; // PENDIENTE, APROBADA, CONFIRMADA, CANCELADA, COMPLETADA

    private Timestamp fecha;

    // ✅ AGREGA ESTE CAMPO
    private String servicio; // Ejemplo: "Cambio de aceite", "Reparación de frenos", etc.

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "automovil_id")
    private Automovil automovil;

    @OneToOne
    @JoinColumn(name = "horario_id")
    private Horario horario;
}
