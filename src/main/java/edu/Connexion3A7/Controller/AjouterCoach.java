package edu.Connexion3A7.Controller;

import edu.Connexion3A7.entities.DomaineCoaching;
import edu.Connexion3A7.entities.DomaineNom;
import edu.Connexion3A7.entities.coach;
import edu.Connexion3A7.services.CoachService;
import edu.Connexion3A7.services.DomaineCoachingService;
import edu.Connexion3A7.tools.MyConnection;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
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

public class AjouterCoach {

    // --- Profile section ---
    @FXML
    private VBox profileCardsContainer;
    @FXML
    private Label profileCount;
    @FXML
    private Label statusLabel;

    // --- Form fields ---
    @FXML
    private TextField nom;
    @FXML
    private TextField prenom;
    @FXML
    private TextArea biographie;
    @FXML
    private TextField experience;
    @FXML
    private TextField tarif;
    @FXML
    private ComboBox<String> dispo;
    @FXML
    private TextField certif;
    @FXML
    private TextField note;
    @FXML
    private ComboBox<DomaineNom> domaine;
    @FXML
    private Label formStatusLabel;

    private final CoachService coachService = new CoachService();
    private final DomaineCoachingService domaineService = new DomaineCoachingService();

    private DashboardController dashboardController;
    private coach editingCoach = null;

    /** The logged-in user's ID — used for coach.id_user FK */
    private int loggedInUserId = 0;

    public void setDashboardController(DashboardController dc) {
        this.dashboardController = dc;
    }

    public void setLoggedInUserId(int userId) {
        this.loggedInUserId = userId;
    }

    @FXML
    public void initialize() {
        dispo.setItems(FXCollections.observableArrayList(
                "Disponible", "Indisponible", "Sur rendez-vous"));
        dispo.getSelectionModel().selectFirst();

        // Populate domaine combo from enum
        domaine.setItems(FXCollections.observableArrayList(DomaineNom.values()));
        domaine.getSelectionModel().selectFirst();

        domaine.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(DomaineNom item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getDisplayName());
            }
        });
        domaine.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(DomaineNom item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getDisplayName());
            }
        });

        loadProfileCards();
    }

    private void loadProfileCards() {
        profileCardsContainer.getChildren().clear();

        if (!MyConnection.getInstance().isConnected()) {
            statusLabel.setText("Base de donnees non disponible.");
            statusLabel.setStyle("-fx-text-fill: #E74C3C;");
            profileCount.setText("0 coachs");
            return;
        }

        try {
            List<coach> coaches = coachService.getData();
            profileCount.setText(coaches.size() + " coach(s)");

            if (coaches.isEmpty()) {
                Label empty = new Label("Aucun coach enregistre. Utilisez le formulaire ci-dessous.");
                empty.setStyle("-fx-text-fill: #7F8C8D; -fx-font-size: 13px; -fx-padding: 20;");
                profileCardsContainer.getChildren().add(empty);
            } else {
                for (coach c : coaches) {
                    profileCardsContainer.getChildren().add(createProfileCard(c));
                }
            }
            statusLabel.setText("");
        } catch (SQLException e) {
            showErrorAlert("Erreur chargement coachs", e.getMessage());
            statusLabel.setText("Erreur: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: #E74C3C;");
            profileCount.setText("Erreur");
        }
    }

    private HBox createProfileCard(coach c) {
        HBox card = new HBox(15);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(12, 15, 12, 15));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 6, 0, 0, 2);");

        // Avatar
        StackPane avatar = new StackPane();
        Circle circle = new Circle(22);
        String[] colors = { "#2ECC9B", "#3498DB", "#E67E22", "#9B59B6", "#E74C3C", "#1ABC9C" };
        circle.setFill(Color.web(colors[Math.abs(c.getId_coach()) % colors.length]));
        Label initials = new Label(getInitials(c));
        initials.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        avatar.getChildren().addAll(circle, initials);

        // Info
        VBox info = new VBox(3);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label nameLabel = new Label(c.getNom() + " " + c.getPrenom());
        nameLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        nameLabel.setStyle("-fx-text-fill: #2C3E50;");

        Label detailLabel = new Label(
                "Exp: " + c.getExperience() + " ans | Tarif: " + c.getTarif() +
                        "/h | Note: " + c.getNote() + "/5");
        detailLabel.setStyle("-fx-text-fill: #7F8C8D; -fx-font-size: 11px;");

        Label dispoLabel = new Label(
                (c.getDispo() != null ? c.getDispo() : "N/A") +
                        " | "
                        + (c.getCertif() != null && !c.getCertif().isEmpty() ? c.getCertif() : "Pas de certification"));
        dispoLabel.setStyle("-fx-text-fill: #95A5A6; -fx-font-size: 11px;");

        info.getChildren().addAll(nameLabel, detailLabel, dispoLabel);

        // Buttons
        HBox buttons = new HBox(6);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        Button modifyBtn = new Button("Modifier");
        modifyBtn.getStyleClass().add("btn-primary");
        modifyBtn.setStyle("-fx-font-size: 11px; -fx-padding: 5 12;");
        modifyBtn.setOnAction(e -> handleModifyCoach(c));

        Button deleteBtn = new Button("Supprimer");
        deleteBtn.getStyleClass().add("btn-danger");
        deleteBtn.setStyle("-fx-font-size: 11px; -fx-padding: 5 12;");
        deleteBtn.setOnAction(e -> handleDeleteCoach(c));

        buttons.getChildren().addAll(modifyBtn, deleteBtn);
        card.getChildren().addAll(avatar, info, buttons);

        card.setOnMouseEntered(e -> card.setStyle(
                "-fx-background-color: #F0FFF4; -fx-background-radius: 10; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(46,204,155,0.2), 10, 0, 0, 3);"));
        card.setOnMouseExited(e -> card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 10; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 6, 0, 0, 2);"));

        return card;
    }

    private String getInitials(coach c) {
        String initials = "";
        if (c.getNom() != null && !c.getNom().isEmpty())
            initials += c.getNom().charAt(0);
        if (c.getPrenom() != null && !c.getPrenom().isEmpty())
            initials += c.getPrenom().charAt(0);
        return initials.toUpperCase();
    }

    // ==================== CRUD ====================

    @FXML
    void AjouterPersonneAction(ActionEvent event) {
        if (nom.getText().isEmpty() || prenom.getText().isEmpty()) {
            showErrorAlert("Validation", "Nom et Prenom sont obligatoires.");
            return;
        }

        if (domaine.getValue() == null) {
            showErrorAlert("Validation", "Veuillez selectionner un domaine.");
            return;
        }

        if (!MyConnection.getInstance().isConnected()) {
            showErrorAlert("Connexion", "Base de donnees non disponible.");
            return;
        }

        try {
            coach c;
            if (editingCoach != null) {
                c = editingCoach;
            } else {
                c = new coach();
                // Use the REAL logged-in user ID (not hardcoded 1)
                c.setId_user(loggedInUserId);
            }

            c.setNom(nom.getText().trim());
            c.setPrenom(prenom.getText().trim());
            c.setBiographie(biographie.getText() != null ? biographie.getText().trim() : "");
            c.setExperience(parseIntSafe(experience.getText()));
            c.setTarif(parseFloatSafe(tarif.getText()));
            c.setNote(parseFloatSafe(note.getText()));
            c.setDispo(dispo.getValue() != null ? dispo.getValue() : "Disponible");
            c.setCertif(certif.getText() != null ? certif.getText().trim() : "");

            // Look up domaine ID from selected enum
            DomaineNom selectedDomaine = domaine.getValue();
            c.setDomaine(selectedDomaine.name());   // "E_COMMERCE", "BRANDING", ...

            if (editingCoach != null) {
                coachService.updateCoach(c);
                showInfoAlert("Succes", "Coach modifie avec succes !");
                editingCoach = null;
            } else {
                coachService.addCoach(c);
                showInfoAlert("Succes", "Coach ajoute avec succes !");
            }

            formStatusLabel.setText("Operation reussie");
            formStatusLabel.setStyle("-fx-text-fill: #27AE7A;");

            clearForm();
            loadProfileCards();

            if (dashboardController != null) {
                dashboardController.refreshTable();
            }

        } catch (SQLException e) {
            showErrorAlert("Erreur base de donnees", e.getMessage());
            formStatusLabel.setText("Erreur: " + e.getMessage());
            formStatusLabel.setStyle("-fx-text-fill: #E74C3C;");
        }
    }

    private int findDomaineIdByNom(DomaineNom nom) {
        try {
            List<DomaineCoaching> domaines = domaineService.getData();
            for (DomaineCoaching d : domaines) {
                if (d.getNomDomaine() == nom) {
                    return d.getIdDomaine();
                }
            }
        } catch (SQLException e) {
            System.out.println("Erreur lookup domaine: " + e.getMessage());
        }
        return 1;
    }

    private void handleModifyCoach(coach c) {
        editingCoach = c;
        nom.setText(c.getNom());
        prenom.setText(c.getPrenom());
        biographie.setText(c.getBiographie());
        experience.setText(String.valueOf(c.getExperience()));
        tarif.setText(String.valueOf(c.getTarif()));
        note.setText(String.valueOf(c.getNote()));
        certif.setText(c.getCertif());

        if (c.getDispo() != null) {
            dispo.getSelectionModel().select(c.getDispo());
        }

        try {
            DomaineCoaching dc = domaineService.getByNomDomaine(c.getDomaine());
            if (dc != null && dc.getNomDomaine() != null) {
                domaine.getSelectionModel().select(dc.getNomDomaine());
            }
        } catch (SQLException e) {
            System.out.println("Erreur lookup domaine: " + e.getMessage());
        }

        formStatusLabel.setText("Mode modification: " + c.getNom() + " " + c.getPrenom());
        formStatusLabel.setStyle("-fx-text-fill: #3498DB;");
    }

    private void handleDeleteCoach(coach c) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer le coach " + c.getNom() + " " + c.getPrenom() + " ?",
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText("Confirmation de suppression");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    coachService.deleteCoach(c);
                    loadProfileCards();
                    if (dashboardController != null) {
                        dashboardController.refreshTable();
                    }
                    formStatusLabel.setText("Coach supprime.");
                    formStatusLabel.setStyle("-fx-text-fill: #27AE7A;");
                } catch (SQLException e) {
                    showErrorAlert("Erreur suppression", e.getMessage());
                }
            }
        });
    }

    @FXML
    void handleRetour(ActionEvent event) {
        if (dashboardController != null) {
            dashboardController.showCoachTable();
        }
    }

    private void clearForm() {
        nom.clear();
        prenom.clear();
        biographie.clear();
        experience.clear();
        tarif.clear();
        certif.clear();
        note.clear();
        dispo.getSelectionModel().selectFirst();
        domaine.getSelectionModel().selectFirst();
        editingCoach = null;
    }

    /** Show an error Alert dialog */
    private void showErrorAlert(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /** Show an info Alert dialog */
    private void showInfoAlert(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private int parseIntSafe(String s) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private float parseFloatSafe(String s) {
        try {
            return Float.parseFloat(s.trim());
        } catch (Exception e) {
            return 0f;
        }
    }
}
