package cl.ubb.petcare.dto;

import cl.ubb.petcare.model.Cliente;

public record ClienteResponse(
        Long id,
        String rut,
        String nombre,
        String email,
        String telefono,
        String direccion
) {
    public static ClienteResponse desde(Cliente c) {
        return new ClienteResponse(c.getId(), c.getRut(), c.getNombre(),
                c.getEmail(), c.getTelefono(), c.getDireccion());
    }
}
