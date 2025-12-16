package isil.pe.glassimport.controller;

import isil.pe.glassimport.services.HorarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/horarios")
@CrossOrigin(origins = "http://localhost:5173")
public class HorarioController {

    @Autowired
    private HorarioService horarioService;

    // Devuelve el estado de todos los horarios (LIBRE / EN_PROCESO / OCUPADO) para una fecha
    @GetMapping
    public Map<String, String> getHorarios(@RequestParam String fecha) {
        return horarioService.getEstadosHorarios(fecha);
    }

    // Bloquea un horario durante 3 minutos (EN_PROCESO)
    @PostMapping("/lock")
    public ResponseEntity<?> lockHorario(@RequestBody Map<String, String> body) {
        String fecha = body.get("fecha");
        String hora = body.get("hora"); // ej: "09:00" o "9:00"

        boolean ok = horarioService.bloquearHorario(fecha, hora);
        if (ok) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(409).body("Ya ocupado");
    }

    // Reserva definitiva del horario (pasa a OCUPADO)
    @PostMapping("/reservar")
    public ResponseEntity<?> reservarHorario(@RequestBody Map<String, String> body) {
        String fecha = body.get("fecha");
        String hora = body.get("hora");

        horarioService.reservarHorario(fecha, hora);
        return ResponseEntity.ok().build();
    }
}
