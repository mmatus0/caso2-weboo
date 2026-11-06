package cl.ubb.petcare.controller;

import cl.ubb.petcare.dto.*;
import cl.ubb.petcare.model.EstadoReserva;
import cl.ubb.petcare.service.AtencionService;
import cl.ubb.petcare.service.ReservaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reservas")
@Tag(name = "Reservas", description = "Horas médicas: solicitar, modificar, confirmar, cancelar y atender")
public class ReservaController {

    private final ReservaService reservaService;
    private final AtencionService atencionService;

    public ReservaController(ReservaService reservaService, AtencionService atencionService) {
        this.reservaService = reservaService;
        this.atencionService = atencionService;
    }

    @GetMapping
    @Operation(summary = "Listar reservas", description = "Filtros opcionales: fecha, estado, veterinarioId, mascotaId")
    public List<ReservaResponse> listar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false) EstadoReserva estado,
            @RequestParam(required = false) Long veterinarioId,
            @RequestParam(required = false) Long mascotaId) {
        return reservaService.listar(fecha, estado, veterinarioId, mascotaId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una reserva")
    public ReservaResponse obtener(@PathVariable Long id) {
        return reservaService.obtener(id);
    }

    @PostMapping
    @Operation(summary = "Solicitar una hora médica", description = "Queda en estado PENDIENTE")
    public ResponseEntity<ReservaResponse> crear(@Valid @RequestBody ReservaRequest request) {
        ReservaResponse creada = reservaService.crear(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creada.id()).toUri();
        return ResponseEntity.created(location).body(creada);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modificar una reserva", description = "Notifica al cliente por correo")
    public ReservaAccionResponse modificar(@PathVariable Long id, @Valid @RequestBody ReservaRequest request) {
        return reservaService.modificar(id, request);
    }

    @PatchMapping("/{id}/confirmar")
    @Operation(summary = "Confirmar una reserva", description = "PENDIENTE -> CONFIRMADA. Notifica al cliente")
    public ReservaAccionResponse confirmar(@PathVariable Long id) {
        return reservaService.confirmar(id);
    }

    @PatchMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar una reserva", description = "Libera el bloque horario. Notifica al cliente")
    public ReservaAccionResponse cancelar(@PathVariable Long id) {
        return reservaService.cancelar(id);
    }

    @PostMapping("/{id}/atencion")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar la atención", description = "Diagnóstico, tratamiento y recetas. CONFIRMADA -> ATENDIDA")
    public AtencionResponse atender(@PathVariable Long id, @Valid @RequestBody AtencionRequest request) {
        return atencionService.registrar(id, request);
    }
}
