package cl.ubb.petcare;

import cl.ubb.petcare.service.HorarioAtencion;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

/** Pruebas de las reglas de bloques horarios (09:00 a 17:00, cada 1 hora). */
class HorarioAtencionTest {

    private final HorarioAtencion horario = new HorarioAtencion(9, 18);

    @Test
    void generaNueveBloquesDeUnaHora() {
        assertEquals(9, horario.bloquesDelDia().size());
        assertEquals(LocalTime.of(9, 0), horario.bloquesDelDia().get(0));
        assertEquals(LocalTime.of(17, 0), horario.bloquesDelDia().get(8));
    }

    @Test
    void validaBloquesExactosDentroDelHorario() {
        assertTrue(horario.esBloqueValido(LocalTime.of(9, 0)));
        assertTrue(horario.esBloqueValido(LocalTime.of(17, 0)));
        assertFalse(horario.esBloqueValido(LocalTime.of(10, 30)));
        assertFalse(horario.esBloqueValido(LocalTime.of(18, 0)));
        assertFalse(horario.esBloqueValido(LocalTime.of(8, 0)));
    }
}
