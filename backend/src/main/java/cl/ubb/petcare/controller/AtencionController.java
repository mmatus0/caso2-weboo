package cl.ubb.petcare.controller;

import cl.ubb.petcare.dto.AtencionResponse;
import cl.ubb.petcare.service.AtencionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/atenciones")
@Tag(name = "Atenciones", description = "Registros médicos")
public class AtencionController {

    private final AtencionService atencionService;

    public AtencionController(AtencionService atencionService) {
        this.atencionService = atencionService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una atención con sus recetas")
    public AtencionResponse obtener(@PathVariable Long id) {
        return atencionService.obtener(id);
    }
}
