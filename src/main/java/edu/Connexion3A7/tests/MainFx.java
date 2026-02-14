package edu.Connexion3A7.tests;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainFx extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        try {
            // Start at the login screen — role-based routing happens after auth
            Parent root = FXMLLoader
                    .load(getClass().getResource("/edu/Connexion3A7/Controller/login.fxml"));
            Scene scene = new Scene(root);
            stage.setTitle("Bizcore - Admin Panel");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.out.println("Erreur chargement login: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
