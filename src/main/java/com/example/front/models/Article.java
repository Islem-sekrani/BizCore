package com.example.front.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Article {
    private int id;
    private String title;
    private String content;
    private String author;
    private LocalDateTime publishedDate;
    private String imageUrl;

    // Relation One-to-Many: Un article peut avoir plusieurs commentaires
    private List<Comment> comments;

    public Article(int id, String title, String content, String author, String imageUrl) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.author = author;
        this.publishedDate = LocalDateTime.now();
        this.imageUrl = imageUrl;
        this.comments = new ArrayList<>();
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public LocalDateTime getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(LocalDateTime publishedDate) {
        this.publishedDate = publishedDate;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void addComment(Comment comment) {
        if (this.comments == null) {
            this.comments = new ArrayList<>();
        }
        this.comments.add(comment);
        // Assurer la cohérence bidirectionnelle si nécessaire
        if (comment.getArticle() != this) {
            comment.setArticle(this);
        }
    }
}
