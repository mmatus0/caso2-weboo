package cl.ubb.petcare.dto.externo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record HistorialNotificaciones(
        int total,
        List<NotificacionEmailRespuesta> resultados
) {
}
