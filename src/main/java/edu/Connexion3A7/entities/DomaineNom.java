package edu.Connexion3A7.entities;

/**
 * Enum matching the MySQL ENUM for column nom_domaine in table
 * domaine_coaching.
 * Values: BRANDING, E_COMMERCE, LEADERSHIP, FINANCE, FUNDING
 */
public enum DomaineNom {

    BRANDING("Branding"),
    E_COMMERCE("E-Commerce"),
    LEADERSHIP("Leadership"),
    FINANCE("Finance"),
    FUNDING("Funding");

    private final String displayName;

    DomaineNom(String displayName) {
        this.displayName = displayName;
    }

    /**
     * User-friendly label for UI display (ComboBox, profile cards, etc.)
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the value to store in the database (must match MySQL ENUM exactly).
     */
    public String toDbValue() {
        return this.name(); // BRANDING, E_COMMERCE, LEADERSHIP, FINANCE, FUNDING
    }

    /**
     * Parse a database string into a DomaineNom enum constant.
     * 
     * @throws IllegalArgumentException if the value does not match any constant
     */
    public static DomaineNom fromDbValue(String dbValue) {
        if (dbValue == null || dbValue.isBlank()) {
            throw new IllegalArgumentException("nom_domaine cannot be null or blank");
        }
        return DomaineNom.valueOf(dbValue.trim());
    }

    /**
     * Safe parse that returns null instead of throwing.
     */
    public static DomaineNom fromDbValueOrNull(String dbValue) {
        try {
            return fromDbValue(dbValue);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return displayName;
    }
}
