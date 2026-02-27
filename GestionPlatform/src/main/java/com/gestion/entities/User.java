package com.gestion.entities;

import java.sql.Timestamp;

public class User {

    private int idUser;                 // maps to id_user
    private String nom;                 // maps to nom
    private String prenom;              // maps to prenom
    private String email;               // maps to email
    private String password;            // maps to password
    private String telephone;           // maps to telephone
    private String adresse;             // maps to adresse
    private String statut;              // maps to statut
    private int idRole;                 // maps to id_role
    private Timestamp dateInscription;  // maps to date_inscription
    private Timestamp derniereConnexion; // maps to derniere_connexion
    private String nomRole;

    // ✅ Empty constructor required for JavaFX or frameworks
    public User() {}

    // ✅ Constructor for creating a new user (without auto-generated fields)
    public User(String nom, String prenom, String email, String password, int idRole) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.password = password;
        this.idRole = idRole;
        this.statut = "deconnecte"; // default statut
    }

    // ✅ Full constructor
    public User(int idUser, String nom, String prenom, String email, String password,
                String telephone, String adresse, String statut, int idRole,
                Timestamp dateInscription, Timestamp derniereConnexion) {
        this.idUser = idUser;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.password = password;
        this.telephone = telephone;
        this.adresse = adresse;
        this.statut = statut;
        this.idRole = idRole;
        this.dateInscription = dateInscription;
        this.derniereConnexion = derniereConnexion;
    }

    // ✅ Getters and Setters
    public int getIdUser() { return idUser; }
    public void setIdUser(int idUser) { this.idUser = idUser; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public int getIdRole() { return idRole; }
    public void setIdRole(int idRole) { this.idRole = idRole; }

    public Timestamp getDateInscription() { return dateInscription; }
    public void setDateInscription(Timestamp dateInscription) { this.dateInscription = dateInscription; }

    public Timestamp getDerniereConnexion() { return derniereConnexion; }
    public void setDerniereConnexion(Timestamp derniereConnexion) { this.derniereConnexion = derniereConnexion; }

    public String getNomRole() {
        return nomRole;
    }

    public void setNomRole(String nomRole) {
        this.nomRole = nomRole;
    }

    @Override
    public String toString() {
        return nom + " " + prenom + " (" + email + ")";
    }
}
