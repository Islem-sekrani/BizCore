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
import java.util.ArrayList;
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
    private TextField numTel;
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
                "Disponible", "Indisponible"));
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

        // Info — no IDs displayed
        VBox info = new VBox(3);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label nameLabel = new Label(c.getNom() + " " + c.getPrenom());
        nameLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        nameLabel.setStyle("-fx-text-fill: #2C3E50;");

        Label detailLabel = new Label(
                "Exp: " + c.getExperience() + " ans | Tarif: " + c.getTarif() +
                        " DT/H | Note: " + c.getNote() + "/5");
        detailLabel.setStyle("-fx-text-fill: #7F8C8D; -fx-font-size: 11px;");

        Label dispoLabel = new Label(
                (c.getDispo() != null ? c.getDispo() : "N/A") +
                        " | Tel: "
                        + (c.getNumTel() != null && !c.getNumTel().isEmpty() ? c.getNumTel() : "N/A"));
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

    // ==================== VALIDATION ====================

    /**
     * Validate all form fields. Returns a list of error messages.
     * Empty list = valid.
     */
    private List<String> validateForm() {
        List<String> errors = new ArrayList<>();

        // nom: required, letters/spaces only, min 2
        String nomVal = nom.getText() != null ? nom.getText().trim() : "";
        if (nomVal.isEmpty()) {
            errors.add("Le nom est obligatoire.");
        } else if (nomVal.length() < 2) {
            errors.add("Le nom doit contenir au moins 2 caracteres.");
        } else if (!nomVal.matches("[a-zA-ZÀ-ÿ\\s]+")) {
            errors.add("Le nom ne doit contenir que des lettres et espaces.");
        }

        // prenom: required, letters/spaces only, min 2
        String prenomVal = prenom.getText() != null ? prenom.getText().trim() : "";
        if (prenomVal.isEmpty()) {
            errors.add("Le prenom est obligatoire.");
        } else if (prenomVal.length() < 2) {
            errors.add("Le prenom doit contenir au moins 2 caracteres.");
        } else if (!prenomVal.matches("[a-zA-ZÀ-ÿ\\s]+")) {
            errors.add("Le prenom ne doit contenir que des lettres et espaces.");
        }

        // biographie: required, min 10 chars
        String bioVal = biographie.getText() != null ? biographie.getText().trim() : "";
        if (bioVal.isEmpty()) {
            errors.add("La biographie est obligatoire.");
        } else if (bioVal.length() < 10) {
            errors.add("La biographie doit contenir au moins 10 caracteres.");
        }

        // experience: required, integer >= 0
        String expVal = experience.getText() != null ? experience.getText().trim() : "";
        if (expVal.isEmpty()) {
            errors.add("L'experience est obligatoire.");
        } else {
            try {
                int exp = Integer.parseInt(expVal);
                if (exp < 0) {
                    errors.add("L'experience doit etre >= 0.");
                }
            } catch (NumberFormatException e) {
                errors.add("L'experience doit etre un nombre entier valide.");
            }
        }

        // tarif: required, decimal > 0
        String tarifVal = tarif.getText() != null ? tarif.getText().trim() : "";
        if (tarifVal.isEmpty()) {
            errors.add("Le tarif horaire est obligatoire.");
        } else {
            try {
                double t = Double.parseDouble(tarifVal);
                if (t <= 0) {
                    errors.add("Le tarif horaire doit etre > 0.");
                }
            } catch (NumberFormatException e) {
                errors.add("Le tarif horaire doit etre un nombre valide.");
            }
        }

        // disponibilite: ComboBox not null
        if (dispo.getValue() == null || dispo.getValue().isEmpty()) {
            errors.add("La disponibilite est obligatoire.");
        }

        // domaine: ComboBox not null
        if (domaine.getValue() == null) {
            errors.add("Le domaine est obligatoire.");
        }

        // numTel: required, exactly 8 digits, numeric only
        String telVal = numTel.getText() != null ? numTel.getText().trim() : "";
        if (telVal.isEmpty()) {
            errors.add("Le numero de telephone est obligatoire.");
        } else if (!telVal.matches("\\d{8}")) {
            errors.add("Le numero de telephone doit contenir exactement 8 chiffres.");
        }

        return errors;
    }

    // ==================== CRUD ====================

    @FXML
    void AjouterPersonneAction(ActionEvent event) {
        // Full validation
        List<String> errors = validateForm();
        if (!errors.isEmpty()) {
            showErrorAlert("Erreurs de validation", String.join("\n", errors));
            formStatusLabel.setText("Veuillez corriger les erreurs.");
            formStatusLabel.setStyle("-fx-text-fill: #E74C3C;");
            return;
        }

        if (!MyConnection.getInstance().isConnected()) {
            showErrorAlert("Connexion", "Base de donnees non disponible.");
            return;
        }

        try {
            String nomVal = nom.getText().trim();
            String prenomVal = prenom.getText().trim();

            // Duplicate check
            if (editingCoach != null) {
                if (coachService.isCoachDuplicateExcluding(nomVal, prenomVal, editingCoach.getId_coach())) {
                    showErrorAlert("Coach deja existant",
                            "Un coach avec le nom \"" + nomVal + " " + prenomVal + "\" existe deja.");
                    return;
                }
            } else {
                if (coachService.isCoachDuplicate(nomVal, prenomVal)) {
                    showErrorAlert("Coach deja existant",
                            "Un coach avec le nom \"" + nomVal + " " + prenomVal + "\" existe deja.");
                    return;
                }
            }

            coach c;
            if (editingCoach != null) {
                c = editingCoach;
            } else {
                c = new coach();
                c.setId_user(loggedInUserId);
            }

            c.setNom(nomVal);
            c.setPrenom(prenomVal);
            c.setBiographie(biographie.getText().trim());
            c.setExperience(Integer.parseInt(experience.getText().trim()));
            c.setTarif(Float.parseFloat(tarif.getText().trim()));
            c.setDispo(dispo.getValue());
            c.setNumTel(numTel.getText().trim());

            // Note defaults to 0; users rate via their dashboard
            if (editingCoach == null) {
                c.setNote(0f);
            }

            // Domaine — store the enum name
            DomaineNom selectedDomaine = domaine.getValue();
            c.setDomaine(selectedDomaine.name());

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

    private void handleModifyCoach(coach c) {
        editingCoach = c;
        nom.setText(c.getNom());
        prenom.setText(c.getPrenom());
        biographie.setText(c.getBiographie());
        experience.setText(String.valueOf(c.getExperience()));
        tarif.setText(String.valueOf(c.getTarif()));
        numTel.setText(c.getNumTel());

        if (c.getDispo() != null) {
            dispo.getSelectionModel().select(c.getDispo());
        }

        try {
            DomaineCoaching dc = domaineService.getByNomDomaine(c.getDomaine());
            if (dc != null && dc.getNomDomaine() != null) {
                domaine.getSelectionModel().select(dc.getNomDomaine());
            }
        } catch (SQLException e) {
            try {
                DomaineNom dn = DomaineNom.valueOf(c.getDomaine());
                domaine.getSelectionModel().select(dn);
            } catch (Exception ex) {
                System.out.println("Erreur lookup domaine: " + e.getMessage());
            }
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
        numTel.clear();
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
}
