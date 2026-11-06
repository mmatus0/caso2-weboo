package cl.ubb.petcare.dto.externo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/** Medicamento tal como lo entrega la API externa de Node. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Medicamento(
        Integer id,
        String nombre,
        String categoria,
        List<String> presentaciones,
        List<String> especies,
        String uso,
        Boolean requiereReceta,
        String advertencias
) {
}
