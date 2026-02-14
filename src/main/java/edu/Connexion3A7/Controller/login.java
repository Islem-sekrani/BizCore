package edu.Connexion3A7.Controller;

import edu.Connexion3A7.entities.user;
import edu.Connexion3A7.services.userService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.sql.SQLException;

public class login {

    @FXML
    private TextField email;

    @FXML
    private PasswordField mdp;

    @FXML
    private Label errorLabel;

    private final userService us = new userService();

    @FXML
    void handleLogin(ActionEvent event) {
        String emailText = email.getText();
        String mdpText = mdp.getText();

        if (emailText.isEmpty() || mdpText.isEmpty()) {
            errorLabel.setText("Veuillez remplir tous les champs.");
            return;
        }

        try {
            user authenticatedUser = us.authenticate(emailText, mdpText);

            if (authenticatedUser != null) {
                if (authenticatedUser.isAdmin()) {
                    // ADMIN → load admin dashboard
                    FXMLLoader loader = new FXMLLoader(
                            getClass().getResource("/edu/Connexion3A7/Controller/dashboard.fxml"));
                    Parent root = loader.load();

                    DashboardController dashCtrl = loader.getController();
                    dashCtrl.setLoggedInUserId(authenticatedUser.getId_user());
                    dashCtrl.setLoggedInUser(authenticatedUser);

                    email.getScene().setRoot(root);
                } else {
                    // USER → load user dashboard
                    FXMLLoader loader = new FXMLLoader(
                            getClass().getResource("/edu/Connexion3A7/Controller/userDashboard.fxml"));
                    Parent root = loader.load();

                    UserDashboardController userCtrl = loader.getController();
                    userCtrl.setLoggedInUser(authenticatedUser);

                    email.getScene().setRoot(root);
                }
            } else {
                errorLabel.setText("Email ou mot de passe incorrect.");
            }
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de connexion");
            alert.setHeaderText("Erreur base de données");
            alert.setContentText(e.getMessage());
            alert.show();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Erreur chargement interface");
            alert.setContentText(e.getMessage());
            alert.show();
        }
    }
}
