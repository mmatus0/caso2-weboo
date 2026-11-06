package cl.ubb.petcare.service;

import cl.ubb.petcare.dto.ClienteRequest;
import cl.ubb.petcare.dto.ClienteResponse;
import cl.ubb.petcare.dto.MascotaResponse;
import cl.ubb.petcare.exception.ConflictoException;
import cl.ubb.petcare.exception.RecursoNoEncontradoException;
import cl.ubb.petcare.model.Cliente;
import cl.ubb.petcare.model.EstadoReserva;
import cl.ubb.petcare.repository.ClienteRepository;
import cl.ubb.petcare.repository.MascotaRepository;
import cl.ubb.petcare.repository.ReservaRepository;
import cl.ubb.petcare.validation.RutValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClienteService {

    private static final List<EstadoReserva> ACTIVAS = List.of(EstadoReserva.PENDIENTE, EstadoReserva.CONFIRMADA);

    private final ClienteRepository clienteRepository;
    private final MascotaRepository mascotaRepository;
    private final ReservaRepository reservaRepository;

    public ClienteService(ClienteRepository clienteRepository,
                          MascotaRepository mascotaRepository,
                          ReservaRepository reservaRepository) {
        this.clienteRepository = clienteRepository;
        this.mascotaRepository = mascotaRepository;
        this.reservaRepository = reservaRepository;
    }

    @Transactional(readOnly = true)
    public List<ClienteResponse> listar(String buscar) {
        List<Cliente> clientes = (buscar == null || buscar.isBlank())
                ? clienteRepository.findAllByOrderByNombreAsc()
                : clienteRepository.buscar(buscar.trim());
        return clientes.stream().map(ClienteResponse::desde).toList();
    }

    @Transactional(readOnly = true)
    public ClienteResponse obtener(Long id) {
        return ClienteResponse.desde(buscarEntidad(id));
    }

    @Transactional
    public ClienteResponse crear(ClienteRequest req) {
        String rut = RutValidator.normalizar(req.rut());
        if (clienteRepository.existsByRut(rut)) {
            throw new ConflictoException("Ya existe un cliente con el RUT " + rut + ".");
        }
        if (clienteRepository.existsByEmailIgnoreCase(req.email())) {
            throw new ConflictoException("Ya existe un cliente con el email " + req.email() + ".");
        }
        Cliente cliente = new Cliente();
        copiarDatos(req, cliente);
        return ClienteResponse.desde(clienteRepository.save(cliente));
    }

    @Transactional
    public ClienteResponse actualizar(Long id, ClienteRequest req) {
        Cliente cliente = buscarEntidad(id);
        String rut = RutValidator.normalizar(req.rut());
        if (clienteRepository.existsByRutAndIdNot(rut, id)) {
            throw new ConflictoException("Otro cliente ya tiene el RUT " + rut + ".");
        }
        if (clienteRepository.existsByEmailIgnoreCaseAndIdNot(req.email(), id)) {
            throw new ConflictoException("Otro cliente ya tiene el email " + req.email() + ".");
        }
        copiarDatos(req, cliente);
        return ClienteResponse.desde(clienteRepository.save(cliente));
    }

    /**
     * Elimina al cliente junto a sus mascotas e historial (ON DELETE CASCADE en la BD).
     * No se permite si tiene horas pendientes o confirmadas: primero hay que cancelarlas.
     */
    @Transactional
    public void eliminar(Long id) {
        Cliente cliente = buscarEntidad(id);
        if (reservaRepository.existsByMascotaClienteIdAndEstadoIn(id, ACTIVAS)) {
            throw new ConflictoException("El cliente tiene horas pendientes o confirmadas. Cancélelas antes de eliminarlo.");
        }
        clienteRepository.delete(cliente);
    }

    @Transactional(readOnly = true)
    public List<MascotaResponse> mascotasDeCliente(Long clienteId) {
        buscarEntidad(clienteId);
        return mascotaRepository.findByClienteIdOrderByNombreAsc(clienteId).stream()
                .map(MascotaResponse::desde)
                .toList();
    }

    /** Usado también por otros servicios. */
    public Cliente buscarEntidad(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente", id));
    }

    private void copiarDatos(ClienteRequest req, Cliente cliente) {
        cliente.setRut(RutValidator.normalizar(req.rut()));
        cliente.setNombre(req.nombre().trim());
        cliente.setEmail(req.email().trim().toLowerCase());
        cliente.setTelefono(req.telefono().trim());
        cliente.setDireccion(req.direccion() == null ? null : req.direccion().trim());
    }
}
