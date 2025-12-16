package isil.pe.glassimport.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import isil.pe.glassimport.entity.Servicio;

public interface ServicioRepository extends JpaRepository<Servicio, Long> {
    boolean existsByNombre(String nombre);
}
