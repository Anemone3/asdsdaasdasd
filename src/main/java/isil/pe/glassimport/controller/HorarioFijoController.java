package isil.pe.glassimport.controller;

import isil.pe.glassimport.dto.request.ToggleRequest;
import isil.pe.glassimport.entity.HorarioFijo;
import isil.pe.glassimport.service.HorarioFijoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173") // ajusta al front
public class HorarioFijoController {

    private final HorarioFijoService service;

    public HorarioFijoController(HorarioFijoService service) {
        this.service = service;
    }

    @GetMapping("/newhorarios")
    public ResponseEntity<List<HorarioFijo>> listarHorarios() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @PostMapping("/horarios-fijos")
    public ResponseEntity<?> crearHorario(@RequestBody HorarioFijo body) {
        try {
            HorarioFijo creado = service.crear(body.getHora());
            return ResponseEntity.ok(creado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/horarios-fijos/{id}")
    public ResponseEntity<?> eliminarHorario(@PathVariable Long id) {
        try {
            service.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PatchMapping("/horarios-fijos/{id}/toggle")
    public ResponseEntity<?> toggleHorario(@PathVariable Long id,
                                           @RequestBody ToggleRequest request) {
        try {
            HorarioFijo actualizado = service.toggleEstado(id, request.getHabilitado());
            return ResponseEntity.ok(actualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    public static class ErrorResponse {
        private String message;
        public ErrorResponse(String message) { this.message = message; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
