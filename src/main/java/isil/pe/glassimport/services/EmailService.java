package isil.pe.glassimport.services;

import isil.pe.glassimport.entity.Reserva;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Value("${jwt.secret}")
    private String jwtSecret;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // ---------------------------
    // NUEVO: Enviar confirmación de reserva con plantilla HTML
    // ---------------------------
    public void enviarEmailConfirmacionReserva(
            String email,
            String username,
            String automovil,
            String anio,
            String confirmUrl,
            Timestamp fechaReserva
    ) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            LocalDateTime fechaHora = fechaReserva.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();

            String fechaFormateada = fechaHora.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            String horaFormateada = fechaHora.format(DateTimeFormatter.ofPattern("HH:mm"));

            String htmlContent = generarPlantillaConfirmacion(
                    username,
                    automovil,
                    anio,
                    fechaFormateada,
                    horaFormateada,
                    confirmUrl
            );

            helper.setFrom(fromAddress);
            helper.setTo(email);
            helper.setSubject("Confirma tu cita - Glass Import Automotriz");
            helper.setText(htmlContent, true); // true = es HTML

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Error al enviar email de confirmación", e);
        }
    }

    // ---------------------------
    // Plantilla HTML para confirmación de reserva
    // ---------------------------
    private String generarPlantillaConfirmacion(
            String username,
            String automovil,
            String anio,
            String fecha,
            String hora,
            String confirmUrl
    ) {
        String htmlTemplate = """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <style>
                        body {
                            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                            background-color: #f4f4f4;
                            margin: 0;
                            padding: 0;
                        }
                        .email-container {
                            max-width: 600px;
                            margin: 40px auto;
                            background-color: #ffffff;
                            border-radius: 10px;
                            overflow: hidden;
                            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
                        }
                        .header {
                            background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
                            color: #ffffff;
                            padding: 40px 20px;
                            text-align: center;
                        }
                        .header h1 {
                            margin: 0;
                            font-size: 28px;
                            font-weight: 600;
                        }
                        .content {
                            padding: 40px 30px;
                        }
                        .greeting {
                            font-size: 18px;
                            color: #333;
                            margin-bottom: 20px;
                        }
                        .info-box {
                            background-color: #f8f9fa;
                            border-left: 4px solid #667eea;
                            padding: 20px;
                            margin: 25px 0;
                            border-radius: 5px;
                        }
                        .info-item {
                            display: flex;
                            align-items: center;
                            margin: 12px 0;
                            font-size: 16px;
                        }
                        .info-item .icon {
                            font-size: 24px;
                            margin-right: 15px;
                            width: 30px;
                            text-align: center;
                        }
                        .info-item .label {
                            font-weight: 600;
                            color: #555;
                            margin-right: 8px;
                        }
                        .info-item .value {
                            color: #333;
                        }
                        .cta-button {
                            display: inline-block;
                            background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
                            color: #ffffff;
                            text-decoration: none;
                            padding: 16px 40px;
                            border-radius: 50px;
                            font-size: 16px;
                            font-weight: 600;
                            margin: 20px 0;
                            transition: transform 0.2s;
                            box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
                        }
                        .cta-button:hover {
                            transform: translateY(-2px);
                        }
                        .button-container {
                            text-align: center;
                            margin: 30px 0;
                        }
                        .warning {
                            background-color: #fff3cd;
                            border-left: 4px solid #ffc107;
                            padding: 15px;
                            margin: 20px 0;
                            border-radius: 5px;
                            font-size: 14px;
                            color: #856404;
                        }
                        .footer {
                            background-color: #f8f9fa;
                            padding: 30px;
                            text-align: center;
                            color: #6c757d;
                            font-size: 14px;
                            border-top: 1px solid #e9ecef;
                        }
                        .footer p {
                            margin: 5px 0;
                        }
                        .divider {
                            height: 1px;
                            background-color: #e9ecef;
                            margin: 25px 0;
                        }
                    </style>
                </head>
                <body>
                    <div class="email-container">
                        <div class="header">
                            <h1>🚗 Glass Import Automotriz</h1>
                        </div>
                        
                        <div class="content">
                            <p class="greeting">¡Hola <strong>%s</strong>!</p>
                            
                            <p style="color: #555; line-height: 1.6;">
                                Tu reserva ha sido creada exitosamente. Por favor, confirma tu asistencia 
                                haciendo clic en el botón de abajo.
                            </p>
                            
                            <div class="info-box">
                                <div class="info-item">
                                    <span class="icon">🚙</span>
                                    <span class="label">Vehículo:</span>
                                    <span class="value">%s (%s)</span>
                                </div>
                                <div class="info-item">
                                    <span class="icon">📅</span>
                                    <span class="label">Fecha:</span>
                                    <span class="value">%s</span>
                                </div>
                                <div class="info-item">
                                    <span class="icon">🕐</span>
                                    <span class="label">Hora:</span>
                                    <span class="value">%s</span>
                                </div>
                            </div>
                            
                            <div class="button-container">
                                <a href="%s" class="cta-button">
                                    ✓ Confirmar mi cita
                                </a>
                            </div>
                            
                            <div class="warning">
                                <strong>⚠️ Importante:</strong> Este enlace expirará en 72 horas. 
                                Si no confirmas tu cita, será cancelada automáticamente.
                            </div>
                            
                            <div class="divider"></div>
                            
                            <p style="color: #6c757d; font-size: 14px;">
                                Si no solicitaste esta cita o deseas cancelarla, ignora este correo 
                                o contáctanos directamente.
                            </p>
                        </div>
                        
                        <div class="footer">
                            <p><strong>Glass Import Automotriz</strong></p>
                            <p>Servicio de cristales automotrices de calidad</p>
                            <p style="margin-top: 15px;">
                                © 2025 Glass Import. Todos los derechos reservados.
                            </p>
                        </div>
                    </div>
                </body>
                </html>
                """;
        return String.format(htmlTemplate, username, automovil, anio, fecha, hora, confirmUrl);
    }

    // ---------------------------
    // Plantilla HTML para confirmación exitosa
    // ---------------------------
    public void enviarConfirmacionExitosa(Reserva reserva) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            LocalDateTime fechaHora = reserva.getFecha().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();

            String fechaFormateada = fechaHora.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            String horaFormateada = fechaHora.format(DateTimeFormatter.ofPattern("HH:mm"));

            String htmlContent = generarPlantillaConfirmacionExitosa(
                    reserva.getUser().getUsername(),
                    reserva.getAutomovil().getMarca() + " " + reserva.getAutomovil().getModelo(),
                    String.valueOf(reserva.getAutomovil().getAnio()),
                    fechaFormateada,
                    horaFormateada
            );

            helper.setFrom(fromAddress);
            helper.setTo(reserva.getUser().getEmail());
            helper.setSubject("✓ Cita confirmada - Glass Import Automotriz");
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Error al enviar email de confirmación exitosa", e);
        }
    }

    private String generarPlantillaConfirmacionExitosa(
            String username,
            String automovil,
            String anio,
            String fecha,
            String hora
    ) {
        String htmlTemplate = """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <style>
                        body {
                            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                            background-color: #f4f4f4;
                            margin: 0;
                            padding: 0;
                        }
                        .email-container {
                            max-width: 600px;
                            margin: 40px auto;
                            background-color: #ffffff;
                            border-radius: 10px;
                            overflow: hidden;
                            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
                        }
                        .header {
                            background: linear-gradient(135deg, #28a745 0%%, #20c997 100%%);
                            color: #ffffff;
                            padding: 40px 20px;
                            text-align: center;
                        }
                        .header h1 {
                            margin: 0;
                            font-size: 28px;
                            font-weight: 600;
                        }
                        .success-icon {
                            font-size: 60px;
                            margin: 20px 0;
                        }
                        .content {
                            padding: 40px 30px;
                            text-align: center;
                        }
                        .info-box {
                            background-color: #f8f9fa;
                            border-left: 4px solid #28a745;
                            padding: 20px;
                            margin: 25px 0;
                            border-radius: 5px;
                            text-align: left;
                        }
                        .info-item {
                            display: flex;
                            align-items: center;
                            margin: 12px 0;
                            font-size: 16px;
                        }
                        .info-item .icon {
                            font-size: 24px;
                            margin-right: 15px;
                            width: 30px;
                            text-align: center;
                        }
                        .info-item .label {
                            font-weight: 600;
                            color: #555;
                            margin-right: 8px;
                        }
                        .info-item .value {
                            color: #333;
                        }
                        .footer {
                            background-color: #f8f9fa;
                            padding: 30px;
                            text-align: center;
                            color: #6c757d;
                            font-size: 14px;
                            border-top: 1px solid #e9ecef;
                        }
                    </style>
                </head>
                <body>
                    <div class="email-container">
                        <div class="header">
                            <div class="success-icon">✓</div>
                            <h1>¡Cita Confirmada!</h1>
                        </div>
                        
                        <div class="content">
                            <p style="font-size: 18px; color: #333; margin-bottom: 20px;">
                                <strong>¡Perfecto, %s!</strong>
                            </p>
                            
                            <p style="color: #555; line-height: 1.6;">
                                Tu cita ha sido confirmada exitosamente. Te esperamos en:
                            </p>
                            
                            <div class="info-box">
                                <div class="info-item">
                                    <span class="icon">🚙</span>
                                    <span class="label">Vehículo:</span>
                                    <span class="value">%s (%s)</span>
                                </div>
                                <div class="info-item">
                                    <span class="icon">📅</span>
                                    <span class="label">Fecha:</span>
                                    <span class="value">%s</span>
                                </div>
                                <div class="info-item">
                                    <span class="icon">🕐</span>
                                    <span class="label">Hora:</span>
                                    <span class="value">%s</span>
                                </div>
                            </div>
                            
                            <p style="color: #6c757d; font-size: 14px; margin-top: 30px;">
                                Si necesitas reprogramar o cancelar, contáctanos con anticipación.
                            </p>
                        </div>
                        
                        <div class="footer">
                            <p><strong>Glass Import Automotriz</strong></p>
                            <p>Servicio de cristales automotrices de calidad</p>
                            <p style="margin-top: 15px;">
                                © 2025 Glass Import. Todos los derechos reservados.
                            </p>
                        </div>
                    </div>
                </body>
                </html>
                """;
        return String.format(htmlTemplate, username, automovil, anio, fecha, hora);
    }

    // ---------------------------
    // Mantener métodos antiguos para compatibilidad
    // ---------------------------
    public void enviarConfirmacionCita(String destinatario, String nombreCliente, String fecha, String hora) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(fromAddress);
        mensaje.setTo(destinatario);
        mensaje.setSubject("Confirmación de tu cita - Glass Import Automotriz");
        mensaje.setText("Hola " + nombreCliente + ",\n\n" +
                "Tu cita ha sido registrada exitosamente.\n\n" +
                "📅 Fecha: " + fecha + "\n" +
                "🕓 Hora: " + hora + "\n\n" +
                "Gracias por confiar en Glass Import Automotriz.\n\n" +
                "Atentamente,\nEl equipo de Glass Import");

        mailSender.send(mensaje);
    }

    public void enviarCancelacion(Reserva reserva) {
        LocalDateTime fechaHora = reserva.getFecha().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom(fromAddress);
        mail.setTo(reserva.getUser().getEmail());
        mail.setSubject("Cita Cancelada - Glass Import");
        mail.setText(String.format(
                "Tu cita del %s para tu %s %s ha sido cancelada.",
                fechaHora.toLocalDate(),
                reserva.getAutomovil().getMarca(),
                reserva.getAutomovil().getModelo()
        ));
        mailSender.send(mail);
    }

    public void sendVerificationCode(String destinatario, String codigo) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(fromAddress);
        mensaje.setTo(destinatario);
        mensaje.setSubject("Código de verificación - Glass Import");
        mensaje.setText("Tu código de verificación es: " + codigo);
        mailSender.send(mensaje);
    }
}