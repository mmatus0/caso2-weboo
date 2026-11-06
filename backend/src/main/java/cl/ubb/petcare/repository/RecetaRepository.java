package cl.ubb.petcare.repository;

import cl.ubb.petcare.model.Receta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecetaRepository extends JpaRepository<Receta, Long> {

    List<Receta> findByAtencionId(Long atencionId);
}
