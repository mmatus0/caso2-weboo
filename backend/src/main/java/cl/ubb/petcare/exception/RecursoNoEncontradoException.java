package cl.ubb.petcare.exception;

/** Se lanza cuando un registro buscado por id no existe. Responde 404. */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String recurso, Long id) {
        super(recurso + " con id " + id + " no existe.");
    }

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
