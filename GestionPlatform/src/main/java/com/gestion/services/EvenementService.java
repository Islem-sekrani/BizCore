package com.gestion.services;

import com.gestion.entities.Evenement;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EvenementService {

    private Connection cnx;

    public EvenementService() {
        try {
            cnx = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/bizcore",
                    "root",
                    ""
            );
            System.out.println("Connexion réussie !");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 🔹 AJOUTER
    public void ajouter(Evenement e) {

        String sql = "INSERT INTO evenement "
                + "(titre, description, date_debut, date_fin, lieu, capacite_max, prix, image_url, statut, id_organisateur, id_categorie) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setString(1, e.getTitre());
            ps.setString(2, e.getDescription());
            ps.setTimestamp(3, Timestamp.valueOf(e.getDateDebut()));
            ps.setTimestamp(4, Timestamp.valueOf(e.getDateFin()));
            ps.setString(5, e.getLieu());
            ps.setInt(6, e.getCapacite());
            ps.setDouble(7, e.getPrix());
            ps.setString(8, e.getImageUrl());
            ps.setString(9, e.getStatut());
            ps.setInt(10, e.getIdOrganisateur());
            ps.setInt(11, e.getIdCategorie());

            ps.executeUpdate();
            System.out.println("Evenement ajouté avec succès !");

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // 🔹 AFFICHER
    public List<Evenement> afficher() {
        List<Evenement> list = new ArrayList<>();
        String sql = "SELECT * FROM evenement";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Evenement e = new Evenement();
                e.setIdEvenement(rs.getInt("id_evenement"));
                e.setTitre(rs.getString("titre"));
                e.setDescription(rs.getString("description"));
                e.setDateDebut(rs.getTimestamp("date_debut").toLocalDateTime());
                e.setDateFin(rs.getTimestamp("date_fin").toLocalDateTime());
                e.setLieu(rs.getString("lieu"));
                e.setCapacite(rs.getInt("capacite_max"));
                e.setPrix(rs.getDouble("prix"));
                e.setImageUrl(rs.getString("image_url"));
                e.setStatut(rs.getString("statut"));
                e.setIdOrganisateur(rs.getInt("id_organisateur"));
                e.setIdCategorie(rs.getInt("id_categorie"));

                list.add(e);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return list;
    }


    // 🔹 MODIFIER
    public void modifier(Evenement e) {

        String sql = "UPDATE evenement SET "
                + "titre=?, description=?, date_debut=?, date_fin=?, lieu=?, "
                + "capacite_max=?, prix=?, image_url=?, statut=?, id_organisateur=?, id_categorie=? "
                + "WHERE id_evenement=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setString(1, e.getTitre());
            ps.setString(2, e.getDescription());
            ps.setTimestamp(3, Timestamp.valueOf(e.getDateDebut()));
            ps.setTimestamp(4, Timestamp.valueOf(e.getDateFin()));
            ps.setString(5, e.getLieu());
            ps.setInt(6, e.getCapacite());
            ps.setDouble(7, e.getPrix());
            ps.setString(8, e.getImageUrl());
            ps.setString(9, e.getStatut());
            ps.setInt(10, e.getIdOrganisateur());
            ps.setInt(11, e.getIdCategorie());
            ps.setInt(12, e.getIdEvenement());

            ps.executeUpdate();
            System.out.println("Evenement modifié !");

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // 🔹 SUPPRIMER
    public boolean supprimer(int id) {

        String sql = "DELETE FROM evenement WHERE id_evenement=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);

            int rows = ps.executeUpdate(); // renvoie le nombre de lignes affectées
            if (rows > 0) {
                System.out.println("Événement supprimé !");
                return true; // suppression réussie
            } else {
                System.out.println("Aucun événement trouvé avec cet ID !");
                return false; // aucun enregistrement supprimé
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            return false; // erreur SQL
        }
    }


}
