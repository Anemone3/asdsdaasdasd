package isil.pe.glassimport.repository;

import isil.pe.glassimport.entity.HorarioFijo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HorarioFijoRepository extends JpaRepository<HorarioFijo, Long> {
    boolean existsByHora(String hora);
}
