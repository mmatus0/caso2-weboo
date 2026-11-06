package cl.ubb.petcare.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MascotaRequest(

        @NotBlank(message = "es obligatorio")
        @Size(max = 60, message = "máximo 60 caracteres")
        String nombre,

        @NotBlank(message = "es obligatoria")
        @Size(max = 30, message = "máximo 30 caracteres")
        String especie,

        @Size(max = 60, message = "máximo 60 caracteres")
        String raza,

        @PastOrPresent(message = "no puede ser una fecha futura")
        LocalDate fechaNacimiento,

        @Positive(message = "debe ser mayor a 0")
        @Digits(integer = 3, fraction = 2, message = "máximo 999.99 kg con 2 decimales")
        BigDecimal peso,

        @NotNull(message = "es obligatorio")
        Long clienteId
) {
}
