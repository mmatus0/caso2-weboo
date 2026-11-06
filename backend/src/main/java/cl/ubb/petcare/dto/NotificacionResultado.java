package cl.ubb.petcare.dto;

/**
 * Resultado del envío de correo vía API externa.
 * Si la API está caída, la reserva igual cambia de estado y aquí se informa enviada=false.
 */
public record NotificacionResultado(
        boolean enviada,
        String modo,
        String previewUrl,
        String detalle
) {
    public static NotificacionResultado fallida(String detalle) {
        return new NotificacionResultado(false, null, null, detalle);
    }
}
