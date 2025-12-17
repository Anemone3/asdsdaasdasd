package isil.pe.glassimport.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import isil.pe.glassimport.dto.request.ServicioRequestDTO;
import isil.pe.glassimport.dto.response.ServicioResponseDto;
import isil.pe.glassimport.services.ServicioService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/servicios")
@RequiredArgsConstructor
public class ServicioController {

    private final ServicioService servicioService;

    @PostMapping
    public ServicioResponseDto crear(@RequestBody ServicioRequestDTO dto) {
        return servicioService.crear(dto);
    }

    @GetMapping
    public List<ServicioResponseDto> listar() {
        return servicioService.listar();
    }

    @GetMapping("/{id}")
    public ServicioResponseDto obtener(@PathVariable Long id) {
        return servicioService.obtenerPorId(id);
    }

    @PutMapping("/{id}")
    public ServicioResponseDto actualizar(
            @PathVariable Long id,
            @RequestBody ServicioRequestDTO dto) {
        return servicioService.actualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        servicioService.eliminar(id);
    }

    // 🔹 NUEVO: toggle habilitado
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<ServicioResponseDto> toggleHabilitado(
            @PathVariable Long id,
            @RequestBody ToggleRequest body) {

        ServicioResponseDto dto = servicioService.toggleHabilitado(id, body.isHabilitado());
        return ResponseEntity.ok(dto);
    }

    // DTO interno para leer { "habilitado": true/false }
    public static class ToggleRequest {
        private boolean habilitado;
        public boolean isHabilitado() { return habilitado; }
        public void setHabilitado(boolean habilitado) { this.habilitado = habilitado; }
    }
}
