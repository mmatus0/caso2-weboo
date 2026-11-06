package cl.ubb.petcare.dto;

/** Respuesta de confirmar / cancelar / modificar: la reserva actualizada + qué pasó con el correo. */
public record ReservaAccionResponse(
        ReservaResponse reserva,
        NotificacionResultado notificacion
) {
}
