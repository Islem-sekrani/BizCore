package com.gestion.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainDashboardController implements Initializable {

    @FXML private VBox sidebarMenu;
    @FXML private Button btnUtilisateurs;
    @FXML private Button btnEvenements;
    @FXML private Button btnCoaching;
    @FXML private Button btnBlog;
    @FXML private Button btnProduits;
    @FXML private AnchorPane contentArea;
    @FXML private Label lblTitle;
    @FXML private Label lblUserName;

    private String currentModule = "UTILISATEURS";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Charger le module par défaut
        loadModule("UTILISATEURS");
        lblUserName.setText("Admin User");
    }

    @FXML
    private void handleUtilisateurs() {
        loadModule("UTILISATEURS");
        updateActiveButton(btnUtilisateurs);
    }

    @FXML
    private void handleEvenements() {
        loadModule("EVENEMENTS");
        updateActiveButton(btnEvenements);
    }

    @FXML
    private void handleCoaching() {
        loadModule("COACHING");
        updateActiveButton(btnCoaching);
    }

    @FXML
    private void handleBlog() {
        loadModule("BLOG");
        updateActiveButton(btnBlog);
    }

    @FXML
    private void handleProduits() {
        loadModule("PRODUITS");
        updateActiveButton(btnProduits);
    }

    private void loadModule(String moduleName) {
        try {
            currentModule = moduleName;
            lblTitle.setText("GESTION " + moduleName);
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ModuleView.fxml"));
            Parent moduleView = loader.load();
            
            ModuleViewController controller = loader.getController();
            controller.setModuleType(moduleName);
            
            contentArea.getChildren().clear();
            contentArea.getChildren().add(moduleView);
            
            AnchorPane.setTopAnchor(moduleView, 0.0);
            AnchorPane.setBottomAnchor(moduleView, 0.0);
            AnchorPane.setLeftAnchor(moduleView, 0.0);
            AnchorPane.setRightAnchor(moduleView, 0.0);
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger le module: " + moduleName);
        }
    }

    private void updateActiveButton(Button activeButton) {
        // Retirer la classe active de tous les boutons
        btnUtilisateurs.getStyleClass().remove("active-menu-btn");
        btnEvenements.getStyleClass().remove("active-menu-btn");
        btnCoaching.getStyleClass().remove("active-menu-btn");
        btnBlog.getStyleClass().remove("active-menu-btn");
        btnProduits.getStyleClass().remove("active-menu-btn");
        
        // Ajouter la classe active au bouton cliqué
        activeButton.getStyleClass().add("active-menu-btn");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
