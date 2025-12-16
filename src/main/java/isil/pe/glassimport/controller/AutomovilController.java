package isil.pe.glassimport.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import isil.pe.glassimport.dto.request.AutomovilRequestDto;
import isil.pe.glassimport.dto.response.AutomovilResponseDto;
import isil.pe.glassimport.services.AutomovilService;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/automoviles")
@AllArgsConstructor
public class AutomovilController {

    private final AutomovilService automovilService;

    /**
     * ✅ Crear automóvil SIN asociarlo a usuario (PÚBLICO - no requiere autenticación)
     * Este endpoint debe estar permitido en SecurityConfig con:
     * .requestMatchers("/api/automoviles/**").permitAll()
     */
    @PostMapping
    public ResponseEntity<AutomovilResponseDto> createAutomovil(
            @RequestBody AutomovilRequestDto automovilRequestDto) {
        try {
            System.out.println("========================================");
            System.out.println("📥 POST /api/automoviles - DTO recibido:");
            System.out.println("   Marca: " + automovilRequestDto.getMarca());
            System.out.println("   Modelo: " + automovilRequestDto.getModelo());
            System.out.println("   Año: " + automovilRequestDto.getAnio());
            System.out.println("   Placa: " + automovilRequestDto.getPlaca());
            System.out.println("   Nota: " + automovilRequestDto.getNota());
            System.out.println("========================================");

            // ✅ Usar método SIN usuario (no requiere autenticación)
            AutomovilResponseDto createdAutomovil = automovilService.createAutomovil(automovilRequestDto);

            System.out.println("✅ Automóvil creado exitosamente con ID: " + createdAutomovil.getId());
            System.out.println("========================================");

            return ResponseEntity.status(HttpStatus.CREATED).body(createdAutomovil);

        } catch (Exception e) {
            System.err.println("❌ Error al crear automóvil:");
            System.err.println("   Tipo: " + e.getClass().getName());
            System.err.println("   Mensaje: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null);
        }
    }

    /**
     * Obtener todos los automóviles (sin filtrar por usuario)
     */
    @GetMapping
    public ResponseEntity<List<AutomovilResponseDto>> getAllAutomoviles() {
        List<AutomovilResponseDto> automoviles = automovilService.getAllAutomoviles();
        return ResponseEntity.ok(automoviles);
    }

    /**
     * Obtener automóvil por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<AutomovilResponseDto> getAutomovilById(@PathVariable Long id) {
        Optional<AutomovilResponseDto> automovil = automovilService.getAutomovilById(id);
        return automovil.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Obtener automóviles por marca
     */
    @GetMapping("/marca/{marca}")
    public ResponseEntity<List<AutomovilResponseDto>> getAutomovilesByMarca(@PathVariable String marca) {
        List<AutomovilResponseDto> automoviles = automovilService.getAutomovilesByMarca(marca);
        return ResponseEntity.ok(automoviles);
    }

    /**
     * Obtener automóviles por modelo
     */
    @GetMapping("/modelo/{modelo}")
    public ResponseEntity<List<AutomovilResponseDto>> getAutomovilesByModelo(@PathVariable String modelo) {
        List<AutomovilResponseDto> automoviles = automovilService.getAutomovilesByModelo(modelo);
        return ResponseEntity.ok(automoviles);
    }

    /**
     * Obtener todas las marcas únicas
     */
    @GetMapping("/marcas")
    public ResponseEntity<List<String>> getAllMarcas() {
        List<String> marcas = automovilService.getAllMarcas();
        return ResponseEntity.ok(marcas);
    }

    /**
     * Obtener todos los modelos únicos
     */
    @GetMapping("/modelos")
    public ResponseEntity<List<String>> getAllModelos() {
        List<String> modelos = automovilService.getAllModelos();
        return ResponseEntity.ok(modelos);
    }
}