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
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((req, res, ex) -> {

                            // Resto de la app: tu JSON personalizado
                            res.setStatus(401);
                            res.setContentType("application/json");
                            res.getWriter().write("{\"error\":\"UNAUTHORIZED\"}");
                        }))

                .authorizeHttpRequests(auth -> auth
                        // ✅ Swagger / OpenAPI sin autenticación
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html")
                        .permitAll()

                        // ✅ Endpoints públicos (ajusta a tu gusto)
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/register",
                                "/oauth2/**",
                                "/api/newhorarios",
                                "/api/horarios-fijos/**",
                                "/api/servicios/**")
                        .permitAll()

                        .anyRequest().authenticated())

                .addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class)

                // ✅ OAuth2 Google SOLO cuando se llama explícitamente
                .oauth2Login(oauth -> oauth
                        .authorizationEndpoint(a -> a.baseUri("/oauth2/authorization"))
                        .redirectionEndpoint(r -> r.baseUri("/oauth2/code/**"))
                        .successHandler((request, response, authentication) -> {
                            try {
                                OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

                                String email = oAuth2User.getAttribute("email");
                                String name = oAuth2User.getAttribute("name");
                                String googleId = oAuth2User.getAttribute("sub");

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

                                long expiration = 7 * 24 * 60 * 60;
                                JwtPayload payload = new JwtPayload(
                                        user.getId().toString(),
                                        user.getEmail(),
                                        List.of("ROLE_USER"));

                                String token = jwtService.generateToken(payload, expiration);

                                String redirectUrl = String.format(
                                        FRONTEND_URL + "/auth/callback?token=%s&userId=%d&userName=%s&userEmail=%s",
                                        token,
                                        user.getId(),
                                        URLEncoder.encode(user.getUsername(), StandardCharsets.UTF_8),
                                        URLEncoder.encode(user.getEmail(), StandardCharsets.UTF_8));

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

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // Si quieres ser más estricto, cambia "*" por FRONTEND_URL
        config.setAllowedOrigins(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
