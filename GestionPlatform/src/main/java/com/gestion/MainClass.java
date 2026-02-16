package com.gestion;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Classe principale de l'application GestionPlatform
 * Utilise désormais l'architecture MVC avec contrôleurs séparés.
 */
public class MainClass extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Charger le FXML principal qui contient son propre contrôleur
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainDashboard.fxml"));
            Parent root = loader.load();

            // Configurer la scène
            Scene scene = new Scene(root, 1400, 800);

            primaryStage.setTitle("Admin Panel - Gestion Platform");
            primaryStage.setScene(scene);
            primaryStage.setMaximized(true);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Erreur fatale lors du démarrage de l'application: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
