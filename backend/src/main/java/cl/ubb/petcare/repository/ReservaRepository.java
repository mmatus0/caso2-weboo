package cl.ubb.petcare.repository;

import cl.ubb.petcare.model.EstadoReserva;
import cl.ubb.petcare.model.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    /**
     * ¿El veterinario ya tiene una reserva activa (no cancelada) en ese día y hora?
     * excluirId permite ignorar la propia reserva cuando se está modificando
     * (al crear se envía -1, que nunca coincide con un id real).
     */
    @Query("""
            select count(r) > 0 from Reserva r
            where r.veterinario.id = :veterinarioId
              and r.fecha = :fecha
              and r.hora = :hora
              and r.estado <> :cancelada
              and r.id <> :excluirId
            """)
    boolean existeChoque(@Param("veterinarioId") Long veterinarioId,
                         @Param("fecha") LocalDate fecha,
                         @Param("hora") LocalTime hora,
                         @Param("cancelada") EstadoReserva cancelada,
                         @Param("excluirId") Long excluirId);

    /** Horas ocupadas por un veterinario en un día (para calcular disponibilidad). */
    @Query("""
            select r.hora from Reserva r
            where r.veterinario.id = :veterinarioId
              and r.fecha = :fecha
              and r.estado <> :cancelada
            """)
    List<LocalTime> horasOcupadas(@Param("veterinarioId") Long veterinarioId,
                                  @Param("fecha") LocalDate fecha,
                                  @Param("cancelada") EstadoReserva cancelada);

    /** Listado con filtros opcionales: si un filtro viene null, se ignora. */
    @Query("""
            select r from Reserva r
            join fetch r.mascota m
            join fetch m.cliente
            join fetch r.veterinario v
            where (:fecha is null or r.fecha = :fecha)
              and (:estado is null or r.estado = :estado)
              and (:veterinarioId is null or v.id = :veterinarioId)
              and (:mascotaId is null or m.id = :mascotaId)
            order by r.fecha, r.hora
            """)
    List<Reserva> buscar(@Param("fecha") LocalDate fecha,
                         @Param("estado") EstadoReserva estado,
                         @Param("veterinarioId") Long veterinarioId,
                         @Param("mascotaId") Long mascotaId);

    @Query("""
            select r from Reserva r
            join fetch r.mascota m
            join fetch m.cliente
            join fetch r.veterinario
            where r.id = :id
            """)
    Optional<Reserva> findDetalleById(@Param("id") Long id);

    boolean existsByMascotaIdAndEstadoIn(Long mascotaId, Collection<EstadoReserva> estados);

    boolean existsByMascotaClienteIdAndEstadoIn(Long clienteId, Collection<EstadoReserva> estados);

    boolean existsByVeterinarioId(Long veterinarioId);
}
