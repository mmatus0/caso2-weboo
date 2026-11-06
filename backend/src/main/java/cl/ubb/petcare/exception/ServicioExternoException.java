package cl.ubb.petcare.exception;

/** La API externa (Node) no respondió o respondió con error. Responde 503. */
public class ServicioExternoException extends RuntimeException {

    public ServicioExternoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
