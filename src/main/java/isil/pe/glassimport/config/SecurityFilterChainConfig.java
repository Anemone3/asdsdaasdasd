package isil.pe.glassimport.config;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import isil.pe.glassimport.dto.response.AuthResponse;
import isil.pe.glassimport.dto.response.JwtPayload;
import isil.pe.glassimport.entity.User;
import isil.pe.glassimport.repository.UserRepository;
import isil.pe.glassimport.services.JwtService;

@Configuration
@EnableWebSecurity
public class SecurityFilterChainConfig {

    private final JwtAuthorization jwtAuthorizationFilter;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public SecurityFilterChainConfig(JwtAuthorization jwtAuthorizationFilter, JwtService jwtService,
            UserRepository userRepository) {
        this.jwtAuthorizationFilter = jwtAuthorizationFilter;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        .requestMatchers("/auth/**", "/oauth2/**", "/login/**", "/api/automoviles/**",
                                "/api/horarios/**")
                        .permitAll()
                        .anyRequest().permitAll())
                .addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class)
                .oauth2Login(oauth -> oauth
                        .authorizationEndpoint(authorizationEndpointConfig -> authorizationEndpointConfig
                                .baseUri("/oauth2/authorization"))
                        .redirectionEndpoint(
                                redirectionEndpointConfig -> redirectionEndpointConfig.baseUri("/oauth2/code/**"))
                        .successHandler((request, response, authentication) -> {
                            try {
                                OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

                                // Extraer datos del usuario de Google
                                String email = oAuth2User.getAttribute("email");
                                String name = oAuth2User.getAttribute("name");
                                String googleId = oAuth2User.getAttribute("sub");

                                System.out.println("✅ Usuario autenticado con Google: " + email);

                                // Buscar o crear usuario
                                User user = userRepository.findByEmail(email)
                                        .orElseGet(() -> {
                                            System.out.println("🆕 Creando nuevo usuario: " + email);
                                            User newUser = User.builder()
                                                    .email(email)
                                                    .username(name)
                                                    .googleId(googleId)
                                                    .estado("ACTIVO")
                                                    .build();
                                            return userRepository.save(newUser);
                                        });

                                System.out.println("✅ Usuario guardado/encontrado - ID: " + user.getId() + ", Nombre: "
                                        + user.getUsername());

                                // Generar token JWT
                                long expiration = 7 * 24 * 60 * 60;
                                JwtPayload tokenPayload = new JwtPayload(
                                        user.getId().toString(),
                                        user.getEmail(),
                                        List.of());
                                String token = jwtService.generateToken(tokenPayload, expiration);

                                // Crear AuthResponse
                                AuthResponse authResponse = AuthResponse.fromUser(user, token);

                                // Redirigir al frontend con los datos
                                String redirectUrl = String.format(
                                        "http://localhost:5173/auth/callback?token=%s&userId=%d&userName=%s&userEmail=%s",
                                        token,
                                        user.getId(),
                                        URLEncoder.encode(user.getUsername(), StandardCharsets.UTF_8),
                                        URLEncoder.encode(user.getEmail(), StandardCharsets.UTF_8));

                                response.sendRedirect(redirectUrl);

                            } catch (Exception e) {
                                System.err.println("❌ Error en OAuth2 login: " + e.getMessage());
                                e.printStackTrace();
                                try {
                                    response.sendRedirect("http://localhost:5173/auth?error=true");
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }))
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(false);

        source.registerCorsConfiguration("/**", config);
        return source;
    }

}
