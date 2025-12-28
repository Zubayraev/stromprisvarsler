package no.strompris.repository;

import no.strompris.model.PriceData;
import no.strompris.model.PriceZone;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PriceDataRepository extends JpaRepository<PriceData, Long> {

    // ============================================
    // BASIC QUERIES
    // ============================================

    List<PriceData> findByZone(PriceZone zone);

    List<PriceData> findByZoneOrderByPriceTimestampDesc(PriceZone zone);

    List<PriceData> findByZoneAndPriceTimestampBetween(
            PriceZone zone,
            LocalDateTime start,
            LocalDateTime end
    );

    Optional<PriceData> findByZoneAndPriceTimestamp(
            PriceZone zone,
            LocalDateTime timestamp
    );

    List<PriceData> findByZoneAndPriceNokLessThan(
            PriceZone zone,
            BigDecimal threshold
    );

    List<PriceData> findByZoneAndPriceNokGreaterThan(
            PriceZone zone,
            BigDecimal threshold
    );

    // ============================================
    // CUSTOM QUERIES
    // ============================================

    @Query("""
        SELECT p FROM PriceData p
        WHERE p.zone = :zone
          AND p.priceTimestamp >= :startOfDay
          AND p.priceTimestamp <= :endOfDay
        ORDER BY p.priceTimestamp ASC
    """)
    List<PriceData> findTodaysPrices(
            @Param("zone") PriceZone zone,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );

    /**
     * ✅ RIKTIG: Hent de N billigste timene ved hjelp av Pageable
     */
    @Query("""
        SELECT p FROM PriceData p
        WHERE p.zone = :zone
          AND p.priceTimestamp >= :startOfDay
          AND p.priceTimestamp <= :endOfDay
        ORDER BY p.priceNok ASC
    """)
    List<PriceData> findCheapestHours(
            @Param("zone") PriceZone zone,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay,
            Pageable pageable
    );

    @Query("""
        SELECT AVG(p.priceNok) FROM PriceData p
        WHERE p.zone = :zone
          AND p.priceTimestamp >= :startOfDay
          AND p.priceTimestamp <= :endOfDay
    """)
    BigDecimal findAveragePriceToday(
            @Param("zone") PriceZone zone,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );

    @Query("""
        SELECT MIN(p.priceNok) FROM PriceData p
        WHERE p.zone = :zone
          AND p.priceTimestamp >= :startOfDay
          AND p.priceTimestamp <= :endOfDay
    """)
    BigDecimal findMinPriceToday(
            @Param("zone") PriceZone zone,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );

    @Query("""
        SELECT MAX(p.priceNok) FROM PriceData p
        WHERE p.zone = :zone
          AND p.priceTimestamp >= :startOfDay
          AND p.priceTimestamp <= :endOfDay
    """)
    BigDecimal findMaxPriceToday(
            @Param("zone") PriceZone zone,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );

    @Query("""
        SELECT p FROM PriceData p
        WHERE p.zone = :zone
          AND p.priceTimestamp = :currentHour
    """)
    Optional<PriceData> findCurrentPrice(
            @Param("zone") PriceZone zone,
            @Param("currentHour") LocalDateTime currentHour
    );

    @Query("""
        SELECT COUNT(p) > 0 FROM PriceData p
        WHERE p.zone = :zone
          AND p.priceTimestamp >= :startOfDay
          AND p.priceTimestamp <= :endOfDay
    """)
    boolean existsPriceDataForDate(
            @Param("zone") PriceZone zone,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );

    @Query("""
        DELETE FROM PriceData p
        WHERE p.priceTimestamp < :cutoffDate
    """)
    void deleteOldPriceData(@Param("cutoffDate") LocalDateTime cutoffDate);
}
