package isil.pe.glassimport.services;

import java.util.List;

import org.springframework.stereotype.Service;

import isil.pe.glassimport.dto.request.ServicioRequestDTO;
import isil.pe.glassimport.dto.response.ServicioResponseDto;
import isil.pe.glassimport.entity.Servicio;
import isil.pe.glassimport.repository.ServicioRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServicioService {

    private final ServicioRepository servicioRepository;

    public ServicioResponseDto crear(ServicioRequestDTO dto) {

        if (servicioRepository.existsByNombre(dto.getNombre())) {
            throw new RuntimeException("El servicio ya existe");
        }

        Servicio servicio = Servicio.builder()
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .precio(dto.getPrecio())
                .activo(dto.getActivo() != null ? dto.getActivo() : true)
                .habilitado(dto.getHabilitado() != null ? dto.getHabilitado() : true) // default true
                .build();

        servicioRepository.save(servicio);

        return toDto(servicio);
    }

    public List<ServicioResponseDto> listar() {
        return servicioRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    public ServicioResponseDto obtenerPorId(Long id) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));

        return toDto(servicio);
    }

    public ServicioResponseDto actualizar(Long id, ServicioRequestDTO dto) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));

        servicio.setNombre(dto.getNombre());
        servicio.setDescripcion(dto.getDescripcion());
        servicio.setPrecio(dto.getPrecio());
        servicio.setActivo(dto.getActivo());
        if (dto.getHabilitado() != null) {
            servicio.setHabilitado(dto.getHabilitado());
        }

        servicioRepository.save(servicio);

        return toDto(servicio);
    }

    public void eliminar(Long id) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));

        servicio.setActivo(false);

        servicioRepository.save(servicio);
    }

    // 🔹 nuevo método toggle
    public ServicioResponseDto toggleHabilitado(Long id, boolean habilitado) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));

        servicio.setHabilitado(habilitado);
        servicioRepository.save(servicio);

        return toDto(servicio);
    }

    private ServicioResponseDto toDto(Servicio servicio) {
        return ServicioResponseDto.builder()
                .id(servicio.getId())
                .nombre(servicio.getNombre())
                .descripcion(servicio.getDescripcion())
                .precio(servicio.getPrecio())
                .activo(servicio.getActivo())
                .habilitado(servicio.getHabilitado())
                .build();
    }
}
