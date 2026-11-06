package cl.ubb.petcare.exception;

/** Datos duplicados o choque con el estado actual (ej: hora ya reservada). Responde 409. */
public class ConflictoException extends RuntimeException {

    public ConflictoException(String mensaje) {
        super(mensaje);
    }
}
