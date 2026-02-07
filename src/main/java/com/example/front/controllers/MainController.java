package com.example.front.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.geometry.Side;
import com.example.front.controllers.ArticleDetailController;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML
    private StackPane contentArea;

    @FXML
    private Button userButton;

    // Simulated login state
    private boolean isLoggedIn = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        showHome();
    }

    @FXML
    public void showHome() {
        loadView("/com/example/front/HomeView.fxml");
    }

    @FXML
    public void showOffers() {
        loadView("/com/example/front/OffersView.fxml");
    }

    @FXML
    public void showEvents() {
        loadView("/com/example/front/EventsView.fxml");
    }

    @FXML
    public void showCoaching() {
        loadView("/com/example/front/CoachingView.fxml");
    }

    @FXML
    public void showBlog() {
        loadView("/com/example/front/BlogView.fxml");
    }

    @FXML
    public void showUserMenu(MouseEvent event) {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem signInItem = new MenuItem("Sign In");
        signInItem.setOnAction(e -> {
            System.out.println("Sign In clicked");
            isLoggedIn = true;
            // logic to show login dialog
        });

        MenuItem signOutItem = new MenuItem("Sign Out");
        signOutItem.setOnAction(e -> {
            System.out.println("Sign Out clicked");
            isLoggedIn = false;
            // logic to logout
        });

        if (isLoggedIn) {
            contextMenu.getItems().add(signOutItem);
        } else {
            contextMenu.getItems().addAll(signInItem);
        }

        // Show the context menu below the user button
        contextMenu.show(userButton, Side.BOTTOM, 0, 0);
    }

    @FXML
    public void showArticleDetail() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/front/ArticleDetailView.fxml"));
            Parent view = fxmlLoader.load();

            // Create dummy data for demonstration
            com.example.front.models.Article demoArticle = new com.example.front.models.Article(
                    1,
                    "L'importance du Design UX dans les Applications Modernes",
                    "Le design d'expérience utilisateur (UX) est devenu un facteur différenciateur majeur...\\n\\nLorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.",
                    "Jean Dupont",
                    "https://via.placeholder.com/600x200");

            // Add some demo comments
            demoArticle.addComment(
                    new com.example.front.models.Comment(1, "Super article ! Très informatif.", "Alice", demoArticle));
            demoArticle.addComment(
                    new com.example.front.models.Comment(2, "Merci pour ces conseils précieux.", "Bob", demoArticle));

            ArticleDetailController controller = fxmlLoader.getController();
            controller.setArticleData(demoArticle);

            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void showSellerForm() {
        loadView("/com/example/front/SellerFormView.fxml");
    }

    @FXML
    public void showAdmin() {
        loadView("/com/example/front/AdminView.fxml");
    }

    // Helper method to pass the MainController to sub-controllers if needed for
    // navigation
    public void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();

            // If the controller needs reference to MainController for navigation
            Object controller = loader.getController();
            if (controller instanceof NavigationAware) {
                ((NavigationAware) controller).setMainController(this);
            }

            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
