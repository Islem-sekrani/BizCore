package edu.Connexion3A7.entities;

import java.util.Objects;

public class user {
    private int id_user;
    private String email;
    private String mdp;
    private String role; // "ADMIN" or "USER"

    public user() {
    }

    public user(int id_user) {
        this.id_user = id_user;
    }

    public user(String email, String mdp) {
        this.email = email;
        this.mdp = mdp;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMdp() {
        return mdp;
    }

    public void setMdp(String mdp) {
        this.mdp = mdp;
    }

    public int getId_user() {
        return id_user;
    }

    public void setId_user(int id_user) {
        this.id_user = id_user;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }

    @Override
    public String toString() {
        return "user{" + "email=" + email + ", role=" + role + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass())
            return false;
        user user = (user) o;
        return Objects.equals(email, user.email) && Objects.equals(mdp, user.mdp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, mdp);
    }
}
