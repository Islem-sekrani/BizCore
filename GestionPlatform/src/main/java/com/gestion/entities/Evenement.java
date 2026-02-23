package com.gestion.entities;

import java.time.LocalDateTime;

public class Evenement {

    private int idEvenement;
    private String titre;
    private String description;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private String lieu;
    private int capacite;
    private double prix;
    private String imageUrl;
    private String statut;
    private int idOrganisateur;
    private int idCategorie;

    // 🔹 Constructeur vide
    public Evenement() {}

    // 🔹 Constructeur sans id (pour ajout)
    public Evenement(String titre, String description, LocalDateTime dateDebut,
                     LocalDateTime dateFin, String lieu, int capacite,
                     double prix, String imageUrl, String statut,
                     int idOrganisateur, int idCategorie) {

        this.titre = titre;
        this.description = description;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.lieu = lieu;
        this.capacite = capacite;
        this.prix = prix;
        this.imageUrl = imageUrl;
        this.statut = statut;
        this.idOrganisateur = idOrganisateur;
        this.idCategorie = idCategorie;
    }

    public Evenement(String text, String text1, String text2, int i, double v, String value, LocalDateTime localDateTime, LocalDateTime localDateTime1) {
    }

    // 🔹 Getters & Setters

    public int getIdEvenement() { return idEvenement; }
    public void setIdEvenement(int idEvenement) { this.idEvenement = idEvenement; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDateTime dateDebut) { this.dateDebut = dateDebut; }

    public LocalDateTime getDateFin() { return dateFin; }
    public void setDateFin(LocalDateTime dateFin) { this.dateFin = dateFin; }

    public String getLieu() { return lieu; }
    public void setLieu(String lieu) { this.lieu = lieu; }

    public int getCapacite() { return capacite; }
    public void setCapacite(int capacite) { this.capacite = capacite; }

    public double getPrix() { return prix; }
    public void setPrix(double prix) { this.prix = prix; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public int getIdOrganisateur() { return idOrganisateur; }
    public void setIdOrganisateur(int idOrganisateur) { this.idOrganisateur = idOrganisateur; }

    public int getIdCategorie() { return idCategorie; }
    public void setIdCategorie(int idCategorie) { this.idCategorie = idCategorie; }
}
