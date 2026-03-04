package com.gestion.entities;

public class Article {

    private int idArticle;
    private String titre;
    private String contenu;
    private String imagePrincipale;
    private String categorie;
    private String statut;
    private int nombreVues;

    public Article() {
    }

    public Article(int idArticle, String titre, String contenu, String imagePrincipale,
                   String categorie, String statut, int nombreVues) {
        this.idArticle = idArticle;
        this.titre = titre;
        this.contenu = contenu;
        this.imagePrincipale = imagePrincipale;
        this.categorie = categorie;
        this.statut = statut;
        this.nombreVues = nombreVues;
    }

    public int getIdArticle() {
        return idArticle;
    }

    public String getTitre() {
        return titre;
    }

    public String getContenu() {
        return contenu;
    }

    public String getImagePrincipale() {
        return imagePrincipale;
    }

    public String getCategorie() {
        return categorie;
    }

    public String getStatut() {
        return statut;
    }

    public int getNombreVues() {
        return nombreVues;
    }

    public void setIdArticle(int idArticle) {
        this.idArticle = idArticle;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public void setImagePrincipale(String imagePrincipale) {
        this.imagePrincipale = imagePrincipale;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public void setNombreVues(int nombreVues) {
        this.nombreVues = nombreVues;
    }

    @Override
    public String toString() {
        return titre + " (" + categorie + ") - " + nombreVues + " vues";
    }
}
