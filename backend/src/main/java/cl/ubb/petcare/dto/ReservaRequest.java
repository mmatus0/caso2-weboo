package cl.ubb.petcare.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

/** Solicitud de hora médica (crear o modificar). */
public record ReservaRequest(

        @NotNull(message = "es obligatoria")
        Long mascotaId,

        @NotNull(message = "es obligatorio")
        Long veterinarioId,

        @NotNull(message = "es obligatoria")
        @FutureOrPresent(message = "no puede ser una fecha pasada")
        LocalDate fecha,

        @NotNull(message = "es obligatoria")
        @JsonFormat(pattern = "HH:mm")
        LocalTime hora,

        @NotBlank(message = "es obligatorio")
        @Size(max = 200, message = "máximo 200 caracteres")
        String motivo
) {
}
