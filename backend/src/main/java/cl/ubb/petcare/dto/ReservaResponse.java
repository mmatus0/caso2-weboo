package cl.ubb.petcare.dto;

import cl.ubb.petcare.model.EstadoReserva;
import cl.ubb.petcare.model.Reserva;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservaResponse(
        Long id,
        LocalDate fecha,
        @JsonFormat(pattern = "HH:mm") LocalTime hora,
        String motivo,
        EstadoReserva estado,
        Long mascotaId,
        String mascotaNombre,
        String mascotaEspecie,
        Long clienteId,
        String clienteNombre,
        String clienteEmail,
        Long veterinarioId,
        String veterinarioNombre
) {
    public static ReservaResponse desde(Reserva r) {
        var m = r.getMascota();
        var c = m.getCliente();
        var v = r.getVeterinario();
        return new ReservaResponse(r.getId(), r.getFecha(), r.getHora(), r.getMotivo(), r.getEstado(),
                m.getId(), m.getNombre(), m.getEspecie(),
                c.getId(), c.getNombre(), c.getEmail(),
                v.getId(), v.getNombre());
    }
}
