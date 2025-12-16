package isil.pe.glassimport.dto.request;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutomovilPatchDto {
    
    @Size(max = 50, message = "La marca no puede exceder 50 caracteres")
    private String marca;
    
    @Size(max = 50, message = "El modelo no puede exceder 50 caracteres")
    private String modelo;
    
    @Positive(message = "El año debe ser un número positivo")
    private Integer anio;
    
    @Size(max = 500, message = "La nota no puede exceder 500 caracteres")
    private String nota;
}
