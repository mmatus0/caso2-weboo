package cl.ubb.petcare.dto.externo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MedicamentoBusqueda(
        String busqueda,
        int total,
        List<Medicamento> resultados
) {
}
