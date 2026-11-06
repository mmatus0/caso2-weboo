package cl.ubb.petcare.dto;

import cl.ubb.petcare.model.Atencion;

import java.time.LocalDateTime;
import java.util.List;

public record AtencionResponse(
        Long id,
        Long reservaId,
        LocalDateTime fecha,
        Long mascotaId,
        String mascotaNombre,
        String veterinarioNombre,
        String motivo,
        String diagnostico,
        String tratamiento,
        String observaciones,
        List<RecetaResponse> recetas
) {
    public static AtencionResponse desde(Atencion a) {
        var r = a.getReserva();
        return new AtencionResponse(a.getId(), r.getId(), a.getFecha(),
                r.getMascota().getId(), r.getMascota().getNombre(), r.getVeterinario().getNombre(),
                r.getMotivo(), a.getDiagnostico(), a.getTratamiento(), a.getObservaciones(),
                a.getRecetas().stream().map(RecetaResponse::desde).toList());
    }
}
