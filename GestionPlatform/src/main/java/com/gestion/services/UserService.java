package com.gestion.services;

import com.gestion.tools.DatabaseConnection;
import com.gestion.entities.User;
import com.gestion.interfaces.IUserService;
import com.gestion.tools.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * User Service Implementation
 * Handles all user-related database operations
 */
public class UserService implements IUserService {

    @Override
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String query = "SELECT u.*, r.nom_role FROM users u " +
                "LEFT JOIN role r ON u.id_role = r.id_role";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                User user = new User(
                        rs.getInt("id_user"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("telephone"),
                        rs.getString("adresse"),
                        rs.getString("statut"),
                        rs.getInt("id_role"),
                        rs.getTimestamp("date_inscription"),
                        rs.getTimestamp("derniere_connexion")
                );
                user.setNomRole(rs.getString("nom_role"));
                users.add(user);
            }

            System.out.println("✅ " + users.size() + " utilisateurs chargés depuis la base de données");

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération des utilisateurs: " + e.getMessage());
            e.printStackTrace();
        }

        return users;
    }

    @Override
    public boolean addUser(User user) {
        String query = "INSERT INTO users (nom, prenom, email, password, id_role, statut) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, user.getNom());
            pstmt.setString(2, user.getPrenom());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getPassword());
            pstmt.setInt(5, user.getIdRole());
            pstmt.setString(6, user.getStatut() != null ? user.getStatut() : "deconnecte");

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    user.setIdUser(generatedKeys.getInt(1));
                }
                System.out.println("✅ Utilisateur ajouté: " + user.getNom() + " " + user.getPrenom());
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'ajout de l'utilisateur: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean updateUser(User user) {
        String query = "UPDATE users SET nom = ?, prenom = ?, email = ?, password = ?, telephone = ?, adresse = ?, statut = ?, id_role = ? WHERE id_user = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, user.getNom());
            pstmt.setString(2, user.getPrenom());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getPassword());
            pstmt.setString(5, user.getTelephone());
            pstmt.setString(6, user.getAdresse());
            pstmt.setString(7, user.getStatut());
            pstmt.setInt(8, user.getIdRole());
            pstmt.setInt(9, user.getIdUser());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✅ Utilisateur modifié: " + user.getNom() + " " + user.getPrenom());
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la modification de l'utilisateur: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean deleteUser(int idUser) {
        String query = "DELETE FROM users WHERE id_user = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, idUser);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✅ Utilisateur supprimé (ID: " + idUser + ")");
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la suppression de l'utilisateur: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public User getUserById(int idUser) {
        String query = "SELECT * FROM users WHERE id_user = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, idUser);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getInt("id_user"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("telephone"),
                        rs.getString("adresse"),
                        rs.getString("statut"),
                        rs.getInt("id_role"),
                        rs.getTimestamp("date_inscription"),
                        rs.getTimestamp("derniere_connexion")
                );
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération de l'utilisateur: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public User getUserByEmail(String email) {
        String query = "SELECT * FROM users WHERE email = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getInt("id_user"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("telephone"),
                        rs.getString("adresse"),
                        rs.getString("statut"),
                        rs.getInt("id_role"),
                        rs.getTimestamp("date_inscription"),
                        rs.getTimestamp("derniere_connexion")
                );
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération de l'utilisateur par email: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public void updateLastConnection(int idUser, Timestamp ts) {
        String sql = "UPDATE users SET derniere_connexion = ?, statut = ? WHERE id_user = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, ts);
            ps.setString(2, "connecte"); // also set statut = connecte here
            ps.setInt(3, idUser);

            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
            // optionally rethrow or log properly
        }
    }
}
