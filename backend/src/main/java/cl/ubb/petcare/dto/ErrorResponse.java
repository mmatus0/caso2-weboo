package cl.ubb.petcare.dto;

import java.time.LocalDateTime;
import java.util.List;

/** Formato único de error que devuelve toda la API. */
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String mensaje,
        String ruta,
        List<String> detalles
) {
}
