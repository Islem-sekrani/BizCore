package com.gestion.analyzers;

import com.gestion.tools.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * BlogTrendingService
 * ───────────────────
 * Implements the logic behind:
 *   GET /api/blogs/trending
 *
 * Algorithm:
 *   TrendingScore = (Views × 0.6) + (RecencyFactor × 0.4)
 *
 * Where RecencyFactor is derived from the article's age:
 *   0–3 days   →  90–100  (very recent, high boost)
 *   4–10 days  →  50–89   (medium recent)
 *   11–30 days →  20–49   (somewhat old)
 *   31+ days   →  0–19    (old, minimal boost)
 *
 * Database design note:
 *   The existing `article` table does NOT have a `created_at` column in
 *   the base schema. This service queries for it gracefully:
 *     - If the column exists  → uses real date for recency
 *     - If the column absent  → derives recency from id_article rank
 *       (higher ID = inserted later = more recent)
 *
 *   This means the feature works with ANY state of the database, including
 *   after the user adds the column later, without any code change.
 *
 * Rules:
 *   - Only "Publié" articles are included (published filter).
 *   - Score is NEVER stored in the database.
 *   - No database writes occur.
 *   - Returns top 5, sorted by descending score.
 */
public class BlogTrendingService {

    private static final int    TOP_N          = 5;
    private static final String PUBLISHED_STATUS = "Publié";

    // =========================================================================
    //  Public entry point — GET /api/blogs/trending
    // =========================================================================

    /**
     * Retrieve all published articles, compute their trending score,
     * sort by score descending, and return the top 5.
     *
     * @return list of up to 5 TrendingResult entries, highest score first
     */
    public List<TrendingResult> getTopTrending() {
        List<TrendingResult> results = new ArrayList<>();

        // Try to read created_at column — fall back gracefully if absent
        boolean hasCreatedAt = columnExists("article", "created_at");

        String sql = hasCreatedAt
            ? "SELECT id_article, titre, nombre_vues, created_at " +
              "FROM article WHERE statut = ?"
            : "SELECT id_article, titre, nombre_vues " +
              "FROM article WHERE statut = ?";

        // Max ID used for ID-rank recency proxy when created_at is absent
        int maxId = hasCreatedAt ? 0 : fetchMaxId();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, PUBLISHED_STATUS);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int    id    = rs.getInt("id_article");
                    String titre = rs.getString("titre");
                    int    vues  = rs.getInt("nombre_vues");

                    // ── Recency factor ────────────────────────────────────────
                    double recencyFactor;
                    String createdAtStr;

                    if (hasCreatedAt) {
                        // Real date available — compute age in days
                        Date sqlDate = rs.getDate("created_at");
                        if (sqlDate != null) {
                            LocalDate articleDate = sqlDate.toLocalDate();
                            LocalDate today       = LocalDate.now();
                            long ageDays = ChronoUnit.DAYS.between(articleDate, today);
                            recencyFactor = recencyFromDays(ageDays);
                            createdAtStr  = articleDate.toString();   // "YYYY-MM-DD"
                        } else {
                            // NULL date in DB — treat as old
                            recencyFactor = 10.0;
                            createdAtStr  = "N/A";
                        }
                    } else {
                        // No created_at column — use ID rank as proxy
                        // Higher ID = more recently inserted = more recent
                        recencyFactor = recencyFromIdRank(id, maxId);
                        createdAtStr  = estimateDateFromIdRank(id, maxId);
                    }

                    // ── Trending score formula ────────────────────────────────
                    // TrendingScore = (Views × 0.6) + (RecencyFactor × 0.4)
                    double trendingScore = (vues * 0.6) + (recencyFactor * 0.4);

                    results.add(new TrendingResult(id, titre, vues, createdAtStr, trendingScore));
                }
            }

            System.out.println("🔥 Trending calculé sur " + results.size() + " articles publiés");

        } catch (SQLException e) {
            System.err.println("❌ BlogTrendingService: " + e.getMessage());
        }

        // Sort by score descending, return top N
        results.sort(Comparator.comparingDouble(TrendingResult::getTrendingScore).reversed());
        return results.subList(0, Math.min(TOP_N, results.size()));
    }

    // =========================================================================
    //  Recency calculation
    // =========================================================================

    /**
     * Converts article age in days to a recency factor (0–100).
     *
     *   0–3 days   → 100 → 90   (very fresh)
     *   4–10 days  → 89  → 50   (recent)
     *   11–30 days → 49  → 20   (medium age)
     *   31–90 days → 19  → 5    (old)
     *   91+ days   → 5   → 0    (very old)
     */
    private double recencyFromDays(long ageDays) {
        if (ageDays < 0)   ageDays = 0;      // future-dated articles treated as brand new
        if (ageDays <= 3)  return 100.0 - (ageDays * 3.3);          // 100 → ~90
        if (ageDays <= 10) return 89.0  - ((ageDays - 4) * 5.57);   // ~89 → ~50
        if (ageDays <= 30) return 49.0  - ((ageDays - 11) * 1.45);  // ~49 → ~20
        if (ageDays <= 90) return 19.0  - ((ageDays - 31) * 0.23);  // ~19 → ~5
        return Math.max(0.0, 5.0 - ((ageDays - 91) * 0.05));        // 5 → 0
    }

    /**
     * When created_at does not exist, derives recency from id_article rank.
     * The article with the highest ID is assumed the most recent (score = 100),
     * the one with ID = 1 is assumed the oldest (score ≈ 0).
     */
    private double recencyFromIdRank(int id, int maxId) {
        if (maxId <= 0) return 50.0;
        return (double) id / maxId * 100.0;
    }

    /**
     * Produces a human-readable estimate like "~récent" / "~ancien"
     * for the created_at display when the real date is unavailable.
     */
    private String estimateDateFromIdRank(int id, int maxId) {
        if (maxId <= 0) return "N/A";
        double rankRatio = (double) id / maxId;
        if (rankRatio >= 0.9) return "~très récent";
        if (rankRatio >= 0.7) return "~récent";
        if (rankRatio >= 0.4) return "~moyen";
        return "~ancien";
    }

    // =========================================================================
    //  Database helpers — read-only, no writes
    // =========================================================================

    /**
     * Checks whether a given column exists in a given table.
     * Uses JDBC metadata — works with any MySQL version, no schema change.
     */
    private boolean columnExists(String tableName, String columnName) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            try (ResultSet cols = meta.getColumns(null, null, tableName, columnName)) {
                return cols.next();
            }
        } catch (SQLException e) {
            System.err.println("⚠ columnExists check failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Returns the maximum id_article in the table.
     * Used as the upper bound for ID-rank recency proxy.
     */
    private int fetchMaxId() {
        String sql = "SELECT MAX(id_article) FROM article";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement  stmt = conn.createStatement();
             ResultSet  rs   = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("⚠ fetchMaxId failed: " + e.getMessage());
        }
        return 1;
    }
}
