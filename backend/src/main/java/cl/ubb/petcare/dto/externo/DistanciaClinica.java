package cl.ubb.petcare.dto.externo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DistanciaClinica(
        Map<String, Object> origen,
        Map<String, Object> destino,
        Double distanciaKm
) {
}
