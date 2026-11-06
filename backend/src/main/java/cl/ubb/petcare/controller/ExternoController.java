package cl.ubb.petcare.controller;

import cl.ubb.petcare.dto.externo.*;
import cl.ubb.petcare.service.ExternoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

/**
 * Expone al front los datos de la API externa (Node), que corre en otro equipo.
 * El navegador nunca llama a Node directamente: Spring hace de puente.
 */
@RestController
@RequestMapping("/api/externo")
@Tag(name = "Integración externa", description = "Medicamentos, geolocalización y notificaciones (API Node)")
public class ExternoController {

    private final ExternoService externoService;

    public ExternoController(ExternoService externoService) {
        this.externoService = externoService;
    }

    @GetMapping("/medicamentos")
    @Operation(summary = "Buscar medicamentos por nombre", description = "?nombre=amoxi")
    public MedicamentoBusqueda buscarMedicamentos(@RequestParam String nombre) {
        return externoService.buscarMedicamentos(nombre);
    }

    @GetMapping("/medicamentos/{id}")
    @Operation(summary = "Detalle de un medicamento")
    public Medicamento medicamento(@PathVariable Long id) {
        return externoService.obtenerMedicamento(id);
    }

    @GetMapping("/clinica")
    @Operation(summary = "Ubicación de la clínica")
    public ClinicaUbicacion clinica() {
        return externoService.ubicacionClinica();
    }

    @GetMapping("/clinica/distancia")
    @Operation(summary = "Distancia desde un punto hasta la clínica", description = "?lat=-36.61&lng=-72.10")
    public DistanciaClinica distancia(@RequestParam double lat, @RequestParam double lng) {
        return externoService.distanciaClinica(lat, lng);
    }

    @GetMapping("/notificaciones")
    @Operation(summary = "Últimas notificaciones enviadas")
    public HistorialNotificaciones notificaciones() {
        return externoService.historialNotificaciones();
    }
}
