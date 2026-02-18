package com.example.front.models;

public class Coach {
    private int id;
    private String name;
    private String bio;
    private int yearsExperience;
    private double hourlyRate;
    private String availability;

    // Relation ManyToOne
    private CoachingDomain domain;

    public Coach(int id, String name, String bio, int yearsExperience, double hourlyRate, String availability,
            CoachingDomain domain) {
        this.id = id;
        this.name = name;
        this.bio = bio;
        this.yearsExperience = yearsExperience;
        this.hourlyRate = hourlyRate;
        this.availability = availability;
        this.domain = domain;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public int getYearsExperience() {
        return yearsExperience;
    }

    public void setYearsExperience(int yearsExperience) {
        this.yearsExperience = yearsExperience;
    }

    public double getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(double hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public String getAvailability() {
        return availability;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }

    public CoachingDomain getDomain() {
        return domain;
    }

    public void setDomain(CoachingDomain domain) {
        this.domain = domain;
    }

    // Helper pour afficher le nom du domaine dans les tables
    public String getDomainName() {
        return domain != null ? domain.getName() : "Aucun"; // Protection null-safe
    }
}
