package cl.ubb.petcare.dto.externo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

/** Ubicación de la clínica entregada por la API externa de geolocalización. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ClinicaUbicacion(
        String nombre,
        String direccion,
        String ciudad,
        String region,
        String pais,
        String telefono,
        String email,
        String horario,
        Double latitud,
        Double longitud,
        Map<String, Object> mapa
) {
}
