package cl.ubb.petcare.dto;

import java.time.LocalDate;
import java.util.List;

/** Bloques de 1 hora libres y ocupados de un veterinario en un día. */
public record DisponibilidadResponse(
        Long veterinarioId,
        String veterinarioNombre,
        LocalDate fecha,
        List<String> bloquesLibres,
        List<String> bloquesOcupados
) {
}
