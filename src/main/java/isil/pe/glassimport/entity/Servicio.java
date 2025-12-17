package isil.pe.glassimport.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "servicios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Servicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String descripcion;
    private Double precio;

    // ya lo usas para borrado lógico
    private Boolean activo;

    // 🔹 nuevo: para mostrar/ocultar en la app
    private Boolean habilitado;
}
