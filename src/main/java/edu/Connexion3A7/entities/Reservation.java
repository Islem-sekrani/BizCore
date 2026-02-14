package edu.Connexion3A7.entities;

import java.time.LocalDateTime;
import java.util.Objects;

public class Reservation {
    private int idReservation;
    private int idUser;
    private int idCoach;
    private LocalDateTime dateReservation;
    private String statut; // CONFIRMEE, ANNULEE

    public Reservation() {
    }

    public Reservation(int idUser, int idCoach) {
        this.idUser = idUser;
        this.idCoach = idCoach;
        this.dateReservation = LocalDateTime.now();
        this.statut = "CONFIRMEE";
    }

    public int getIdReservation() {
        return idReservation;
    }

    public void setIdReservation(int idReservation) {
        this.idReservation = idReservation;
    }

    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    public int getIdCoach() {
        return idCoach;
    }

    public void setIdCoach(int idCoach) {
        this.idCoach = idCoach;
    }

    public LocalDateTime getDateReservation() {
        return dateReservation;
    }

    public void setDateReservation(LocalDateTime dateReservation) {
        this.dateReservation = dateReservation;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "idReservation=" + idReservation +
                ", idUser=" + idUser +
                ", idCoach=" + idCoach +
                ", dateReservation=" + dateReservation +
                ", statut='" + statut + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass())
            return false;
        Reservation that = (Reservation) o;
        return idUser == that.idUser && idCoach == that.idCoach;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idUser, idCoach);
    }
}
