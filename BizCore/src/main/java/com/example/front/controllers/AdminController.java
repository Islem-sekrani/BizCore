package com.example.front.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class AdminController {

    @FXML
    private Label currentModuleLabel;

    @FXML
    private StackPane contentArea;

    @FXML
    private Button btnUsers;
    @FXML
    private Button btnEvents;
    @FXML
    private Button btnCoaching;
    @FXML
    private Button btnBlog;
    @FXML
    private Button btnProducts;

    @FXML
    private void handleModuleNavigation(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String moduleName = clickedButton.getText();
        currentModuleLabel.setText(moduleName);

        String fxmlFile = "/com/example/front/GenericTable.fxml"; // Default

        if (clickedButton == btnCoaching) {
            fxmlFile = "/com/example/front/CoachingAdminView.fxml";
        }
        // Ajouter d'autres conditions ici pour les autres modules plus tard

        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource(fxmlFile));
            javafx.scene.Parent moduleView = loader.load();
            contentArea.getChildren().setAll(moduleView);
        } catch (java.io.IOException e) {
            e.printStackTrace();
            contentArea.getChildren().add(new Label("Erreur de chargement : " + e.getMessage()));
        }
    }
}
