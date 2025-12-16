package isil.pe.glassimport.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutomovilResponseDto {
    
    private Long id;
    private String marca;
    private String modelo;
    private Integer anio;
    private String placa;
    private String nota;
}
