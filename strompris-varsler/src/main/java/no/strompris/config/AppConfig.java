package no.strompris.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * AppConfig - Application-wide configuration
 *
 * Definerer beans som kan brukes i hele applikasjonen
 */
@Configuration
public class AppConfig {

    /**
     * RestTemplate bean for å gjøre HTTP requests til eksterne APIer
     * Brukes av PriceFetcherService
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(5))
                .build();
    }
}