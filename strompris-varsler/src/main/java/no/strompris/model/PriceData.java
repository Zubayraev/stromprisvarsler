package no.strompris.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * PriceData Entity - Mapper til price_data tabellen i databasen
 *
 * Representerer strømpriser per time for et gitt prisområde
 */
@Entity
@Table(name = "price_data",
        uniqueConstraints = @UniqueConstraint(columnNames = {"zone", "price_timestamp"}))
@Data  // Lombok: Genererer getters, setters, toString, equals, hashCode
@NoArgsConstructor  // Lombok: Genererer no-args constructor (påkrevd av JPA)
@AllArgsConstructor  // Lombok: Genererer constructor med alle felter
public class PriceData {

    /**
     * Primærnøkkel - Auto-generert ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Prisområde (NO1, NO2, NO3, NO4, NO5)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private PriceZone zone;

    /**
     * Tidspunkt for prisen (time-oppløsning)
     * Eksempel: 2025-12-21 14:00:00
     */
    @Column(name = "price_timestamp", nullable = false)
    private LocalDateTime priceTimestamp;

    /**
     * Pris i norske kroner per kWh
     * Eksempel: 1.25 kr/kWh
     */
    @Column(name = "price_nok", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceNok;

    /**
     * Originalpris i EUR per kWh (fra API)
     * Eksempel: 0.11 EUR/kWh
     */
    @Column(name = "price_eur", nullable = false, precision = 10, scale = 4)
    private BigDecimal priceEur;

    /**
     * Tidspunkt når denne prisen ble lagret i databasen
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Pre-persist callback - Sett createdAt automatisk før lagring
     */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    /**
     * Constructor for å opprette PriceData uten ID (for nye records)
     */
    public PriceData(PriceZone zone, LocalDateTime priceTimestamp,
                     BigDecimal priceNok, BigDecimal priceEur) {
        this.zone = zone;
        this.priceTimestamp = priceTimestamp;
        this.priceNok = priceNok;
        this.priceEur = priceEur;
    }

    /**
     * Hjelpemetode for å få time på dagen (0-23)
     */
    public int getHourOfDay() {
        return priceTimestamp.getHour();
    }

    /**
     * Sjekk om prisen er høy (over gitt terskel)
     */
    public boolean isPriceHigh(BigDecimal threshold) {
        return priceNok.compareTo(threshold) > 0;
    }

    /**
     * Sjekk om prisen er lav (under gitt terskel)
     */
    public boolean isPriceLow(BigDecimal threshold) {
        return priceNok.compareTo(threshold) < 0;
    }
}