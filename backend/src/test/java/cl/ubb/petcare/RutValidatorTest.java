package cl.ubb.petcare;

import cl.ubb.petcare.validation.RutValidator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Pruebas unitarias del validador de RUT (no necesitan base de datos). */
class RutValidatorTest {

    @Test
    void aceptaRutsValidosConYSinPuntos() {
        assertTrue(RutValidator.esValido("12345678-5"));
        assertTrue(RutValidator.esValido("12.345.678-5"));
        assertTrue(RutValidator.esValido("18765234-0"));
    }

    @Test
    void rechazaDigitoVerificadorIncorrecto() {
        assertFalse(RutValidator.esValido("12345678-9"));
    }

    @Test
    void rechazaFormatoIncorrecto() {
        assertFalse(RutValidator.esValido("12345678"));
        assertFalse(RutValidator.esValido("abc-1"));
    }

    @Test
    void normalizaPuntosYK() {
        assertEquals("10000013-K", RutValidator.normalizar("10.000.013-k"));
    }
}
