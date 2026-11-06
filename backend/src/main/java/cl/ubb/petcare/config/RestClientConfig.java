package cl.ubb.petcare.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * Cliente HTTP para consumir la API externa de Node que corre en otro equipo.
 * La URL se configura en application.properties (petcare.api-externa.url).
 */
@Configuration
public class RestClientConfig {

    @Bean
    public RestClient apiExternaRestClient(RestClient.Builder builder,
                                           @Value("${petcare.api-externa.url}") String baseUrl,
                                           @Value("${petcare.api-externa.timeout-ms:5000}") long timeoutMs) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(timeoutMs));
        factory.setReadTimeout(Duration.ofMillis(timeoutMs));

        return builder
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
    }
}
