package edu.Connexion3A7.Controller;

import edu.Connexion3A7.entities.DomaineCoaching;
import edu.Connexion3A7.entities.DomaineNom;
import edu.Connexion3A7.entities.coach;
import edu.Connexion3A7.services.CoachService;
import edu.Connexion3A7.services.DomaineCoachingService;
import edu.Connexion3A7.services.IpDetectionService;
import edu.Connexion3A7.services.IpDetectionService.CountryData;
import edu.Connexion3A7.services.IpDetectionService.DetectionResult;
import edu.Connexion3A7.tools.MyConnection;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AjouterCoach {

    // ─── Profile section ──────────────────────────────────────────────────────
    @FXML
    private VBox profileCardsContainer;
    @FXML
    private Label profileCount;
    @FXML
    private Label statusLabel;

    // ─── Form fields ──────────────────────────────────────────────────────────
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

    // ─── Phone prefix UI ──────────────────────────────────────────────────────
    @FXML
    private ImageView flagImage; // country flag (40 × ~27 px)
    @FXML
    private Label prefixLabel; // "+216", "+31", etc.
    @FXML
    private Label digitCounterLabel;// "0/8", "3/9", etc.
    @FXML
    private HBox phoneRowBox; // outer border HBox

    // ─── Services ─────────────────────────────────────────────────────────────
    private final CoachService coachService = new CoachService();
    private final DomaineCoachingService domaineService = new DomaineCoachingService();

    // ─── State ────────────────────────────────────────────────────────────────
    private DashboardController dashboardController;
    private coach editingCoach = null;
    private int loggedInUserId = 0;

    /** Currently detected calling prefix, e.g. "+216". */
    private volatile String detectedPrefix = "+216";
    /** Number of local digits required for the detected country. */
    private volatile int requiredDigits = 8;
    /** Last detected ISO-2 country code (uppercase). Used to detect VPN change. */
    private volatile String lastCountryCode = "";

    /** Background scheduler that polls for country changes every 10 seconds. */
    private ScheduledExecutorService scheduler;

    // ─── Setters ──────────────────────────────────────────────────────────────
    public void setDashboardController(DashboardController dc) {
        this.dashboardController = dc;
    }

    public void setLoggedInUserId(int userId) {
        this.loggedInUserId = userId;
    }

    // =========================================================================
    // Lifecycle
    // =========================================================================

    @FXML
    public void initialize() {
        // Availability & domaine combos
        dispo.setItems(FXCollections.observableArrayList("Disponible", "Indisponible"));
        dispo.getSelectionModel().selectFirst();
        domaine.setItems(FXCollections.observableArrayList(DomaineNom.values()));
        domaine.getSelectionModel().selectFirst();
        domaine.setButtonCell(domaineCell());
        domaine.setCellFactory(lv -> domaineCell());

        // Phone field: digits only, max = requiredDigits
        numTel.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null)
                return;
            String digits = newVal.replaceAll("[^0-9]", "");
            if (digits.length() > requiredDigits)
                digits = digits.substring(0, requiredDigits);
            if (!digits.equals(newVal)) {
                numTel.setText(digits);
                return;
            }
            updatePhoneStyle(digits.length());
        });

        // Show loading state
        prefixLabel.setText("...");
        digitCounterLabel.setText("0/?");
        digitCounterLabel.setStyle("-fx-text-fill: #7F8C8D; -fx-font-size: 11px;");

        // First detection on init
        IpDetectionService.getInstance().detect(this::applyDetectionResult);

        // Start 10-second polling for VPN changes
        startPolling();

        loadProfileCards();
    }

    // =========================================================================
    // IP detection & polling
    // =========================================================================

    /**
     * Starts a daemon scheduler that re-checks the country every 10 seconds.
     * If the country has changed (VPN connected/disconnected), applies updates.
     */
    private void startPolling() {
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "country-poll-thread");
            t.setDaemon(true); // won't prevent JVM shutdown
            return t;
        });
        // Delay 15s before first poll (init() already did the first detection)
        scheduler.scheduleAtFixedRate(() -> {
            try {
                DetectionResult result = IpDetectionService.getInstance().detectSync();
                if (!result.countryCode().equals(lastCountryCode)) {
                    // Country changed — apply updates atomically on the FX thread
                    Platform.runLater(() -> applyDetectionResult(result));
                }
            } catch (Exception e) {
                System.err.println("[AjouterCoach] Poll error: " + e.getMessage());
            }
        }, 15, 10, TimeUnit.SECONDS);
    }

    /** Stops the polling scheduler. Call when the form is no longer shown. */
    private void stopPolling() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }

    /**
     * MUST be called on the JavaFX Application Thread.
     * Updates ALL 4 phone-prefix UI elements atomically from one DetectionResult.
     * Also loads the flag image on a background thread.
     */
    private void applyDetectionResult(DetectionResult result) {
        // Record the new country so the poller can detect future changes
        lastCountryCode = result.countryCode();

        CountryData data = result.phone();

        // ── Atomically update state + labels ─────────────────────────────────
        detectedPrefix = data.callingCode();
        requiredDigits = data.digits();

        prefixLabel.setText(detectedPrefix);
        numTel.setPromptText("ex: " + data.example());
        digitCounterLabel.setText("0/" + requiredDigits);

        // Clear stale phone input when country changes (to avoid leftover digits)
        if (!numTel.getText().isEmpty()) {
            numTel.clear();
        }
        updatePhoneStyle(0);

        // ── Load flag image on a background thread ────────────────────────────
        String flagUrl = result.flagUrl();
        Thread flagThread = new Thread(() -> {
            try {
                Image img = new Image(flagUrl, 40, 27, true, true, false);
                if (!img.isError()) {
                    Platform.runLater(() -> flagImage.setImage(img));
                }
            } catch (Exception e) {
                System.err.println("[AjouterCoach] Flag load failed: " + e.getMessage());
            }
        }, "flag-image-thread");
        flagThread.setDaemon(true);
        flagThread.start();
    }

    // =========================================================================
    // Phone field style
    // =========================================================================

    /** Green border when digit count matches required, red otherwise. */
    private void updatePhoneStyle(int currentLen) {
        digitCounterLabel.setText(currentLen + "/" + requiredDigits);
        if (currentLen == requiredDigits) {
            phoneRowBox.setStyle("-fx-background-color: white;"
                    + " -fx-border-color: #2ECC9B; -fx-border-width: 2;"
                    + " -fx-border-radius: 6; -fx-background-radius: 6;");
            digitCounterLabel.setStyle("-fx-text-fill: #2ECC9B; -fx-font-size: 11px; -fx-font-weight: bold;");
        } else {
            phoneRowBox.setStyle("-fx-background-color: white;"
                    + " -fx-border-color: #E74C3C; -fx-border-width: 2;"
                    + " -fx-border-radius: 6; -fx-background-radius: 6;");
            digitCounterLabel.setStyle("-fx-text-fill: #E74C3C; -fx-font-size: 11px; -fx-font-weight: bold;");
        }
    }

    // =========================================================================
    // Dev test button (hidden by default in FXML)
    // =========================================================================

    @FXML
    void handleTestApiBtn(ActionEvent event) {
        Thread t = new Thread(() -> {
            String report = IpDetectionService.getInstance().testIpDetection();
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("🧪 IP Detection Test");
                alert.setHeaderText("Résultats du test API");
                alert.setContentText(report);
                alert.getDialogPane().setMinWidth(480);
                alert.showAndWait();
            });
        }, "api-test-thread");
        t.setDaemon(true);
        t.start();
    }

    // =========================================================================
    // Profile cards
    // =========================================================================

    private void loadProfileCards() {
        profileCardsContainer.getChildren().clear();
        if (!MyConnection.getInstance().isConnected()) {
            statusLabel.setText("Base de données non disponible.");
            statusLabel.setStyle("-fx-text-fill: #E74C3C;");
            profileCount.setText("0 coachs");
            return;
        }
        try {
            List<coach> coaches = coachService.getData();
            profileCount.setText(coaches.size() + " coach(s)");
            if (coaches.isEmpty()) {
                Label empty = new Label("Aucun coach enregistré. Utilisez le formulaire ci-dessous.");
                empty.setStyle("-fx-text-fill: #7F8C8D; -fx-font-size: 13px; -fx-padding: 20;");
                profileCardsContainer.getChildren().add(empty);
            } else {
                for (coach c : coaches)
                    profileCardsContainer.getChildren().add(createProfileCard(c));
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

        StackPane avatar = new StackPane();
        Circle circle = new Circle(22);
        String[] colors = { "#2ECC9B", "#3498DB", "#E67E22", "#9B59B6", "#E74C3C", "#1ABC9C" };
        circle.setFill(Color.web(colors[Math.abs(c.getId_coach()) % colors.length]));
        Label initials = new Label(getInitials(c));
        initials.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        avatar.getChildren().addAll(circle, initials);

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
                (c.getDispo() != null ? c.getDispo() : "N/A") + " | Tel: " +
                        (c.getNumTel() != null && !c.getNumTel().isEmpty() ? c.getNumTel() : "N/A"));
        dispoLabel.setStyle("-fx-text-fill: #95A5A6; -fx-font-size: 11px;");
        info.getChildren().addAll(nameLabel, detailLabel, dispoLabel);

        Button modifyBtn = new Button("Modifier");
        modifyBtn.getStyleClass().add("btn-primary");
        modifyBtn.setStyle("-fx-font-size: 11px; -fx-padding: 5 12;");
        modifyBtn.setOnAction(e -> handleModifyCoach(c));

        Button deleteBtn = new Button("Supprimer");
        deleteBtn.getStyleClass().add("btn-danger");
        deleteBtn.setStyle("-fx-font-size: 11px; -fx-padding: 5 12;");
        deleteBtn.setOnAction(e -> handleDeleteCoach(c));

        HBox buttons = new HBox(6, modifyBtn, deleteBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);
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
        String s = "";
        if (c.getNom() != null && !c.getNom().isEmpty())
            s += c.getNom().charAt(0);
        if (c.getPrenom() != null && !c.getPrenom().isEmpty())
            s += c.getPrenom().charAt(0);
        return s.toUpperCase();
    }

    // =========================================================================
    // Validation
    // =========================================================================

    private List<String> validateForm() {
        List<String> errors = new ArrayList<>();

        String nomVal = nom.getText() != null ? nom.getText().trim() : "";
        if (nomVal.isEmpty())
            errors.add("Le nom est obligatoire.");
        else if (nomVal.length() < 2)
            errors.add("Le nom doit contenir au moins 2 caractères.");
        else if (!nomVal.matches("[a-zA-ZÀ-ÿ\\s]+"))
            errors.add("Le nom ne doit contenir que des lettres.");

        String prenomVal = prenom.getText() != null ? prenom.getText().trim() : "";
        if (prenomVal.isEmpty())
            errors.add("Le prénom est obligatoire.");
        else if (prenomVal.length() < 2)
            errors.add("Le prénom doit contenir au moins 2 caractères.");
        else if (!prenomVal.matches("[a-zA-ZÀ-ÿ\\s]+"))
            errors.add("Le prénom ne doit contenir que des lettres.");

        String bioVal = biographie.getText() != null ? biographie.getText().trim() : "";
        if (bioVal.isEmpty())
            errors.add("La biographie est obligatoire.");
        else if (bioVal.length() < 10)
            errors.add("La biographie doit contenir au moins 10 caractères.");

        String expVal = experience.getText() != null ? experience.getText().trim() : "";
        if (expVal.isEmpty()) {
            errors.add("L'expérience est obligatoire.");
        } else {
            try {
                if (Integer.parseInt(expVal) < 0)
                    errors.add("L'expérience doit être >= 0.");
            } catch (NumberFormatException e) {
                errors.add("L'expérience doit être un entier.");
            }
        }

        String tarifVal = tarif.getText() != null ? tarif.getText().trim() : "";
        if (tarifVal.isEmpty()) {
            errors.add("Le tarif horaire est obligatoire.");
        } else {
            try {
                if (Double.parseDouble(tarifVal) <= 0)
                    errors.add("Le tarif doit être > 0.");
            } catch (NumberFormatException e) {
                errors.add("Le tarif doit être un nombre valide.");
            }
        }

        if (dispo.getValue() == null || dispo.getValue().isEmpty())
            errors.add("La disponibilité est obligatoire.");

        if (domaine.getValue() == null)
            errors.add("Le domaine est obligatoire.");

        // Phone: validate against the dynamically detected requiredDigits
        String telVal = numTel.getText() != null ? numTel.getText().trim() : "";
        if (telVal.isEmpty()) {
            errors.add("Le numéro de téléphone est obligatoire.");
        } else if (!telVal.matches("\\d+")) {
            errors.add("Le numéro de téléphone ne doit contenir que des chiffres.");
        } else if (telVal.length() != requiredDigits) {
            errors.add("Le numéro doit contenir exactement " + requiredDigits +
                    " chiffres pour " + lastCountryCode +
                    " (actuel : " + telVal.length() + ").");
        }

        return errors;
    }

    // =========================================================================
    // CRUD actions
    // =========================================================================

    @FXML
    void AjouterPersonneAction(ActionEvent event) {
        List<String> errors = validateForm();
        if (!errors.isEmpty()) {
            showErrorAlert("Erreurs de validation", String.join("\n", errors));
            formStatusLabel.setText("Veuillez corriger les erreurs.");
            formStatusLabel.setStyle("-fx-text-fill: #E74C3C;");
            return;
        }
        if (!MyConnection.getInstance().isConnected()) {
            showErrorAlert("Connexion", "Base de données non disponible.");
            return;
        }
        try {
            String nomVal = nom.getText().trim();
            String prenomVal = prenom.getText().trim();

            if (editingCoach != null) {
                if (coachService.isCoachDuplicateExcluding(nomVal, prenomVal, editingCoach.getId_coach())) {
                    showErrorAlert("Coach déjà existant", "\"" + nomVal + " " + prenomVal + "\" existe déjà.");
                    return;
                }
            } else {
                if (coachService.isCoachDuplicate(nomVal, prenomVal)) {
                    showErrorAlert("Coach déjà existant", "\"" + nomVal + " " + prenomVal + "\" existe déjà.");
                    return;
                }
            }

            coach c = (editingCoach != null) ? editingCoach : new coach();
            if (editingCoach == null)
                c.setId_user(loggedInUserId);

            c.setNom(nomVal);
            c.setPrenom(prenomVal);
            c.setBiographie(biographie.getText().trim());
            c.setExperience(Integer.parseInt(experience.getText().trim()));
            c.setTarif(Float.parseFloat(tarif.getText().trim()));
            c.setDispo(dispo.getValue());

            // Store E.164: prefix + local digits (e.g. "+4012345678")
            c.setNumTel(detectedPrefix + numTel.getText().trim());

            if (editingCoach == null)
                c.setNote(0f);
            c.setDomaine(domaine.getValue().name());

            if (editingCoach != null) {
                coachService.updateCoach(c);
                showInfoAlert("Succès", "Coach modifié avec succès !");
                editingCoach = null;
            } else {
                coachService.addCoach(c);
                showInfoAlert("Succès", "Coach ajouté avec succès !");
            }

            formStatusLabel.setText("Opération réussie");
            formStatusLabel.setStyle("-fx-text-fill: #27AE7A;");
            clearForm();
            loadProfileCards();
            if (dashboardController != null)
                dashboardController.refreshTable();

        } catch (SQLException e) {
            showErrorAlert("Erreur base de données", e.getMessage());
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

        // Strip stored prefix before populating phone field
        String stored = c.getNumTel() != null ? c.getNumTel() : "";
        if (!stored.isEmpty() && stored.startsWith(detectedPrefix)) {
            numTel.setText(stored.substring(detectedPrefix.length()));
        } else {
            numTel.setText(stored.replaceAll("[^0-9]", ""));
        }

        if (c.getDispo() != null)
            dispo.getSelectionModel().select(c.getDispo());

        try {
            DomaineCoaching dc = domaineService.getByNomDomaine(c.getDomaine());
            if (dc != null && dc.getNomDomaine() != null)
                domaine.getSelectionModel().select(dc.getNomDomaine());
        } catch (SQLException e) {
            try {
                domaine.getSelectionModel().select(DomaineNom.valueOf(c.getDomaine()));
            } catch (Exception ex) {
                System.out.println("Erreur domaine: " + e.getMessage());
            }
        }

        formStatusLabel.setText("Mode modification : " + c.getNom() + " " + c.getPrenom());
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
                    if (dashboardController != null)
                        dashboardController.refreshTable();
                    formStatusLabel.setText("Coach supprimé.");
                    formStatusLabel.setStyle("-fx-text-fill: #27AE7A;");
                } catch (SQLException e) {
                    showErrorAlert("Erreur suppression", e.getMessage());
                }
            }
        });
    }

    @FXML
    void handleRetour(ActionEvent event) {
        stopPolling(); // <— shut down the scheduler cleanly
        if (dashboardController != null)
            dashboardController.showCoachTable();
    }

    @FXML
    void openChatbot(ActionEvent event) {
        ChatbotController.openChatbotWindow();
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
        formStatusLabel.setText("");
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private ListCell<DomaineNom> domaineCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(DomaineNom item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getDisplayName());
            }
        };
    }

    private void showErrorAlert(String header, String content) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Erreur");
        a.setHeaderText(header);
        a.setContentText(content);
        a.showAndWait();
    }

    private void showInfoAlert(String header, String content) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Information");
        a.setHeaderText(header);
        a.setContentText(content);
        a.showAndWait();
    }
}
