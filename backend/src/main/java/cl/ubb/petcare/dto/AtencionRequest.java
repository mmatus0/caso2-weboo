package cl.ubb.petcare.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

/** Registro de la atención de una reserva: diagnóstico, tratamiento y medicamentos recetados. */
public record AtencionRequest(

        @NotBlank(message = "es obligatorio")
        String diagnostico,

        @NotBlank(message = "es obligatorio")
        String tratamiento,

        String observaciones,

        // @Valid hace que también se validen cada una de las recetas
        @Valid
        List<RecetaRequest> recetas
) {
}
