package isil.pe.glassimport.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import isil.pe.glassimport.dto.request.AutomovilRequestDto;
import isil.pe.glassimport.dto.request.AutomovilPatchDto;
import isil.pe.glassimport.dto.response.AutomovilResponseDto;
import isil.pe.glassimport.entity.Automovil;
import isil.pe.glassimport.entity.User;
import isil.pe.glassimport.repository.AutomovilRepository;
import isil.pe.glassimport.repository.UserRepository;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AutomovilService {

    private final AutomovilRepository automovilRepository;
    private final UserRepository userRepository;

    // ============= MÉTODOS SIN SEGURIDAD (para uso interno/admin) =============

    /**
     * Crea un automóvil sin usuario asociado (uso interno/legacy)
     * ✅ ESTE ES EL MÉTODO QUE SE USA AHORA
     */
    public AutomovilResponseDto createAutomovil(AutomovilRequestDto automovilRequestDto) {
        System.out.println("========================================");
        System.out.println("🚗 AutomovilService.createAutomovil SIN USUARIO");
        System.out.println("📥 Marca: " + automovilRequestDto.getMarca());
        System.out.println("📥 Modelo: " + automovilRequestDto.getModelo());
        System.out.println("📥 Año: " + automovilRequestDto.getAnio());
        System.out.println("📥 Placa: " + automovilRequestDto.getPlaca());
        System.out.println("📥 Nota: " + automovilRequestDto.getNota());
        System.out.println("========================================");

        Automovil automovil = Automovil.builder()
                .marca(automovilRequestDto.getMarca())
                .modelo(automovilRequestDto.getModelo())
                .anio(automovilRequestDto.getAnio())
                .placa(automovilRequestDto.getPlaca())
                .nota(automovilRequestDto.getNota())
                // ✅ NO se asigna user (queda null)
                .build();

        System.out.println("💾 Guardando automóvil en BD sin usuario asociado...");
        Automovil savedAutomovil = automovilRepository.save(automovil);
        System.out.println("✅ Automóvil guardado exitosamente con ID: " + savedAutomovil.getId());
        System.out.println("========================================");

        return convertToResponseDto(savedAutomovil);
    }

    /**
     * Obtiene un automóvil por ID sin validar usuario (uso interno)
     */
    public Optional<AutomovilResponseDto> getAutomovilById(Long id) {
        return automovilRepository.findById(id)
                .map(this::convertToResponseDto);
    }

    public List<AutomovilResponseDto> getAllAutomoviles() {
        List<Automovil> automoviles = automovilRepository.findAll();
        return automoviles.stream()
                .map(this::convertToResponseDto)
                .toList();
    }

    public List<AutomovilResponseDto> getAutomovilesByMarca(String marca) {
        List<Automovil> automoviles = automovilRepository.findByMarca(marca);
        return automoviles.stream()
                .map(this::convertToResponseDto)
                .toList();
    }

    public List<AutomovilResponseDto> getAutomovilesByModelo(String modelo) {
        List<Automovil> automoviles = automovilRepository.findByModelo(modelo);
        return automoviles.stream()
                .map(this::convertToResponseDto)
                .toList();
    }

    public List<AutomovilResponseDto> getAutomovilesByAnio(int anio) {
        List<Automovil> automoviles = automovilRepository.findByAnio(anio);
        return automoviles.stream()
                .map(this::convertToResponseDto)
                .toList();
    }

    public List<AutomovilResponseDto> getAutomovilesByMarcaAndModelo(String marca, String modelo) {
        List<Automovil> automoviles = automovilRepository.findByMarcaAndModelo(marca, modelo);
        return automoviles.stream()
                .map(this::convertToResponseDto)
                .toList();
    }

    public boolean existsById(Long id) {
        return automovilRepository.existsById(id);
    }

    public long countAutomoviles() {
        return automovilRepository.count();
    }

    public List<String> getAllMarcas() {
        return automovilRepository.findDistinctMarca();
    }

    public List<String> getAllModelos() {
        return automovilRepository.findDistinctModelo();
    }

    // ============= MÉTODOS CON SEGURIDAD (para usuarios) - OPCIONAL =============
    // Estos métodos quedaron por si en el futuro quieres asociar automóviles a usuarios

    public AutomovilResponseDto createAutomovilConUsuario(AutomovilRequestDto automovilRequestDto, String email) {
        System.out.println("🚗 AutomovilService.createAutomovilConUsuario");
        System.out.println("📧 Email recibido: " + email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    System.err.println("❌ Usuario NO encontrado con email: " + email);
                    return new RuntimeException("Usuario no encontrado con email: " + email);
                });

        System.out.println("✅ Usuario encontrado: " + user.getId() + " - " + user.getUsername());

        Automovil automovil = Automovil.builder()
                .marca(automovilRequestDto.getMarca())
                .modelo(automovilRequestDto.getModelo())
                .anio(automovilRequestDto.getAnio())
                .placa(automovilRequestDto.getPlaca())
                .nota(automovilRequestDto.getNota())
                .user(user)
                .build();

        System.out.println("💾 Guardando automóvil en BD...");
        Automovil savedAutomovil = automovilRepository.save(automovil);
        System.out.println("✅ Automóvil guardado con ID: " + savedAutomovil.getId());

        return convertToResponseDto(savedAutomovil);
    }

    public List<AutomovilResponseDto> getAutomovilesByUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<Automovil> automoviles = automovilRepository.findByUser(user);
        return automoviles.stream()
                .map(this::convertToResponseDto)
                .toList();
    }

    public Optional<AutomovilResponseDto> getAutomovilByIdAndUser(Long id, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return automovilRepository.findByIdAndUser(id, user)
                .map(this::convertToResponseDto);
    }

    public AutomovilResponseDto updateAutomovil(Long id, AutomovilRequestDto automovilRequestDto, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Automovil automovil = automovilRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Automóvil no encontrado o no pertenece al usuario"));

        automovil.setMarca(automovilRequestDto.getMarca());
        automovil.setModelo(automovilRequestDto.getModelo());
        automovil.setAnio(automovilRequestDto.getAnio());
        automovil.setPlaca(automovilRequestDto.getPlaca());
        automovil.setNota(automovilRequestDto.getNota());

        Automovil updatedAutomovil = automovilRepository.save(automovil);
        return convertToResponseDto(updatedAutomovil);
    }

    public AutomovilResponseDto patchAutomovil(Long id, AutomovilPatchDto automovilPatchDto, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Automovil automovil = automovilRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Automóvil no encontrado o no pertenece al usuario"));

        if (automovilPatchDto.getMarca() != null) {
            automovil.setMarca(automovilPatchDto.getMarca());
        }
        if (automovilPatchDto.getModelo() != null) {
            automovil.setModelo(automovilPatchDto.getModelo());
        }
        if (automovilPatchDto.getAnio() != null) {
            automovil.setAnio(automovilPatchDto.getAnio());
        }
        if (automovilPatchDto.getNota() != null) {
            automovil.setNota(automovilPatchDto.getNota());
        }

        Automovil updatedAutomovil = automovilRepository.save(automovil);
        return convertToResponseDto(updatedAutomovil);
    }

    public void deleteAutomovil(Long id, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Automovil automovil = automovilRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Automóvil no encontrado o no pertenece al usuario"));

        automovilRepository.delete(automovil);
    }

    public long countAutomovilesByUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return automovilRepository.countByUser(user);
    }

    public boolean userHasAutomoviles(String email) {
        return countAutomovilesByUser(email) > 0;
    }

    // ============= MÉTODO COMÚN =============

    private AutomovilResponseDto convertToResponseDto(Automovil automovil) {
        return Automovil.toDto(automovil);
    }
}
