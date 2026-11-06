package cl.ubb.petcare.dto;

import cl.ubb.petcare.model.Veterinario;

public record VeterinarioResponse(
        Long id,
        String nombre,
        String especialidad,
        String email,
        boolean activo
) {
    public static VeterinarioResponse desde(Veterinario v) {
        return new VeterinarioResponse(v.getId(), v.getNombre(), v.getEspecialidad(), v.getEmail(), v.isActivo());
    }
}
