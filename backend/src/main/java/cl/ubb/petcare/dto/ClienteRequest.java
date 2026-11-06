package cl.ubb.petcare.dto;

import cl.ubb.petcare.validation.RutValido;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Datos que llegan al crear o modificar un cliente. */
public record ClienteRequest(

        @NotBlank(message = "es obligatorio")
        @RutValido
        String rut,

        @NotBlank(message = "es obligatorio")
        @Size(max = 100, message = "máximo 100 caracteres")
        String nombre,

        @NotBlank(message = "es obligatorio")
        @Email(message = "debe ser un correo válido")
        @Size(max = 120, message = "máximo 120 caracteres")
        String email,

        @NotBlank(message = "es obligatorio")
        @Pattern(regexp = "^\\+?\\d{8,15}$", message = "debe tener entre 8 y 15 dígitos, ej: +56912345678")
        String telefono,

        @Size(max = 200, message = "máximo 200 caracteres")
        String direccion
) {
}
