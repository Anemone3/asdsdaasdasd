package isil.pe.glassimport.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServicioRequestDTO {

    private String nombre;
    private String descripcion;
    private Double precio;
    private Boolean activo;
    // opcional si quieres editarlo también desde otros lados
    private Boolean habilitado;
}
