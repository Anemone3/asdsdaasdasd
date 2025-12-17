package isil.pe.glassimport.service;

import isil.pe.glassimport.entity.HorarioFijo;
import isil.pe.glassimport.repository.HorarioFijoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class HorarioFijoService {

    private final HorarioFijoRepository repository;

    public HorarioFijoService(HorarioFijoRepository repository) {
        this.repository = repository;
    }

    public List<HorarioFijo> listarTodos() {
        return repository.findAll();
    }

    public HorarioFijo crear(String hora) {
        if (hora == null || hora.isBlank()) {
            throw new IllegalArgumentException("La hora no puede estar vacía.");
        }
        if (!hora.matches("^([01]\\d|2[0-3]):[0-5]\\d$")) {
            throw new IllegalArgumentException("Formato de hora inválido. Use HH:MM.");
        }
        if (repository.existsByHora(hora)) {
            throw new IllegalArgumentException("La hora ya existe.");
        }
        HorarioFijo h = new HorarioFijo();
        h.setHora(hora);
        h.setHabilitado(true);
        return repository.save(h);
    }

    public void eliminar(Long id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("El horario no existe.");
        }
        repository.deleteById(id);
    }

    @Transactional
    public HorarioFijo toggleEstado(Long id, boolean habilitado) {
        HorarioFijo h = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Horario no encontrado."));
        h.setHabilitado(habilitado);
        return h;
    }
}
