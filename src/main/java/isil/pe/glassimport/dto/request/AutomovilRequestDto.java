package isil.pe.glassimport.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutomovilRequestDto {

    @NotBlank(message = "La marca es obligatoria")
    @Size(max = 50, message = "La marca no puede exceder 50 caracteres")
    private String marca;

    @NotBlank(message = "El modelo es obligatorio")
    @Size(max = 50, message = "El modelo no puede exceder 50 caracteres")
    private String modelo;

    @NotNull(message = "El año es obligatorio")
    @Positive(message = "El año debe ser un número positivo")
    private Integer anio;

    // ✅ CORREGIDO - quitar @Positive de String
    @NotBlank(message = "La placa es obligatoria")
    @Size(min = 6, max = 10, message = "La placa debe tener entre 6 y 10 caracteres")
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "La placa solo puede contener letras mayúsculas, números y guiones")
    private String placa;

    @Size(max = 500, message = "La nota no puede exceder 500 caracteres")
    private String nota;
}
