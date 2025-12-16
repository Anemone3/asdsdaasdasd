package isil.pe.glassimport.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}