package no.strompris.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.strompris.model.PriceData;
import no.strompris.model.PriceZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * PriceFetcherService - Henter strømpriser fra eksternt API
 *
 * Henter data fra Hvakosterstrommen.no API:
 * - Dagens priser
 * - Morgendagens priser (tilgjengelig fra ca. kl 13:00)
 *
 * Kjører automatisk hver time via @Scheduled annotation
 */
@Service
public class PriceFetcherService {

    private static final Logger logger = LoggerFactory.getLogger(PriceFetcherService.class);

    // API URL fra application.properties
    @Value("${strompris.api.base-url:https://www.hvakosterstrommen.no/api/v1/prices}")
    private String apiBaseUrl;

    private final PriceService priceService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public PriceFetcherService(PriceService priceService) {
        this.priceService = priceService;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    // ============================================
    // SCHEDULED TASKS
    // ============================================

    /**
     * Hent priser hver time (kjører automatisk)
     * Cron: "0 5 * * * *" = Hver time, 5 minutter over (00:05, 01:05, 02:05, etc.)
     */
    @Scheduled(cron = "${strompris.price-fetch.cron:0 5 * * * *}")
    public void fetchPricesScheduled() {
        logger.info("=== SCHEDULED: Starter prisoppdatering ===");

        try {
            // Hent priser for alle zoner
            fetchPricesForAllZones();

            logger.info("=== SCHEDULED: Prisoppdatering fullført ===");
        } catch (Exception e) {
            logger.error("Feil under scheduled prisoppdatering", e);
        }
    }

    /**
     * Hent morgendagens priser (kjører én gang per dag kl 13:30)
     * Morgendagens priser publiseres rundt kl 13:00
     */
    @Scheduled(cron = "0 30 13 * * *")
    public void fetchTomorrowsPricesScheduled() {
        logger.info("=== SCHEDULED: Henter morgendagens priser ===");

        try {
            fetchTomorrowsPricesForAllZones();
            logger.info("=== SCHEDULED: Morgendagens priser hentet ===");
        } catch (Exception e) {
            logger.error("Feil under henting av morgendagens priser", e);
        }
    }

    // ============================================
    // HENTE PRISER
    // ============================================

    /**
     * Hent dagens priser for alle prisområder
     */
    public void fetchPricesForAllZones() {
        logger.info("Henter dagens priser for alle zoner");

        for (PriceZone zone : PriceZone.values()) {
            try {
                fetchPricesForZoneAndDate(zone, LocalDate.now());
            } catch (Exception e) {
                logger.error("Feil ved henting av priser for {}: {}", zone, e.getMessage());
            }
        }
    }

    /**
     * Hent morgendagens priser for alle prisområder
     */
    public void fetchTomorrowsPricesForAllZones() {
        logger.info("Henter morgendagens priser for alle zoner");

        LocalDate tomorrow = LocalDate.now().plusDays(1);

        for (PriceZone zone : PriceZone.values()) {
            try {
                fetchPricesForZoneAndDate(zone, tomorrow);
            } catch (Exception e) {
                logger.error("Feil ved henting av morgendagens priser for {}: {}",
                        zone, e.getMessage());
            }
        }
    }

    /**
     * Hent priser for en spesifikk zone og dato
     * @param zone Prisområde
     * @param date Dato
     */
    public void fetchPricesForZoneAndDate(PriceZone zone, LocalDate date) {
        logger.info("Henter priser for {} på dato {}", zone, date);

        try {
            // Bygg API URL
            // Format: https://www.hvakosterstrommen.no/api/v1/prices/2025/12-21_NO1.json
            String year = String.valueOf(date.getYear());
            String monthDay = date.format(DateTimeFormatter.ofPattern("MM-dd"));
            String url = String.format("%s/%s/%s_%s.json",
                    apiBaseUrl, year, monthDay, zone.toString());

            logger.debug("API URL: {}", url);

            // Hent data fra API
            String jsonResponse = restTemplate.getForObject(url, String.class);

            if (jsonResponse == null || jsonResponse.isEmpty()) {
                logger.warn("Tom respons fra API for {} på {}", zone, date);
                return;
            }

            // Parse JSON og konverter til PriceData objekter
            List<PriceData> priceDataList = parsePriceData(jsonResponse, zone);

            if (priceDataList.isEmpty()) {
                logger.warn("Ingen prisdata funnet for {} på {}", zone, date);
                return;
            }

            // Lagre i database
            priceService.savePriceDataBulk(priceDataList);

            logger.info("Lagret {} prisdata records for {} på {}",
                    priceDataList.size(), zone, date);

        } catch (Exception e) {
            logger.error("Feil ved henting av priser for {} på {}: {}",
                    zone, date, e.getMessage(), e);
        }
    }

    // ============================================
    // JSON PARSING
    // ============================================

    /**
     * Parse JSON respons fra API til PriceData objekter
     *
     * JSON format fra API:
     * [
     *   {
     *     "NOK_per_kWh": 0.85,
     *     "EUR_per_kWh": 0.075,
     *     "EXR": 11.33,
     *     "time_start": "2025-12-21T00:00:00+01:00",
     *     "time_end": "2025-12-21T01:00:00+01:00"
     *   },
     *   ...
     * ]
     */
    private List<PriceData> parsePriceData(String jsonResponse, PriceZone zone) {
        List<PriceData> priceDataList = new ArrayList<>();

        try {
            JsonNode rootNode = objectMapper.readTree(jsonResponse);

            if (!rootNode.isArray()) {
                logger.error("Uventet JSON format - forventet array");
                return priceDataList;
            }

            for (JsonNode node : rootNode) {
                try {
                    // Hent felter fra JSON
                    BigDecimal priceNok = new BigDecimal(node.get("NOK_per_kWh").asText());
                    BigDecimal priceEur = new BigDecimal(node.get("EUR_per_kWh").asText());
                    String timeStart = node.get("time_start").asText();

                    // Parse tidspunkt (format: "2025-12-21T00:00:00+01:00")
                    LocalDateTime timestamp = LocalDateTime.parse(
                            timeStart.substring(0, 19),
                            DateTimeFormatter.ISO_LOCAL_DATE_TIME
                    );

                    // Opprett PriceData objekt
                    PriceData priceData = new PriceData(zone, timestamp, priceNok, priceEur);
                    priceDataList.add(priceData);

                } catch (Exception e) {
                    logger.error("Feil ved parsing av prisdata node: {}", e.getMessage());
                }
            }

        } catch (Exception e) {
            logger.error("Feil ved parsing av JSON: {}", e.getMessage(), e);
        }

        return priceDataList;
    }

    // ============================================
    // MANUELL OPPDATERING (for testing)
    // ============================================

    /**
     * Manuell oppdatering av priser (kan kalles fra controller)
     * @param zone Prisområde (null = alle zoner)
     */
    public void manualFetchPrices(PriceZone zone) {
        logger.info("Manuell oppdatering av priser startet");

        if (zone == null) {
            fetchPricesForAllZones();
        } else {
            fetchPricesForZoneAndDate(zone, LocalDate.now());
        }

        logger.info("Manuell oppdatering fullført");
    }
}