package no.strompris.controller;

import no.strompris.model.PriceZone;
import no.strompris.model.User;
import no.strompris.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * UserController - REST API for brukere
 *
 * Base URL: /api/users
 *
 * Endpoints:
 * - POST /api/users - Registrer ny bruker
 * - GET /api/users/{id} - Hent bruker
 * - GET /api/users - Hent alle brukere
 * - PUT /api/users/{id} - Oppdater bruker
 * - DELETE /api/users/{id} - Slett bruker
 * - PATCH /api/users/{id}/alerts/enable - Aktiver varsler
 * - PATCH /api/users/{id}/alerts/disable - Deaktiver varsler
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // ============================================
    // REGISTRERING
    // ============================================

    /**
     * POST /api/users
     * Registrer ny bruker
     *
     * Body: {
     *   "email": "bruker@example.com",
     *   "phone": "+4712345678",
     *   "priceZone": "NO1",
     *   "alertThreshold": 1.50,
     *   "alertEnabled": true
     * }
     */
    @PostMapping
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        logger.info("API: Registrerer ny bruker: {}", user.getEmail());

        try {
            // Valider bruker
            if (!userService.isValidUser(user)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Ugyldig brukerdata"));
            }

            // Registrer bruker
            User savedUser = userService.registerUser(user);

            return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Feil ved registrering av bruker", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Kunne ikke registrere bruker"));
        }
    }

    // ============================================
    // HENTE BRUKERE
    // ============================================

    /**
     * GET /api/users/{id}
     * Hent bruker basert på ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        logger.info("API: Henter bruker med ID: {}", id);

        Optional<User> user = userService.getUserById(id);

        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Bruker ikke funnet"));
        }

        return ResponseEntity.ok(user.get());
    }

    /**
     * GET /api/users/email/{email}
     * Hent bruker basert på e-post
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<?> getUserByEmail(@PathVariable String email) {
        logger.info("API: Henter bruker med e-post: {}", email);

        Optional<User> user = userService.getUserByEmail(email);

        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Bruker ikke funnet"));
        }

        return ResponseEntity.ok(user.get());
    }

    /**
     * GET /api/users
     * Hent alle brukere
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        logger.info("API: Henter alle brukere");

        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * GET /api/users/zone/{zone}
     * Hent alle brukere i et prisområde
     */
    @GetMapping("/zone/{zone}")
    public ResponseEntity<?> getUsersByZone(@PathVariable String zone) {
        logger.info("API: Henter brukere i zone: {}", zone);

        try {
            PriceZone priceZone = PriceZone.fromString(zone);
            List<User> users = userService.getUsersByZone(priceZone);

            return ResponseEntity.ok(users);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ============================================
    // OPPDATERING
    // ============================================

    /**
     * PUT /api/users/{id}
     * Oppdater brukerens preferanser
     *
     * Body: {
     *   "priceZone": "NO2",
     *   "alertThreshold": 2.00,
     *   "alertEnabled": true
     * }
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates) {
        logger.info("API: Oppdaterer bruker med ID: {}", id);

        try {
            // Parse updates
            PriceZone priceZone = null;
            if (updates.containsKey("priceZone")) {
                priceZone = PriceZone.fromString((String) updates.get("priceZone"));
            }

            BigDecimal alertThreshold = null;
            if (updates.containsKey("alertThreshold")) {
                alertThreshold = new BigDecimal(updates.get("alertThreshold").toString());
            }

            Boolean alertEnabled = null;
            if (updates.containsKey("alertEnabled")) {
                alertEnabled = (Boolean) updates.get("alertEnabled");
            }

            // Oppdater bruker
            User updatedUser = userService.updateUserPreferences(
                    id, priceZone, alertThreshold, alertEnabled
            );

            return ResponseEntity.ok(updatedUser);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Feil ved oppdatering av bruker", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Kunne ikke oppdatere bruker"));
        }
    }

    /**
     * PATCH /api/users/{id}/alerts/enable
     * Aktiver varsler for bruker
     */
    @PatchMapping("/{id}/alerts/enable")
    public ResponseEntity<?> enableAlerts(@PathVariable Long id) {
        logger.info("API: Aktiverer varsler for bruker: {}", id);

        try {
            User updatedUser = userService.enableAlerts(id);
            return ResponseEntity.ok(updatedUser);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PATCH /api/users/{id}/alerts/disable
     * Deaktiver varsler for bruker
     */
    @PatchMapping("/{id}/alerts/disable")
    public ResponseEntity<?> disableAlerts(@PathVariable Long id) {
        logger.info("API: Deaktiverer varsler for bruker: {}", id);

        try {
            User updatedUser = userService.disableAlerts(id);
            return ResponseEntity.ok(updatedUser);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ============================================
    // SLETTING
    // ============================================

    /**
     * DELETE /api/users/{id}
     * Slett bruker
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        logger.info("API: Sletter bruker med ID: {}", id);

        try {
            userService.deleteUser(id);

            return ResponseEntity.ok(Map.of(
                    "message", "Bruker slettet",
                    "userId", id
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Feil ved sletting av bruker", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Kunne ikke slette bruker"));
        }
    }

    // ============================================
    // STATISTIKK
    // ============================================

    /**
     * GET /api/users/statistics
     * Hent brukerstatistikk
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getUserStatistics() {
        logger.info("API: Henter brukerstatistikk");

        long totalUsers = userService.countUsers();
        List<Object[]> usersByZone = userService.countUsersByZone();

        return ResponseEntity.ok(Map.of(
                "totalUsers", totalUsers,
                "usersByZone", usersByZone
        ));
    }

    // ============================================
    // HEALTH CHECK
    // ============================================

    /**
     * GET /api/users/health
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "UserController"
        ));
    }
}