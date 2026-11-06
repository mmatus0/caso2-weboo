package cl.ubb.petcare.dto;

import cl.ubb.petcare.model.Receta;

public record RecetaResponse(
        Long id,
        String medicamento,
        String dosis,
        String frecuencia,
        Integer dias
) {
    public static RecetaResponse desde(Receta r) {
        return new RecetaResponse(r.getId(), r.getMedicamento(), r.getDosis(), r.getFrecuencia(), r.getDias());
    }
}
