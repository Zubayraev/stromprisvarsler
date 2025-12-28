package no.strompris.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Alert Entity - Mapper til alerts tabellen i databasen
 *
 * Representerer et varsel som er sendt til en bruker
 */
@Entity
@Table(name = "alerts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Alert {

    /**
     * Primærnøkkel - Auto-generert ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Relasjon til bruker (Many-to-One)
     * Mange varsler kan tilhøre én bruker
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Type varsel (PRICE_LOW, PRICE_HIGH, DAILY_SUMMARY, CHEAPEST_HOURS)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "alert_type", nullable = false, length = 50)
    private AlertType alertType;

    /**
     * Varselmeldingen som ble sendt til brukeren
     * Eksempel: "Strømprisen er nå kun 0.45 kr/kWh - perfekt tid for vask!"
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    /**
     * Strømprisen da varselet ble utløst (valgfritt)
     */
    @Column(name = "price_at_trigger", precision = 10, scale = 2)
    private BigDecimal priceAtTrigger;

    /**
     * Tidspunkt når varselet ble sendt
     */
    @Column(name = "triggered_at", nullable = false)
    private LocalDateTime triggeredAt;

    /**
     * Pre-persist callback - Sett triggeredAt automatisk
     */
    @PrePersist
    protected void onCreate() {
        if (triggeredAt == null) {
            triggeredAt = LocalDateTime.now();
        }
    }

    /**
     * Constructor for å opprette et nytt varsel (uten ID)
     */
    public Alert(User user, AlertType alertType, String message, BigDecimal priceAtTrigger) {
        this.user = user;
        this.alertType = alertType;
        this.message = message;
        this.priceAtTrigger = priceAtTrigger;
    }

    /**
     * Constructor uten pris (for varsler som ikke er prisbasert)
     */
    public Alert(User user, AlertType alertType, String message) {
        this(user, alertType, message, null);
    }

    /**
     * Hjelpemetode for å formatere varselet som en streng
     */
    public String getFormattedAlert() {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(alertType.getDisplayName()).append("] ");
        sb.append(message);
        if (priceAtTrigger != null) {
            sb.append(" (").append(priceAtTrigger).append(" kr/kWh)");
        }
        return sb.toString();
    }

    /**
     * Sjekk om varselet er nytt (sendt innen siste time)
     */
    public boolean isRecent() {
        if (triggeredAt == null) {
            return false;
        }
        return triggeredAt.isAfter(LocalDateTime.now().minusHours(1));
    }

    /**
     * Override toString for bedre logging
     */
    @Override
    public String toString() {
        return "Alert{" +
                "id=" + id +
                ", userId=" + (user != null ? user.getId() : "null") +
                ", type=" + alertType +
                ", message='" + message + '\'' +
                ", price=" + priceAtTrigger +
                ", triggeredAt=" + triggeredAt +
                '}';
    }
}