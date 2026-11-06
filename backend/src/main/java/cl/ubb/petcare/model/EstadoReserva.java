package cl.ubb.petcare.model;

/**
 * Ciclo de vida de una reserva:
 *
 *   PENDIENTE ──confirmar──► CONFIRMADA ──atender──► ATENDIDA
 *       │                        │
 *       └──────cancelar──────────┴──────────────────► CANCELADA
 */
public enum EstadoReserva {
    PENDIENTE,
    CONFIRMADA,
    CANCELADA,
    ATENDIDA;

    /** Una reserva activa ocupa su bloque horario y todavía se puede modificar o cancelar. */
    public boolean esActiva() {
        return this == PENDIENTE || this == CONFIRMADA;
    }
}
