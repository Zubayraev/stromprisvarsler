package no.strompris.service;

import no.strompris.model.PriceData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * EmailService - Sender e-postvarsler til brukere
 */
@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${strompris.email.from}")
    private String fromEmail;

    @Value("${strompris.email.enabled:true}")
    private boolean emailEnabled;

    /**
     * Send e-post til bruker
     */
    public void sendEmail(String to, String subject, String text) {
        if (!emailEnabled) {
            logger.info("E-post deaktivert i config. Ville sendt til {}: {}", to, subject);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            mailSender.send(message);
            logger.info("✉️ E-post sendt til {}: {}", to, subject);

        } catch (Exception e) {
            logger.error("❌ Kunne ikke sende e-post til {}: {}", to, e.getMessage());
        }
    }

    /**
     * Send velkomst e-post til ny bruker
     */
    public void sendWelcomeEmail(String to, String priceZone) {
        String subject = "⚡ Velkommen til Strømpris-Varsler!";
        String text = String.format("""
            Hei!
            
            Takk for at du registrerte deg for strømprisvarsler! 🎉
            
            Du er nå registrert for prisområde: %s
            
            Vi sender deg varsler når:
            ✅ Strømprisen er lav (under din terskel)
            ⚠️ Strømprisen er høy (over din terskel)
            💡 Billigste timer i dag
            📊 Daglig sammendrag
            
            Du kan når som helst oppdatere dine preferanser eller avslutte varslene.
            
            Mvh,
            Strømpris-Varsler teamet
            """, priceZone);

        sendEmail(to, subject, text);
    }

    /**
     * Send lav-pris varsel
     */
    public void sendLowPriceAlert(String to, double price, String zone) {
        String subject = String.format("⚡ Lav strømpris nå! %.2f kr/kWh", price);
        String text = String.format("""
            Hei!
            
            God nyhet! Strømprisen er lav akkurat nå! 💚
            
            📍 Område: %s
            💰 Pris: %.2f kr/kWh
            ⏰ Tidspunkt: %s
            
            Dette er en perfekt tid for:
            • Vaske klær
            • Kjøre oppvaskmaskin
            • Lade elbil
            • Andre strømkrevende oppgaver
            
            Utnyt den lave prisen! ⚡
            
            Mvh,
            Strømpris-Varsler
            """, zone, price, java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("HH:mm")));

        sendEmail(to, subject, text);
    }

    /**
     * Send høy-pris varsel
     */
    public void sendHighPriceAlert(String to, double price, String zone) {
        String subject = String.format("⚠️ Høy strømpris nå! %.2f kr/kWh", price);
        String text = String.format("""
            Hei!
            
            Heads up! Strømprisen er høy nå. ⚠️
            
            📍 Område: %s
            💰 Pris: %.2f kr/kWh
            ⏰ Tidspunkt: %s
            
            Vurder å utsette:
            • Vask og tørk
            • Oppvaskmaskin
            • Elbil-lading
            • Andre strømkrevende oppgaver
            
            Vent til prisen synker! 💡
            
            Mvh,
            Strømpris-Varsler
            """, zone, price, java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("HH:mm")));

        sendEmail(to, subject, text);
    }

    /**
     * Send varsel om billigste timer
     */
    public void sendCheapestHoursAlert(String to, String zone, List<PriceData> cheapestHours) {
        String subject = "💡 De billigste timene i dag";

        StringBuilder text = new StringBuilder();
        text.append("Hei!\n\n");
        text.append("Her er de 3 billigste timene i dag for ").append(zone).append(":\n\n");

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        for (int i = 0; i < cheapestHours.size() && i < 3; i++) {
            PriceData price = cheapestHours.get(i);
            text.append(String.format("%d. %s - %.2f kr/kWh\n",
                    i + 1,
                    price.getPriceTimestamp().format(timeFormatter),
                    price.getPriceNok()));
        }

        text.append("\nPlanlegg dine strømkrevende oppgaver i disse timene for å spare penger! 💰\n\n");
        text.append("Mvh,\nStrømpris-Varsler");

        sendEmail(to, subject, text.toString());
    }

    /**
     * Send daglig sammendrag
     */
    public void sendDailySummary(String to, String zone, BigDecimal avgPrice,
                                 BigDecimal minPrice, BigDecimal maxPrice,
                                 List<PriceData> cheapestHours) {
        String subject = "📊 Daglig strømpris-sammendrag";

        StringBuilder text = new StringBuilder();
        text.append("Hei!\n\n");
        text.append("Her er dagens strømpris-sammendrag for ").append(zone).append(":\n\n");
        text.append(String.format("📈 Gjennomsnittspris: %.2f kr/kWh\n", avgPrice));
        text.append(String.format("📉 Laveste pris: %.2f kr/kWh\n", minPrice));
        text.append(String.format("📊 Høyeste pris: %.2f kr/kWh\n\n", maxPrice));

        text.append("💡 De 3 billigste timene var:\n");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        for (int i = 0; i < cheapestHours.size() && i < 3; i++) {
            PriceData price = cheapestHours.get(i);
            text.append(String.format("%d. %s - %.2f kr/kWh\n",
                    i + 1,
                    price.getPriceTimestamp().format(timeFormatter),
                    price.getPriceNok()));
        }

        text.append("\nVi sender deg oppdateringer i morgen! ⚡\n\n");
        text.append("Mvh,\nStrømpris-Varsler");

        sendEmail(to, subject, text.toString());
    }

    /**
     * Test e-post funksjonalitet
     */
    public boolean testEmail(String to) {
        try {
            sendEmail(to, "Test fra Strømpris-Varsler",
                    "Dette er en test e-post. Hvis du ser denne, fungerer e-post varsling! ✅");
            return true;
        } catch (Exception e) {
            logger.error("Test e-post feilet: {}", e.getMessage());
            return false;
        }
    }
}