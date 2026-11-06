package cl.ubb.petcare.service;

import cl.ubb.petcare.dto.AtencionRequest;
import cl.ubb.petcare.dto.AtencionResponse;
import cl.ubb.petcare.dto.RecetaRequest;
import cl.ubb.petcare.exception.ConflictoException;
import cl.ubb.petcare.exception.RecursoNoEncontradoException;
import cl.ubb.petcare.exception.ReglaNegocioException;
import cl.ubb.petcare.model.Atencion;
import cl.ubb.petcare.model.EstadoReserva;
import cl.ubb.petcare.model.Receta;
import cl.ubb.petcare.model.Reserva;
import cl.ubb.petcare.repository.AtencionRepository;
import cl.ubb.petcare.repository.ReservaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Seguimiento médico: el veterinario registra diagnóstico, tratamiento y medicamentos
 * al atender una reserva CONFIRMADA. La reserva pasa a ATENDIDA.
 */
@Service
public class AtencionService {

    private final AtencionRepository atencionRepository;
    private final ReservaRepository reservaRepository;
    private final ReservaService reservaService;

    public AtencionService(AtencionRepository atencionRepository,
                           ReservaRepository reservaRepository,
                           ReservaService reservaService) {
        this.atencionRepository = atencionRepository;
        this.reservaRepository = reservaRepository;
        this.reservaService = reservaService;
    }

    @Transactional
    public AtencionResponse registrar(Long reservaId, AtencionRequest req) {
        Reserva reserva = reservaService.buscarEntidad(reservaId);

        if (atencionRepository.existsByReservaId(reservaId)) {
            throw new ConflictoException("La reserva #" + reservaId + " ya tiene una atención registrada.");
        }
        if (reserva.getEstado() != EstadoReserva.CONFIRMADA) {
            throw new ReglaNegocioException("Solo se pueden atender reservas CONFIRMADAS. Estado actual: "
                    + reserva.getEstado() + ".");
        }

        Atencion atencion = new Atencion();
        atencion.setReserva(reserva);
        atencion.setDiagnostico(req.diagnostico().trim());
        atencion.setTratamiento(req.tratamiento().trim());
        atencion.setObservaciones(req.observaciones());
        atencion.setFecha(LocalDateTime.now(HorarioAtencion.ZONA));

        if (req.recetas() != null) {
            for (RecetaRequest r : req.recetas()) {
                Receta receta = new Receta();
                receta.setMedicamento(r.medicamento().trim());
                receta.setDosis(r.dosis().trim());
                receta.setFrecuencia(r.frecuencia().trim());
                receta.setDias(r.dias());
                atencion.agregarReceta(receta);
            }
        }

        reserva.setEstado(EstadoReserva.ATENDIDA);
        reservaRepository.save(reserva);

        return AtencionResponse.desde(atencionRepository.save(atencion));
    }

    @Transactional(readOnly = true)
    public AtencionResponse obtener(Long id) {
        Atencion atencion = atencionRepository.findDetalleById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Atención", id));
        return AtencionResponse.desde(atencion);
    }
}
