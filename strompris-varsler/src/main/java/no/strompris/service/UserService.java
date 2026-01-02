package no.strompris.service;

import no.strompris.model.PriceZone;
import no.strompris.model.User;
import no.strompris.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * UserService - Business logic for brukere
 *
 * Håndterer alle operasjoner relatert til brukere:
 * - Registrering og validering
 * - Oppdatering av preferanser
 * - Finne brukere som skal varsles
 */

@Service
@Transactional
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ============================================
    // REGISTRERING OG OPPDATERING
    // ============================================

    /**
     * Registrer ny bruker
     * @param user Bruker-objekt
     * @return Lagret bruker
     * @throws IllegalArgumentException hvis e-post allerede eksisterer
     */
    // I UserService.java, oppdater registerUser metoden:

    @Autowired
    private EmailService emailService;

    public User registerUser(User user) {
        logger.info("Registrerer ny bruker: {}", user.getEmail());

        if (!user.hasValidEmail()) {
            throw new IllegalArgumentException("Ugyldig e-postadresse: " + user.getEmail());
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("E-post allerede registrert: " + user.getEmail());
        }

        User savedUser = userRepository.save(user);
        logger.info("Bruker registrert med ID: {}", savedUser.getId());

        // ✅ Send velkomst e-post
        emailService.sendWelcomeEmail(savedUser.getEmail(), savedUser.getPriceZone().toString());

        return savedUser;
    }

    /**
     * Oppdater brukerens preferanser
     * @param userId Bruker-ID
     * @param priceZone Nytt prisområde (valgfritt)
     * @param alertThreshold Ny pristerskel (valgfritt)
     * @param alertEnabled Aktivere/deaktivere varsler (valgfritt)
     * @return Oppdatert bruker
     * @throws IllegalArgumentException hvis bruker ikke finnes
     */
    public User updateUserPreferences(Long userId, PriceZone priceZone,
                                      BigDecimal alertThreshold, Boolean alertEnabled) {
        logger.info("Oppdaterer preferanser for bruker ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Bruker ikke funnet: " + userId));

        // Oppdater kun felter som ikke er null
        if (priceZone != null) {
            user.setPriceZone(priceZone);
        }
        if (alertThreshold != null) {
            user.setAlertThreshold(alertThreshold);
        }
        if (alertEnabled != null) {
            user.setAlertEnabled(alertEnabled);
        }

        return userRepository.save(user);
    }

    /**
     * Aktiver varsler for en bruker
     * @param userId Bruker-ID
     * @return Oppdatert bruker
     */
    public User enableAlerts(Long userId) {
        logger.info("Aktiverer varsler for bruker ID: {}", userId);
        return updateUserPreferences(userId, null, null, true);
    }

    /**
     * Deaktiver varsler for en bruker
     * @param userId Bruker-ID
     * @return Oppdatert bruker
     */
    public User disableAlerts(Long userId) {
        logger.info("Deaktiverer varsler for bruker ID: {}", userId);
        return updateUserPreferences(userId, null, null, false);
    }

    // ============================================
    // HENTE BRUKERE
    // ============================================

    /**
     * Hent bruker basert på ID
     * @param userId Bruker-ID
     * @return Bruker
     */
    public Optional<User> getUserById(Long userId) {
        logger.debug("Henter bruker med ID: {}", userId);
        return userRepository.findById(userId);
    }

    /**
     * Hent bruker basert på e-post
     * @param email E-postadresse
     * @return Bruker
     */
    public Optional<User> getUserByEmail(String email) {
        logger.debug("Henter bruker med e-post: {}", email);
        return userRepository.findByEmail(email);
    }

    /**
     * Hent alle brukere
     * @return Liste av alle brukere
     */
    public List<User> getAllUsers() {
        logger.debug("Henter alle brukere");
        return userRepository.findAll();
    }

    /**
     * Hent alle brukere i et prisområde
     * @param zone Prisområde
     * @return Liste av brukere
     */
    public List<User> getUsersByZone(PriceZone zone) {
        logger.debug("Henter brukere i zone: {}", zone);
        return userRepository.findByPriceZone(zone);
    }

    /**
     * Hent alle brukere med aktiverte varsler
     * @return Liste av brukere med varsler aktivert
     */
    public List<User> getUsersWithAlertsEnabled() {
        logger.debug("Henter brukere med aktiverte varsler");
        return userRepository.findByAlertEnabledTrue();
    }

    // ============================================
    // VARSLINGS-LOGIKK
    // ============================================

    /**
     * Finn brukere som skal varsles om lav pris
     * @param zone Prisområde
     * @param currentPrice Nåværende pris
     * @return Liste av brukere som skal varsles
     */
    public List<User> getUsersForLowPriceAlert(PriceZone zone, BigDecimal currentPrice) {
        logger.info("Finner brukere for lav-pris varsel i {} (pris: {})", zone, currentPrice);
        return userRepository.findUsersForLowPriceAlert(zone, currentPrice);
    }

    /**
     * Finn brukere som skal varsles om høy pris
     * @param zone Prisområde
     * @param currentPrice Nåværende pris
     * @return Liste av brukere som skal varsles
     */
    public List<User> getUsersForHighPriceAlert(PriceZone zone, BigDecimal currentPrice) {
        logger.info("Finner brukere for høy-pris varsel i {} (pris: {})", zone, currentPrice);
        return userRepository.findUsersForHighPriceAlert(zone, currentPrice);
    }

    /**
     * Finn alle brukere som skal motta varsler i en gitt zone
     * @param zone Prisområde
     * @return Liste av brukere
     */
    public List<User> getUsersToNotifyInZone(PriceZone zone) {
        logger.info("Finner brukere å varsle i zone: {}", zone);
        return userRepository.findUsersToNotifyInZone(zone);
    }

    // ============================================
    // SLETTING
    // ============================================

    /**
     * Slett en bruker
     * @param userId Bruker-ID
     * @throws IllegalArgumentException hvis bruker ikke finnes
     */
    public void deleteUser(Long userId) {
        logger.info("Sletter bruker med ID: {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("Bruker ikke funnet: " + userId);
        }

        userRepository.deleteById(userId);
        logger.info("Bruker {} slettet", userId);
    }

    // ============================================
    // STATISTIKK
    // ============================================

    /**
     * Tell antall brukere
     * @return Totalt antall brukere
     */
    public long countUsers() {
        return userRepository.count();
    }

    /**
     * Tell antall brukere per prisområde
     * @return Liste med [zone, count]
     */
    public List<Object[]> countUsersByZone() {
        return userRepository.countUsersByZone();
    }

    /**
     * Valider bruker-data
     * @param user Bruker å validere
     * @return true hvis gyldig
     */
    public boolean isValidUser(User user) {
        if (user == null) {
            return false;
        }

        // Sjekk påkrevde felter
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            logger.warn("Validering feilet: Mangler e-post");
            return false;
        }

        if (user.getPriceZone() == null) {
            logger.warn("Validering feilet: Mangler prisområde");
            return false;
        }

        // Valider e-post format
        if (!user.hasValidEmail()) {
            logger.warn("Validering feilet: Ugyldig e-post format");
            return false;
        }

        return true;
    }
}