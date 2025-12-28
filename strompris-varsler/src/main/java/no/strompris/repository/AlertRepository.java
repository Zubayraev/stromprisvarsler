package no.strompris.repository;

import no.strompris.model.Alert;
import no.strompris.model.AlertType;
import no.strompris.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AlertRepository - Repository for å håndtere varsler
 *
 * Gir CRUD-operasjoner og custom queries for Alert entity
 */
@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    // ============================================
    // BASIC QUERIES
    // ============================================

    /**
     * Finn alle varsler for en bruker
     * @param user Bruker-objekt
     * @return Liste av varsler
     */
    List<Alert> findByUser(User user);

    /**
     * Finn alle varsler for en bruker, sortert etter tidspunkt (nyeste først)
     * @param user Bruker-objekt
     * @return Liste av varsler
     */
    List<Alert> findByUserOrderByTriggeredAtDesc(User user);

    /**
     * Finn varsler basert på bruker-ID
     * @param userId Bruker-ID
     * @return Liste av varsler
     */
    List<Alert> findByUserId(Long userId);

    /**
     * Finn varsler basert på bruker-ID, sortert etter tidspunkt (nyeste først)
     * @param userId Bruker-ID
     * @return Liste av varsler
     */
    List<Alert> findByUserIdOrderByTriggeredAtDesc(Long userId);

    /**
     * Finn varsler av en bestemt type
     * @param alertType Type varsel (PRICE_LOW, PRICE_HIGH, etc.)
     * @return Liste av varsler
     */
    List<Alert> findByAlertType(AlertType alertType);

    /**
     * Finn varsler for en bruker av en bestemt type
     * @param user Bruker-objekt
     * @param alertType Type varsel
     * @return Liste av varsler
     */
    List<Alert> findByUserAndAlertType(User user, AlertType alertType);

    /**
     * Finn varsler sendt etter et gitt tidspunkt
     * @param dateTime Tidspunkt
     * @return Liste av varsler
     */
    List<Alert> findByTriggeredAtAfter(LocalDateTime dateTime);

    /**
     * Finn varsler sendt mellom to tidspunkter
     * @param start Start-tidspunkt
     * @param end Slutt-tidspunkt
     * @return Liste av varsler
     */
    List<Alert> findByTriggeredAtBetween(LocalDateTime start, LocalDateTime end);

    // ============================================
    // CUSTOM QUERIES
    // ============================================

    /**
     * Finn de siste N varslene for en bruker
     * @param userId Bruker-ID
     * @return Liste av nyeste varsler (begrenses med Pageable)
     */
    @Query("SELECT a FROM Alert a WHERE a.user.id = :userId " +
            "ORDER BY a.triggeredAt DESC")
    List<Alert> findRecentAlertsForUser(@Param("userId") Long userId);

    /**
     * Finn varsler for i dag for en bruker
     * @param userId Bruker-ID
     * @param startOfDay Start av dagen (00:00:00)
     * @param endOfDay Slutt av dagen (23:59:59)
     * @return Liste av dagens varsler
     */
    @Query("SELECT a FROM Alert a WHERE a.user.id = :userId " +
            "AND a.triggeredAt >= :startOfDay " +
            "AND a.triggeredAt <= :endOfDay " +
            "ORDER BY a.triggeredAt DESC")
    List<Alert> findTodaysAlertsForUser(
            @Param("userId") Long userId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );

    /**
     * Tell antall varsler per type for en bruker
     * @param userId Bruker-ID
     * @return Liste med [alertType, count]
     */
    @Query("SELECT a.alertType, COUNT(a) FROM Alert a " +
            "WHERE a.user.id = :userId " +
            "GROUP BY a.alertType")
    List<Object[]> countAlertsByTypeForUser(@Param("userId") Long userId);

    /**
     * Tell totalt antall varsler sendt i dag
     * @param startOfDay Start av dagen
     * @param endOfDay Slutt av dagen
     * @return Antall varsler
     */
    @Query("SELECT COUNT(a) FROM Alert a " +
            "WHERE a.triggeredAt >= :startOfDay " +
            "AND a.triggeredAt <= :endOfDay")
    Long countAlertsToday(
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );

    /**
     * Sjekk om bruker har fått et varsel av en gitt type i dag
     * (for å unngå spam - ikke send samme varsel flere ganger samme dag)
     * @param userId Bruker-ID
     * @param alertType Type varsel
     * @param startOfDay Start av dagen
     * @return true hvis varsel allerede sendt i dag
     */
    @Query("SELECT COUNT(a) > 0 FROM Alert a " +
            "WHERE a.user.id = :userId " +
            "AND a.alertType = :alertType " +
            "AND a.triggeredAt >= :startOfDay")
    boolean hasReceivedAlertToday(
            @Param("userId") Long userId,
            @Param("alertType") AlertType alertType,
            @Param("startOfDay") LocalDateTime startOfDay
    );

    /**
     * Hent gjennomsnittlig antall varsler per bruker
     * @return Gjennomsnitt (kan være null)
     */
    @Query("SELECT AVG(alertCount) FROM " +
            "(SELECT COUNT(a) as alertCount FROM Alert a GROUP BY a.user.id)")
    Double getAverageAlertsPerUser();

    /**
     * Finn mest aktive brukere (flest varsler mottatt)
     * @param limit Antall brukere (f.eks. top 10)
     * @return Liste med [userId, email, alertCount]
     */
    @Query("SELECT a.user.id, a.user.email, COUNT(a) as alertCount " +
            "FROM Alert a " +
            "GROUP BY a.user.id, a.user.email " +
            "ORDER BY alertCount DESC")
    List<Object[]> findMostActiveUsers(@Param("limit") int limit);

    /**
     * Slett gamle varsler (for å holde databasen ryddig)
     * @param cutoffDate Dato - alt før dette slettes
     */
    @Query("DELETE FROM Alert a WHERE a.triggeredAt < :cutoffDate")
    void deleteOldAlerts(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Finn alle varsler for brukere i en bestemt priszone
     * @param zoneString Prisområde som streng (f.eks. "NO1")
     * @return Liste av varsler
     */
    @Query("SELECT a FROM Alert a WHERE a.user.priceZone = :zone " +
            "ORDER BY a.triggeredAt DESC")
    List<Alert> findAlertsByZone(@Param("zone") String zoneString);
}