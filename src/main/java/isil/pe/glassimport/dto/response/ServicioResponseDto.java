package isil.pe.glassimport.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ServicioResponseDto {
    private Long id;
    private String nombre;
    private String descripcion;
    private Double precio;
    private Boolean activo;
}