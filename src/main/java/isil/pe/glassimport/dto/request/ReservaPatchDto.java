package isil.pe.glassimport.dto.request;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservaPatchDto {
    
    private String estado;
    
    @Positive(message = "El ID del usuario debe ser un número positivo")
    private Long userId;
    
    @Positive(message = "El ID del automóvil debe ser un número positivo")
    private Long automovilId;
}
