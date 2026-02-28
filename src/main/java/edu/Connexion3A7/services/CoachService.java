package edu.Connexion3A7.services;

import edu.Connexion3A7.entities.coach;
import edu.Connexion3A7.interfaces.IService;
import edu.Connexion3A7.tools.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CoachService implements IService<coach> {

    @Override
    public void addCoach(coach coach) throws SQLException {
        // Do NOT insert id_coach — let the DB auto-increment it
        String sql = "INSERT INTO coach " +
                "(id_user, domaine, nom, prenom, biographie, experience_annees, tarif_horaire, disponibilite, num_tel, note_moyenne) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(sql);

        pst.setInt(1, coach.getId_user());
        pst.setString(2, coach.getDomaine());
        pst.setString(3, coach.getNom());
        pst.setString(4, coach.getPrenom());
        pst.setString(5, coach.getBiographie());
        pst.setInt(6, coach.getExperience());
        pst.setDouble(7, coach.getTarif());
        pst.setString(8, coach.getDispo());
        pst.setString(9, coach.getNumTel());
        pst.setDouble(10, coach.getNote());

        pst.executeUpdate();
        System.out.println("Coach ajoute avec succes");
    }

    @Override
    public void deleteCoach(coach coach) throws SQLException {
        String sql = "DELETE FROM coach WHERE id_coach = ?";
        PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(sql);
        pst.setInt(1, coach.getId_coach());
        pst.executeUpdate();
        System.out.println("Coach supprime avec succes");
    }

    @Override
    public void updateCoach(coach coach) throws SQLException {
        String sql = "UPDATE coach SET id_user = ?, domaine = ?, nom = ?, prenom = ?, biographie = ?, " +
                "experience_annees = ?, tarif_horaire = ?, disponibilite = ?, num_tel = ?, note_moyenne = ? " +
                "WHERE id_coach = ?";
        PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(sql);
        pst.setInt(1, coach.getId_user());
        pst.setString(2, coach.getDomaine());
        pst.setString(3, coach.getNom());
        pst.setString(4, coach.getPrenom());
        pst.setString(5, coach.getBiographie());
        pst.setInt(6, coach.getExperience());
        pst.setDouble(7, coach.getTarif());
        pst.setString(8, coach.getDispo());
        pst.setString(9, coach.getNumTel());
        pst.setDouble(10, coach.getNote());
        pst.setInt(11, coach.getId_coach());
        pst.executeUpdate();
        System.out.println("Coach mis a jour avec succes");
    }

    @Override
    public List<coach> getData() throws SQLException {
        List<coach> data = new ArrayList<>();
        String sql = "SELECT * FROM coach";
        Statement st = MyConnection.getInstance().getCnx().createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            coach c = new coach();
            c.setId_coach(rs.getInt("id_coach"));
            c.setId_user(rs.getInt("id_user"));
            c.setDomaine(rs.getString("domaine"));
            c.setNom(rs.getString("nom"));
            c.setPrenom(rs.getString("prenom"));
            c.setBiographie(rs.getString("biographie"));
            c.setExperience(rs.getInt("experience_annees"));
            c.setTarif(rs.getFloat("tarif_horaire"));
            c.setDispo(rs.getString("disponibilite"));
            c.setNumTel(rs.getString("num_tel"));
            c.setNote(rs.getFloat("note_moyenne"));
            data.add(c);
        }

        return data;
    }

    /**
     * Check if a coach with the same (nom, prenom) already exists.
     * Used before INSERT to prevent duplicates.
     */
    public boolean isCoachDuplicate(String nom, String prenom) throws SQLException {
        String sql = "SELECT COUNT(*) FROM coach WHERE LOWER(nom) = LOWER(?) AND LOWER(prenom) = LOWER(?)";
        PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(sql);
        pst.setString(1, nom.trim());
        pst.setString(2, prenom.trim());
        ResultSet rs = pst.executeQuery();
        if (rs.next()) {
            return rs.getInt(1) > 0;
        }
        return false;
    }

    /**
     * Check if a coach with the same (nom, prenom) already exists,
     * excluding a specific coach ID. Used before UPDATE.
     */
    public boolean isCoachDuplicateExcluding(String nom, String prenom, int excludeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM coach WHERE LOWER(nom) = LOWER(?) AND LOWER(prenom) = LOWER(?) AND id_coach != ?";
        PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(sql);
        pst.setString(1, nom.trim());
        pst.setString(2, prenom.trim());
        pst.setInt(3, excludeId);
        ResultSet rs = pst.executeQuery();
        if (rs.next()) {
            return rs.getInt(1) > 0;
        }
        return false;
    }

    /**
     * Add or update a user's rating for a specific coach.
     * Uses INSERT ... ON DUPLICATE KEY UPDATE for upsert behavior.
     * Then recalculates note_moyenne as AVG of all ratings.
     */
    public void addOrUpdateUserRating(int userId, int coachId, int rating) throws SQLException {
        // Upsert into coach_rating
        String sql = "INSERT INTO coach_rating (id_user, id_coach, rating) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE rating = VALUES(rating)";
        PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(sql);
        pst.setInt(1, userId);
        pst.setInt(2, coachId);
        pst.setInt(3, rating);
        pst.executeUpdate();

        // Recalculate average
        recalculateAverage(coachId);
    }

    /**
     * Recalculate note_moyenne for a coach based on all ratings in coach_rating.
     */
    public void recalculateAverage(int coachId) throws SQLException {
        String sql = "UPDATE coach SET note_moyenne = " +
                "(SELECT COALESCE(AVG(rating), 0) FROM coach_rating WHERE id_coach = ?) " +
                "WHERE id_coach = ?";
        PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(sql);
        pst.setInt(1, coachId);
        pst.setInt(2, coachId);
        pst.executeUpdate();
    }

    /**
     * Get the current average rating for a coach from coach_rating table.
     */
    public float getAverageRating(int coachId) throws SQLException {
        String sql = "SELECT COALESCE(AVG(rating), 0) AS avg_rating FROM coach_rating WHERE id_coach = ?";
        PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(sql);
        pst.setInt(1, coachId);
        ResultSet rs = pst.executeQuery();
        if (rs.next()) {
            return rs.getFloat("avg_rating");
        }
        return 0f;
    }

    /**
     * Returns coach count grouped by domaine.
     * Used for domain-based statistics on the admin dashboard.
     */
    public java.util.Map<String, Integer> getCoachCountByDomaine() throws SQLException {
        java.util.Map<String, Integer> stats = new java.util.LinkedHashMap<>();
        String sql = "SELECT domaine, COUNT(*) AS cnt FROM coach GROUP BY domaine ORDER BY cnt DESC";
        Statement st = MyConnection.getInstance().getCnx().createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            stats.put(rs.getString("domaine"), rs.getInt("cnt"));
        }
        return stats;
    }

    /**
     * Search coaches with optional filters.
     * 
     * @param domaine  if non-null/non-empty, filter by domaine (case-insensitive)
     * @param maxTarif if > 0, only return coaches with tarif_horaire <= maxTarif
     * @return filtered list of coaches (only available ones)
     */
    public List<coach> searchCoaches(String domaine, double maxTarif) throws SQLException {
        List<coach> results = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM coach WHERE disponibilite = 'Disponible'");
        List<Object> params = new ArrayList<>();

        if (domaine != null && !domaine.trim().isEmpty()) {
            sql.append(" AND LOWER(domaine) LIKE LOWER(?)");
            params.add("%" + domaine.trim() + "%");
        }
        if (maxTarif > 0) {
            sql.append(" AND tarif_horaire <= ?");
            params.add(maxTarif);
        }

        sql.append(" ORDER BY note_moyenne DESC, tarif_horaire ASC");

        PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(sql.toString());
        for (int i = 0; i < params.size(); i++) {
            Object p = params.get(i);
            if (p instanceof String) {
                pst.setString(i + 1, (String) p);
            } else if (p instanceof Double) {
                pst.setDouble(i + 1, (Double) p);
            }
        }

        ResultSet rs = pst.executeQuery();
        while (rs.next()) {
            coach c = new coach();
            c.setId_coach(rs.getInt("id_coach"));
            c.setId_user(rs.getInt("id_user"));
            c.setDomaine(rs.getString("domaine"));
            c.setNom(rs.getString("nom"));
            c.setPrenom(rs.getString("prenom"));
            c.setBiographie(rs.getString("biographie"));
            c.setExperience(rs.getInt("experience_annees"));
            c.setTarif(rs.getFloat("tarif_horaire"));
            c.setDispo(rs.getString("disponibilite"));
            c.setNumTel(rs.getString("num_tel"));
            c.setNote(rs.getFloat("note_moyenne"));
            results.add(c);
        }
        return results;
    }

    /**
     * Builds a human-readable summary of all coaches for AI context injection.
     * The text is designed to be included in the chatbot system prompt so the
     * LLM can recommend real coaches from the database.
     */
    public String buildCoachSummary() {
        try {
            List<coach> coaches = getData();
            if (coaches.isEmpty()) {
                return "Aucun coach n'est actuellement enregistre dans le systeme.";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("Voici les coachs disponibles dans notre systeme :\n\n");

            for (coach c : coaches) {
                sb.append("- ").append(c.getNom()).append(" ").append(c.getPrenom());
                sb.append(" | Domaine: ").append(c.getDomaine() != null ? c.getDomaine() : "N/A");
                sb.append(" | Tarif: ").append(String.format("%.0f", c.getTarif())).append(" DT/H");
                sb.append(" | Experience: ").append(c.getExperience()).append(" ans");
                sb.append(" | Note: ").append(String.format("%.1f", c.getNote())).append("/5");
                sb.append(" | Disponibilite: ").append(c.getDispo() != null ? c.getDispo() : "N/A");
                if (c.getBiographie() != null && !c.getBiographie().isEmpty()) {
                    sb.append(" | Bio: ").append(c.getBiographie());
                }
                sb.append("\n");
            }

            return sb.toString();
        } catch (SQLException e) {
            System.err.println("[CoachService] Erreur buildCoachSummary: " + e.getMessage());
            return "Erreur lors de la recuperation des donnees des coachs.";
        }
    }
}
