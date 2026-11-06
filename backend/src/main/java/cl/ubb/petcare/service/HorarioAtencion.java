package cl.ubb.petcare.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Reglas del horario de la clínica: bloques de 1 hora.
 * Con inicio=9 y fin=18 los bloques son 09:00, 10:00, ..., 17:00 (el último termina a las 18:00).
 */
@Component
public class HorarioAtencion {

    public static final ZoneId ZONA = ZoneId.of("America/Santiago");

    private final int horaInicio;
    private final int horaFin;

    public HorarioAtencion(@Value("${petcare.horario.hora-inicio:9}") int horaInicio,
                           @Value("${petcare.horario.hora-fin:18}") int horaFin) {
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
    }

    /** true si la hora es exacta (minuto 0) y está dentro del horario. */
    public boolean esBloqueValido(LocalTime hora) {
        return hora.getMinute() == 0
                && hora.getSecond() == 0
                && hora.getNano() == 0
                && hora.getHour() >= horaInicio
                && hora.getHour() < horaFin;
    }

    public List<LocalTime> bloquesDelDia() {
        List<LocalTime> bloques = new ArrayList<>();
        for (int h = horaInicio; h < horaFin; h++) {
            bloques.add(LocalTime.of(h, 0));
        }
        return bloques;
    }

    /** true si el bloque (fecha + hora) ya comenzó. */
    public boolean yaPaso(LocalDate fecha, LocalTime hora) {
        return LocalDateTime.of(fecha, hora).isBefore(LocalDateTime.now(ZONA));
    }

    public String descripcion() {
        return String.format("bloques de 1 hora entre %02d:00 y %02d:00", horaInicio, horaFin - 1);
    }
}
