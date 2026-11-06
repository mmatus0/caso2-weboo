package cl.ubb.petcare.controller;

import cl.ubb.petcare.dto.AtencionResponse;
import cl.ubb.petcare.dto.MascotaRequest;
import cl.ubb.petcare.dto.MascotaResponse;
import cl.ubb.petcare.dto.ReservaResponse;
import cl.ubb.petcare.service.MascotaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/mascotas")
@Tag(name = "Mascotas", description = "Mascotas e historial médico")
public class MascotaController {

    private final MascotaService mascotaService;

    public MascotaController(MascotaService mascotaService) {
        this.mascotaService = mascotaService;
    }

    @GetMapping
    @Operation(summary = "Listar mascotas", description = "Filtro opcional ?especie=Perro")
    public List<MascotaResponse> listar(@RequestParam(required = false) String especie) {
        return mascotaService.listar(especie);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una mascota")
    public MascotaResponse obtener(@PathVariable Long id) {
        return mascotaService.obtener(id);
    }

    @PostMapping
    @Operation(summary = "Registrar una mascota y asociarla a un cliente")
    public ResponseEntity<MascotaResponse> crear(@Valid @RequestBody MascotaRequest request) {
        MascotaResponse creada = mascotaService.crear(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creada.id()).toUri();
        return ResponseEntity.created(location).body(creada);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modificar una mascota")
    public MascotaResponse actualizar(@PathVariable Long id, @Valid @RequestBody MascotaRequest request) {
        return mascotaService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar una mascota")
    public void eliminar(@PathVariable Long id) {
        mascotaService.eliminar(id);
    }

    @GetMapping("/{id}/historial")
    @Operation(summary = "Historial médico", description = "Atenciones con diagnóstico, tratamiento y recetas")
    public List<AtencionResponse> historial(@PathVariable Long id) {
        return mascotaService.historial(id);
    }

    @GetMapping("/{id}/reservas")
    @Operation(summary = "Reservas de una mascota")
    public List<ReservaResponse> reservas(@PathVariable Long id) {
        return mascotaService.reservas(id);
    }
}
