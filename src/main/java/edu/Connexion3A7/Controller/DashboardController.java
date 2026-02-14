package edu.Connexion3A7.Controller;

import edu.Connexion3A7.entities.coach;
import edu.Connexion3A7.entities.user;
import edu.Connexion3A7.services.CoachService;
import edu.Connexion3A7.tools.MyConnection;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

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

    // --- Table columns ---
    @FXML
    private TableColumn<coach, Integer> colId;
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
    private TableColumn<coach, String> colCertif;
    @FXML
    private TableColumn<coach, Float> colNote;
    @FXML
    private TableColumn<coach, Void> colActions;

    private final CoachService coachService = new CoachService();

    /** The logged-in user's ID — passed from login controller */
    private int loggedInUserId = 0;
    private user loggedInUser;

    public void setLoggedInUserId(int userId) {
        this.loggedInUserId = userId;
    }

    public int getLoggedInUserId() {
        return loggedInUserId;
    }

    public void setLoggedInUser(user u) {
        this.loggedInUser = u;
        if (u != null) {
            String initial = u.getEmail() != null && !u.getEmail().isEmpty()
                    ? String.valueOf(u.getEmail().charAt(0)).toUpperCase()
                    : "A";
            profileInitials.setText(initial);
        }
    }

    @FXML
    public void initialize() {
        colId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getId_coach()).asObject());
        colNom.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNom()));
        colPrenom.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPrenom()));
        colExperience
                .setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getExperience()).asObject());
        colTarif.setCellValueFactory(data -> new SimpleFloatProperty(data.getValue().getTarif()).asObject());
        colDispo.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDispo()));
        colCertif.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCertif()));
        colNote.setCellValueFactory(data -> new SimpleFloatProperty(data.getValue().getNote()).asObject());

        // Actions column with Delete button
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
                setGraphic(empty ? null : deleteBtn);
            }
        });

        // Sidebar hover
        gestionContainer.setOnMouseEntered(
                e -> gestionContainer.setStyle("-fx-background-color: rgba(52,73,94,0.5); -fx-background-radius: 8;"));
        gestionContainer.setOnMouseExited(
                e -> gestionContainer.setStyle("-fx-background-color: transparent;"));

        profileInitials.setText("A");
        refreshTable();
    }

    public void refreshTable() {
        if (!MyConnection.getInstance().isConnected()) {
            statusLabel.setText("Base de donnees non disponible. Verifiez MySQL.");
            statusLabel.setStyle("-fx-text-fill: #E74C3C;");
            coachTable.setItems(FXCollections.observableArrayList());
            return;
        }

        try {
            List<coach> coaches = coachService.getData();
            ObservableList<coach> data = FXCollections.observableArrayList(coaches);
            coachTable.setItems(data);
            statusLabel.setText(coaches.size() + " coach(s) trouve(s)");
            statusLabel.setStyle("-fx-text-fill: #27AE7A;");

            if (!coaches.isEmpty()) {
                coach first = coaches.get(0);
                String initials = "";
                if (first.getNom() != null && !first.getNom().isEmpty())
                    initials += first.getNom().charAt(0);
                if (first.getPrenom() != null && !first.getPrenom().isEmpty())
                    initials += first.getPrenom().charAt(0);
                profileInitials.setText(initials.toUpperCase());
            }
        } catch (SQLException e) {
            showErrorAlert("Erreur chargement", e.getMessage());
            statusLabel.setText("Erreur: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: #E74C3C;");
            coachTable.setItems(FXCollections.observableArrayList());
        }
    }

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

    /** Show an error Alert dialog */
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
            loggedInUser = null;
            Parent root = FXMLLoader.load(
                    getClass().getResource("/edu/Connexion3A7/Controller/login.fxml"));
            contentArea.getScene().setRoot(root);
        } catch (IOException e) {
            showErrorAlert("Erreur déconnexion", e.getMessage());
        }
    }
}
