package cl.ubb.petcare.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class RutValidator implements ConstraintValidator<RutValido, String> {

    @Override
    public boolean isValid(String rut, ConstraintValidatorContext context) {
        if (rut == null || rut.isBlank()) {
            return true; // de los vacíos se encarga @NotBlank
        }
        return esValido(rut);
    }

    /** Quita puntos y espacios y deja la K en mayúscula: "12.345.678-k" -> "12345678-K". */
    public static String normalizar(String rut) {
        return rut == null ? null : rut.replace(".", "").replace(" ", "").toUpperCase();
    }

    public static boolean esValido(String rut) {
        String limpio = normalizar(rut);
        if (!limpio.matches("^\\d{7,8}-[\\dK]$")) {
            return false;
        }
        String[] partes = limpio.split("-");
        return calcularDv(partes[0]) == partes[1].charAt(0);
    }

    /** Algoritmo módulo 11. */
    static char calcularDv(String numero) {
        int suma = 0;
        int multiplicador = 2;
        for (int i = numero.length() - 1; i >= 0; i--) {
            suma += Character.getNumericValue(numero.charAt(i)) * multiplicador;
            multiplicador = multiplicador == 7 ? 2 : multiplicador + 1;
        }
        int resto = 11 - (suma % 11);
        if (resto == 11) return '0';
        if (resto == 10) return 'K';
        return Character.forDigit(resto, 10);
    }
}
