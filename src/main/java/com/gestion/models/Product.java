package com.gestion.models;

public class Product {
    private int id;
    private String productName;
    private String category;
    private String price;
    private int stock;
    private int orders;

    public Product(int id, String productName, String category, String price, int stock, int orders) {
        this.id = id;
        this.productName = productName;
        this.category = category;
        this.price = price;
        this.stock = stock;
        this.orders = orders;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public int getOrders() { return orders; }
    public void setOrders(int orders) { this.orders = orders; }

    @Override
    public String toString() {
        return productName + " - " + price;
    }
}
