package com.gestion.controllers;

import com.gestion.entities.Article;
import com.gestion.services.ArticleService;
import com.gestion.interfaces.IArticleService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.stream.Collectors;

public class ArticleController {

    private final IArticleService articleService;

    public ArticleController() {
        this.articleService = new ArticleService();
    }

    // =========================================================================
    //  CRUD OPERATIONS
    // =========================================================================

    public boolean addArticle(Article article) {
        return articleService.addArticle(article);
    }

    public boolean updateArticle(Article article) {
        return articleService.updateArticle(article);
    }

    public boolean deleteArticle(int idArticle) {
        return articleService.deleteArticle(idArticle);
    }

    public Article getArticleById(int idArticle) {
        return articleService.getArticleById(idArticle);
    }

    public ObservableList<Article> getAllArticles() {
        List<Article> articles = articleService.getAllArticles();
        return FXCollections.observableArrayList(articles);
    }

    // =========================================================================
    //  FILTERING — by titre, categorie, statut
    // =========================================================================

    public ObservableList<Article> filterArticles(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            return getAllArticles();
        }

        String keyword = searchText.toLowerCase().trim();

        List<Article> filtered = articleService.getAllArticles().stream()
                .filter(a ->
                        (a.getTitre()     != null && a.getTitre().toLowerCase().contains(keyword))     ||
                        (a.getCategorie() != null && a.getCategorie().toLowerCase().contains(keyword)) ||
                        (a.getStatut()    != null && a.getStatut().toLowerCase().contains(keyword)))
                .collect(Collectors.toList());

        System.out.println("🔍 Filtre '" + searchText + "' → " + filtered.size() + " article(s) trouvé(s)");
        return FXCollections.observableArrayList(filtered);
    }

    // =========================================================================
    //  SORTING — by Vues and Titre
    // =========================================================================

    public ObservableList<Article> sortArticles(ObservableList<Article> list, String sortOption) {
        if (sortOption == null || list == null) return list;

        List<Article> sorted;

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
}
