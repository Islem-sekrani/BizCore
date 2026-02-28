package edu.Connexion3A7.services;

import edu.Connexion3A7.tools.MyConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

public class DisponibiliteService {

    /**
     * Returns a map of 7 days (Mon–Sun) with their availability status.
     * Days without an explicit entry in the disponibilite table fall back
     * to the coach's general disponibilite field.
     */
    public Map<LocalDate, String> getWeekAvailability(int coachId, LocalDate weekStart) throws SQLException {
        Map<LocalDate, String> result = new LinkedHashMap<>();

        // Default every day to the coach's general dispo
        String fallback = getCoachGeneralDispo(coachId);
        for (int i = 0; i < 7; i++) {
            result.put(weekStart.plusDays(i), fallback);
        }

        // Override with explicit per-day entries
        String sql = "SELECT jour, statut FROM disponibilite WHERE id_coach = ? AND jour BETWEEN ? AND ?";
        PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(sql);
        pst.setInt(1, coachId);
        pst.setDate(2, Date.valueOf(weekStart));
        pst.setDate(3, Date.valueOf(weekStart.plusDays(6)));
        ResultSet rs = pst.executeQuery();
        while (rs.next()) {
            LocalDate d = rs.getDate("jour").toLocalDate();
            result.put(d, rs.getString("statut"));
        }

        return result;
    }

    /**
     * Returns the coach's general disponibilite column value.
     */
    private String getCoachGeneralDispo(int coachId) throws SQLException {
        String sql = "SELECT disponibilite FROM coach WHERE id_coach = ?";
        PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(sql);
        pst.setInt(1, coachId);
        ResultSet rs = pst.executeQuery();
        if (rs.next()) {
            String d = rs.getString("disponibilite");
            return (d != null) ? d : "Indisponible";
        }
        return "Indisponible";
    }
}
