package edu.Connexion3A7.entities;

import java.util.Objects;

public class DomaineCoaching {

    private int idDomaine;
    private DomaineNom nomDomaine;
    private String description;

    public DomaineCoaching() {
    }

    public DomaineCoaching(int idDomaine, DomaineNom nomDomaine, String description) {
        this.idDomaine = idDomaine;
        this.nomDomaine = nomDomaine;
        this.description = description;
    }

    public int getIdDomaine() {
        return idDomaine;
    }

    public void setIdDomaine(int idDomaine) {
        this.idDomaine = idDomaine;
    }

    public DomaineNom getNomDomaine() {
        return nomDomaine;
    }

    public void setNomDomaine(DomaineNom nomDomaine) {
        this.nomDomaine = nomDomaine;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "DomaineCoaching{" +
                "idDomaine=" + idDomaine +
                ", nomDomaine=" + nomDomaine +
                ", description='" + description + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass())
            return false;
        DomaineCoaching that = (DomaineCoaching) o;
        return idDomaine == that.idDomaine && nomDomaine == that.nomDomaine
                && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idDomaine, nomDomaine, description);
    }
}
