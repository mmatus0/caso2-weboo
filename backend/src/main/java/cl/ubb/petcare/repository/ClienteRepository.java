package cl.ubb.petcare.repository;

import cl.ubb.petcare.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    boolean existsByRut(String rut);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByRutAndIdNot(String rut, Long id);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    /** Búsqueda por nombre, RUT o email (contiene, sin distinguir mayúsculas). */
    @Query("""
            select c from Cliente c
            where lower(c.nombre) like lower(concat('%', :texto, '%'))
               or lower(c.rut)    like lower(concat('%', :texto, '%'))
               or lower(c.email)  like lower(concat('%', :texto, '%'))
            order by c.nombre
            """)
    List<Cliente> buscar(@Param("texto") String texto);

    List<Cliente> findAllByOrderByNombreAsc();
}
