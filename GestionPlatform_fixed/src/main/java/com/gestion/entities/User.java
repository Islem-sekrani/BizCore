package com.gestion.entities;

public class User {
    private int id;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
    private String lastUpdate;

    // Full constructor
    public User(int id, String firstName, String lastName, String email, String role, String lastUpdate) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.role = role;
        this.lastUpdate = lastUpdate;
    }

    // Overloaded constructor without lastUpdate (sets it to current timestamp)
    public User(int id, String firstName, String lastName, String email, String role) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.role = role;
        this.lastUpdate = java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        );
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    // Compatibility getter for TableView PropertyValueFactory
    public int getIdUser() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    // Compatibility getters for TableView PropertyValueFactory (French property names)
    public String getNom() {
        return lastName;
    }

    public String getPrenom() {
        return firstName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    @Override
    public String toString() {
        return firstName + " " + lastName;
    }
}