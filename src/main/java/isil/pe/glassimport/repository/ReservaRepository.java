package isil.pe.glassimport.repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import isil.pe.glassimport.entity.Reserva;
import isil.pe.glassimport.entity.enums.EstadoReserva;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    List<Reserva> findByUserId(Long userId);

    List<Reserva> findByAutomovilId(Long automovilId);

    // Cambiado de String a EstadoReserva
    List<Reserva> findByEstado(EstadoReserva estado);

    List<Reserva> findByUserIdAndEstado(Long userId, EstadoReserva estado);

    long countByEstado(EstadoReserva estado);

    long countByUserId(Long userId);

    long countByAutomovilId(Long automovilId);

    // Método que necesita tu Scheduler
    List<Reserva> findAllByFechaBetweenAndEstado(LocalDateTime inicio, LocalDateTime fin, EstadoReserva estado);

    boolean existsByFechaAndUserId(Timestamp fecha, Long userId);

    boolean existsByFechaAndAutomovilId(Timestamp fecha, Long automovilId);
}
