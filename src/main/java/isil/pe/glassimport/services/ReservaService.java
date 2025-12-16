package isil.pe.glassimport.services;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import isil.pe.glassimport.dto.request.ReservaRequestDto;
import isil.pe.glassimport.dto.response.AutomovilResponseDto;
import isil.pe.glassimport.dto.response.ReservaResponseDto;
import isil.pe.glassimport.dto.response.UserResponseDto;
import isil.pe.glassimport.entity.Automovil;
import isil.pe.glassimport.entity.Reserva;
import isil.pe.glassimport.entity.User;
import isil.pe.glassimport.entity.enums.EstadoReserva;
import isil.pe.glassimport.exceptions.CitaExistsException;
import isil.pe.glassimport.repository.ReservaRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final UserService userService;
    private final AutomovilService automovilService;
    private final EmailService emailService;
    // INYECTA websocket sender
    private final SimpMessagingTemplate webSocketSender;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${frontend.url}")
    private String frontendUrl;

    // ========================================
    // CREAR RESERVA
    // ========================================

    public ReservaResponseDto createReserva(ReservaRequestDto reservaRequestDto) {
        Optional<UserResponseDto> user = userService.getUserById(reservaRequestDto.getUserId());
        Optional<AutomovilResponseDto> automovil = automovilService
                .getAutomovilById(reservaRequestDto.getAutomovilId());

        if (user.isEmpty() || automovil.isEmpty()) {
            throw new RuntimeException("Error al crear reserva - Usuario o automóvil no encontrado");
        }

        String estado = reservaRequestDto.getEstado() != null ? reservaRequestDto.getEstado() : "PENDIENTE";
        Timestamp fecha = reservaRequestDto.getFecha();

        if (reservaRepository.existsByFechaAndUserId(fecha, reservaRequestDto.getUserId())) {
            throw new CitaExistsException("Ya existe una reserva para este usuario en esa fecha.");
        }

        if (reservaRepository.existsByFechaAndAutomovilId(fecha, reservaRequestDto.getAutomovilId())) {
            throw new CitaExistsException("Ya existe una reserva para este automóvil en esa fecha.");
        }

        Reserva reserva = Reserva.builder()
                .estado(estado)
                .fecha(fecha)
                .servicio(reservaRequestDto.getServicio())
                .user(dtoToEntity(user.get()))
                .automovil(automovilDtoToEntity(automovil.get()))
                .build();

        Reserva savedReserva = reservaRepository.save(reserva);
        ReservaResponseDto dto = convertToResponseDto(savedReserva);

        // --> WEBSOCKET: Notifica creación
        webSocketSender.convertAndSend("/topic/reservas", dto);

        return dto;
    }

    public ReservaResponseDto createReserva(Long userId, Long automovilId, String estado) {
        Optional<UserResponseDto> user = userService.getUserById(userId);
        Optional<AutomovilResponseDto> automovil = automovilService.getAutomovilById(automovilId);

        if (user.isEmpty() || automovil.isEmpty()) {
            throw new RuntimeException("Usuario o automóvil no encontrado");
        }

        Reserva reserva = Reserva.builder()
                .estado(estado != null ? estado : "PENDIENTE")
                .fecha(new Timestamp(System.currentTimeMillis()))
                .user(dtoToEntity(user.get()))
                .automovil(automovilDtoToEntity(automovil.get()))
                .build();

        Reserva savedReserva = reservaRepository.save(reserva);
        ReservaResponseDto dto = convertToResponseDto(savedReserva);

        // --> WEBSOCKET: Notifica creación
        webSocketSender.convertAndSend("/topic/reservas", dto);

        return dto;
    }

    // ========================================
    // APROBAR RESERVA (ADMIN)
    // ========================================

    public ReservaResponseDto aprobarReserva(Long reservaId) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

        if (!"PENDIENTE".equals(reserva.getEstado())) {
            throw new RuntimeException("Solo se pueden aprobar reservas pendientes");
        }

        reserva.setEstado("APROBADA");
        reserva = reservaRepository.save(reserva);

        // Enviar email con el link de confirmación
        String token = generarTokenConfirmacion(reserva.getId());
        String confirmUrl = frontendUrl + "/confirmar-cita?token=" + token;

        emailService.enviarEmailConfirmacionReserva(
                reserva.getUser().getEmail(),
                reserva.getUser().getUsername(),
                reserva.getAutomovil().getMarca() + " " + reserva.getAutomovil().getModelo(),
                String.valueOf(reserva.getAutomovil().getAnio()),
                confirmUrl,
                reserva.getFecha());

        ReservaResponseDto dto = convertToResponseDto(reserva);

        // ---> WEBSOCKET: Notifica actualización
        webSocketSender.convertAndSend("/topic/reservas", dto);

        System.out.println("✅ Reserva aprobada y email enviado a: " + reserva.getUser().getEmail());

        return dto;
    }

    // ========================================
    // CONFIRMAR RESERVA CON TOKEN
    // ========================================

    public ReservaResponseDto confirmarReservaConToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Long reservaId = Long.parseLong(claims.getSubject());

            Reserva reserva = reservaRepository.findById(reservaId)
                    .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

            if (!"APROBADA".equals(reserva.getEstado())) {
                throw new RuntimeException("Esta reserva no está en estado aprobada");
            }

            reserva.setEstado("CONFIRMADA");
            reserva = reservaRepository.save(reserva);

            // Enviar email de confirmación exitosa
            emailService.enviarConfirmacionExitosa(reserva);

            ReservaResponseDto dto = convertToResponseDto(reserva);

            // ---> WEBSOCKET: Notifica la actualización
            webSocketSender.convertAndSend("/topic/reservas", dto);

            System.out.println("✅ Reserva confirmada por el cliente");

            return dto;

        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            throw new RuntimeException("El token ha expirado. Por favor contacte con soporte.");
        } catch (io.jsonwebtoken.security.SignatureException e) {
            throw new RuntimeException("Token inválido - firma incorrecta");
        } catch (Exception e) {
            throw new RuntimeException("Token inválido: " + e.getMessage());
        }
    }

    // ========================================
    // GENERAR TOKEN JWT (sin cambios)
    // ========================================

    private String generarTokenConfirmacion(Long reservaId) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

        Instant now = Instant.now();
        Instant expiration = now.plus(72, ChronoUnit.HOURS);

        return Jwts.builder()
                .setSubject(String.valueOf(reservaId))
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiration))
                .signWith(key)
                .compact();
    }

    // ========================================
    // LISTAR RESERVAS (sin cambios)
    // ========================================

    public List<ReservaResponseDto> getAllReservas() {
        return reservaRepository.findAll()
                .stream()
                .map(this::convertToResponseDto)
                .toList();
    }

    public Optional<ReservaResponseDto> getReservaById(Long id) {
        return reservaRepository.findById(id)
                .map(this::convertToResponseDto);
    }

    public List<ReservaResponseDto> getReservasByUser(Long userId) {
        return reservaRepository.findByUserId(userId)
                .stream()
                .map(this::convertToResponseDto)
                .toList();
    }

    public List<ReservaResponseDto> getReservasByEstado(String estado) {
        return reservaRepository.findByEstado(EstadoReserva.valueOf(estado))
                .stream()
                .map(this::convertToResponseDto)
                .toList();
    }

    // ========================================
    // CANCELAR Y COMPLETAR RESERVA (con WS)
    // ========================================

    public ReservaResponseDto cancelarReserva(Long reservaId) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));
        if ("COMPLETADA".equals(reserva.getEstado())) {
            throw new RuntimeException("No se puede cancelar una reserva completada");
        }
        reserva.setEstado("CANCELADA");
        Reserva updatedReserva = reservaRepository.save(reserva);

        emailService.enviarCancelacion(updatedReserva);

        ReservaResponseDto dto = convertToResponseDto(updatedReserva);

        // ---> WEBSOCKET: Notifica actualización/cancelación
        webSocketSender.convertAndSend("/topic/reservas", dto);

        return dto;
    }

    public ReservaResponseDto completarReserva(Long reservaId) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));
        if (!"CONFIRMADA".equals(reserva.getEstado())) {
            throw new RuntimeException("Solo se pueden completar reservas confirmadas");
        }
        reserva.setEstado("COMPLETADA");
        Reserva updatedReserva = reservaRepository.save(reserva);

        ReservaResponseDto dto = convertToResponseDto(updatedReserva);
        webSocketSender.convertAndSend("/topic/reservas", dto);

        return dto;
    }

    public void procesarReserva(Long userId, Long automovilId) {
        createReserva(userId, automovilId, "PENDIENTE");
    }

    // ========================================
    // CONVERSIÓNES DTO <-> ENTITY (COMPLETOS)
    // ========================================
    private User dtoToEntity(UserResponseDto dto) {
        return User.builder()
                .id(dto.getId())
                .estado(dto.getEstado())
                .email(dto.getEmail())
                .username(dto.getUsername())
                .build();
    }

    private Automovil automovilDtoToEntity(AutomovilResponseDto dto) {
        return Automovil.builder()
                .id(dto.getId())
                .nota(dto.getNota())
                .anio(dto.getAnio())
                .modelo(dto.getModelo())
                .marca(dto.getMarca())
                .placa(dto.getPlaca())
                .build();
    }

    private ReservaResponseDto convertToResponseDto(Reserva reserva) {
        UserResponseDto userDto = UserResponseDto.builder()
                .id(reserva.getUser().getId())
                .username(reserva.getUser().getUsername())
                .email(reserva.getUser().getEmail())
                .estado(reserva.getUser().getEstado())
                .build();

        AutomovilResponseDto automovilDto = AutomovilResponseDto.builder()
                .id(reserva.getAutomovil().getId())
                .marca(reserva.getAutomovil().getMarca())
                .modelo(reserva.getAutomovil().getModelo())
                .anio(reserva.getAutomovil().getAnio())
                .nota(reserva.getAutomovil().getNota())
                .placa(reserva.getAutomovil().getPlaca())
                .build();

        return ReservaResponseDto.builder()
                .id(reserva.getId())
                .estado(reserva.getEstado())
                .fecha(reserva.getFecha())
                .servicio(reserva.getServicio())
                .user(userDto)
                .automovil(automovilDto)
                .build();
    }
}
