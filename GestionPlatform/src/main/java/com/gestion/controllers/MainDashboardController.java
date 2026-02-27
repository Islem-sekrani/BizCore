package com.gestion.controllers;

import com.gestion.entities.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

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

    private String currentModule;
    private User currentUser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        lblUserName.setText("Admin User");

        // Charger module par défaut
        loadModule("UTILISATEURS");
        updateActiveButton(btnUtilisateurs);
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

    // 🔥 MÉTHODE CORRIGÉE
    public void loadModule(String moduleName) {

        try {

            currentModule = moduleName;
            lblTitle.setText("GESTION " + moduleName);

            String fxmlPath;

            switch (moduleName) {

                case "UTILISATEURS":
                    fxmlPath = "/fxml/UtilisateursView.fxml";
                    break;

                case "EVENEMENTS":
                    fxmlPath = "/fxml/EvenementView.fxml";
                    break;

                case "COACHING":
                    fxmlPath = "/fxml/CoachingView.fxml";
                    break;

                case "BLOG":
                    fxmlPath = "/fxml/BlogView.fxml";
                    break;



                default:
                    throw new RuntimeException("Module inconnu : " + moduleName);
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent moduleView = loader.load();

            contentArea.getChildren().clear();
            contentArea.getChildren().add(moduleView);

            AnchorPane.setTopAnchor(moduleView, 0.0);
            AnchorPane.setBottomAnchor(moduleView, 0.0);
            AnchorPane.setLeftAnchor(moduleView, 0.0);
            AnchorPane.setRightAnchor(moduleView, 0.0);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger le module : " + moduleName);
        }
    }

    private void updateActiveButton(Button activeButton) {

        btnUtilisateurs.getStyleClass().remove("active-menu-btn");
        btnEvenements.getStyleClass().remove("active-menu-btn");
        btnCoaching.getStyleClass().remove("active-menu-btn");
        btnBlog.getStyleClass().remove("active-menu-btn");
        btnProduits.getStyleClass().remove("active-menu-btn");

        activeButton.getStyleClass().add("active-menu-btn");
    }

    private void showAlert(String title, String message) {

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }
}
