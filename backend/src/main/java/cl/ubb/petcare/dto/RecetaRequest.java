package cl.ubb.petcare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record RecetaRequest(

        @NotBlank(message = "es obligatorio")
        @Size(max = 100, message = "máximo 100 caracteres")
        String medicamento,

        @NotBlank(message = "es obligatoria")
        @Size(max = 60, message = "máximo 60 caracteres")
        String dosis,

        @NotBlank(message = "es obligatoria")
        @Size(max = 60, message = "máximo 60 caracteres")
        String frecuencia,

        @NotNull(message = "es obligatorio")
        @Positive(message = "debe ser mayor a 0")
        Integer dias
) {
}
