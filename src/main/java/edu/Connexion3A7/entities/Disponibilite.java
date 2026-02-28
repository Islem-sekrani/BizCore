package edu.Connexion3A7.entities;

import java.time.LocalDate;
import java.util.Objects;

public class Disponibilite {
    private int idDispo;
    private int idCoach;
    private LocalDate jour;
    private String statut; // "Disponible" or "Indisponible"

    public Disponibilite() {}

    public Disponibilite(int idCoach, LocalDate jour, String statut) {
        this.idCoach = idCoach;
        this.jour = jour;
        this.statut = statut;
    }

    public int getIdDispo() { return idDispo; }
    public void setIdDispo(int idDispo) { this.idDispo = idDispo; }

    public int getIdCoach() { return idCoach; }
    public void setIdCoach(int idCoach) { this.idCoach = idCoach; }

    public LocalDate getJour() { return jour; }
    public void setJour(LocalDate jour) { this.jour = jour; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public boolean isDisponible() {
        return "Disponible".equalsIgnoreCase(statut);
    }

    @Override
    public String toString() {
        return "Disponibilite{idCoach=" + idCoach + ", jour=" + jour + ", statut='" + statut + "'}";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Disponibilite that = (Disponibilite) o;
        return idCoach == that.idCoach && Objects.equals(jour, that.jour);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idCoach, jour);
    }
}
