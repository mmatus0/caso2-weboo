package cl.ubb.petcare.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Metadatos de la documentación Swagger: http://localhost:8080/swagger-ui.html */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI petcareOpenApi() {
        return new OpenAPI().info(new Info()
                .title("PetCare API")
                .version("1.0.0")
                .description("Sistema de Gestión de Atención Veterinaria - Desarrollo Web Orientado a Objeto, UBB")
                .contact(new Contact().name("Gonzalo y María Zapata Fica")));
    }
}
