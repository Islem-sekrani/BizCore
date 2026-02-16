package com.gestion.services;

import com.gestion.entities.Product;
import com.gestion.interfaces.IProductService;
import com.gestion.tools.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductService implements IProductService {

    @Override
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String query = "SELECT * FROM produit";

        try (Connection conn = DatabaseConnection.getInstance().getCnx();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Product product = new Product(
                        rs.getInt("id_produit"),
                        rs.getString("nom_produit"),
                        rs.getString("description"),
                        rs.getString("prix"),
                        rs.getInt("stock_disponible"),
                        rs.getString("categorie"),
                        rs.getString("image_url"),
                        rs.getString("statut"));
                products.add(product);
            }

            System.out.println("✅ " + products.size() + " produits chargés");

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return products;
    }

    @Override
    public boolean addProduct(Product product) {

        String query = "INSERT INTO produit (nom_produit, description, prix, stock_disponible, categorie, image_url, statut) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getCnx();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, product.getNomProduit());
            pstmt.setString(2, product.getDescription());
            pstmt.setString(3, product.getPrix());
            pstmt.setInt(4, product.getStockDisponible());
            pstmt.setString(5, product.getCategorie());
            pstmt.setString(6, product.getImageUrl());
            pstmt.setString(7, product.getStatut());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    product.setIdProduit(generatedKeys.getInt(1));
                }
                System.out.println("✅ Produit ajouté: " + product.getNomProduit());
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean updateProduct(Product product) {

        String query = "UPDATE produit SET nom_produit=?, description=?, prix=?, stock_disponible=?, categorie=?, image_url=?, statut=? WHERE id_produit=?";

        try (Connection conn = DatabaseConnection.getInstance().getCnx();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, product.getNomProduit());
            pstmt.setString(2, product.getDescription());
            pstmt.setString(3, product.getPrix());
            pstmt.setInt(4, product.getStockDisponible());
            pstmt.setString(5, product.getCategorie());
            pstmt.setString(6, product.getImageUrl());
            pstmt.setString(7, product.getStatut());
            pstmt.setInt(8, product.getIdProduit());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean deleteProduct(int idProduit) {

        String query = "DELETE FROM produit WHERE id_produit=?";

        try (Connection conn = DatabaseConnection.getInstance().getCnx();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, idProduit);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public Product getProductById(int idProduit) {

        String query = "SELECT * FROM produit WHERE id_produit=?";

        try (Connection conn = DatabaseConnection.getInstance().getCnx();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, idProduit);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Product(
                        rs.getInt("id_produit"),
                        rs.getString("nom_produit"),
                        rs.getString("description"),
                        rs.getString("prix"),
                        rs.getInt("stock_disponible"),
                        rs.getString("categorie"),
                        rs.getString("image_url"),
                        rs.getString("statut"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}
