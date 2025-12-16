package isil.pe.glassimport.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import isil.pe.glassimport.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    Optional<User> findByEmailOrUsername(String email, String username);
    Optional<User> findByEmail(String email);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
}
