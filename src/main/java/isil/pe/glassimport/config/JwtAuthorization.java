package isil.pe.glassimport.config;

import com.auth0.jwt.JWT;
import isil.pe.glassimport.services.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthorization extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthorization(
            JwtService jwtService,
            UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // 🔹 Ignorar rutas públicas / OAuth2
        if (path.startsWith("/oauth2")
                || path.startsWith("/login/oauth2")
                || path.startsWith("/api/auth")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui")) {

            filterChain.doFilter(request, response);
            return;
        }

        // 🔹 Obtener Authorization: Bearer <token>
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = authHeader.substring(7);

        // 🔹 Validar token
        if (!jwtService.validateToken(accessToken)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 🔹 Extraer email
        String email = JWT.decode(accessToken)
                .getClaim("email")
                .asString();

        if (email == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 🔹 Cargar usuario
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        // 🔹 Setear autenticación si no existe
        if (SecurityContextHolder.getContext().getAuthentication() == null) {

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        filterChain.doFilter(request, response);
    }
}
