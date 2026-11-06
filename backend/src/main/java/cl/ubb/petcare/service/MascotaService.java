package cl.ubb.petcare.service;

import cl.ubb.petcare.dto.AtencionResponse;
import cl.ubb.petcare.dto.MascotaRequest;
import cl.ubb.petcare.dto.MascotaResponse;
import cl.ubb.petcare.dto.ReservaResponse;
import cl.ubb.petcare.exception.ConflictoException;
import cl.ubb.petcare.exception.RecursoNoEncontradoException;
import cl.ubb.petcare.model.EstadoReserva;
import cl.ubb.petcare.model.Mascota;
import cl.ubb.petcare.repository.AtencionRepository;
import cl.ubb.petcare.repository.MascotaRepository;
import cl.ubb.petcare.repository.ReservaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MascotaService {

    private static final List<EstadoReserva> ACTIVAS = List.of(EstadoReserva.PENDIENTE, EstadoReserva.CONFIRMADA);

    private final MascotaRepository mascotaRepository;
    private final ReservaRepository reservaRepository;
    private final AtencionRepository atencionRepository;
    private final ClienteService clienteService;

    public MascotaService(MascotaRepository mascotaRepository,
                          ReservaRepository reservaRepository,
                          AtencionRepository atencionRepository,
                          ClienteService clienteService) {
        this.mascotaRepository = mascotaRepository;
        this.reservaRepository = reservaRepository;
        this.atencionRepository = atencionRepository;
        this.clienteService = clienteService;
    }

    @Transactional(readOnly = true)
    public List<MascotaResponse> listar(String especie) {
        return mascotaRepository.findAllConCliente().stream()
                .filter(m -> especie == null || especie.isBlank() || m.getEspecie().equalsIgnoreCase(especie.trim()))
                .map(MascotaResponse::desde)
                .toList();
    }

    @Transactional(readOnly = true)
    public MascotaResponse obtener(Long id) {
        return MascotaResponse.desde(buscarEntidad(id));
    }

    @Transactional
    public MascotaResponse crear(MascotaRequest req) {
        Mascota mascota = new Mascota();
        copiarDatos(req, mascota);
        return MascotaResponse.desde(mascotaRepository.save(mascota));
    }

    @Transactional
    public MascotaResponse actualizar(Long id, MascotaRequest req) {
        Mascota mascota = buscarEntidad(id);
        copiarDatos(req, mascota);
        return MascotaResponse.desde(mascotaRepository.save(mascota));
    }

    @Transactional
    public void eliminar(Long id) {
        Mascota mascota = buscarEntidad(id);
        if (reservaRepository.existsByMascotaIdAndEstadoIn(id, ACTIVAS)) {
            throw new ConflictoException("La mascota tiene horas pendientes o confirmadas. Cancélelas antes de eliminarla.");
        }
        mascotaRepository.delete(mascota);
    }

    /** Historial médico: todas las atenciones de la mascota con sus recetas. */
    @Transactional(readOnly = true)
    public List<AtencionResponse> historial(Long mascotaId) {
        buscarEntidad(mascotaId);
        return atencionRepository.historialDeMascota(mascotaId).stream()
                .map(AtencionResponse::desde)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservaResponse> reservas(Long mascotaId) {
        buscarEntidad(mascotaId);
        return reservaRepository.buscar(null, null, null, mascotaId).stream()
                .map(ReservaResponse::desde)
                .toList();
    }

    public Mascota buscarEntidad(Long id) {
        return mascotaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Mascota", id));
    }

    private void copiarDatos(MascotaRequest req, Mascota mascota) {
        mascota.setNombre(req.nombre().trim());
        mascota.setEspecie(capitalizar(req.especie()));
        mascota.setRaza(req.raza() == null ? null : req.raza().trim());
        mascota.setFechaNacimiento(req.fechaNacimiento());
        mascota.setPeso(req.peso());
        mascota.setCliente(clienteService.buscarEntidad(req.clienteId()));
    }

    /** "PERRO" o "perro" -> "Perro", para que los filtros por especie sean consistentes. */
    private String capitalizar(String texto) {
        String t = texto.trim().toLowerCase();
        return t.isEmpty() ? t : Character.toUpperCase(t.charAt(0)) + t.substring(1);
    }
}
