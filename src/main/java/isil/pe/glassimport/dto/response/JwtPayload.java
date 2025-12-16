package isil.pe.glassimport.dto.response;

import java.util.List;

public record JwtPayload(String sub,
                         String email,
                         List<String> role) {
}