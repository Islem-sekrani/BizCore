package edu.Connexion3A7.Controller;

import edu.Connexion3A7.entities.coach;
import edu.Connexion3A7.entities.user;
import edu.Connexion3A7.services.CoachService;
import edu.Connexion3A7.services.ReservationService;
import edu.Connexion3A7.tools.MyConnection;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.sql.SQLException;
import java.util.List;

/**
 * Controller for the user-side dashboard.
 * Users can browse coach profiles and book/remove bookings.
 */
public class UserDashboardController {

    // --- Sidebar ---
    @FXML
    private Label userNameLabel;
    @FXML
    private Label userRoleLabel;
    @FXML
    private Label sidebarInitial;

    // --- Top bar ---
    @FXML
    private Circle profileCircle;
    @FXML
    private Label profileInitials;
    @FXML
    private Label breadcrumbLabel;

    // --- Content ---
    @FXML
    private VBox coachCardsContainer;
    @FXML
    private Label statusLabel;
    @FXML
    private Label coachCountLabel;

    private final CoachService coachService = new CoachService();
    private final ReservationService reservationService = new ReservationService();

    private user loggedInUser;

    public void setLoggedInUser(user u) {
        this.loggedInUser = u;
        if (u != null) {
            String initial = u.getEmail() != null && !u.getEmail().isEmpty()
                    ? String.valueOf(u.getEmail().charAt(0)).toUpperCase()
                    : "U";
            profileInitials.setText(initial);
            sidebarInitial.setText(initial);
            userNameLabel.setText(u.getEmail());
            userRoleLabel.setText("Utilisateur");
        }
        loadCoachCards();
    }

    @FXML
    public void initialize() {
        profileInitials.setText("U");
    }

    private void loadCoachCards() {
        coachCardsContainer.getChildren().clear();

        if (!MyConnection.getInstance().isConnected()) {
            statusLabel.setText("Base de donnees non disponible.");
            statusLabel.setStyle("-fx-text-fill: #E74C3C;");
            coachCountLabel.setText("0 coachs");
            return;
        }

        try {
            List<coach> coaches = coachService.getData();
            coachCountLabel.setText(coaches.size() + " coach(s) disponible(s)");

            if (coaches.isEmpty()) {
                Label empty = new Label("Aucun coach disponible pour le moment.");
                empty.setStyle("-fx-text-fill: #7F8C8D; -fx-font-size: 14px; -fx-padding: 30;");
                coachCardsContainer.getChildren().add(empty);
            } else {
                for (coach c : coaches) {
                    coachCardsContainer.getChildren().add(createCoachCard(c));
                }
            }
            statusLabel.setText("");
        } catch (SQLException e) {
            showErrorAlert("Erreur chargement coachs", e.getMessage());
            statusLabel.setText("Erreur: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: #E74C3C;");
            coachCountLabel.setText("Erreur");
        }
    }

    private VBox createCoachCard(coach c) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 10, 0, 0, 3);");

        // === Top section: Avatar + Name + Domain badge ===
        HBox topRow = new HBox(15);
        topRow.setAlignment(Pos.CENTER_LEFT);

        // Avatar circle
        StackPane avatar = new StackPane();
        Circle circle = new Circle(28);
        String[] colors = { "#2ECC9B", "#3498DB", "#E67E22", "#9B59B6", "#E74C3C", "#1ABC9C" };
        circle.setFill(Color.web(colors[Math.abs(c.getId_coach()) % colors.length]));
        Label initials = new Label(getInitials(c));
        initials.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");
        avatar.getChildren().addAll(circle, initials);

        // Name + domain
        VBox nameBox = new VBox(2);
        HBox.setHgrow(nameBox, Priority.ALWAYS);
        Label nameLabel = new Label(c.getNom() + " " + c.getPrenom());
        nameLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        nameLabel.setStyle("-fx-text-fill: #2C3E50;");

        Label domainBadge = new Label(c.getDomaine() != null ? c.getDomaine() : "N/A");
        domainBadge.setStyle("-fx-background-color: #EBF5FB; -fx-text-fill: #2980B9; " +
                "-fx-padding: 3 10; -fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold;");

        nameBox.getChildren().addAll(nameLabel, domainBadge);
        topRow.getChildren().addAll(avatar, nameBox);

        // === Info grid ===
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(20);
        infoGrid.setVgap(6);
        infoGrid.setPadding(new Insets(10, 0, 0, 0));

        addInfoRow(infoGrid, 0, "⭐ Note", String.format("%.1f / 5", c.getNote()));
        addInfoRow(infoGrid, 1, "📅 Experience", c.getExperience() + " ans");
        addInfoRow(infoGrid, 2, "💰 Tarif", String.format("%.0f €/h", c.getTarif()));
        addInfoRow(infoGrid, 3, "📋 Disponibilite", c.getDispo() != null ? c.getDispo() : "N/A");
        addInfoRow(infoGrid, 4, "🏅 Certification",
                c.getCertif() != null && !c.getCertif().isEmpty() ? c.getCertif() : "Aucune");

        // Biography
        if (c.getBiographie() != null && !c.getBiographie().isEmpty()) {
            Label bioLabel = new Label(c.getBiographie());
            bioLabel.setWrapText(true);
            bioLabel.setMaxWidth(Double.MAX_VALUE);
            bioLabel.setStyle("-fx-text-fill: #7F8C8D; -fx-font-size: 12px; -fx-padding: 8 0 0 0;");
            infoGrid.add(bioLabel, 0, 5, 2, 1);
        }

        // === Action buttons ===
        HBox buttonBox = new HBox(12);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        boolean booked = false;
        try {
            if (loggedInUser != null) {
                booked = reservationService.isBooked(loggedInUser.getId_user(), c.getId_coach());
            }
        } catch (SQLException e) {
            System.out.println("Erreur check booking: " + e.getMessage());
        }

        if (booked) {
            // Show "Annuler Reservation" button
            Label bookedBadge = new Label("✅ Reservé");
            bookedBadge.setStyle("-fx-background-color: #D5F5E3; -fx-text-fill: #27AE60; " +
                    "-fx-padding: 5 12; -fx-background-radius: 12; -fx-font-size: 12px; -fx-font-weight: bold;");
            HBox.setHgrow(bookedBadge, Priority.ALWAYS);

            Button cancelBtn = new Button("Annuler Réservation");
            cancelBtn.getStyleClass().add("btn-danger");
            cancelBtn.setStyle("-fx-font-size: 12px; -fx-padding: 8 20;");
            cancelBtn.setOnAction(e -> handleCancelBooking(c));

            buttonBox.getChildren().addAll(bookedBadge, cancelBtn);
        } else {
            // Show "Reserver" button
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Button bookBtn = new Button("Réserver");
            bookBtn.getStyleClass().add("btn-book");
            bookBtn.setStyle("-fx-font-size: 13px; -fx-padding: 8 28;");
            bookBtn.setOnAction(e -> handleBookCoach(c));

            buttonBox.getChildren().addAll(spacer, bookBtn);
        }

        card.getChildren().addAll(topRow, infoGrid, buttonBox);

        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle(
                "-fx-background-color: #FAFFFE; -fx-background-radius: 12; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(46,204,155,0.2), 14, 0, 0, 4);"));
        card.setOnMouseExited(e -> card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 12; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 10, 0, 0, 3);"));

        return card;
    }

    private void addInfoRow(GridPane grid, int row, String label, String value) {
        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill: #95A5A6; -fx-font-size: 12px;");
        Label val = new Label(value);
        val.setStyle("-fx-text-fill: #2C3E50; -fx-font-size: 12px; -fx-font-weight: bold;");
        grid.add(lbl, 0, row);
        grid.add(val, 1, row);
    }

    private void handleBookCoach(coach c) {
        if (loggedInUser == null) {
            showErrorAlert("Erreur", "Utilisateur non connecte.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Reserver le coach " + c.getNom() + " " + c.getPrenom() + " ?",
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText("Confirmer la reservation");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    reservationService.addReservation(loggedInUser.getId_user(), c.getId_coach());
                    showInfoAlert("Succes", "Reservation confirmee pour " + c.getNom() + " " + c.getPrenom());
                    loadCoachCards(); // reload to show updated buttons
                } catch (SQLException e) {
                    if (e.getMessage().contains("Duplicate")) {
                        showErrorAlert("Deja reserve", "Vous avez deja reserve ce coach.");
                    } else {
                        showErrorAlert("Erreur reservation", e.getMessage());
                    }
                }
            }
        });
    }

    private void handleCancelBooking(coach c) {
        if (loggedInUser == null) {
            showErrorAlert("Erreur", "Utilisateur non connecte.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Annuler la reservation avec " + c.getNom() + " " + c.getPrenom() + " ?",
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText("Annuler la reservation");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    reservationService.removeReservation(loggedInUser.getId_user(), c.getId_coach());
                    showInfoAlert("Succes", "Reservation annulee.");
                    loadCoachCards();
                } catch (SQLException e) {
                    showErrorAlert("Erreur annulation", e.getMessage());
                }
            }
        });
    }

    private String getInitials(coach c) {
        String init = "";
        if (c.getNom() != null && !c.getNom().isEmpty())
            init += c.getNom().charAt(0);
        if (c.getPrenom() != null && !c.getPrenom().isEmpty())
            init += c.getPrenom().charAt(0);
        return init.toUpperCase();
    }

    private void showErrorAlert(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showInfoAlert(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Sidebar navigation handlers (placeholders)
    @FXML
    void handleCoachingClick(javafx.event.ActionEvent event) {
        loadCoachCards();
    }

    @FXML
    void handleUtilisateursClick(javafx.event.ActionEvent event) {
        /* placeholder */ }

    @FXML
    void handleEvenementsClick(javafx.event.ActionEvent event) {
        /* placeholder */ }

    @FXML
    void handleBlogClick(javafx.event.ActionEvent event) {
        /* placeholder */ }

    @FXML
    void handleProduitsClick(javafx.event.ActionEvent event) {
        /* placeholder */ }

    @FXML
    void handleLogout(javafx.event.ActionEvent event) {
        try {
            loggedInUser = null;
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/edu/Connexion3A7/Controller/login.fxml"));
            javafx.scene.Parent root = loader.load();
            coachCardsContainer.getScene().setRoot(root);
        } catch (java.io.IOException e) {
            showErrorAlert("Erreur déconnexion", e.getMessage());
        }
    }
}
