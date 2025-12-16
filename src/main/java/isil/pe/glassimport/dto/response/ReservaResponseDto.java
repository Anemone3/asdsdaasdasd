package isil.pe.glassimport.dto.response;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservaResponseDto {
    
    private Long id;
    private String estado;
    private Timestamp fecha;
    private UserResponseDto user;
    private AutomovilResponseDto automovil;
    String servicio;
}
