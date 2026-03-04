package com.gestion.analyzers;

/**
 * TrendingResult
 * ──────────────
 * Immutable data holder for one entry in the GET /api/blogs/trending response.
 *
 * Mirrors the JSON shape specified:
 *   { "id", "titre", "vues", "created_at", "trending_score" }
 *
 * Score is computed dynamically — never stored in the database.
 * No JavaFX dependency. No database access.
 */
public class TrendingResult {

    private final int    id;
    private final String titre;
    private final int    vues;
    private final String createdAt;       // "YYYY-MM-DD" or estimated from ID rank
    private final double trendingScore;   // (vues × 0.6) + (recencyFactor × 0.4)

    // =========================================================================
    //  Constructor — all 5 fields assigned
    // =========================================================================
    public TrendingResult(int id, String titre, int vues,
                          String createdAt, double trendingScore) {
        this.id            = id;
        this.titre         = titre != null ? titre : "";
        this.vues          = vues;
        this.createdAt     = createdAt != null ? createdAt : "N/A";
        this.trendingScore = Math.round(trendingScore * 10.0) / 10.0;
    }

    // =========================================================================
    //  Getters
    // =========================================================================
    public int    getId()             { return id; }
    public String getTitre()         { return titre; }
    public int    getVues()          { return vues; }
    public String getCreatedAt()     { return createdAt; }
    public double getTrendingScore() { return trendingScore; }

    /**
     * JSON-style string representation — matches the spec response format.
     * Example:
     *   { "id": 12, "titre": "Guide Marketing", "vues": 540,
     *     "created_at": "2026-03-01", "trending_score": 82.5 }
     */
    @Override
    public String toString() {
        return String.format(
            "{ \"id\": %d, \"titre\": \"%s\", \"vues\": %d, " +
            "\"created_at\": \"%s\", \"trending_score\": %.1f }",
            id, titre.replace("\"", "\\\""), vues, createdAt, trendingScore);
    }
}
