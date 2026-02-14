package edu.Connexion3A7.entities;

import java.util.Objects;

public class coach {

    private int id_coach;
    private int id_user;
    private String domaine;
    private String nom;
    private String prenom;
    private String biographie;
    private int experience;
    private float tarif;
    private String dispo;
    private String certif;
    private float note;

    public coach() {
    }

    public coach(int id_coach, int id_user, String domaine, String nom, String prenom, String biographie, int experience, float tarif, String dispo, String certif, float note) {
        this.id_coach = id_coach;
        this.id_user = id_user;
        this.domaine = domaine;
        this.nom = nom;
        this.prenom = prenom;
        this.biographie = biographie;
        this.experience = experience;
        this.tarif = tarif;
        this.dispo = dispo;
        this.certif = certif;
        this.note = note;
    }

    public int getId_coach() {
        return id_coach;
    }

    public void setId_coach(int id_coach) {
        this.id_coach = id_coach;
    }

    public int getId_user() {
        return id_user;
    }

    public void setId_user(int id_user) {
        this.id_user = id_user;
    }

    public String getDomaine() {
        return domaine;
    }

    public void setDomaine(String domaine) {
        this.domaine = domaine;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getBiographie() {
        return biographie;
    }

    public void setBiographie(String biographie) {
        this.biographie = biographie;
    }

    public int getExperience() {
        return experience;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    public float getTarif() {
        return tarif;
    }

    public void setTarif(float tarif) {
        this.tarif = tarif;
    }

    public String getDispo() {
        return dispo;
    }

    public void setDispo(String dispo) {
        this.dispo = dispo;
    }

    public String getCertif() {
        return certif;
    }

    public void setCertif(String certif) {
        this.certif = certif;
    }

    public float getNote() {
        return note;
    }

    public void setNote(float note) {
        this.note = note;
    }

    @Override
    public String toString() {
        return "coach{" +
                "id_coach=" + id_coach +
                ", id_user=" + id_user +
                ", id_domaine=" + domaine +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", biographie='" + biographie + '\'' +
                ", experience=" + experience +
                ", tarif=" + tarif +
                ", dispo='" + dispo + '\'' +
                ", certif='" + certif + '\'' +
                ", note=" + note +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        coach coach = (coach) o;
        return id_coach == coach.id_coach && id_user == coach.id_user && domaine == coach.domaine && experience == coach.experience && Float.compare(tarif, coach.tarif) == 0 && Float.compare(note, coach.note) == 0 && Objects.equals(nom, coach.nom) && Objects.equals(prenom, coach.prenom) && Objects.equals(biographie, coach.biographie) && Objects.equals(dispo, coach.dispo) && Objects.equals(certif, coach.certif);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id_coach, id_user, domaine, nom, prenom, biographie, experience, tarif, dispo, certif, note);
    }
}
