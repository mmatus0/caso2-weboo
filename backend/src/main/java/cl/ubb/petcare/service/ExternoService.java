package cl.ubb.petcare.service;

import cl.ubb.petcare.client.ApiExternaClient;
import cl.ubb.petcare.dto.externo.*;
import cl.ubb.petcare.exception.ReglaNegocioException;
import org.springframework.stereotype.Service;

/**
 * Funcionalidades que se obtienen de la API externa (Node):
 * consulta de medicamentos, geolocalización de la clínica e historial de notificaciones.
 */
@Service
public class ExternoService {

    private final ApiExternaClient apiExterna;

    public ExternoService(ApiExternaClient apiExterna) {
        this.apiExterna = apiExterna;
    }

    public MedicamentoBusqueda buscarMedicamentos(String nombre) {
        if (nombre == null || nombre.trim().length() < 2) {
            throw new ReglaNegocioException("Ingrese al menos 2 caracteres del nombre del medicamento.");
        }
        return apiExterna.buscarMedicamentos(nombre.trim());
    }

    public Medicamento obtenerMedicamento(Long id) {
        return apiExterna.obtenerMedicamento(id);
    }

    public ClinicaUbicacion ubicacionClinica() {
        return apiExterna.obtenerUbicacionClinica();
    }

    public DistanciaClinica distanciaClinica(double lat, double lng) {
        return apiExterna.calcularDistancia(lat, lng);
    }

    public HistorialNotificaciones historialNotificaciones() {
        return apiExterna.historialNotificaciones();
    }
}
