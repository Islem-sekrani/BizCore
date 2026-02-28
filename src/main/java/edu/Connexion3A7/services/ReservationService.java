package edu.Connexion3A7.services;

import edu.Connexion3A7.entities.Reservation;
import edu.Connexion3A7.tools.MyConnection;

import java.sql.*;
import java.time.LocalDate;
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

    // ── Date-specific booking (coach selection weekly view) ──────────────────

    /**
     * Book a coach for a user on a specific date.
     * Throws if ANYONE already booked this coach on that date (prevents
     * double-booking).
     */
    public void addReservation(int userId, int coachId, LocalDate dateSeance) throws SQLException {
        // Check if the coach is already booked by ANY user on this date
        if (isCoachBookedOnDate(coachId, dateSeance)) {
            throw new SQLException("Ce coach est deja reserve par un autre utilisateur pour le "
                    + dateSeance + ". Veuillez choisir un autre jour.");
        }

        String sql = "INSERT INTO reservation (id_user, id_coach, date_seance, statut) VALUES (?, ?, ?, 'CONFIRMEE')";
        PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(sql);
        pst.setInt(1, userId);
        pst.setInt(2, coachId);
        pst.setDate(3, Date.valueOf(dateSeance));
        pst.executeUpdate();
        System.out.println("Reservation ajoutee: user=" + userId + " coach=" + coachId + " date=" + dateSeance);
    }

    /**
     * Check if a user already has a booking with a coach on a specific date.
     */
    public boolean isBookedOnDate(int userId, int coachId, LocalDate date) throws SQLException {
        String sql = "SELECT COUNT(*) FROM reservation WHERE id_user = ? AND id_coach = ? AND date_seance = ?";
        PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(sql);
        pst.setInt(1, userId);
        pst.setInt(2, coachId);
        pst.setDate(3, Date.valueOf(date));
        ResultSet rs = pst.executeQuery();
        return rs.next() && rs.getInt(1) > 0;
    }

    /**
     * Check if ANY user has booked a coach on a specific date.
     * Used to prevent double-booking: only one user can book a coach per day.
     */
    public boolean isCoachBookedOnDate(int coachId, LocalDate date) throws SQLException {
        String sql = "SELECT COUNT(*) FROM reservation WHERE id_coach = ? AND date_seance = ? AND statut = 'CONFIRMEE'";
        PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(sql);
        pst.setInt(1, coachId);
        pst.setDate(2, Date.valueOf(date));
        ResultSet rs = pst.executeQuery();
        return rs.next() && rs.getInt(1) > 0;
    }

    /**
     * Cancel a date-specific booking for a user.
     */
    public void removeReservationOnDate(int userId, int coachId, LocalDate date) throws SQLException {
        String sql = "DELETE FROM reservation WHERE id_user = ? AND id_coach = ? AND date_seance = ?";
        PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(sql);
        pst.setInt(1, userId);
        pst.setInt(2, coachId);
        pst.setDate(3, Date.valueOf(date));
        int rows = pst.executeUpdate();
        System.out.println("Reservation supprimee (" + rows + " lignes): user=" + userId
                + " coach=" + coachId + " date=" + date);
    }
}
