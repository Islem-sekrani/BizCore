package edu.Connexion3A7.Controller;

import edu.Connexion3A7.entities.coach;
import edu.Connexion3A7.entities.user;
import edu.Connexion3A7.services.CoachService;
import edu.Connexion3A7.tools.MyConnection;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;

import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class DashboardController {

    // --- Sidebar ---
    @FXML
    private ImageView logoImage;
    @FXML
    private VBox gestionContainer;
    @FXML
    private Button coachingBtn;
    @FXML
    private Label adminNameLabel;
    @FXML
    private Label adminRoleLabel;

    // --- Top bar ---
    @FXML
    private Circle profileCircle;
    @FXML
    private Label profileInitials;

    // --- Content area ---
    @FXML
    private StackPane contentArea;
    @FXML
    private VBox coachTableCard;
    @FXML
    private TableView<coach> coachTable;
    @FXML
    private Label statusLabel;
    @FXML
    private Button addCoachBtn;

    // --- Search + Sort ---
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> sortOrder;

    // --- CSV Export ---
    @FXML
    private Button exportCsvBtn;

    // --- Coach table columns ---
    @FXML
    private TableColumn<coach, String> colNom;
    @FXML
    private TableColumn<coach, String> colPrenom;
    @FXML
    private TableColumn<coach, Integer> colExperience;
    @FXML
    private TableColumn<coach, Float> colTarif;
    @FXML
    private TableColumn<coach, String> colDispo;
    @FXML
    private TableColumn<coach, String> colNumTel;
    @FXML
    private TableColumn<coach, Float> colNote;
    @FXML
    private TableColumn<coach, Void> colActions;

    // ─────────────────────────────────────────────────────────────────────────
    private final CoachService coachService = new CoachService();

    /** Full unfiltered list — kept for search/sort */
    private List<coach> allCoaches = List.of();

    private int loggedInUserId = 0;

    public void setLoggedInUserId(int userId) {
        this.loggedInUserId = userId;
    }

    public int getLoggedInUserId() {
        return loggedInUserId;
    }

    public void setLoggedInUser(user u) {
        if (u != null) {
            String initial = u.getEmail() != null && !u.getEmail().isEmpty()
                    ? String.valueOf(u.getEmail().charAt(0)).toUpperCase()
                    : "A";
            profileInitials.setText(initial);
        }
    }

    // =========================================================================
    // JavaFX lifecycle
    // =========================================================================

    @FXML
    public void initialize() {
        // ── Coach table columns ──────────────────────────────────────────────
        colNom.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNom()));
        colNom.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(v);
                setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #1A7DC4;");
            }
        });

        colPrenom.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPrenom()));
        colPrenom.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(v);
                setStyle("-fx-font-size: 12px; -fx-text-fill: #2C3E50; -fx-font-weight: bold;");
            }
        });
        colExperience.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getExperience()).asObject());
        colTarif.setCellValueFactory(d -> new SimpleFloatProperty(d.getValue().getTarif()).asObject());
        colDispo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDispo()));
        colNumTel.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNumTel()));
        colNote.setCellValueFactory(d -> new SimpleFloatProperty(d.getValue().getNote()).asObject());

        // Actions column — delete button per row
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button deleteBtn = new Button("Sup.");
            {
                deleteBtn.getStyleClass().add("btn-danger");
                deleteBtn.setOnAction(e -> {
                    coach c = getTableView().getItems().get(getIndex());
                    handleDeleteCoach(c);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteBtn);
                }
            }
        });

        // Sort ComboBox
        sortOrder.setItems(FXCollections.observableArrayList("Nom (A → Z)", "Nom (Z → A)"));
        sortOrder.getSelectionModel().selectFirst();
        sortOrder.valueProperty().addListener((obs, oldVal, newVal) -> filterAndSort());

        // Search listener
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterAndSort());

        // Sidebar hover
        gestionContainer.setOnMouseEntered(
                e -> gestionContainer.setStyle("-fx-background-color: rgba(52,73,94,0.5); -fx-background-radius: 8;"));
        gestionContainer.setOnMouseExited(
                e -> gestionContainer.setStyle("-fx-background-color: transparent;"));

        profileInitials.setText("A");
        coachTable.setItems(FXCollections.observableArrayList());

        refreshTable();
    }

    // =========================================================================
    // Data loading
    // =========================================================================

    /**
     * Reload all coach data from DB, refresh domain stats, and apply filter/sort.
     */
    public void refreshTable() {
        if (!MyConnection.getInstance().isConnected()) {
            statusLabel.setText("Base de donnees non disponible. Verifiez MySQL.");
            statusLabel.setStyle("-fx-text-fill: #E74C3C;");
            coachTable.getItems().clear();
            return;
        }
        try {
            allCoaches = coachService.getData();
            filterAndSort();
        } catch (SQLException e) {
            showErrorAlert("Erreur chargement", e.getMessage());
            statusLabel.setText("Erreur: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: #E74C3C;");
            coachTable.getItems().clear();
        }
    }

    // =========================================================================
    // Navigation — Statistics
    // =========================================================================

    /** Loads stat.fxml into the content area when the user clicks STATISTIQUES. */
    @FXML
    void handleStatsClick() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/edu/Connexion3A7/Controller/stat.fxml"));
            Parent statView = loader.load();
            contentArea.getChildren().setAll(statView);
        } catch (IOException e) {
            showErrorAlert("Erreur chargement statistiques", e.getMessage());
        }
    }

    // =========================================================================
    // Filter & sort
    // =========================================================================

    private void filterAndSort() {
        String query = searchField.getText() != null ? searchField.getText().trim().toLowerCase() : "";
        String order = sortOrder.getValue();

        List<coach> filtered = allCoaches.stream()
                .filter(c -> {
                    if (query.isEmpty())
                        return true;
                    String fullName = ((c.getNom() != null ? c.getNom() : "") + " "
                            + (c.getPrenom() != null ? c.getPrenom() : "")).toLowerCase();
                    return fullName.contains(query);
                })
                .sorted(getComparator(order))
                .collect(Collectors.toList());

        coachTable.getItems().setAll(filtered);
        statusLabel.setText(filtered.size() + " coach(s) trouve(s)");
        statusLabel.setStyle("-fx-text-fill: #27AE7A;");
    }

    private Comparator<coach> getComparator(String order) {
        if ("Nom (Z → A)".equals(order)) {
            return (a, b) -> {
                String na = a.getNom() != null ? a.getNom() : "";
                String nb = b.getNom() != null ? b.getNom() : "";
                return nb.compareToIgnoreCase(na);
            };
        }
        return (a, b) -> {
            String na = a.getNom() != null ? a.getNom() : "";
            String nb = b.getNom() != null ? b.getNom() : "";
            return na.compareToIgnoreCase(nb);
        };
    }

    // =========================================================================
    // Actions
    // =========================================================================

    @FXML
    void handleCoachingClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/edu/Connexion3A7/Controller/ajouterCoach.fxml"));
            Parent formRoot = loader.load();

            AjouterCoach childController = loader.getController();
            childController.setDashboardController(this);
            childController.setLoggedInUserId(this.loggedInUserId);

            contentArea.getChildren().clear();
            contentArea.getChildren().add(formRoot);
        } catch (IOException e) {
            showErrorAlert("Erreur chargement formulaire", e.getMessage());
        }
    }

    public void showCoachTable() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(coachTableCard);
        refreshTable();
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
                    refreshTable();
                } catch (SQLException e) {
                    showErrorAlert("Erreur suppression", e.getMessage());
                }
            }
        });
    }

    private void showErrorAlert(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    void handleUtilisateursClick(ActionEvent event) {
        showCoachTable();
    }

    @FXML
    void handleEvenementsClick(ActionEvent event) {
        /* placeholder */ }

    @FXML
    void handleBlogClick(ActionEvent event) {
        /* placeholder */ }

    @FXML
    void handleProduitsClick(ActionEvent event) {
        /* placeholder */ }

    @FXML
    void handleLogout(ActionEvent event) {
        try {
            loggedInUserId = 0;
            Parent root = FXMLLoader.load(
                    getClass().getResource("/edu/Connexion3A7/Controller/login.fxml"));
            contentArea.getScene().setRoot(root);
        } catch (IOException e) {
            showErrorAlert("Erreur déconnexion", e.getMessage());
        }
    }

    @FXML
    void handleExportCSV(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le fichier CSV");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers CSV", "*.csv"));
        fileChooser.setInitialFileName("coachs_export.csv");

        File initialDir = new File(System.getProperty("user.home") + "/Downloads");
        if (initialDir.exists())
            fileChooser.setInitialDirectory(initialDir);

        File file = fileChooser.showSaveDialog(contentArea.getScene().getWindow());
        if (file == null)
            return;

        var items = coachTable.getItems();
        try (PrintWriter pw = new PrintWriter(new FileWriter(file, java.nio.charset.StandardCharsets.UTF_8))) {
            pw.println(
                    "nom,prenom,domaine,biographie,experience_annees,tarif_horaire,disponibilite,num_tel,note_moyenne");
            for (coach c : items) {
                pw.println(
                        escapeCsv(c.getNom()) + "," +
                                escapeCsv(c.getPrenom()) + "," +
                                escapeCsv(c.getDomaine()) + "," +
                                escapeCsv(c.getBiographie()) + "," +
                                c.getExperience() + "," +
                                c.getTarif() + "," +
                                escapeCsv(c.getDispo()) + "," +
                                escapeCsv(c.getNumTel()) + "," +
                                c.getNote());
            }
            Alert info = new Alert(Alert.AlertType.INFORMATION);
            info.setTitle("Export CSV");
            info.setHeaderText("Export terminé");
            info.setContentText(items.size() + " coach(s) exporté(s) vers:\n" + file.getAbsolutePath());
            info.showAndWait();
        } catch (IOException e) {
            showErrorAlert("Erreur export CSV", e.getMessage());
        }
    }

    private String escapeCsv(String value) {
        if (value == null)
            return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
