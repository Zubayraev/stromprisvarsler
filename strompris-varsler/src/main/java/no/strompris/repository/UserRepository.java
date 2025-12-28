package no.strompris.repository;

import no.strompris.model.PriceZone;
import no.strompris.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * UserRepository - Repository for å håndtere brukere
 *
 * Gir CRUD-operasjoner og custom queries for User entity
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // ============================================
    // BASIC QUERIES
    // ============================================

    /**
     * Finn bruker basert på e-post
     * @param email E-postadresse
     * @return Optional med bruker hvis funnet
     */
    Optional<User> findByEmail(String email);

    /**
     * Finn bruker basert på telefonnummer
     * @param phone Telefonnummer
     * @return Optional med bruker hvis funnet
     */
    Optional<User> findByPhone(String phone);

    /**
     * Sjekk om e-post allerede eksisterer
     * @param email E-postadresse
     * @return true hvis e-post eksisterer
     */
    boolean existsByEmail(String email);

    /**
     * Finn alle brukere i et gitt prisområde
     * @param zone Prisområde (NO1-NO5)
     * @return Liste av brukere
     */
    List<User> findByPriceZone(PriceZone zone);

    /**
     * Finn alle brukere med aktiverte varsler
     * @return Liste av brukere med alertEnabled = true
     */
    List<User> findByAlertEnabledTrue();

    /**
     * Finn alle brukere med deaktiverte varsler
     * @return Liste av brukere med alertEnabled = false
     */
    List<User> findByAlertEnabledFalse();

    // ============================================
    // ADVANCED QUERIES
    // ============================================

    /**
     * Finn alle brukere i en zone som har aktiverte varsler
     * @param zone Prisområde
     * @return Liste av brukere
     */
    List<User> findByPriceZoneAndAlertEnabledTrue(PriceZone zone);

    /**
     * Finn brukere med pristerskel under en gitt verdi
     * @param threshold Prisgrense
     * @return Liste av brukere
     */
    List<User> findByAlertThresholdLessThan(BigDecimal threshold);

    /**
     * Finn brukere med pristerskel over en gitt verdi
     * @param threshold Prisgrense
     * @return Liste av brukere
     */
    List<User> findByAlertThresholdGreaterThan(BigDecimal threshold);

    // ============================================
    // CUSTOM QUERIES
    // ============================================

    /**
     * Finn alle brukere som skal motta varsler for en gitt zone
     * (har aktiverte varsler OG er i den gitte zonen)
     * @param zone Prisområde
     * @return Liste av brukere som skal varsles
     */
    @Query("SELECT u FROM User u WHERE u.priceZone = :zone " +
            "AND u.alertEnabled = true " +
            "AND u.alertThreshold IS NOT NULL")
    List<User> findUsersToNotifyInZone(@Param("zone") PriceZone zone);

    /**
     * Finn brukere som skal varsles om lav pris
     * (pris er under brukerens terskel)
     * @param zone Prisområde
     * @param currentPrice Nåværende pris
     * @return Liste av brukere som skal varsles
     */
    @Query("SELECT u FROM User u WHERE u.priceZone = :zone " +
            "AND u.alertEnabled = true " +
            "AND u.alertThreshold IS NOT NULL " +
            "AND u.alertThreshold > :currentPrice")
    List<User> findUsersForLowPriceAlert(
            @Param("zone") PriceZone zone,
            @Param("currentPrice") BigDecimal currentPrice
    );

    /**
     * Finn brukere som skal varsles om høy pris
     * (pris er over brukerens terskel)
     * @param zone Prisområde
     * @param currentPrice Nåværende pris
     * @return Liste av brukere som skal varsles
     */
    @Query("SELECT u FROM User u WHERE u.priceZone = :zone " +
            "AND u.alertEnabled = true " +
            "AND u.alertThreshold IS NOT NULL " +
            "AND u.alertThreshold < :currentPrice")
    List<User> findUsersForHighPriceAlert(
            @Param("zone") PriceZone zone,
            @Param("currentPrice") BigDecimal currentPrice
    );

    /**
     * Tell antall brukere per prisområde
     * @return Liste med zone og antall brukere
     */
    @Query("SELECT u.priceZone, COUNT(u) FROM User u GROUP BY u.priceZone")
    List<Object[]> countUsersByZone();

    /**
     * Hent statistikk: Antall aktive vs inaktive brukere
     * @return Array med [aktive, inaktive]
     */
    @Query("SELECT " +
            "SUM(CASE WHEN u.alertEnabled = true THEN 1 ELSE 0 END) as active, " +
            "SUM(CASE WHEN u.alertEnabled = false THEN 1 ELSE 0 END) as inactive " +
            "FROM User u")
    Object[] getUserStatistics();
}