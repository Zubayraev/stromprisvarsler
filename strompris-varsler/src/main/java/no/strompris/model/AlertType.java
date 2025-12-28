package no.strompris.model;

/**
 * AlertType - Enum for ulike typer varsler
 *
 * Definerer hvilke typer varsler systemet kan sende til brukere
 */
public enum AlertType {
    /**
     * Varsel når strømprisen er under brukerens terskelverdi
     * Eksempel: "Strømmen er nå kun 0.45 kr/kWh - perfekt tid for vask!"
     */
    PRICE_LOW("Lav pris"),

    /**
     * Varsel når strømprisen er over brukerens terskelverdi
     * Eksempel: "Høy strømpris nå (2.50 kr/kWh) - utsett strømkrevende oppgaver"
     */
    PRICE_HIGH("Høy pris"),

    /**
     * Daglig sammendrag av strømpriser
     * Eksempel: "I dag: Snitt 1.20 kr/kWh. Billigste time: 03-04 (0.35 kr/kWh)"
     */
    DAILY_SUMMARY("Daglig sammendrag"),

    /**
     * Varsler om de billigste timene i dag
     * Eksempel: "De 3 billigste timene: 02-03, 03-04, 14-15"
     */
    CHEAPEST_HOURS("Billigste timer");

    private final String displayName;

    /**
     * Constructor
     * @param displayName Brukervenlig navn på varseltypen
     */
    AlertType(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Henter brukervenlig navn
     * @return Display name (f.eks. "Lav pris")
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Konverterer string til AlertType enum
     * @param type String representasjon (f.eks. "PRICE_LOW")
     * @return AlertType enum
     * @throws IllegalArgumentException hvis type er ugyldig
     */
    public static AlertType fromString(String type) {
        if (type == null) {
            throw new IllegalArgumentException("Alert type cannot be null");
        }

        try {
            return AlertType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Ugyldig varseltype: " + type +
                            ". Gyldige verdier: PRICE_LOW, PRICE_HIGH, DAILY_SUMMARY, CHEAPEST_HOURS"
            );
        }
    }
}