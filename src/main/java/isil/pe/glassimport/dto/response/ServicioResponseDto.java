package isil.pe.glassimport.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServicioResponseDto {

    private Long id;
    private String nombre;
    private String descripcion;
    private Double precio;
    private Boolean activo;
    private Boolean habilitado;
}
