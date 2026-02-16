package com.gestion;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp {

    // Méthode générique pour ouvrir un FXML et retourner le controller
    public static <T> T open(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource(fxmlPath));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle(title);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait(); // attend la fermeture
            return loader.getController(); // retourne le controller pour récupérer l'événement créé
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
