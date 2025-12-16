package isil.pe.glassimport.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Builder
@Data
@Getter
@Setter
public class ErrorResponseDTO {
    private String errorMsg;
    private String path;
    private int status;
}
