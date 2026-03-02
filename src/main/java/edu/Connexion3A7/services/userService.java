package edu.Connexion3A7.services;

import edu.Connexion3A7.entities.user;
import edu.Connexion3A7.interfaces.IUserService;
import edu.Connexion3A7.tools.MyConnection;

import java.sql.*;

public class userService implements IUserService {

    private Connection cnx;

    public userService() {
        cnx = MyConnection.getInstance().getCnx();
    }

    // ── Reconnect helper ──────────────────────────────────────────────────────
    private Connection conn() throws SQLException {
        if (cnx == null || cnx.isClosed())
            cnx = MyConnection.getInstance().getCnx();
        return cnx;
    }

    // ── Authentication ────────────────────────────────────────────────────────
    @Override
    public user authenticate(String email, String password) throws SQLException {
        String query = "SELECT * FROM users WHERE email = ? AND password = ?";
        PreparedStatement ps = conn().prepareStatement(query);
        ps.setString(1, email);
        ps.setString(2, password);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            user u = new user();
            u.setId_user(rs.getInt("id_user"));
            u.setEmail(rs.getString("email"));
            u.setMdp(rs.getString("password"));
            u.setRole(rs.getString("role"));
            // telephone column may not exist yet — read it safely
            try {
                u.setTelephone(rs.getString("telephone"));
            } catch (SQLException ignored) {
            }
            return u;
        }
        return null;
    }

    // ── Phone number methods ──────────────────────────────────────────────────

    /**
     * Returns the stored E.164 telephone for a user, or null if not set.
     * Safe to call before the SQL migration has been run (catches missing column).
     */
    public String getUserTelephone(int userId) {
        String sql = "SELECT telephone FROM users WHERE id_user = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getString("telephone");
        } catch (SQLException e) {
            // Column `telephone` not yet created — silently ignore
            System.err.println("[userService] getUserTelephone: " + e.getMessage());
        }
        return null;
    }

    /**
     * Persists a phone number (E.164 format) for the given user.
     * Requires the SQL migration to have been run first.
     */
    public void updateTelephone(int userId, String telephone) throws SQLException {
        String sql = "UPDATE users SET telephone = ? WHERE id_user = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, telephone);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }
}
