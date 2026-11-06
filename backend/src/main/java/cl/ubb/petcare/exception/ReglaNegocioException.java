package cl.ubb.petcare.exception;

/** Solicitud válida en formato pero que rompe una regla del negocio. Responde 400. */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
