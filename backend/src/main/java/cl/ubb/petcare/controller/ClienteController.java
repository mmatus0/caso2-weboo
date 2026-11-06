package cl.ubb.petcare.controller;

import cl.ubb.petcare.dto.ClienteRequest;
import cl.ubb.petcare.dto.ClienteResponse;
import cl.ubb.petcare.dto.MascotaResponse;
import cl.ubb.petcare.service.ClienteService;
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
@RequestMapping("/api/clientes")
@Tag(name = "Clientes", description = "Registro y mantención de clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping
    @Operation(summary = "Listar clientes", description = "Filtro opcional ?buscar= por nombre, RUT o email")
    public List<ClienteResponse> listar(@RequestParam(required = false) String buscar) {
        return clienteService.listar(buscar);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un cliente")
    public ClienteResponse obtener(@PathVariable Long id) {
        return clienteService.obtener(id);
    }

    @PostMapping
    @Operation(summary = "Registrar un cliente")
    public ResponseEntity<ClienteResponse> crear(@Valid @RequestBody ClienteRequest request) {
        ClienteResponse creado = clienteService.crear(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.id()).toUri();
        return ResponseEntity.created(location).body(creado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modificar un cliente")
    public ClienteResponse actualizar(@PathVariable Long id, @Valid @RequestBody ClienteRequest request) {
        return clienteService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar un cliente", description = "Elimina también sus mascotas e historial")
    public void eliminar(@PathVariable Long id) {
        clienteService.eliminar(id);
    }

    @GetMapping("/{id}/mascotas")
    @Operation(summary = "Mascotas de un cliente")
    public List<MascotaResponse> mascotas(@PathVariable Long id) {
        return clienteService.mascotasDeCliente(id);
    }
}
