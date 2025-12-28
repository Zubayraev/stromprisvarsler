package no.strompris.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * SchedulingConfig - Aktiverer scheduled tasks
 *
 * Nødvendig for at @Scheduled annotation i PriceFetcherService skal fungere
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
    // Denne klassen aktiverer scheduling i Spring Boot
}