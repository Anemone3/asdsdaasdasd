package isil.pe.glassimport.config;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
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

    @Value("${frontend.url}")
    private String FRONTEND_URL;

    public SecurityFilterChainConfig(
            JwtAuthorization jwtAuthorizationFilter,
            JwtService jwtService,
            UserRepository userRepository) {
        this.jwtAuthorizationFilter = jwtAuthorizationFilter;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // =========================
                // CONFIG BÁSICA
                // =========================
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                // =========================
                // MANEJO DE ERRORES 401
                // =========================
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((req, res, ex) -> {
                            res.setStatus(401);
                            res.setContentType("application/json");
                            res.getWriter().write("{\"error\":\"UNAUTHORIZED\"}");
                        }))

                // =========================
                // AUTORIZACIÓN
                // =========================
                .authorizeHttpRequests(auth -> auth

                        // Swagger (solo dev)
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html")
                        .permitAll()

                        // Endpoints públicos
                        .requestMatchers(
                                "/api/auth/**",
                                "/oauth2/**",
                                "/login/oauth2/**",
                                "/api/newhorarios",
                                "/api/horarios-fijos/**",
                                "/api/servicios/**")
                        .permitAll()

                        // Todo lo demás requiere JWT
                        .anyRequest().authenticated())

                // =========================
                // FILTRO JWT
                // =========================
                .addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class)

                // =========================
                // OAUTH2 GOOGLE
                // =========================
                .oauth2Login(oauth -> oauth
                        .authorizationEndpoint(a -> a.baseUri("/oauth2/authorization"))
                        .successHandler((request, response, authentication) -> {

                            try {
                                OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

                                String email = oAuth2User.getAttribute("email");
                                Boolean emailVerified = oAuth2User.getAttribute("email_verified");
                                String name = oAuth2User.getAttribute("name");
                                String googleId = oAuth2User.getAttribute("sub");

                                if (email == null || emailVerified == null || !emailVerified) {
                                    response.sendRedirect(FRONTEND_URL + "/auth?error=email_not_verified");
                                    return;
                                }

                                User user = userRepository.findByEmail(email)
                                        .orElseGet(() -> {
                                            User newUser = User.builder()
                                                    .email(email)
                                                    .username(name)
                                                    .googleId(googleId)
                                                    .estado("ACTIVO")
                                                    .build();
                                            return userRepository.save(newUser);
                                        });

                                // JWT corto (5 minutos)
                                long expiration = 5 * 60;

                                JwtPayload payload = new JwtPayload(
                                        user.getId().toString(),
                                        user.getEmail(),
                                        List.of("ROLE_USER"));

                                String token = jwtService.generateToken(payload, expiration);

                                // 🔐 NO exponemos datos sensibles
                                String redirectUrl = FRONTEND_URL + "/auth/callback?token="
                                        + URLEncoder.encode(token, StandardCharsets.UTF_8);

                                response.sendRedirect(redirectUrl);

                            } catch (Exception e) {
                                try {
                                    response.sendRedirect(FRONTEND_URL + "/auth?error=true");
                                } catch (Exception ignored) {
                                }
                            }
                        }));

        return http.build();
    }

    // =========================
    // CORS SEGURO
    // =========================
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of("http://localhost:3000",
                "http://localhost:5173", FRONTEND_URL));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
