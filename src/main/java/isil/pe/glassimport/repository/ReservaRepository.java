package isil.pe.glassimport.repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import isil.pe.glassimport.entity.Reserva;
import isil.pe.glassimport.entity.enums.EstadoReserva;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    List<Reserva> findByUserId(Long userId);

    List<Reserva> findByAutomovilId(Long automovilId);

    List<Reserva> findByEstado(EstadoReserva estado);

    List<Reserva> findByUserIdAndEstado(Long userId, EstadoReserva estado);

    long countByEstado(EstadoReserva estado);

    long countByUserId(Long userId);

    long countByAutomovilId(Long automovilId);

    List<Reserva> findAllByFechaBetweenAndEstado(LocalDateTime inicio, LocalDateTime fin, EstadoReserva estado);

    boolean existsByFechaAndUserId(Timestamp fecha, Long userId);

    boolean existsByFechaAndAutomovilId(Timestamp fecha, Long automovilId);

    // ✅ NUEVO: Verifica si existe una reserva activa con un horario específico
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Reserva r " +
            "WHERE r.horario.id = :horarioId " +
            "AND r.estado IN ('PENDIENTE', 'APROBADA', 'CONFIRMADA')")
    boolean existsReservaActivaByHorarioId(@Param("horarioId") Long horarioId);

    // ✅ NUEVO: Busca reserva activa por horario
    Optional<Reserva> findByHorarioIdAndEstadoIn(Long horarioId, List<EstadoReserva> estados);
}
