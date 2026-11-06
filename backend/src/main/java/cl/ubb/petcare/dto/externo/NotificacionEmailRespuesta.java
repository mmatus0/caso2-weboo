package cl.ubb.petcare.dto.externo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NotificacionEmailRespuesta(
        Long id,
        boolean enviada,
        String tipo,
        String para,
        String asunto,
        Long reservaId,
        String modo,
        String previewUrl,
        String fecha
) {
}
