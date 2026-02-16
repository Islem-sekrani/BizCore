package com.gestion.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.Parent;
import javafx.fxml.FXMLLoader;
import java.io.IOException;

public class MainDashboardController {

    @FXML
    private Label lblTitle;

    @FXML
    private Label lblUserName;

    @FXML
    private AnchorPane contentArea;

    @FXML
    private VBox sidebarMenu;

    @FXML
    private Button btnUtilisateurs;

    @FXML
    private Button btnEvenements;

    @FXML
    private Button btnCoaching;

    @FXML
    private Button btnBlog;

    @FXML
    private Button btnProduits;

    @FXML
    public void initialize() {
        if (lblUserName != null) {
            lblUserName.setText("Admin User");
        }

        setupMenuButtons();
    }

    private void setupMenuButtons() {
        btnUtilisateurs.setOnAction(e -> loadModule("UTILISATEURS", btnUtilisateurs));
        btnEvenements.setOnAction(e -> loadModule("EVENEMENTS", btnEvenements));
        btnCoaching.setOnAction(e -> loadModule("COACHING", btnCoaching));
        btnBlog.setOnAction(e -> loadModule("BLOG", btnBlog));
        btnProduits.setOnAction(e -> loadModule("PRODUITS", btnProduits));
    }

    private void loadModule(String moduleName, Button activeButton) {
        try {
            if (lblTitle != null) {
                lblTitle.setText("GESTION " + moduleName);
            }

            // Update active button style
            updateActiveButton(activeButton);

            // Load module FXML without controller
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ModuleView.fxml"));
            Parent moduleView = loader.load();

            // Clear and add to content area
            if (contentArea != null) {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(moduleView);

                AnchorPane.setTopAnchor(moduleView, 0.0);
                AnchorPane.setBottomAnchor(moduleView, 0.0);
                AnchorPane.setLeftAnchor(moduleView, 0.0);
                AnchorPane.setRightAnchor(moduleView, 0.0);
            }

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur de chargement", "Impossible de charger le module: " + moduleName);
        }
    }

    private void showError(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void updateActiveButton(Button activeButton) {
        Button[] buttons = { btnUtilisateurs, btnEvenements, btnCoaching, btnBlog, btnProduits };
        for (Button btn : buttons) {
            if (btn != null) {
                btn.getStyleClass().remove("active-menu-btn");
            }
        }
        if (activeButton != null) {
            activeButton.getStyleClass().add("active-menu-btn");
        }
    }

    public void setContent(Parent view) {
        if (contentArea != null) {
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
            AnchorPane.setTopAnchor(view, 0.0);
            AnchorPane.setBottomAnchor(view, 0.0);
            AnchorPane.setLeftAnchor(view, 0.0);
            AnchorPane.setRightAnchor(view, 0.0);
        }
    }

    public void setTitle(String title) {
        if (lblTitle != null) {
            lblTitle.setText(title);
        }
    }
}
