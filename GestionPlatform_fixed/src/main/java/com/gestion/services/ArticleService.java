package com.gestion.services;

import com.gestion.entities.Article;
import com.gestion.interfaces.IArticleService;
import com.gestion.tools.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ArticleService implements IArticleService {

    @Override
    public List<Article> getAllArticles() {
        List<Article> articles = new ArrayList<>();
        String query = "SELECT * FROM article";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Article article = new Article(
                        rs.getInt("id_article"),
                        rs.getString("titre"),
                        rs.getString("contenu"),
                        rs.getString("image_principale"),
                        rs.getString("categorie"),
                        rs.getString("statut"),
                        rs.getInt("nombre_vues"));
                articles.add(article);
            }

            System.out.println("✅ " + articles.size() + " articles chargés depuis la base de données");

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération des articles: " + e.getMessage());
            e.printStackTrace();
        }

        return articles;
    }

    @Override
    public boolean addArticle(Article article) {
        String query = "INSERT INTO article (titre, contenu, image_principale, categorie, statut, nombre_vues) " +
                       "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, article.getTitre());
            pstmt.setString(2, article.getContenu());
            pstmt.setString(3, article.getImagePrincipale());
            pstmt.setString(4, article.getCategorie());
            pstmt.setString(5, article.getStatut());
            pstmt.setInt(6, article.getNombreVues());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    article.setIdArticle(generatedKeys.getInt(1));
                }
                System.out.println("✅ Article ajouté à la base de données: " + article.getTitre());
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'ajout de l'article: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean updateArticle(Article article) {
        String query = "UPDATE article SET titre = ?, contenu = ?, image_principale = ?, " +
                       "categorie = ?, statut = ?, nombre_vues = ? WHERE id_article = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, article.getTitre());
            pstmt.setString(2, article.getContenu());
            pstmt.setString(3, article.getImagePrincipale());
            pstmt.setString(4, article.getCategorie());
            pstmt.setString(5, article.getStatut());
            pstmt.setInt(6, article.getNombreVues());
            pstmt.setInt(7, article.getIdArticle());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✅ Article modifié dans la base de données: " + article.getTitre());
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la modification de l'article: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean deleteArticle(int idArticle) {
        String query = "DELETE FROM article WHERE id_article = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, idArticle);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✅ Article supprimé de la base de données (ID: " + idArticle + ")");
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la suppression de l'article: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public Article getArticleById(int idArticle) {
        String query = "SELECT * FROM article WHERE id_article = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, idArticle);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Article(
                        rs.getInt("id_article"),
                        rs.getString("titre"),
                        rs.getString("contenu"),
                        rs.getString("image_principale"),
                        rs.getString("categorie"),
                        rs.getString("statut"),
                        rs.getInt("nombre_vues"));
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération de l'article: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }
}
