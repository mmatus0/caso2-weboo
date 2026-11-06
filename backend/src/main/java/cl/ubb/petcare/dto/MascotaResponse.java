package cl.ubb.petcare.dto;

import cl.ubb.petcare.model.Mascota;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MascotaResponse(
        Long id,
        String nombre,
        String especie,
        String raza,
        LocalDate fechaNacimiento,
        String edad,
        BigDecimal peso,
        Long clienteId,
        String clienteNombre
) {
    public static MascotaResponse desde(Mascota m) {
        return new MascotaResponse(m.getId(), m.getNombre(), m.getEspecie(), m.getRaza(),
                m.getFechaNacimiento(), m.getEdad(), m.getPeso(),
                m.getCliente().getId(), m.getCliente().getNombre());
    }
}
