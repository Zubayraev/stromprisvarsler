package no.strompris.controller;

import no.strompris.model.Alert;
import no.strompris.service.AlertService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * AlertController - REST API for varsler
 *
 * Base URL: /api/alerts
 *
 * Endpoints:
 * - GET /api/alerts/user/{userId} - Hent varsler for bruker
 * - GET /api/alerts/user/{userId}/today - Hent dagens varsler
 * - POST /api/alerts/check/low-price - Trigger lav-pris sjekk
 * - POST /api/alerts/check/high-price - Trigger høy-pris sjekk
 * - POST /api/alerts/send/cheapest-hours - Send billigste-timer varsler
 * - POST /api/alerts/send/daily-summary - Send daglig sammendrag
 */
@RestController
@RequestMapping("/api/alerts")
@CrossOrigin(origins = "*")
public class AlertController {

    private static final Logger logger = LoggerFactory.getLogger(AlertController.class);

    private final AlertService alertService;

    @Autowired
    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    // ============================================
    // HENTE VARSLER
    // ============================================

    /**
     * GET /api/alerts/user/{userId}
     * Hent alle varsler for en bruker
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getAlertsForUser(@PathVariable Long userId) {
        logger.info("API: Henter varsler for bruker: {}", userId);

        try {
            List<Alert> alerts = alertService.getAlertsForUser(userId);

            return ResponseEntity.ok(alerts);

        } catch (Exception e) {
            logger.error("Feil ved henting av varsler", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Kunne ikke hente varsler"));
        }
    }

    /**
     * GET /api/alerts/user/{userId}/today
     * Hent dagens varsler for en bruker
     */
    @GetMapping("/user/{userId}/today")
    public ResponseEntity<?> getTodaysAlertsForUser(@PathVariable Long userId) {
        logger.info("API: Henter dagens varsler for bruker: {}", userId);

        try {
            List<Alert> alerts = alertService.getTodaysAlertsForUser(userId);

            return ResponseEntity.ok(Map.of(
                    "userId", userId,
                    "date", java.time.LocalDate.now(),
                    "alerts", alerts,
                    "count", alerts.size()
            ));

        } catch (Exception e) {
            logger.error("Feil ved henting av dagens varsler", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Kunne ikke hente varsler"));
        }
    }

    // ============================================
    // TRIGGER VARSLER (manuelt - for testing/admin)
    // ============================================

    /**
     * POST /api/alerts/check/low-price
     * Manuelt trigger sjekk av lav-pris varsler
     * (Normalt kjører dette automatisk hver time)
     */
    @PostMapping("/check/low-price")
    public ResponseEntity<?> checkLowPriceAlerts() {
        logger.info("API: Manuell trigger av lav-pris varsler");

        try {
            alertService.checkAndSendLowPriceAlerts();

            return ResponseEntity.ok(Map.of(
                    "message", "Lav-pris varsler sjekket og sendt",
                    "timestamp", java.time.LocalDateTime.now()
            ));

        } catch (Exception e) {
            logger.error("Feil ved sending av lav-pris varsler", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Kunne ikke sende varsler"));
        }
    }

    /**
     * POST /api/alerts/check/high-price
     * Manuelt trigger sjekk av høy-pris varsler
     */
    @PostMapping("/check/high-price")
    public ResponseEntity<?> checkHighPriceAlerts() {
        logger.info("API: Manuell trigger av høy-pris varsler");

        try {
            alertService.checkAndSendHighPriceAlerts();

            return ResponseEntity.ok(Map.of(
                    "message", "Høy-pris varsler sjekket og sendt",
                    "timestamp", java.time.LocalDateTime.now()
            ));

        } catch (Exception e) {
            logger.error("Feil ved sending av høy-pris varsler", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Kunne ikke sende varsler"));
        }
    }

    /**
     * POST /api/alerts/send/cheapest-hours
     * Send varsler om billigste timer
     * (Normalt kjører dette én gang per dag)
     */
    @PostMapping("/send/cheapest-hours")
    public ResponseEntity<?> sendCheapestHoursAlerts() {
        logger.info("API: Sender billigste-timer varsler");

        try {
            alertService.sendCheapestHoursAlerts();

            return ResponseEntity.ok(Map.of(
                    "message", "Billigste-timer varsler sendt",
                    "timestamp", java.time.LocalDateTime.now()
            ));

        } catch (Exception e) {
            logger.error("Feil ved sending av billigste-timer varsler", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Kunne ikke sende varsler"));
        }
    }

    /**
     * POST /api/alerts/send/daily-summary
     * Send daglig sammendrag
     * (Normalt kjører dette én gang per dag)
     */
    @PostMapping("/send/daily-summary")
    public ResponseEntity<?> sendDailySummaryAlerts() {
        logger.info("API: Sender daglig sammendrag");

        try {
            alertService.sendDailySummaryAlerts();

            return ResponseEntity.ok(Map.of(
                    "message", "Daglig sammendrag sendt",
                    "timestamp", java.time.LocalDateTime.now()
            ));

        } catch (Exception e) {
            logger.error("Feil ved sending av daglig sammendrag", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Kunne ikke sende sammendrag"));
        }
    }

    // ============================================
    // OPPRYDDING
    // ============================================

    /**
     * DELETE /api/alerts/cleanup
     * Slett gamle varsler (eldre enn 90 dager)
     * Kun for admin/maintenance
     */
    @DeleteMapping("/cleanup")
    public ResponseEntity<?> cleanupOldAlerts() {
        logger.info("API: Sletter gamle varsler");

        try {
            alertService.deleteOldAlerts();

            return ResponseEntity.ok(Map.of(
                    "message", "Gamle varsler slettet",
                    "cutoffDays", 90
            ));

        } catch (Exception e) {
            logger.error("Feil ved sletting av gamle varsler", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Kunne ikke slette varsler"));
        }
    }

    // ============================================
    // HEALTH CHECK
    // ============================================

    /**
     * GET /api/alerts/health
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "AlertController"
        ));
    }
}