package edu.Connexion3A7.services;

import edu.Connexion3A7.entities.DomaineCoaching;
import edu.Connexion3A7.entities.DomaineNom;
import edu.Connexion3A7.tools.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DomaineCoachingService {

    /**
     * Insert a new domaine. Validates that nomDomaine is not null before executing.
     */
    public void addDomaine(DomaineCoaching domaine) throws SQLException {
        if (domaine.getNomDomaine() == null) {
            throw new SQLException(
                    "nom_domaine ne peut pas etre null. Valeurs acceptees: BRANDING, E_COMMERCE, LEADERSHIP, FINANCE, FUNDING");
        }
        String sql = "INSERT INTO domaine_coaching (nom_domaine, description) VALUES (?, ?)";
        PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(sql);
        pst.setString(1, domaine.getNomDomaine().toDbValue());
        pst.setString(2, domaine.getDescription());
        pst.executeUpdate();
        System.out.println("Domaine ajoute avec succes: " + domaine.getNomDomaine());
    }

    public void deleteDomaine(int idDomaine) throws SQLException {
        String sql = "DELETE FROM domaine_coaching WHERE id_domaine = ?";
        PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(sql);
        pst.setInt(1, idDomaine);
        pst.executeUpdate();
        System.out.println("Domaine supprime avec succes");
    }

    /**
     * Update a domaine. Validates that nomDomaine is not null before executing.
     */
    public void updateDomaine(DomaineCoaching domaine) throws SQLException {
        if (domaine.getNomDomaine() == null) {
            throw new SQLException(
                    "nom_domaine ne peut pas etre null. Valeurs acceptees: BRANDING, E_COMMERCE, LEADERSHIP, FINANCE, FUNDING");
        }
        String sql = "UPDATE domaine_coaching SET nom_domaine = ?, description = ? WHERE id_domaine = ?";
        PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(sql);
        pst.setString(1, domaine.getNomDomaine().toDbValue());
        pst.setString(2, domaine.getDescription());
        pst.setInt(3, domaine.getIdDomaine());
        pst.executeUpdate();
        System.out.println("Domaine mis a jour avec succes");
    }

    public List<DomaineCoaching> getData() throws SQLException {
        List<DomaineCoaching> data = new ArrayList<>();
        String sql = "SELECT * FROM domaine_coaching";
        Statement st = MyConnection.getInstance().getCnx().createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            DomaineCoaching d = new DomaineCoaching();
            d.setIdDomaine(rs.getInt("id_domaine"));
            // Parse DB ENUM string -> Java enum
            DomaineNom nom = DomaineNom.fromDbValueOrNull(rs.getString("nom_domaine"));
            d.setNomDomaine(nom);
            d.setDescription(rs.getString("description"));
            data.add(d);
        }
        return data;
    }

    public DomaineCoaching getByNomDomaine(String nomDomaine) throws SQLException {
        String sql = "SELECT * FROM domaine_coaching WHERE nom_domaine = ?";
        PreparedStatement ps = MyConnection.getInstance().getCnx().prepareStatement(sql);
        ps.setString(1, nomDomaine);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            DomaineCoaching d = new DomaineCoaching();
            // If you still have id_domaine keep it; if removed, ignore it
            // d.setIdDomaine(rs.getInt("id_domaine"));
            d.setNomDomaine(DomaineNom.valueOf(rs.getString("nom_domaine")));
            d.setDescription(rs.getString("description"));
            return d;
        }
        return null;
    }

}
