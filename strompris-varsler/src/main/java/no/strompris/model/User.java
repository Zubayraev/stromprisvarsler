package no.strompris.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * User Entity - Mapper til users tabellen i databasen
 *
 * Representerer en bruker som ønsker strømprisvarsler
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    /**
     * Primærnøkkel - Auto-generert ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Brukerens e-postadresse (må være unik)
     */
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    /**
     * Brukerens telefonnummer (valgfritt)
     * Format: +47xxxxxxxx
     */
    @Column(length = 20)
    private String phone;

    /**
     * Brukerens prisområde (NO1-NO5)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "price_zone", nullable = false, length = 10)
    private PriceZone priceZone;

    /**
     * Pristerskel for varsler (kr/kWh)
     * Eksempel: 1.50 - Send varsel når pris < 1.50 eller > 1.50
     */
    @Column(name = "alert_threshold", precision = 10, scale = 2)
    private BigDecimal alertThreshold;

    /**
     * Er varsler aktivert for denne brukeren?
     */
    @Column(name = "alert_enabled", nullable = false)
    private Boolean alertEnabled = true;

    /**
     * Tidspunkt når brukeren ble opprettet
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Tidspunkt når brukeren sist ble oppdatert
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Relasjon til varsler (One-to-Many)
     * En bruker kan ha mange varsler
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Alert> alerts = new ArrayList<>();

    /**
     * Pre-persist callback - Sett timestamps før lagring
     */
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    /**
     * Pre-update callback - Oppdater updatedAt før hver oppdatering
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Constructor for å opprette ny bruker (uten ID)
     */
    public User(String email, String phone, PriceZone priceZone,
                BigDecimal alertThreshold, Boolean alertEnabled) {
        this.email = email;
        this.phone = phone;
        this.priceZone = priceZone;
        this.alertThreshold = alertThreshold;
        this.alertEnabled = alertEnabled != null ? alertEnabled : true;
    }

    /**
     * Hjelpemetode for å sjekke om brukeren har aktiverte varsler
     */
    public boolean hasAlertsEnabled() {
        return alertEnabled != null && alertEnabled;
    }

    /**
     * Hjelpemetode for å legge til et varsel
     */
    public void addAlert(Alert alert) {
        alerts.add(alert);
        alert.setUser(this);
    }

    /**
     * Validering av e-post format
     */
    public boolean hasValidEmail() {
        if (email == null || email.isEmpty()) {
            return false;
        }
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
}