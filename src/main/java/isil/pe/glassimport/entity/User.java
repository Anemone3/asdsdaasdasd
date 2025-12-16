package isil.pe.glassimport.entity;

import isil.pe.glassimport.dto.response.UserResponseDto;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    private String username;
    private String password;
    private String googleId;
    private String estado;

    // Método de conversión a DTO
    public static UserResponseDto entityToDto(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .estado(user.getEstado())
                .build();
    }

    // ========== Implementación de UserDetails ==========

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Si tienes roles, usa: new SimpleGrantedAuthority("ROLE_" + role)
        // Por ahora, retorna un rol por defecto
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        // Puedes usar email como username si prefieres
        return this.username != null ? this.username : this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        // Puedes usar el campo 'estado' para controlar si está habilitado
        return "ACTIVO".equalsIgnoreCase(this.estado);
    }
}