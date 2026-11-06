package cl.ubb.petcare.service;

import cl.ubb.petcare.dto.DisponibilidadResponse;
import cl.ubb.petcare.dto.VeterinarioRequest;
import cl.ubb.petcare.dto.VeterinarioResponse;
import cl.ubb.petcare.exception.ConflictoException;
import cl.ubb.petcare.exception.RecursoNoEncontradoException;
import cl.ubb.petcare.model.EstadoReserva;
import cl.ubb.petcare.model.Veterinario;
import cl.ubb.petcare.repository.ReservaRepository;
import cl.ubb.petcare.repository.VeterinarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class VeterinarioService {

    private final VeterinarioRepository veterinarioRepository;
    private final ReservaRepository reservaRepository;
    private final HorarioAtencion horario;

    public VeterinarioService(VeterinarioRepository veterinarioRepository,
                              ReservaRepository reservaRepository,
                              HorarioAtencion horario) {
        this.veterinarioRepository = veterinarioRepository;
        this.reservaRepository = reservaRepository;
        this.horario = horario;
    }

    @Transactional(readOnly = true)
    public List<VeterinarioResponse> listar(boolean soloActivos) {
        List<Veterinario> lista = soloActivos
                ? veterinarioRepository.findByActivoTrueOrderByNombreAsc()
                : veterinarioRepository.findAllByOrderByNombreAsc();
        return lista.stream().map(VeterinarioResponse::desde).toList();
    }

    @Transactional(readOnly = true)
    public VeterinarioResponse obtener(Long id) {
        return VeterinarioResponse.desde(buscarEntidad(id));
    }

    @Transactional
    public VeterinarioResponse crear(VeterinarioRequest req) {
        if (veterinarioRepository.existsByEmailIgnoreCase(req.email())) {
            throw new ConflictoException("Ya existe un veterinario con el email " + req.email() + ".");
        }
        Veterinario vet = new Veterinario();
        copiarDatos(req, vet);
        return VeterinarioResponse.desde(veterinarioRepository.save(vet));
    }

    @Transactional
    public VeterinarioResponse actualizar(Long id, VeterinarioRequest req) {
        Veterinario vet = buscarEntidad(id);
        if (veterinarioRepository.existsByEmailIgnoreCaseAndIdNot(req.email(), id)) {
            throw new ConflictoException("Otro veterinario ya tiene el email " + req.email() + ".");
        }
        copiarDatos(req, vet);
        return VeterinarioResponse.desde(veterinarioRepository.save(vet));
    }

    /**
     * Solo se puede eliminar un veterinario sin reservas.
     * Si tiene historial, se debe desactivar (activo=false) para no perder la información.
     */
    @Transactional
    public void eliminar(Long id) {
        Veterinario vet = buscarEntidad(id);
        if (reservaRepository.existsByVeterinarioId(id)) {
            throw new ConflictoException("El veterinario tiene reservas registradas. "
                    + "En lugar de eliminarlo, desactívelo con PUT enviando \"activo\": false.");
        }
        veterinarioRepository.delete(vet);
    }

    /** Bloques libres y ocupados del veterinario en una fecha. Los bloques que ya pasaron no se ofrecen. */
    @Transactional(readOnly = true)
    public DisponibilidadResponse disponibilidad(Long id, LocalDate fecha) {
        Veterinario vet = buscarEntidad(id);
        Set<LocalTime> ocupadas = new HashSet<>(
                reservaRepository.horasOcupadas(id, fecha, EstadoReserva.CANCELADA));

        List<String> libres = horario.bloquesDelDia().stream()
                .filter(h -> !ocupadas.contains(h))
                .filter(h -> !horario.yaPaso(fecha, h))
                .filter(h -> vet.isActivo())
                .map(LocalTime::toString)
                .toList();

        List<String> ocupados = ocupadas.stream()
                .sorted()
                .map(LocalTime::toString)
                .toList();

        return new DisponibilidadResponse(vet.getId(), vet.getNombre(), fecha, libres, ocupados);
    }

    public Veterinario buscarEntidad(Long id) {
        return veterinarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Veterinario", id));
    }

    private void copiarDatos(VeterinarioRequest req, Veterinario vet) {
        vet.setNombre(req.nombre().trim());
        vet.setEspecialidad(req.especialidad().trim());
        vet.setEmail(req.email().trim().toLowerCase());
        vet.setActivo(req.activo() == null || req.activo());
    }
}
