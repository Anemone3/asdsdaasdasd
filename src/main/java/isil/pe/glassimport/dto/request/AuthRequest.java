package isil.pe.glassimport.dto.request;

public record AuthRequest(
        String email,
        String password
) {
}