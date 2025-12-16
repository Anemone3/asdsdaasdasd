package isil.pe.glassimport.repository;

import isil.pe.glassimport.entity.Horario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface HorarioRepository extends JpaRepository<Horario, Long> {
    List<Horario> findByFecha(String fecha);
    Optional<Horario> findByFechaAndHora(String fecha, String hora);
}
