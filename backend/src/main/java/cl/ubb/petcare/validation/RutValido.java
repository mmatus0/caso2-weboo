package cl.ubb.petcare.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Valida un RUT chileno (formato y dígito verificador).
 * Acepta "12345678-5" o "12.345.678-5".
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = RutValidator.class)
public @interface RutValido {

    String message() default "RUT inválido (formato 12345678-5 y dígito verificador correcto)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
