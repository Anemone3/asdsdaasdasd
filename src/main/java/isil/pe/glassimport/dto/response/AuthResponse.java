package isil.pe.glassimport.dto.response;

import isil.pe.glassimport.entity.User;

public record AuthResponse(
        UserResponseDto user,
        String accessToken
) {
    public static AuthResponse fromUser(User user, String accessToken) {
        return new AuthResponse(User.entityToDto(user),accessToken);
    }
}