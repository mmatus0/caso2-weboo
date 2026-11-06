package cl.ubb.petcare.client;

import cl.ubb.petcare.dto.externo.*;
import cl.ubb.petcare.exception.RecursoNoEncontradoException;
import cl.ubb.petcare.exception.ReglaNegocioException;
import cl.ubb.petcare.exception.ServicioExternoException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Único punto de contacto con la API externa de Node (segundo equipo).
 * Si mañana cambia la API, solo se modifica esta clase.
 */
@Component
public class ApiExternaClient {

    private static final String NO_DISPONIBLE =
            "La API externa no está disponible. Verifique que esté encendida y que la URL configurada sea correcta.";

    private final RestClient restClient;

    public ApiExternaClient(RestClient apiExternaRestClient) {
        this.restClient = apiExternaRestClient;
    }

    // ------------------------------------------------------------------ medicamentos

    public MedicamentoBusqueda buscarMedicamentos(String nombre) {
        try {
            return restClient.get()
                    .uri("/api/medicamentos?nombre={nombre}", nombre)
                    .retrieve()
                    .body(MedicamentoBusqueda.class);
        } catch (HttpClientErrorException.BadRequest e) {
            throw new ReglaNegocioException("El nombre a buscar debe tener al menos 2 caracteres.");
        } catch (RestClientException e) {
            throw new ServicioExternoException(NO_DISPONIBLE, e);
        }
    }

    public Medicamento obtenerMedicamento(Long id) {
        try {
            return restClient.get()
                    .uri("/api/medicamentos/{id}", id)
                    .retrieve()
                    .body(Medicamento.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new RecursoNoEncontradoException("Medicamento", id);
        } catch (RestClientException e) {
            throw new ServicioExternoException(NO_DISPONIBLE, e);
        }
    }

    // ------------------------------------------------------------------ geolocalización

    public ClinicaUbicacion obtenerUbicacionClinica() {
        try {
            return restClient.get()
                    .uri("/api/clinica/ubicacion")
                    .retrieve()
                    .body(ClinicaUbicacion.class);
        } catch (RestClientException e) {
            throw new ServicioExternoException(NO_DISPONIBLE, e);
        }
    }

    public DistanciaClinica calcularDistancia(double lat, double lng) {
        try {
            return restClient.get()
                    .uri("/api/clinica/distancia?lat={lat}&lng={lng}", lat, lng)
                    .retrieve()
                    .body(DistanciaClinica.class);
        } catch (HttpClientErrorException.BadRequest e) {
            throw new ReglaNegocioException("Coordenadas inválidas.");
        } catch (RestClientException e) {
            throw new ServicioExternoException(NO_DISPONIBLE, e);
        }
    }

    // ------------------------------------------------------------------ notificaciones

    /** Lanza RestClientException si falla; quien llama decide qué hacer. */
    public NotificacionEmailRespuesta enviarNotificacion(NotificacionEmailRequest request) {
        return restClient.post()
                .uri("/api/notificaciones/email")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(NotificacionEmailRespuesta.class);
    }

    public HistorialNotificaciones historialNotificaciones() {
        try {
            return restClient.get()
                    .uri("/api/notificaciones")
                    .retrieve()
                    .body(HistorialNotificaciones.class);
        } catch (RestClientException e) {
            throw new ServicioExternoException(NO_DISPONIBLE, e);
        }
    }
}
