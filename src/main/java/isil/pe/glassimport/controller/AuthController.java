package isil.pe.glassimport.controller;

import isil.pe.glassimport.dto.request.AuthRequest;
import isil.pe.glassimport.dto.request.RegisterRequest;
import isil.pe.glassimport.dto.response.AuthResponse;
import isil.pe.glassimport.services.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Endpoints para autenticación y registro de usuarios")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Operation(
            summary = "Iniciar sesión",
            description = "Autentica un usuario existente y devuelve un token JWT para acceder a los endpoints protegidos. " +
                    "El token debe incluirse en el header Authorization como 'Bearer {token}'"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Autenticación exitosa - Retorna datos del usuario y token JWT",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Credenciales inválidas - Email o contraseña incorrectos",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Solicitud incorrecta - Datos de entrada inválidos",
                    content = @Content
            )
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Credenciales de inicio de sesión",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = AuthRequest.class),
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    value = "{\"email\": \"usuario@example.com\", \"password\": \"miPassword123\"}"
                            )
                    )
            )
            @RequestBody AuthRequest request,
            HttpServletResponse res) {
        AuthResponse response = authService.login(res, request.email(), request.password());
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Registrar nuevo usuario",
            description = "Crea una nueva cuenta de usuario en el sistema y devuelve un token JWT para acceso inmediato. " +
                    "El email y username deben ser únicos"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Registro exitoso - Retorna datos del usuario y token JWT",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Error en el registro - Email o username ya existe, o datos inválidos",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflicto - Usuario ya existe",
                    content = @Content
            )
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos para crear una nueva cuenta",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = RegisterRequest.class),
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    value = "{\"email\": \"nuevo@example.com\", \"password\": \"Password123!\", \"username\": \"nuevouser\"}"
                            )
                    )
            )
            @RequestBody RegisterRequest request,
            HttpServletResponse res) {
        AuthResponse response = authService.register(res, request);
        return ResponseEntity.ok(response);
    }
}