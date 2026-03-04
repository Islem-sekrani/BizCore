package com.gestion.entities;

public class Coaching {
    private int id;
    private String coachName;
    private String specialty;
    private String clientName;
    private String nextSession;
    private String status;

    public Coaching(int id, String coachName, String specialty, String clientName, String nextSession, String status) {
        this.id = id;
        this.coachName = coachName;
        this.specialty = specialty;
        this.clientName = clientName;
        this.nextSession = nextSession;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCoachName() {
        return coachName;
    }

    public void setCoachName(String coachName) {
        this.coachName = coachName;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getNextSession() {
        return nextSession;
    }

    public void setNextSession(String nextSession) {
        this.nextSession = nextSession;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return coachName + " - " + clientName;
    }
}
