package cl.ubb.petcare.controller;

import cl.ubb.petcare.dto.DisponibilidadResponse;
import cl.ubb.petcare.dto.VeterinarioRequest;
import cl.ubb.petcare.dto.VeterinarioResponse;
import cl.ubb.petcare.service.VeterinarioService;
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
@RequestMapping("/api/veterinarios")
@Tag(name = "Veterinarios", description = "Profesionales y su disponibilidad")
public class VeterinarioController {

    private final VeterinarioService veterinarioService;

    public VeterinarioController(VeterinarioService veterinarioService) {
        this.veterinarioService = veterinarioService;
    }

    @GetMapping
    @Operation(summary = "Listar veterinarios", description = "?soloActivos=true para excluir inactivos")
    public List<VeterinarioResponse> listar(@RequestParam(defaultValue = "false") boolean soloActivos) {
        return veterinarioService.listar(soloActivos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un veterinario")
    public VeterinarioResponse obtener(@PathVariable Long id) {
        return veterinarioService.obtener(id);
    }

    @PostMapping
    @Operation(summary = "Registrar un veterinario")
    public ResponseEntity<VeterinarioResponse> crear(@Valid @RequestBody VeterinarioRequest request) {
        VeterinarioResponse creado = veterinarioService.crear(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.id()).toUri();
        return ResponseEntity.created(location).body(creado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modificar o desactivar un veterinario")
    public VeterinarioResponse actualizar(@PathVariable Long id, @Valid @RequestBody VeterinarioRequest request) {
        return veterinarioService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar un veterinario sin reservas")
    public void eliminar(@PathVariable Long id) {
        veterinarioService.eliminar(id);
    }

    @GetMapping("/{id}/disponibilidad")
    @Operation(summary = "Bloques libres de un día", description = "?fecha=2026-11-20")
    public DisponibilidadResponse disponibilidad(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return veterinarioService.disponibilidad(id, fecha);
    }
}
