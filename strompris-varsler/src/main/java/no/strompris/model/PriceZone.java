package no.strompris.model;

/**
 * PriceZone - Enum for norske strømprisområder
 *
 * Norge er delt inn i 5 prisområder (bidding zones):
 * - NO1: Oslo / Øst-Norge
 * - NO2: Kristiansand / Sør-Norge
 * - NO3: Trondheim / Midt-Norge
 * - NO4: Tromsø / Nord-Norge
 * - NO5: Bergen / Vest-Norge
 */
public enum PriceZone {
    NO1("Oslo / Øst-Norge"),
    NO2("Kristiansand / Sør-Norge"),
    NO3("Trondheim / Midt-Norge"),
    NO4("Tromsø / Nord-Norge"),
    NO5("Bergen / Vest-Norge");

    private final String description;

    /**
     * Constructor
     * @param description Beskrivelse av prisområdet
     */
    PriceZone(String description) {
        this.description = description;
    }

    /**
     * Henter beskrivelse av prisområdet
     * @return Beskrivelse (f.eks. "Oslo / Øst-Norge")
     */
    public String getDescription() {
        return description;
    }

    /**
     * Konverterer string til PriceZone enum
     * @param zone String representasjon (f.eks. "NO1")
     * @return PriceZone enum
     * @throws IllegalArgumentException hvis zone er ugyldig
     */
    public static PriceZone fromString(String zone) {
        if (zone == null) {
            throw new IllegalArgumentException("Zone cannot be null");
        }

        try {
            return PriceZone.valueOf(zone.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Ugyldig prisområde: " + zone + ". Gyldige verdier: NO1, NO2, NO3, NO4, NO5"
            );
        }
    }

    /**
     * Returnerer zone-kode (f.eks. "NO1")
     */
    @Override
    public String toString() {
        return this.name();
    }
}