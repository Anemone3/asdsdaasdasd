package isil.pe.glassimport.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import isil.pe.glassimport.dto.request.ReservaRequestDto;
import isil.pe.glassimport.dto.response.ReservaResponseDto;
import isil.pe.glassimport.services.ReservaService;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/reservas")
@AllArgsConstructor
@Tag(name = "Reservas", description = "Endpoints para la gestión de reservas de automóviles")
@SecurityRequirement(name = "Bearer Authentication")
public class ReservaController {

    private final ReservaService reservaService;

    @Operation(summary = "Crear una nueva reserva")
    @PostMapping
    public ResponseEntity<ReservaResponseDto> createReserva(@RequestBody ReservaRequestDto reservaRequestDto) {
        try {
            ReservaResponseDto createdReserva = reservaService.createReserva(reservaRequestDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdReserva);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @Operation(summary = "Crear reserva con parámetros")
    @PostMapping("/create")
    public ResponseEntity<ReservaResponseDto> createReservaWithIds(
            @RequestParam Long userId,
            @RequestParam Long automovilId,
            @RequestParam(defaultValue = "PENDIENTE") String estado) {
        try {
            ReservaResponseDto createdReserva = reservaService.createReserva(userId, automovilId, estado);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdReserva);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @Operation(summary = "Listar todas las reservas")
    @GetMapping
    public ResponseEntity<List<ReservaResponseDto>> getAllReservas() {
        List<ReservaResponseDto> reservas = reservaService.getAllReservas();
        return ResponseEntity.ok(reservas);
    }

    @Operation(summary = "Obtener reserva por ID")
    @GetMapping("/{id}")
    public ResponseEntity<ReservaResponseDto> getReservaById(@PathVariable Long id) {
        Optional<ReservaResponseDto> reserva = reservaService.getReservaById(id);
        return reserva.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Buscar reservas por usuario")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReservaResponseDto>> getReservasByUser(@PathVariable Long userId) {
        List<ReservaResponseDto> reservas = reservaService.getReservasByUser(userId);
        return ResponseEntity.ok(reservas);
    }

    @Operation(summary = "Buscar reservas por estado")
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<ReservaResponseDto>> getReservasByEstado(@PathVariable String estado) {
        List<ReservaResponseDto> reservas = reservaService.getReservasByEstado(estado);
        return ResponseEntity.ok(reservas);
    }

    @Operation(summary = "Aprobar reserva (Solo Admin)")
    @PutMapping("/{id}/aprobar")
    public ResponseEntity<?> aprobarReserva(@PathVariable Long id) {
        try {
            ReservaResponseDto reserva = reservaService.aprobarReserva(id);
            return ResponseEntity.ok(reserva);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Confirmar reserva con token")
    @PutMapping("/{id}/confirmar")
    public ResponseEntity<ReservaResponseDto> confirmarReserva(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            ReservaResponseDto reserva = reservaService.confirmarReservaConToken(token);
            return ResponseEntity.ok(reserva);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @Operation(summary = "Cancelar reserva")
    @PutMapping("/{id}/cancelar")
    public ResponseEntity<ReservaResponseDto> cancelarReserva(@PathVariable Long id) {
        try {
            ReservaResponseDto reserva = reservaService.cancelarReserva(id);
            return ResponseEntity.ok(reserva);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Completar reserva")
    @PutMapping("/{id}/completar")
    public ResponseEntity<ReservaResponseDto> completarReserva(@PathVariable Long id) {
        try {
            ReservaResponseDto reserva = reservaService.completarReserva(id);
            return ResponseEntity.ok(reserva);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Procesar reserva")
    @PostMapping("/procesar")
    public ResponseEntity<Void> procesarReserva(
            @RequestParam Long userId,
            @RequestParam Long automovilId) {
        try {
            reservaService.procesarReserva(userId, automovilId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
