package isil.pe.glassimport.dto.request;

public record RegisterRequest(
        String email,
        String password,
        String username
) {
}