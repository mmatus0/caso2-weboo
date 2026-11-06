package cl.ubb.petcare.repository;

import cl.ubb.petcare.model.Atencion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AtencionRepository extends JpaRepository<Atencion, Long> {

    boolean existsByReservaId(Long reservaId);

    /** Historial médico de una mascota, de la atención más reciente a la más antigua. */
    @Query("""
            select a from Atencion a
            join fetch a.reserva r
            join fetch r.mascota
            join fetch r.veterinario
            where r.mascota.id = :mascotaId
            order by a.fecha desc
            """)
    List<Atencion> historialDeMascota(@Param("mascotaId") Long mascotaId);

    @Query("""
            select a from Atencion a
            join fetch a.reserva r
            join fetch r.mascota
            join fetch r.veterinario
            where a.id = :id
            """)
    Optional<Atencion> findDetalleById(@Param("id") Long id);
}
