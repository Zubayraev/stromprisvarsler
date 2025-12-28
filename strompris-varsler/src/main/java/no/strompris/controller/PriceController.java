package no.strompris.controller;

import no.strompris.model.PriceData;
import no.strompris.model.PriceZone;
import no.strompris.service.PriceService;
import no.strompris.service.PriceFetcherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * PriceController - REST API for strømpriser
 *
 * Base URL: /api/prices
 *
 * Endpoints:
 * - GET /api/prices/current/{zone} - Nåværende pris
 * - GET /api/prices/today/{zone} - Dagens priser
 * - GET /api/prices/tomorrow/{zone} - Morgendagens priser
 * - GET /api/prices/cheapest/{zone} - Billigste timer
 * - GET /api/prices/statistics/{zone} - Statistikk
 * - POST /api/prices/fetch - Manuell oppdatering
 */
@RestController
@RequestMapping("/api/prices")
@CrossOrigin(origins = "*") // Tillat requests fra frontend (localhost:3000)
public class PriceController {

    private static final Logger logger = LoggerFactory.getLogger(PriceController.class);

    private final PriceService priceService;
    private final PriceFetcherService priceFetcherService;

    @Autowired
    public PriceController(PriceService priceService,
                           PriceFetcherService priceFetcherService) {
        this.priceService = priceService;
        this.priceFetcherService = priceFetcherService;
    }

    // ============================================
    // HENTE PRISER
    // ============================================

    /**
     * GET /api/prices/current/{zone}
     * Hent nåværende strømpris for et prisområde
     *
     * Eksempel: GET /api/prices/current/NO1
     * Response: { "zone": "NO1", "priceNok": 1.25, ... }
     */
    @GetMapping("/current/{zone}")
    public ResponseEntity<?> getCurrentPrice(@PathVariable String zone) {
        logger.info("API: Henter nåværende pris for {}", zone);

        try {
            PriceZone priceZone = PriceZone.fromString(zone);
            Optional<PriceData> currentPrice = priceService.getCurrentPrice(priceZone);

            if (currentPrice.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Ingen prisdata tilgjengelig for " + zone));
            }

            return ResponseEntity.ok(currentPrice.get());

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/prices/today/{zone}
     * Hent alle priser for i dag
     *
     * Eksempel: GET /api/prices/today/NO1
     * Response: [{ "zone": "NO1", "priceNok": 0.85, ... }, ...]
     */
    @GetMapping("/today/{zone}")
    public ResponseEntity<?> getTodaysPrices(@PathVariable String zone) {
        logger.info("API: Henter dagens priser for {}", zone);

        try {
            PriceZone priceZone = PriceZone.fromString(zone);
            List<PriceData> prices = priceService.getTodaysPrices(priceZone);

            if (prices.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Ingen prisdata for i dag"));
            }

            return ResponseEntity.ok(prices);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/prices/tomorrow/{zone}
     * Hent morgendagens priser (tilgjengelig fra ca. kl 13:00)
     *
     * Eksempel: GET /api/prices/tomorrow/NO1
     */
    @GetMapping("/tomorrow/{zone}")
    public ResponseEntity<?> getTomorrowsPrices(@PathVariable String zone) {
        logger.info("API: Henter morgendagens priser for {}", zone);

        try {
            PriceZone priceZone = PriceZone.fromString(zone);
            List<PriceData> prices = priceService.getTomorrowsPrices(priceZone);

            if (prices.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Morgendagens priser er ikke tilgjengelig enda"));
            }

            return ResponseEntity.ok(prices);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/prices/date/{zone}/{date}
     * Hent priser for en bestemt dato
     *
     * Eksempel: GET /api/prices/date/NO1/2025-12-25
     */
    @GetMapping("/date/{zone}/{date}")
    public ResponseEntity<?> getPricesForDate(
            @PathVariable String zone,
            @PathVariable String date) {
        logger.info("API: Henter priser for {} på {}", zone, date);

        try {
            PriceZone priceZone = PriceZone.fromString(zone);
            LocalDate localDate = LocalDate.parse(date);
            List<PriceData> prices = priceService.getPricesForDate(priceZone, localDate);

            if (prices.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Ingen prisdata for " + date));
            }

            return ResponseEntity.ok(prices);

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ============================================
    // STATISTIKK OG ANALYSE
    // ============================================

    /**
     * GET /api/prices/cheapest/{zone}?limit=3
     * Hent de N billigste timene i dag
     *
     * Eksempel: GET /api/prices/cheapest/NO1?limit=3
     */
    @GetMapping("/cheapest/{zone}")
    public ResponseEntity<?> getCheapestHours(
            @PathVariable String zone,
            @RequestParam(defaultValue = "3") int limit) {
        logger.info("API: Henter {} billigste timer for {}", limit, zone);

        try {
            PriceZone priceZone = PriceZone.fromString(zone);
            List<PriceData> cheapestHours = priceService.getCheapestHoursToday(priceZone, limit);

            if (cheapestHours.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Ingen prisdata tilgjengelig"));
            }

            return ResponseEntity.ok(cheapestHours);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/prices/statistics/{zone}
     * Hent statistikk for dagens priser
     *
     * Response: { "average": 1.25, "min": 0.45, "max": 2.50 }
     */
    @GetMapping("/statistics/{zone}")
    public ResponseEntity<?> getStatistics(@PathVariable String zone) {
        logger.info("API: Henter statistikk for {}", zone);

        try {
            PriceZone priceZone = PriceZone.fromString(zone);

            BigDecimal average = priceService.getAveragePriceToday(priceZone);
            BigDecimal min = priceService.getMinPriceToday(priceZone);
            BigDecimal max = priceService.getMaxPriceToday(priceZone);

            Map<String, Object> statistics = new HashMap<>();
            statistics.put("zone", zone);
            statistics.put("date", LocalDate.now());
            statistics.put("average", average);
            statistics.put("min", min);
            statistics.put("max", max);

            return ResponseEntity.ok(statistics);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ============================================
    // MANUELL OPPDATERING
    // ============================================

    /**
     * POST /api/prices/fetch
     * Trigger manuell oppdatering av priser
     *
     * Body (optional): { "zone": "NO1" }
     */
    @PostMapping("/fetch")
    public ResponseEntity<?> fetchPrices(@RequestBody(required = false) Map<String, String> body) {
        logger.info("API: Manuell prisoppdatering startet");

        try {
            if (body != null && body.containsKey("zone")) {
                String zoneStr = body.get("zone");
                PriceZone zone = PriceZone.fromString(zoneStr);
                priceFetcherService.manualFetchPrices(zone);

                return ResponseEntity.ok(Map.of(
                        "message", "Priser oppdatert for " + zone,
                        "zone", zone
                ));
            } else {
                priceFetcherService.manualFetchPrices(null);

                return ResponseEntity.ok(Map.of(
                        "message", "Priser oppdatert for alle zoner"
                ));
            }

        } catch (Exception e) {
            logger.error("Feil ved manuell prisoppdatering", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Feil ved oppdatering: " + e.getMessage()));
        }
    }

    // ============================================
    // TILGJENGELIGHETSSTATUS
    // ============================================

    /**
     * GET /api/prices/status/{zone}
     * Sjekk om det finnes prisdata for i dag og i morgen
     *
     * Response: { "hasToday": true, "hasTomorrow": false }
     */
    @GetMapping("/status/{zone}")
    public ResponseEntity<?> getPriceDataStatus(@PathVariable String zone) {
        logger.info("API: Sjekker prisdata status for {}", zone);

        try {
            PriceZone priceZone = PriceZone.fromString(zone);

            boolean hasToday = priceService.hasPriceDataForToday(priceZone);
            boolean hasTomorrow = priceService.hasPriceDataForTomorrow(priceZone);

            Map<String, Object> status = new HashMap<>();
            status.put("zone", zone);
            status.put("hasToday", hasToday);
            status.put("hasTomorrow", hasTomorrow);

            return ResponseEntity.ok(status);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ============================================
    // HEALTH CHECK
    // ============================================

    /**
     * GET /api/prices/health
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "PriceController"
        ));
    }
}