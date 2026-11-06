package cl.ubb.petcare.repository;

import cl.ubb.petcare.model.Mascota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MascotaRepository extends JpaRepository<Mascota, Long> {

    /** Todas las mascotas de un cliente. */
    List<Mascota> findByClienteIdOrderByNombreAsc(Long clienteId);

    /** Todas las mascotas trayendo a su dueño en la misma consulta (evita N+1). */
    @Query("select m from Mascota m join fetch m.cliente order by m.nombre")
    List<Mascota> findAllConCliente();

    long countByClienteId(Long clienteId);
}
