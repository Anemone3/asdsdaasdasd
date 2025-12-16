package isil.pe.glassimport.repository;

import java.util.List;
import java.util.Optional;

import isil.pe.glassimport.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import isil.pe.glassimport.entity.Automovil;

public interface AutomovilRepository extends JpaRepository<Automovil, Long> {
    
    List<Automovil> findByMarca(String marca);
    
    List<Automovil> findByModelo(String modelo);
    
    List<Automovil> findByAnio(int anio);
    
    List<Automovil> findByMarcaAndModelo(String marca, String modelo);
    List<Automovil> findByUser(User user);
    Optional<Automovil> findByIdAndUser(Long id, User user);
    long countByUser(User user);
    @Query("SELECT DISTINCT a.marca FROM Automovil a ORDER BY a.marca")
    List<String> findDistinctMarca();
    
    @Query("SELECT DISTINCT a.modelo FROM Automovil a ORDER BY a.modelo")
    List<String> findDistinctModelo();
}
