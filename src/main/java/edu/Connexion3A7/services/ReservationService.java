package edu.Connexion3A7.services;

import edu.Connexion3A7.entities.Reservation;
import edu.Connexion3A7.tools.MyConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReservationService {

    /**
     * Book a coach for a user. Inserts into reservation table.
     */
    public void addReservation(int userId, int coachId) throws SQLException {
        String sql = "INSERT INTO reservation (id_user, id_coach) VALUES (?, ?)";
        PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(sql);
        pst.setInt(1, userId);
        pst.setInt(2, coachId);
        pst.executeUpdate();
        System.out.println("Reservation ajoutee: user=" + userId + " coach=" + coachId);
    }

    /**
     * Remove a booking for a user/coach pair.
     */
    public void removeReservation(int userId, int coachId) throws SQLException {
        String sql = "DELETE FROM reservation WHERE id_user = ? AND id_coach = ?";
        PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(sql);
        pst.setInt(1, userId);
        pst.setInt(2, coachId);
        pst.executeUpdate();
        System.out.println("Reservation supprimee: user=" + userId + " coach=" + coachId);
    }

    /**
     * Check if a user has already booked a specific coach.
     */
    public boolean isBooked(int userId, int coachId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM reservation WHERE id_user = ? AND id_coach = ?";
        PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(sql);
        pst.setInt(1, userId);
        pst.setInt(2, coachId);
        ResultSet rs = pst.executeQuery();
        if (rs.next()) {
            return rs.getInt(1) > 0;
        }
        return false;
    }

    /**
     * Get all reservations for a given user.
     */
    public List<Reservation> getByUser(int userId) throws SQLException {
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT * FROM reservation WHERE id_user = ?";
        PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(sql);
        pst.setInt(1, userId);
        ResultSet rs = pst.executeQuery();
        while (rs.next()) {
            Reservation r = new Reservation();
            r.setIdReservation(rs.getInt("id_reservation"));
            r.setIdUser(rs.getInt("id_user"));
            r.setIdCoach(rs.getInt("id_coach"));
            Timestamp ts = rs.getTimestamp("date_reservation");
            r.setDateReservation(ts != null ? ts.toLocalDateTime() : LocalDateTime.now());
            r.setStatut(rs.getString("statut"));
            list.add(r);
        }
        return list;
    }

    /**
     * Get all reservations (admin view).
     */
    public List<Reservation> getAll() throws SQLException {
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT * FROM reservation";
        Statement st = MyConnection.getInstance().getCnx().createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            Reservation r = new Reservation();
            r.setIdReservation(rs.getInt("id_reservation"));
            r.setIdUser(rs.getInt("id_user"));
            r.setIdCoach(rs.getInt("id_coach"));
            Timestamp ts = rs.getTimestamp("date_reservation");
            r.setDateReservation(ts != null ? ts.toLocalDateTime() : LocalDateTime.now());
            r.setStatut(rs.getString("statut"));
            list.add(r);
        }
        return list;
    }
}
