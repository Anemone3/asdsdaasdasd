package isil.pe.glassimport.dto.request;

import lombok.Data;

@Data
public class ServicioRequestDTO {
    private String nombre;
    private String descripcion;
    private Double precio;
    private Boolean activo;
}
