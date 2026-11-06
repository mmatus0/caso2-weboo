package cl.ubb.petcare.service;

import cl.ubb.petcare.dto.NotificacionResultado;
import cl.ubb.petcare.dto.ReservaAccionResponse;
import cl.ubb.petcare.dto.ReservaRequest;
import cl.ubb.petcare.dto.ReservaResponse;
import cl.ubb.petcare.exception.ConflictoException;
import cl.ubb.petcare.exception.RecursoNoEncontradoException;
import cl.ubb.petcare.exception.ReglaNegocioException;
import cl.ubb.petcare.model.EstadoReserva;
import cl.ubb.petcare.model.Mascota;
import cl.ubb.petcare.model.Reserva;
import cl.ubb.petcare.model.Veterinario;
import cl.ubb.petcare.repository.ReservaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Administración de horas médicas.
 *
 * Reglas:
 *  1. Solo bloques de 1 hora dentro del horario de la clínica y no en el pasado.
 *  2. Sin duplicados: un veterinario no puede tener dos reservas activas el mismo día y hora
 *     (se valida aquí y además con un índice UNIQUE en la BD como respaldo).
 *  3. Transiciones de estado permitidas:
 *       PENDIENTE  -> CONFIRMADA | CANCELADA
 *       CONFIRMADA -> CANCELADA  | ATENDIDA
 *  4. Al confirmar, cancelar o modificar se notifica al cliente por correo (API externa).
 */
@Service
public class ReservaService {

    private static final long SIN_EXCLUIR = -1L;

    private final ReservaRepository reservaRepository;
    private final MascotaService mascotaService;
    private final VeterinarioService veterinarioService;
    private final NotificacionService notificacionService;
    private final HorarioAtencion horario;

    public ReservaService(ReservaRepository reservaRepository,
                          MascotaService mascotaService,
                          VeterinarioService veterinarioService,
                          NotificacionService notificacionService,
                          HorarioAtencion horario) {
        this.reservaRepository = reservaRepository;
        this.mascotaService = mascotaService;
        this.veterinarioService = veterinarioService;
        this.notificacionService = notificacionService;
        this.horario = horario;
    }

    @Transactional(readOnly = true)
    public List<ReservaResponse> listar(LocalDate fecha, EstadoReserva estado, Long veterinarioId, Long mascotaId) {
        return reservaRepository.buscar(fecha, estado, veterinarioId, mascotaId).stream()
                .map(ReservaResponse::desde)
                .toList();
    }

    @Transactional(readOnly = true)
    public ReservaResponse obtener(Long id) {
        return ReservaResponse.desde(buscarEntidad(id));
    }

    /** Un cliente solicita una hora para su mascota. Queda PENDIENTE hasta que la clínica la confirme. */
    @Transactional
    public ReservaResponse crear(ReservaRequest req) {
        Mascota mascota = mascotaService.buscarEntidad(req.mascotaId());
        Veterinario vet = veterinarioService.buscarEntidad(req.veterinarioId());

        validarHorario(req, vet, SIN_EXCLUIR);

        Reserva reserva = new Reserva();
        reserva.setMascota(mascota);
        reserva.setVeterinario(vet);
        reserva.setFecha(req.fecha());
        reserva.setHora(req.hora());
        reserva.setMotivo(req.motivo().trim());
        reserva.setEstado(EstadoReserva.PENDIENTE);

        // saveAndFlush fuerza el INSERT ahora: si dos personas reservan el mismo bloque
        // al mismo tiempo, el UNIQUE de la BD lanza el error aquí (y responde 409).
        return ReservaResponse.desde(reservaRepository.saveAndFlush(reserva));
    }

    /** Cambia mascota, veterinario, fecha, hora o motivo de una reserva activa. */
    @Transactional
    public ReservaAccionResponse modificar(Long id, ReservaRequest req) {
        Reserva reserva = buscarEntidad(id);
        exigirActiva(reserva, "modificar");

        Mascota mascota = mascotaService.buscarEntidad(req.mascotaId());
        Veterinario vet = veterinarioService.buscarEntidad(req.veterinarioId());
        validarHorario(req, vet, id);

        reserva.setMascota(mascota);
        reserva.setVeterinario(vet);
        reserva.setFecha(req.fecha());
        reserva.setHora(req.hora());
        reserva.setMotivo(req.motivo().trim());
        reservaRepository.saveAndFlush(reserva);

        NotificacionResultado n = notificacionService.notificar(reserva, NotificacionService.Tipo.MODIFICACION);
        return new ReservaAccionResponse(ReservaResponse.desde(reserva), n);
    }

    @Transactional
    public ReservaAccionResponse confirmar(Long id) {
        Reserva reserva = buscarEntidad(id);
        if (reserva.getEstado() != EstadoReserva.PENDIENTE) {
            throw new ReglaNegocioException("Solo se pueden confirmar reservas PENDIENTES. Estado actual: "
                    + reserva.getEstado() + ".");
        }
        reserva.setEstado(EstadoReserva.CONFIRMADA);
        reservaRepository.saveAndFlush(reserva);

        NotificacionResultado n = notificacionService.notificar(reserva, NotificacionService.Tipo.CONFIRMACION);
        return new ReservaAccionResponse(ReservaResponse.desde(reserva), n);
    }

    /** Cancela la reserva y libera el bloque horario para otra mascota. */
    @Transactional
    public ReservaAccionResponse cancelar(Long id) {
        Reserva reserva = buscarEntidad(id);
        exigirActiva(reserva, "cancelar");

        reserva.setEstado(EstadoReserva.CANCELADA);
        reservaRepository.saveAndFlush(reserva);

        NotificacionResultado n = notificacionService.notificar(reserva, NotificacionService.Tipo.CANCELACION);
        return new ReservaAccionResponse(ReservaResponse.desde(reserva), n);
    }

    public Reserva buscarEntidad(Long id) {
        return reservaRepository.findDetalleById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Reserva", id));
    }

    // ------------------------------------------------------------------ validaciones

    private void validarHorario(ReservaRequest req, Veterinario vet, Long excluirId) {
        if (!vet.isActivo()) {
            throw new ReglaNegocioException("El veterinario " + vet.getNombre() + " no está recibiendo reservas.");
        }
        if (!horario.esBloqueValido(req.hora())) {
            throw new ReglaNegocioException("Hora inválida: la clínica atiende en " + horario.descripcion() + ".");
        }
        if (horario.yaPaso(req.fecha(), req.hora())) {
            throw new ReglaNegocioException("No se puede reservar un horario que ya pasó.");
        }
        boolean choque = reservaRepository.existeChoque(
                vet.getId(), req.fecha(), req.hora(), EstadoReserva.CANCELADA, excluirId);
        if (choque) {
            throw new ConflictoException("El veterinario " + vet.getNombre() + " ya tiene una reserva el "
                    + req.fecha() + " a las " + req.hora() + ". Consulte la disponibilidad y elija otro bloque.");
        }
    }

    private void exigirActiva(Reserva reserva, String accion) {
        if (!reserva.getEstado().esActiva()) {
            throw new ReglaNegocioException("No se puede " + accion + " una reserva "
                    + reserva.getEstado() + ".");
        }
    }
}
