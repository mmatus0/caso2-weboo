package cl.ubb.petcare.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VeterinarioRequest(

        @NotBlank(message = "es obligatorio")
        @Size(max = 100, message = "máximo 100 caracteres")
        String nombre,

        @NotBlank(message = "es obligatoria")
        @Size(max = 80, message = "máximo 80 caracteres")
        String especialidad,

        @NotBlank(message = "es obligatorio")
        @Email(message = "debe ser un correo válido")
        String email,

        // Opcional: si no se envía, el veterinario queda activo
        Boolean activo
) {
}
