package com.example.front.models;

public class Product {
    private String name;
    private double price;
    private String imageUrl;
    private String description;

    public Product(String name, double price, String imageUrl, String description) {
        this.name = name;
        this.price = price;
        this.imageUrl = imageUrl;
        this.description = description;
    }

    private java.util.List<Comment> comments = new java.util.ArrayList<>();

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public java.util.List<Comment> getComments() {
        return comments;
    }

    public void addComment(Comment comment) {
        this.comments.add(comment);
    }
}
