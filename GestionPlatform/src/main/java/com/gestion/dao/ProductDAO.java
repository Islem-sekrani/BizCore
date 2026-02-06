package com.gestion.dao;

import com.gestion.models.Product;
import com.gestion.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Product operations
 */
public class ProductDAO {

    /**
     * Get all products from the database
     * 
     * @return List of all products
     */
    public static List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String query = "SELECT * FROM produit";

        try (Connection conn = DatabaseConnection.getConnection();
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

            System.out.println("✅ " + products.size() + " produits chargés depuis la base de données");

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération des produits: " + e.getMessage());
            e.printStackTrace();
        }

        return products;
    }

    /**
     * Add a new product to the database
     * 
     * @param product Product to add
     * @return true if successful, false otherwise
     */
    public static boolean addProduct(Product product) {
        String query = "INSERT INTO produit (nom_produit, description, prix, stock_disponible, categorie, image_url, statut) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
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
                // Get the generated ID
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    product.setIdProduit(generatedKeys.getInt(1));
                }
                System.out.println("✅ Produit ajouté à la base de données: " + product.getNomProduit());
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'ajout du produit: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Update an existing product in the database
     * 
     * @param product Product to update
     * @return true if successful, false otherwise
     */
    public static boolean updateProduct(Product product) {
        String query = "UPDATE produit SET nom_produit = ?, description = ?, prix = ?, stock_disponible = ?, categorie = ?, image_url = ?, statut = ? WHERE id_produit = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, product.getNomProduit());
            pstmt.setString(2, product.getDescription());
            pstmt.setString(3, product.getPrix());
            pstmt.setInt(4, product.getStockDisponible());
            pstmt.setString(5, product.getCategorie());
            pstmt.setString(6, product.getImageUrl());
            pstmt.setString(7, product.getStatut());
            pstmt.setInt(8, product.getIdProduit());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✅ Produit modifié dans la base de données: " + product.getNomProduit());
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la modification du produit: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Delete a product from the database
     * 
     * @param idProduit ID of the product to delete
     * @return true if successful, false otherwise
     */
    public static boolean deleteProduct(int idProduit) {
        String query = "DELETE FROM produit WHERE id_produit = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, idProduit);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✅ Produit supprimé de la base de données (ID: " + idProduit + ")");
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la suppression du produit: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Get a product by ID
     * 
     * @param idProduit ID of the product
     * @return Product object or null if not found
     */
    public static Product getProductById(int idProduit) {
        String query = "SELECT * FROM produit WHERE id_produit = ?";

        try (Connection conn = DatabaseConnection.getConnection();
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
            System.err.println("❌ Erreur lors de la récupération du produit: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }
}
