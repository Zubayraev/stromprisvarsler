package no.strompris.service;

import no.strompris.model.PriceData;
import no.strompris.model.PriceZone;
import no.strompris.repository.PriceDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PriceService {

    private static final Logger logger = LoggerFactory.getLogger(PriceService.class);

    private final PriceDataRepository priceDataRepository;

    @Autowired
    public PriceService(PriceDataRepository priceDataRepository) {
        this.priceDataRepository = priceDataRepository;
    }

    // ============================================
    // HENTE PRISER
    // ============================================

    public Optional<PriceData> getCurrentPrice(PriceZone zone) {
        logger.info("Henter nåværende pris for {}", zone);

        LocalDateTime currentHour = LocalDateTime.now()
                .withMinute(0)
                .withSecond(0)
                .withNano(0);

        return priceDataRepository.findCurrentPrice(zone, currentHour);
    }

    public List<PriceData> getTodaysPrices(PriceZone zone) {
        logger.info("Henter dagens priser for {}", zone);

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        return priceDataRepository.findTodaysPrices(zone, startOfDay, endOfDay);
    }

    public List<PriceData> getTomorrowsPrices(PriceZone zone) {
        logger.info("Henter morgendagens priser for {}", zone);

        LocalDateTime startOfTomorrow = LocalDate.now().plusDays(1).atStartOfDay();
        LocalDateTime endOfTomorrow = LocalDate.now().plusDays(1).atTime(LocalTime.MAX);

        return priceDataRepository.findTodaysPrices(zone, startOfTomorrow, endOfTomorrow);
    }

    public List<PriceData> getPricesForDate(PriceZone zone, LocalDate date) {
        logger.info("Henter priser for {} på dato {}", zone, date);

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        return priceDataRepository.findTodaysPrices(zone, startOfDay, endOfDay);
    }

    // ============================================
    // STATISTIKK OG ANALYSE
    // ============================================

    /**
     * ✅ FIKSET: Bruker Pageable
     */
    public List<PriceData> getCheapestHoursToday(PriceZone zone, int limit) {
        logger.info("Henter {} billigste timer for {}", limit, zone);

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        Pageable pageable = PageRequest.of(0, limit);

        return priceDataRepository.findCheapestHours(
                zone,
                startOfDay,
                endOfDay,
                pageable
        );
    }

    public BigDecimal getAveragePriceToday(PriceZone zone) {
        logger.info("Beregner gjennomsnittspris for {}", zone);

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        BigDecimal average = priceDataRepository.findAveragePriceToday(zone, startOfDay, endOfDay);

        return average != null
                ? average.setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
    }

    public BigDecimal getMinPriceToday(PriceZone zone) {
        logger.info("Henter laveste pris for {}", zone);

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        BigDecimal min = priceDataRepository.findMinPriceToday(zone, startOfDay, endOfDay);
        return min != null ? min : BigDecimal.ZERO;
    }

    public BigDecimal getMaxPriceToday(PriceZone zone) {
        logger.info("Henter høyeste pris for {}", zone);

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        BigDecimal max = priceDataRepository.findMaxPriceToday(zone, startOfDay, endOfDay);
        return max != null ? max : BigDecimal.ZERO;
    }

    public boolean isPriceAboveAverage(PriceZone zone, BigDecimal price) {
        return price.compareTo(getAveragePriceToday(zone)) > 0;
    }

    public boolean isPriceBelowAverage(PriceZone zone, BigDecimal price) {
        return price.compareTo(getAveragePriceToday(zone)) < 0;
    }

    // ============================================
    // LAGRING
    // ============================================

    public PriceData savePriceData(PriceData priceData) {
        logger.info("Lagrer prisdata: {} - {} kr/kWh",
                priceData.getZone(), priceData.getPriceNok());

        return priceDataRepository.save(priceData);
    }

    public List<PriceData> savePriceDataBulk(List<PriceData> priceDataList) {
        logger.info("Lagrer {} prisdata records", priceDataList.size());
        return priceDataRepository.saveAll(priceDataList);
    }

    public boolean hasPriceDataForToday(PriceZone zone) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        return priceDataRepository.existsPriceDataForDate(zone, startOfDay, endOfDay);
    }

    public boolean hasPriceDataForTomorrow(PriceZone zone) {
        LocalDateTime startOfTomorrow = LocalDate.now().plusDays(1).atStartOfDay();
        LocalDateTime endOfTomorrow = LocalDate.now().plusDays(1).atTime(LocalTime.MAX);

        return priceDataRepository.existsPriceDataForDate(zone, startOfTomorrow, endOfTomorrow);
    }

    // ============================================
    // OPPRYDDING
    // ============================================

    public void deleteOldPriceData() {
        logger.info("Sletter prisdata eldre enn 30 dager");

        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        priceDataRepository.deleteOldPriceData(cutoffDate);
    }
}
