package com.gestion.controllers;

import com.gestion.analyzers.BlogAnalysisResult;
import com.gestion.analyzers.BlogAnalyzerService;
import com.gestion.analyzers.BlogTrendingService;
import com.gestion.analyzers.TrendingResult;
import com.gestion.entities.Blog;
import com.gestion.interfaces.IBlogService;
import com.gestion.services.BlogService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.stream.Collectors;

public class BlogController {

    private final IBlogService blogService;

    public BlogController() {
        this.blogService = new BlogService();
    }

    // =========================================================================
    //  CRUD — delegates to BlogService (table: article)
    // =========================================================================

    public boolean addBlog(Blog blog) {
        return blogService.addArticle(blog);
    }

    public boolean updateBlog(Blog blog) {
        return blogService.updateArticle(blog);
    }

    public boolean deleteBlog(int idArticle) {
        return blogService.deleteArticle(idArticle);
    }

    public Blog getBlogById(int idArticle) {
        return blogService.getArticleById(idArticle);
    }

    public ObservableList<Blog> getAllBlogs() {
        List<Blog> blogs = blogService.getAllArticles();
        System.out.println("✅ BlogController: " + blogs.size() + " article(s) chargé(s)");
        return FXCollections.observableArrayList(blogs);
    }

    // =========================================================================
    //  FILTERING — by titre, categorie, statut
    // =========================================================================

    public ObservableList<Blog> filterBlogs(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            return getAllBlogs();
        }

        String keyword = searchText.toLowerCase().trim();

        List<Blog> filtered = blogService.getAllArticles().stream()
                .filter(b ->
                        (b.getTitre()     != null && b.getTitre().toLowerCase().contains(keyword))     ||
                        (b.getCategorie() != null && b.getCategorie().toLowerCase().contains(keyword)) ||
                        (b.getStatut()    != null && b.getStatut().toLowerCase().contains(keyword)))
                .collect(Collectors.toList());

        System.out.println("🔍 Filtre '" + searchText + "' → " + filtered.size() + " article(s) trouvé(s)");
        return FXCollections.observableArrayList(filtered);
    }

    // =========================================================================
    //  SORTING — by Vues and Titre
    // =========================================================================

    public ObservableList<Blog> sortBlogs(ObservableList<Blog> list, String sortOption) {
        if (sortOption == null || list == null) return list;

        List<Blog> sorted;

        switch (sortOption) {
            case "Vues (Croissant)":
                sorted = list.stream()
                        .sorted((a, b) -> Integer.compare(a.getNombreVues(), b.getNombreVues()))
                        .collect(Collectors.toList());
                break;

            case "Vues (Décroissant)":
                sorted = list.stream()
                        .sorted((a, b) -> Integer.compare(b.getNombreVues(), a.getNombreVues()))
                        .collect(Collectors.toList());
                break;

            case "Titre (A-Z)":
                sorted = list.stream()
                        .sorted((a, b) -> {
                            String ta = a.getTitre() != null ? a.getTitre() : "";
                            String tb = b.getTitre() != null ? b.getTitre() : "";
                            return ta.compareToIgnoreCase(tb);
                        })
                        .collect(Collectors.toList());
                break;

            case "Titre (Z-A)":
                sorted = list.stream()
                        .sorted((a, b) -> {
                            String ta = a.getTitre() != null ? a.getTitre() : "";
                            String tb = b.getTitre() != null ? b.getTitre() : "";
                            return tb.compareToIgnoreCase(ta);
                        })
                        .collect(Collectors.toList());
                break;

            default:
                return list;
        }

        System.out.println("↕ Tri '" + sortOption + "' appliqué sur " + sorted.size() + " article(s)");
        return FXCollections.observableArrayList(sorted);
    }

    // =========================================================================
    //  CONTENT QUALITY ANALYSIS  —  POST /api/blogs/{id}/analyze
    //  Reads from DB via blogService. NEVER writes. Returns BlogAnalysisResult.
    // =========================================================================

    /**
     * Fetch the article by its database ID and run the internal quality algorithm.
     * Returns null if the article does not exist.
     * The database row is never modified.
     */
    public BlogAnalysisResult analyzeBlog(int idArticle) {
        Blog blog = blogService.getArticleById(idArticle);
        if (blog == null) {
            System.err.println("❌ analyzeBlog: article #" + idArticle + " introuvable.");
            return null;
        }
        return new BlogAnalyzerService().analyze(blog);
    }

    // =========================================================================
    //  TRENDING ALGORITHM  —  GET /api/blogs/trending
    //  Reads published articles, computes score dynamically, returns top 5.
    //  Score is NEVER stored in the database. No writes occur.
    // =========================================================================

    /**
     * Implements GET /api/blogs/trending.
     *
     * Formula: TrendingScore = (Views × 0.6) + (RecencyFactor × 0.4)
     *
     * RecencyFactor is derived from the article's age:
     *   0–3 days → very high,  4–10 days → medium,  11+ days → decreasing.
     * If the DB table has no created_at column, recency is estimated from
     * the id_article rank (higher ID = more recently inserted).
     *
     * @return top-5 list of TrendingResult, sorted by score descending
     */
    public List<TrendingResult> getTrendingArticles() {
        List<TrendingResult> results = new BlogTrendingService().getTopTrending();
        System.out.println("🔥 getTrendingArticles → " + results.size() + " article(s) trending");
        return results;
    }
}
