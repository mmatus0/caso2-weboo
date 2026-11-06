package cl.ubb.petcare.dto.externo;

/** Body que Spring envía a POST /api/notificaciones/email de la API Node. */
public record NotificacionEmailRequest(
        String para,
        String nombreCliente,
        String tipo,
        ReservaNotificada reserva
) {
    public record ReservaNotificada(
            Long id,
            String fecha,
            String hora,
            String mascota,
            String veterinario,
            String motivo
    ) {
    }
}
