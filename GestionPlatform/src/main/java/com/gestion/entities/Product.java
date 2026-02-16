package com.gestion.entities;

public class Product {

    private int idProduit;
    private String nomProduit;
    private String description;
    private String prix;
    private int stockDisponible;
    private String categorie;
    private String imageUrl;
    private String statut;

    // ✅ CONSTRUCTEUR VIDE (OBLIGATOIRE POUR JAVAFX)
    public Product() {
    }

    // ✅ CONSTRUCTEUR COMPLET
    public Product(int idProduit, String nomProduit, String description,
            String prix, int stockDisponible,
            String categorie, String imageUrl, String statut) {

        this.idProduit = idProduit;
        this.nomProduit = nomProduit;
        this.description = description;
        this.prix = prix;
        this.stockDisponible = stockDisponible;
        this.categorie = categorie;
        this.imageUrl = imageUrl;
        this.statut = statut;
    }

    // ✅ GETTERS (UTILISÉS PAR TABLEVIEW)
    public int getIdProduit() {
        return idProduit;
    }

    public String getNomProduit() {
        return nomProduit;
    }

    public String getDescription() {
        return description;
    }

    public String getPrix() {
        return prix;
    }

    public int getStockDisponible() {
        return stockDisponible;
    }

    public String getCategorie() {
        return categorie;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getStatut() {
        return statut;
    }

    // ✅ SETTERS
    public void setIdProduit(int idProduit) {
        this.idProduit = idProduit;
    }

    public void setNomProduit(String nomProduit) {
        this.nomProduit = nomProduit;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPrix(String prix) {
        this.prix = prix;
    }

    public void setStockDisponible(int stockDisponible) {
        this.stockDisponible = stockDisponible;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    @Override
    public String toString() {
        return nomProduit + " - " + prix + " (" + statut + ")";
    }
}
