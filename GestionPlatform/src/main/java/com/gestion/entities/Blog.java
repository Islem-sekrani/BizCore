package com.gestion.entities;

public class Blog {
    private int id;
    private String title;
    private String author;
    private String category;
    private String publishDate;
    private String status;

    public Blog(int id, String title, String author, String category, String publishDate, String status) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.category = category;
        this.publishDate = publishDate;
        this.status = status;
    }

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

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(String publishDate) {
        this.publishDate = publishDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return title + " - " + author;
    }
}
