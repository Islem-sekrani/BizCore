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
                "(id_user,domaine, nom, prenom, biographie, experience_annees, tarif_horaire, disponibilite, certification, note_moyenne) "
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
        pst.setString(9, coach.getCertif());
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
                "experience_annees = ?, tarif_horaire = ?, disponibilite = ?, certification = ?, note_moyenne = ? " +
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
        pst.setString(9, coach.getCertif());
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
            c.setCertif(rs.getString("certification"));
            c.setNote(rs.getFloat("note_moyenne"));
            data.add(c);
        }

        return data;
    }
}
