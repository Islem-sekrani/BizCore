package com.example.front.models;

import java.time.LocalDateTime;

public class Comment {
    private int id;
    private String content;
    private String author;
    private LocalDateTime postedDate;

    // Relation Many-to-One: Un commentaire appartient à un seul article
    private Article article;
    private int articleId; // Utile pour la persistance base de données (Clé étrangère)

    public Comment(int id, String content, String author, Article article) {
        this.id = id;
        this.content = content;
        this.author = author;
        this.postedDate = LocalDateTime.now();
        this.setArticle(article);
    }

    // Constructeur simple pour Produits
    public Comment(int id, String content, String author) {
        this.id = id;
        this.content = content;
        this.author = author;
        this.postedDate = LocalDateTime.now();
    }

    // Constructeur alternatif avec ID d'article pour chargement DB
    public Comment(int id, String content, String author, int articleId) {
        this.id = id;
        this.content = content;
        this.author = author;
        this.postedDate = LocalDateTime.now();
        this.articleId = articleId;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public LocalDateTime getPostedDate() {
        return postedDate;
    }

    public void setPostedDate(LocalDateTime postedDate) {
        this.postedDate = postedDate;
    }

    public Article getArticle() {
        return article;
    }

    public void setArticle(Article article) {
        this.article = article;
        if (article != null) {
            this.articleId = article.getId();
        }
    }

    public int getArticleId() {
        return articleId;
    }

    public void setArticleId(int articleId) {
        this.articleId = articleId;
    }
}
