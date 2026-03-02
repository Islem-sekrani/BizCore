package edu.Connexion3A7.services;

import edu.Connexion3A7.dto.CoachPerformanceDto;
import edu.Connexion3A7.tools.MyConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service de statistiques avancées sur la performance des coaches.
 *
 * Chaque facteur est calculé dans une méthode privée isolée utilisant
 * exclusivement des PreparedStatement sur la connexion singleton MyConnection.
 *
 * Score final (sur 100) :
 * score = (tauxOccupation * 0.35)
 * + (notePonderee/5 * 20 * 0.35)
 * + (scoreFidelite * 0.20)
 * + (tendanceNorm * 0.10)
 */
public class StatistiqueService {

    // =========================================================================
    // Public API
    // =========================================================================

    /**
     * Calcule et retourne la liste des coaches triée par score final décroissant.
     * Chaque entrée est un {@link CoachPerformanceDto} complet avec classement.
     */
    public List<CoachPerformanceDto> getCoachesPerformance() throws SQLException {
        List<String[]> rawCoaches = fetchAllCoachNames();
        List<CoachPerformanceDto> result = new ArrayList<>();

        for (String[] row : rawCoaches) {
            int id = Integer.parseInt(row[0]);
            String nomComplet = row[1] + " " + row[2];

            double tauxOcc = calcTauxOccupation(id);
            int tendance = calcTendance(id);
            double fidelite = calcScoreFidelite(id);
            double notePond = calcNotePonderee(id);

            // Normalise tendance → 0..100
            double tendanceNorm = Math.min(100.0, Math.max(0.0, 50.0 + tendance * 10.0));

            // Note pondérée sur 20 (max 5 → 5/5*20 = 20), puis weight 35 %
            double noteScore = (notePond / 5.0) * 20.0;

            double scoreFinal = (tauxOcc * 0.35)
                    + (noteScore * 0.35)
                    + (fidelite * 0.20)
                    + (tendanceNorm * 0.10);

            // Clamp to 0–100
            scoreFinal = Math.min(100.0, Math.max(0.0, scoreFinal));

            result.add(new CoachPerformanceDto(
                    nomComplet,
                    tauxOcc,
                    tendance,
                    fidelite,
                    notePond,
                    scoreFinal,
                    0 // classement assigned below
            ));
        }

        // Sort descending by scoreFinal then assign ranks
        result.sort((a, b) -> Double.compare(b.getScoreFinal(), a.getScoreFinal()));
        for (int i = 0; i < result.size(); i++) {
            result.get(i).setClassement(i + 1);
        }

        return result;
    }

    // =========================================================================
    // Private SQL helpers
    // =========================================================================

    /**
     * Récupère id_coach, nom, prenom pour tous les coaches.
     * 
     * @return liste de tableaux [id_coach, nom, prenom]
     */
    private List<String[]> fetchAllCoachNames() throws SQLException {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT id_coach, nom, prenom FROM coach ORDER BY nom, prenom";
        Connection cnx = MyConnection.getInstance().getCnx();

        try (PreparedStatement ps = cnx.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new String[] {
                        String.valueOf(rs.getInt("id_coach")),
                        rs.getString("nom") != null ? rs.getString("nom") : "",
                        rs.getString("prenom") != null ? rs.getString("prenom") : ""
                });
            }
        }
        return list;
    }

    /**
     * FACTEUR 1 — Taux d'occupation (résultat : 0–100 %).
     *
     * = (réservations CONFIRMEE dans les 30 derniers jours)
     * / (total de jours disponibles dans la table disponibilite)
     * * 100
     */
    private double calcTauxOccupation(int idCoach) throws SQLException {
        Connection cnx = MyConnection.getInstance().getCnx();

        // Nombre de réservations confirmées sur 30 jours
        String sqlResas = """
                SELECT COUNT(*) AS nb
                FROM reservation
                WHERE id_coach = ?
                  AND statut = 'CONFIRMEE'
                  AND date_seance >= CURDATE() - INTERVAL 30 DAY
                """;
        int nbResas = 0;
        try (PreparedStatement ps = cnx.prepareStatement(sqlResas)) {
            ps.setInt(1, idCoach);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    nbResas = rs.getInt("nb");
            }
        }

        // Total de jours disponibles (statut = 'Disponible')
        String sqlDispo = """
                SELECT COUNT(*) AS nb
                FROM disponibilite
                WHERE id_coach = ?
                  AND statut = 'Disponible'
                """;
        int nbDispo = 0;
        try (PreparedStatement ps = cnx.prepareStatement(sqlDispo)) {
            ps.setInt(1, idCoach);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    nbDispo = rs.getInt("nb");
            }
        }

        if (nbDispo == 0)
            return 0.0;
        return Math.min(100.0, (nbResas * 100.0) / nbDispo);
    }

    /**
     * FACTEUR 2 — Tendance (entier signé).
     *
     * = réservations CONFIRMEE ce mois-ci − réservations CONFIRMEE le mois dernier
     */
    private int calcTendance(int idCoach) throws SQLException {
        Connection cnx = MyConnection.getInstance().getCnx();

        String sql = """
                SELECT
                    SUM(CASE WHEN MONTH(date_seance) = MONTH(CURDATE())
                                  AND YEAR(date_seance)  = YEAR(CURDATE())
                             THEN 1 ELSE 0 END) AS ce_mois,
                    SUM(CASE WHEN MONTH(date_seance) = MONTH(CURDATE() - INTERVAL 1 MONTH)
                                  AND YEAR(date_seance)  = YEAR(CURDATE() - INTERVAL 1 MONTH)
                             THEN 1 ELSE 0 END) AS mois_dernier
                FROM reservation
                WHERE id_coach = ?
                  AND statut = 'CONFIRMEE'
                """;

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idCoach);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int ceMois = rs.getInt("ce_mois");
                    int moisDernier = rs.getInt("mois_dernier");
                    return ceMois - moisDernier;
                }
            }
        }
        return 0;
    }

    /**
     * FACTEUR 3 — Score de fidélité (résultat : 0–100 %).
     *
     * = (nb clients ayant réservé >1 fois) / (nb clients uniques) * 100
     */
    private double calcScoreFidelite(int idCoach) throws SQLException {
        Connection cnx = MyConnection.getInstance().getCnx();

        String sql = """
                SELECT
                    COUNT(DISTINCT id_user) AS total_clients,
                    SUM(CASE WHEN cnt > 1 THEN 1 ELSE 0 END) AS fideles
                FROM (
                    SELECT id_user, COUNT(*) AS cnt
                    FROM reservation
                    WHERE id_coach = ?
                      AND statut = 'CONFIRMEE'
                    GROUP BY id_user
                ) sub
                """;

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idCoach);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int total = rs.getInt("total_clients");
                    int fideles = rs.getInt("fideles");
                    if (total == 0)
                        return 0.0;
                    return (fideles * 100.0) / total;
                }
            }
        }
        return 0.0;
    }

    /**
     * FACTEUR 4 — Note pondérée par le volume d'avis (résultat : 0–5).
     *
     * Formule bayésienne : (AVG(rating) * COUNT(rating)) / (COUNT(rating) + 5)
     * Un coach sans avis obtient 0. Plus il a d'avis, plus la note converge
     * vers sa vraie moyenne.
     */
    private double calcNotePonderee(int idCoach) throws SQLException {
        Connection cnx = MyConnection.getInstance().getCnx();

        String sql = """
                SELECT
                    AVG(rating)   AS avg_rating,
                    COUNT(rating) AS nb_ratings
                FROM coach_rating
                WHERE id_coach = ?
                """;

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idCoach);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double avg = rs.getDouble("avg_rating");
                    int nb = rs.getInt("nb_ratings");
                    if (nb == 0)
                        return 0.0;
                    return (avg * nb) / (nb + 5.0);
                }
            }
        }
        return 0.0;
    }
}
