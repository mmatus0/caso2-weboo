package cl.ubb.petcare.service;

import cl.ubb.petcare.client.ApiExternaClient;
import cl.ubb.petcare.dto.NotificacionResultado;
import cl.ubb.petcare.dto.externo.NotificacionEmailRequest;
import cl.ubb.petcare.dto.externo.NotificacionEmailRespuesta;
import cl.ubb.petcare.model.Reserva;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

/**
 * Envía correos al cliente usando la API externa de mensajería.
 * Nunca lanza excepción: si la API falla, la operación sobre la reserva igual se completa
 * y se informa en la respuesta que el correo no pudo enviarse.
 */
@Service
public class NotificacionService {

    public enum Tipo { CONFIRMACION, CANCELACION, MODIFICACION }

    private static final Logger log = LoggerFactory.getLogger(NotificacionService.class);
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final ApiExternaClient apiExterna;

    public NotificacionService(ApiExternaClient apiExterna) {
        this.apiExterna = apiExterna;
    }

    public NotificacionResultado notificar(Reserva reserva, Tipo tipo) {
        var cliente = reserva.getMascota().getCliente();
        var datos = new NotificacionEmailRequest.ReservaNotificada(
                reserva.getId(),
                reserva.getFecha().format(FECHA),
                reserva.getHora().format(HORA),
                reserva.getMascota().getNombre(),
                reserva.getVeterinario().getNombre(),
                reserva.getMotivo());

        var request = new NotificacionEmailRequest(cliente.getEmail(), cliente.getNombre(), tipo.name(), datos);

        try {
            NotificacionEmailRespuesta r = apiExterna.enviarNotificacion(request);
            log.info("Notificación {} enviada a {} (reserva #{})", tipo, cliente.getEmail(), reserva.getId());
            return new NotificacionResultado(r.enviada(), r.modo(), r.previewUrl(),
                    "Correo enviado a " + cliente.getEmail());
        } catch (Exception e) {
            log.warn("No se pudo enviar la notificación {} de la reserva #{}: {}", tipo, reserva.getId(), e.getMessage());
            return NotificacionResultado.fallida("No se pudo contactar la API de notificaciones: " + e.getMessage());
        }
    }
}
