package isil.pe.glassimport.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservaRequestDto {

    private String estado; // Opcional, por defecto "PENDIENTE"

    @NotNull(message = "La fecha es obligatoria")
    private Timestamp fecha; // ✅ FALTABA ESTE CAMPO

    @NotNull(message = "El servicio es obligatorio")
    private String servicio;

    @NotNull(message = "El ID del usuario es obligatorio")
    @Positive(message = "El ID del usuario debe ser un número positivo")
    private Long userId;

    @NotNull(message = "El ID del automóvil es obligatorio")
    @Positive(message = "El ID del automóvil debe ser un número positivo")
    private Long automovilId;
}
