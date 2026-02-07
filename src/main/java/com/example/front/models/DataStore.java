package com.example.front.models;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class DataStore {
    private static DataStore instance;
    private ObservableList<Product> products;
    private ObservableList<CoachingDomain> domains;
    private ObservableList<Coach> coaches;

    private DataStore() {
        products = FXCollections.observableArrayList();
        domains = FXCollections.observableArrayList();
        coaches = FXCollections.observableArrayList();

        // --- Mock Data: Products ---
        products.add(new Product("Formation JavaFX Complète", 120.0,
                "https://via.placeholder.com/200x150/1A2332/FFFFFF?text=JavaFX",
                "Maîtrisez JavaFX de A à Z avec ce cours intensif. [Formation]"));
        products.add(new Product("Template Site E-commerce", 250.0,
                "https://via.placeholder.com/200x150/17BB9C/FFFFFF?text=Web",
                "Site web moderne prêt à l'emploi pour votre boutique. [Site Web]"));
        products.add(new Product("Logiciel de Gestion de Stock", 500.0,
                "https://via.placeholder.com/200x150/2C3E50/FFFFFF?text=Logiciel",
                "Solution performante pour gérer vos stocks. [Logiciel]"));
        products.add(new Product("App Mobile Fitness", 350.0,
                "https://via.placeholder.com/200x150/e74c3c/FFFFFF?text=Mobile",
                "Code source complet application Android/iOS. [Application Mobile]"));

        // --- Mock Data: Coaching Domains ---
        CoachingDomain devPerso = new CoachingDomain(1, "Développement Personnel",
                "Améliorer la confiance en soi et la productivité", "🌱");
        CoachingDomain business = new CoachingDomain(2, "Business & Startup", "Lancer et gérer une entreprise", "💼");
        CoachingDomain tech = new CoachingDomain(3, "Technologie & Code", "Apprendre à coder et gérer des projets IT",
                "💻");

        domains.addAll(devPerso, business, tech);

        // --- Mock Data: Coaches ---
        coaches.add(
                new Coach(1, "Sophie Martin", "Experte en leadership féminin", 10, 150.0, "Lundi-Vendredi", business));
        coaches.add(new Coach(2, "Thomas Dubois", "Senior Java Developer & Mentor", 8, 120.0, "Weekends", tech));
        coaches.add(new Coach(3, "Amira Ben Ali", "Coach de vie certifiée", 5, 90.0, "Soirée", devPerso));
    }

    public static synchronized DataStore getInstance() {
        if (instance == null) {
            instance = new DataStore();
        }
        return instance;
    }

    public ObservableList<Product> getProducts() {
        return products;
    }

    public void addProduct(Product product) {
        products.add(product);
    }

    public ObservableList<CoachingDomain> getDomains() {
        return domains;
    }

    public void addDomain(CoachingDomain domain) {
        domains.add(domain);
    }

    public void removeDomain(CoachingDomain domain) {
        domains.remove(domain);
    }

    public ObservableList<Coach> getCoaches() {
        return coaches;
    }

    public void addCoach(Coach coach) {
        coaches.add(coach);
    }

    public void removeCoach(Coach coach) {
        coaches.remove(coach);
    }
}
