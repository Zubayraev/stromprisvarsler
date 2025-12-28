package no.strompris.service;

import no.strompris.model.Alert;
import no.strompris.model.AlertType;
import no.strompris.model.PriceData;
import no.strompris.model.User;
import no.strompris.repository.AlertRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * AlertService - Business logic for varsler
 *
 * Håndterer:
 * - Opprettelse av varsler
 * - Sjekke om varsler skal sendes
 * - Generere varselmeldinger
 * - Hente varslingshistorikk
 */
@Service
@Transactional
public class AlertService {

    private static final Logger logger = LoggerFactory.getLogger(AlertService.class);

    private final AlertRepository alertRepository;
    private final UserService userService;
    private final PriceService priceService;

    @Autowired
    public AlertService(AlertRepository alertRepository,
                        UserService userService,
                        PriceService priceService) {
        this.alertRepository = alertRepository;
        this.userService = userService;
        this.priceService = priceService;
    }

    // ============================================
    // OPPRETTE VARSLER
    // ============================================

    /**
     * Opprett og lagre et nytt varsel
     * @param user Bruker som skal motta varselet
     * @param alertType Type varsel
     * @param message Varselmeldingen
     * @param price Prisen da varselet ble utløst (valgfritt)
     * @return Lagret varsel
     */
    public Alert createAlert(User user, AlertType alertType, String message, BigDecimal price) {
        logger.info("Oppretter varsel for bruker {}: {}", user.getEmail(), alertType);

        Alert alert = new Alert(user, alertType, message, price);
        return alertRepository.save(alert);
    }

    /**
     * Opprett varsel uten pris
     */
    public Alert createAlert(User user, AlertType alertType, String message) {
        return createAlert(user, alertType, message, null);
    }

    // ============================================
    // LAV PRIS VARSLER
    // ============================================

    /**
     * Sjekk og send lav-pris varsler for alle zoner
     * Kalles hver time av scheduled task
     */
    public void checkAndSendLowPriceAlerts() {
        logger.info("Sjekker lav-pris varsler for alle zoner");

        for (no.strompris.model.PriceZone zone : no.strompris.model.PriceZone.values()) {
            checkAndSendLowPriceAlertsForZone(zone);
        }
    }

    /**
     * Sjekk og send lav-pris varsler for en spesifikk zone
     */
    private void checkAndSendLowPriceAlertsForZone(no.strompris.model.PriceZone zone) {
        logger.debug("Sjekker lav-pris varsler for {}", zone);

        // Hent nåværende pris
        PriceData currentPrice = priceService.getCurrentPrice(zone).orElse(null);
        if (currentPrice == null) {
            logger.warn("Ingen prisdata tilgjengelig for {}", zone);
            return;
        }

        // Finn brukere som skal varsles
        List<User> usersToNotify = userService.getUsersForLowPriceAlert(
                zone,
                currentPrice.getPriceNok()
        );

        if (usersToNotify.isEmpty()) {
            logger.debug("Ingen brukere å varsle om lav pris i {}", zone);
            return;
        }

        // Send varsel til hver bruker
        for (User user : usersToNotify) {
            // Sjekk om bruker allerede har fått dette varselet i dag
            if (hasReceivedAlertToday(user.getId(), AlertType.PRICE_LOW)) {
                logger.debug("Bruker {} har allerede fått lav-pris varsel i dag", user.getEmail());
                continue;
            }

            String message = generateLowPriceMessage(currentPrice);
            createAlert(user, AlertType.PRICE_LOW, message, currentPrice.getPriceNok());

            logger.info("Lav-pris varsel sendt til {}: {} kr/kWh",
                    user.getEmail(), currentPrice.getPriceNok());
        }
    }

    /**
     * Generer melding for lav-pris varsel
     */
    private String generateLowPriceMessage(PriceData priceData) {
        return String.format(
                "⚡ Lav strømpris nå! Kun %.2f kr/kWh i %s. Perfekt tid for strømkrevende oppgaver!",
                priceData.getPriceNok(),
                priceData.getZone().getDescription()
        );
    }

    // ============================================
    // HØY PRIS VARSLER
    // ============================================

    /**
     * Sjekk og send høy-pris varsler for alle zoner
     */
    public void checkAndSendHighPriceAlerts() {
        logger.info("Sjekker høy-pris varsler for alle zoner");

        for (no.strompris.model.PriceZone zone : no.strompris.model.PriceZone.values()) {
            checkAndSendHighPriceAlertsForZone(zone);
        }
    }

    /**
     * Sjekk og send høy-pris varsler for en spesifikk zone
     */
    private void checkAndSendHighPriceAlertsForZone(no.strompris.model.PriceZone zone) {
        logger.debug("Sjekker høy-pris varsler for {}", zone);

        PriceData currentPrice = priceService.getCurrentPrice(zone).orElse(null);
        if (currentPrice == null) {
            return;
        }

        List<User> usersToNotify = userService.getUsersForHighPriceAlert(
                zone,
                currentPrice.getPriceNok()
        );

        for (User user : usersToNotify) {
            if (hasReceivedAlertToday(user.getId(), AlertType.PRICE_HIGH)) {
                continue;
            }

            String message = generateHighPriceMessage(currentPrice);
            createAlert(user, AlertType.PRICE_HIGH, message, currentPrice.getPriceNok());

            logger.info("Høy-pris varsel sendt til {}: {} kr/kWh",
                    user.getEmail(), currentPrice.getPriceNok());
        }
    }

    /**
     * Generer melding for høy-pris varsel
     */
    private String generateHighPriceMessage(PriceData priceData) {
        return String.format(
                "⚠️ Høy strømpris nå! %.2f kr/kWh i %s. Utsett strømkrevende oppgaver hvis mulig.",
                priceData.getPriceNok(),
                priceData.getZone().getDescription()
        );
    }

    // ============================================
    // BILLIGSTE TIMER VARSLER
    // ============================================

    /**
     * Send daglig varsel om billigste timer
     * Sendes én gang per dag (f.eks. kl 06:00)
     */
    public void sendCheapestHoursAlerts() {
        logger.info("Sender varsler om billigste timer for i dag");

        for (no.strompris.model.PriceZone zone : no.strompris.model.PriceZone.values()) {
            sendCheapestHoursAlertsForZone(zone);
        }
    }

    /**
     * Send billigste-timer varsel for en zone
     */
    private void sendCheapestHoursAlertsForZone(no.strompris.model.PriceZone zone) {
        List<User> users = userService.getUsersToNotifyInZone(zone);
        List<PriceData> cheapestHours = priceService.getCheapestHoursToday(zone, 3);

        if (cheapestHours.isEmpty()) {
            logger.warn("Ingen prisdata for {} - kan ikke sende billigste-timer varsel", zone);
            return;
        }

        for (User user : users) {
            if (hasReceivedAlertToday(user.getId(), AlertType.CHEAPEST_HOURS)) {
                continue;
            }

            String message = generateCheapestHoursMessage(zone, cheapestHours);
            createAlert(user, AlertType.CHEAPEST_HOURS, message);

            logger.info("Billigste-timer varsel sendt til {}", user.getEmail());
        }
    }

    /**
     * Generer melding for billigste-timer varsel
     */
    private String generateCheapestHoursMessage(no.strompris.model.PriceZone zone,
                                                List<PriceData> cheapestHours) {
        StringBuilder message = new StringBuilder();
        message.append(String.format("💡 De 3 billigste timene i dag for %s:\n",
                zone.getDescription()));

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        for (int i = 0; i < cheapestHours.size() && i < 3; i++) {
            PriceData price = cheapestHours.get(i);
            message.append(String.format("%d. %s - %.2f kr/kWh\n",
                    i + 1,
                    price.getPriceTimestamp().format(timeFormatter),
                    price.getPriceNok()
            ));
        }

        return message.toString();
    }

    // ============================================
    // DAGLIG SAMMENDRAG
    // ============================================

    /**
     * Send daglig sammendrag til alle brukere
     * Sendes én gang per dag (f.eks. kl 18:00)
     */
    public void sendDailySummaryAlerts() {
        logger.info("Sender daglig sammendrag til alle brukere");

        for (no.strompris.model.PriceZone zone : no.strompris.model.PriceZone.values()) {
            sendDailySummaryAlertsForZone(zone);
        }
    }

    /**
     * Send daglig sammendrag for en zone
     */
    private void sendDailySummaryAlertsForZone(no.strompris.model.PriceZone zone) {
        List<User> users = userService.getUsersToNotifyInZone(zone);

        BigDecimal avgPrice = priceService.getAveragePriceToday(zone);
        BigDecimal minPrice = priceService.getMinPriceToday(zone);
        BigDecimal maxPrice = priceService.getMaxPriceToday(zone);

        for (User user : users) {
            if (hasReceivedAlertToday(user.getId(), AlertType.DAILY_SUMMARY)) {
                continue;
            }

            String message = generateDailySummaryMessage(zone, avgPrice, minPrice, maxPrice);
            createAlert(user, AlertType.DAILY_SUMMARY, message);

            logger.info("Daglig sammendrag sendt til {}", user.getEmail());
        }
    }

    /**
     * Generer melding for daglig sammendrag
     */
    private String generateDailySummaryMessage(no.strompris.model.PriceZone zone,
                                               BigDecimal avgPrice,
                                               BigDecimal minPrice,
                                               BigDecimal maxPrice) {
        return String.format(
                "📊 Dagens strømpriser i %s:\n" +
                        "Gjennomsnitt: %.2f kr/kWh\n" +
                        "Laveste: %.2f kr/kWh\n" +
                        "Høyeste: %.2f kr/kWh",
                zone.getDescription(),
                avgPrice,
                minPrice,
                maxPrice
        );
    }

    // ============================================
    // HJELPEMETODER
    // ============================================

    /**
     * Sjekk om bruker har fått et varsel av en gitt type i dag
     */
    public boolean hasReceivedAlertToday(Long userId, AlertType alertType) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        return alertRepository.hasReceivedAlertToday(userId, alertType, startOfDay);
    }

    /**
     * Hent alle varsler for en bruker
     */
    public List<Alert> getAlertsForUser(Long userId) {
        return alertRepository.findByUserIdOrderByTriggeredAtDesc(userId);
    }

    /**
     * Hent dagens varsler for en bruker
     */
    public List<Alert> getTodaysAlertsForUser(Long userId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        return alertRepository.findTodaysAlertsForUser(userId, startOfDay, endOfDay);
    }

    /**
     * Slett gamle varsler (eldre enn 90 dager)
     */
    public void deleteOldAlerts() {
        logger.info("Sletter varsler eldre enn 90 dager");
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(90);
        alertRepository.deleteOldAlerts(cutoffDate);
    }
}