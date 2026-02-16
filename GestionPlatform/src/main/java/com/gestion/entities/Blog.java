package com.gestion.entities;

public class Blog {
    private int id;
    private String title;
    private String author;
    private String category;
    private String publishDate;
    private int views;

    public Blog(int id, String title, String author, String category, String publishDate, int views) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.category = category;
        this.publishDate = publishDate;
        this.views = views;
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

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }

    @Override
    public String toString() {
        return title + " par " + author;
    }
}
