package com.gestion;

import com.gestion.controllers.EvenementController;
import com.gestion.controllers.MainDashboardController;
import com.gestion.entities.Evenement;
import com.gestion.interfaces.IUserService;
import com.gestion.services.EvenementService;
import com.gestion.interfaces.IProductService;

import com.gestion.entities.*;
import com.gestion.services.ProductService;

import com.gestion.services.UserService;
import javafx.application.Application;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;


import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// Imports supplémentaires pour PDF et UI

import java.io.FileOutputStream;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.geometry.Insets;
import javafx.stage.FileChooser;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;



/**
 * Classe principale de l'application GestionPlatform
 * Gère l'interface utilisateur et la logique métier
 * Charge les fichiers FXML pour conserver le design original
 */
public class MainClass extends Application {


    private ObservableList<Evenement> data = FXCollections.observableArrayList();

    @FXML private TextField txtTitre, txtLieu, txtCapacite, txtPrix;
    @FXML private TextArea txtDescription;
    @FXML private ComboBox<String> cbStatut;

    @FXML private TableView<Evenement> tableEvenements;
    @FXML private TableColumn<Evenement, String> colTitre, colDescription, colLieu, colStatut;
    @FXML private TableColumn<Evenement, Integer> colCapacite;
    @FXML private TableColumn<Evenement, Double> colPrix;


    // Services

    private IUserService userService; //new
    private EvenementService service = new EvenementService();
    private IProductService productService;

    // UI Components (Main Dashboard)
    private Label lblTitle;
    private Label lblUserName;
    private Label lblUserRole;
    private AnchorPane contentArea;

    // UI Components (Module View)
    private TableView<Object> dataTable;
    private TextField searchField;
    private Label lblResults;
    private ComboBox<String> sortComboBox;

    // Data
    private ObservableList<Object> dataList;
    private String currentModule = "PRODUITS";
    private int itemsPerPage = 5;

    @Override
    public void start(Stage primaryStage) {
        try {
            // Init services
            productService = new ProductService();
            userService = new UserService();
            this.primaryStageRef = primaryStage;
            // 👉 show login instead of dashboard
            showLogin(primaryStage);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Startup error: " + e.getMessage());
        }
    }

    // Show login on the primary stage
    public static User currentUser; // session user
    private Stage primaryStageRef;

    public void showLogin(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Parent root = loader.load();

            // get nodes by fx:id
            TextField txtEmail = (TextField) root.lookup("#txtEmail");
            PasswordField txtPassword = (PasswordField) root.lookup("#txtPassword");
            Button btnLogin = (Button) root.lookup("#btnLogin");
            Button btnCancel = (Button) root.lookup("#btnCancel");
            Label lblMessage = (Label) root.lookup("#lblMessage");
            Button btnSignup = (Button) root.lookup("#btnSignup");

            btnLogin.setOnAction(e -> {
                String email = txtEmail.getText().trim();
                String password = txtPassword.getText();

                if (email.isEmpty() || password.isEmpty()) {
                    lblMessage.setText("Enter email and password");
                    return;
                }

                User user = userService.getUserByEmail(email);

                if (user == null) {
                    lblMessage.setText("User not found");
                    return;
                }

                if (!user.getPassword().equals(password)) {
                    lblMessage.setText("Wrong password");
                    return;
                }

                // ✅ SUCCESS LOGIN
                currentUser = user;

                // update last connection + statut in DB (single call)
                Timestamp now = Timestamp.from(Instant.now());
                userService.updateLastConnection(currentUser.getIdUser(), now);

                // update the in-memory user object too
                currentUser.setDerniereConnexion(now);
                currentUser.setStatut("connecte");


                // update statut in DB
                user.setStatut("connecte");
                userService.updateUser(user);

                // open dashboard
                openDashboard(stage);
            });

            btnCancel.setOnAction(e -> stage.close());

            if (btnSignup != null) {
                btnSignup.setOnAction(e -> showSignupDialog());
            }

            Scene scene = new Scene(root, 400, 300);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("Login");
            stage.show();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }//new

    private void showSignupDialog() {

        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Create account");

        DialogPane pane = dialog.getDialogPane();
        pane.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

        ButtonType createBtn = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        pane.getButtonTypes().addAll(createBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField txtNom = new TextField();
        txtNom.setPromptText("Nom");

        TextField txtPrenom = new TextField();
        txtPrenom.setPromptText("Prénom");

        TextField txtEmail = new TextField();
        txtEmail.setPromptText("Email");

        PasswordField txtPassword = new PasswordField();
        txtPassword.setPromptText("Password");

        grid.add(new Label("Nom:"), 0, 0);
        grid.add(txtNom, 1, 0);

        grid.add(new Label("Prénom:"), 0, 1);
        grid.add(txtPrenom, 1, 1);

        grid.add(new Label("Email:"), 0, 2);
        grid.add(txtEmail, 1, 2);

        grid.add(new Label("Password:"), 0, 3);
        grid.add(txtPassword, 1, 3);

        pane.setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == createBtn) {

                if (txtNom.getText().isEmpty() ||
                        txtPrenom.getText().isEmpty() ||
                        txtEmail.getText().isEmpty() ||
                        txtPassword.getText().isEmpty()) {

                    showDialog("Validation", "All fields are required!");
                    return null;
                }

                // 🔥 check if DB is empty
                int roleId;
                try {
                    boolean empty = userService.getAllUsers().isEmpty();
                    roleId = empty ? 1 : 2; // 1 = Admin, 2 = User
                } catch (Exception e) {
                    roleId = 2; // fallback safety
                }

                // create user
                User u = new User(
                        txtNom.getText(),
                        txtPrenom.getText(),
                        txtEmail.getText(),
                        txtPassword.getText(),
                        roleId
                );

                return u;
            }
            return null;
        });

        Optional<User> result = dialog.showAndWait();

        result.ifPresent(u -> {

            boolean success = userService.addUser(u);

            if (success) {

                // 🔐 set current session user
                currentUser = u;

                // update last connection + statut
                Timestamp now = Timestamp.from(Instant.now());
                userService.updateLastConnection(currentUser.getIdUser(), now);

                currentUser.setDerniereConnexion(now);
                currentUser.setStatut("connecte");

                // optional message
                showDialog("Success", "Account created successfully!");

                // 🚀 open dashboard directly
                openDashboard(primaryStageRef);
            } else {
                showDialog("Error", "Error creating account (email may already exist)");
            }
        });
    }//new

    private void openDashboard(Stage primaryStage) {
        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/MainDashboard.fxml")
            );

            Parent root = loader.load();

            // 🔥 GET CONTROLLER
            MainDashboardController controller = loader.getController();

            // 🔥 Pass current user if needed
            controller.setCurrentUser(currentUser); // if you have this method

            // 🔥 Now call loadModule from controller
            controller.loadModule("DASHBOARD");

            Scene scene = new Scene(root, 1400, 800);

            primaryStage.setTitle("Admin Panel");
            primaryStage.setScene(scene);
            primaryStage.setMaximized(true);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void logout() {
        try {
            // optional confirm
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Logout");
            confirm.setHeaderText(null);
            confirm.setContentText("Are you sure you want to log out?");
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() != ButtonType.OK) {
                return; // user cancelled
            }

            // update DB statut to "deconnecte"
            if (currentUser != null) {
                currentUser.setStatut("deconnecte");

                // optionally update last connection / modify object fields if you track that
                // currentUser.setDerniereConnexion(new Timestamp(System.currentTimeMillis()));

                // persist change (uses your existing UserService.updateUser(User))
                userService.updateUser(currentUser);
            }

            // clear session
            currentUser = null;

            // go back to login screen
            if (primaryStageRef != null) {
                showLogin(primaryStageRef);
            } else {
                // fallback: create new Stage if somehow null
                Stage s = new Stage();
                showLogin(s);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            Alert err = new Alert(Alert.AlertType.ERROR);
            err.setTitle("Logout error");
            err.setHeaderText(null);
            err.setContentText("An error occurred while logging out.");
            err.showAndWait();
        }
    }//new


    /**
     * Initialise les composants et événements du tableau de bord principal
     */
    private void initializeDashboard(java.util.Map<String, Object> namespace, Parent root) {
        // Récupération des composants via namespace (fx:id)
        lblTitle = (Label) namespace.get("lblTitle");
        lblUserName = (Label) namespace.get("lblUserName");
        contentArea = (AnchorPane) namespace.get("contentArea");
        if (lblUserName != null && currentUser != null) {
            lblUserName.setText(currentUser.getPrenom() + " " + currentUser.getNom());
        }

        if (lblUserRole != null && currentUser != null) {
            String roleName;

            switch (currentUser.getIdRole()) {
                case 1:
                    roleName = "Admin";
                    break;
                case 2:
                    roleName = "User";
                    break;
                case 3:
                    roleName = "Manager";
                    break;
                default:
                    roleName = "Unknown";
            }

            lblUserRole.setText(roleName);
        }//new

        // Configuration des boutons du menu
        setupMenuButton(namespace, "btnDashboard", "DASHBOARD", root);
        setupMenuButton(namespace, "btnUtilisateurs", "UTILISATEURS", root);
        setupMenuButton(namespace, "btnEvenements", "EVENEMENTS", root);
        setupMenuButton(namespace, "btnCoaching", "COACHING", root);
        setupMenuButton(namespace, "btnBlog", "BLOG", root);
        setupMenuButton(namespace, "btnProduits", "PRODUITS", root);

        Button btnLogout = (Button) namespace.get("btnLogout");
        if (btnLogout != null) {
            btnLogout.setOnAction(evt -> logout());
        }//new
        Button btnProfile = (Button) namespace.get("btnProfile");

        if (btnProfile != null) {
            btnProfile.setOnAction(e -> showUserProfileDialog());
        }

    }

    private void setupMenuButton(Map<String, Object> namespace, String buttonId, String moduleName, Parent root) {

        Button btn = (Button) namespace.get(buttonId);

        if (btn != null) {
            btn.setOnAction(e -> {

                // 🔐 Restrict UTILISATEURS access
                if ("UTILISATEURS".equals(moduleName)) {
                    if (currentUser == null || currentUser.getIdRole() != 1) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Access denied");
                        alert.setHeaderText(null);
                        alert.setContentText("You are not authorized to access this section.");
                        alert.showAndWait();
                        return; // 🚫 stop here
                    }
                }

                // ✅ If allowed → load module safely
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainDashboard.fxml"));
                    Parent loadedRoot = loader.load();  // may throw IOException

                    // Get the controller
                    MainDashboardController controller = loader.getController();

                    // Now call the method on the controller
                    controller.loadModule(moduleName);

                    // Optionally, replace content in your UI:
                    if (root instanceof AnchorPane) {
                        AnchorPane contentArea = (AnchorPane) root;
                        contentArea.getChildren().clear();
                        contentArea.getChildren().add(loadedRoot);
                        AnchorPane.setTopAnchor(loadedRoot, 0.0);
                        AnchorPane.setBottomAnchor(loadedRoot, 0.0);
                        AnchorPane.setLeftAnchor(loadedRoot, 0.0);
                        AnchorPane.setRightAnchor(loadedRoot, 0.0);
                    }

                } catch (IOException ex) {
                    ex.printStackTrace();
                    showDialog("Erreur", "Impossible de charger le module: " + moduleName);
                }
            });
        }
    }

    private void showUserProfileDialog() {

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("My Profile");

        DialogPane pane = dialog.getDialogPane();
        pane.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

        // main container
        VBox card = new VBox(12);
        card.getStyleClass().add("profile-card");

        // avatar circle (initials)
        String initials = currentUser.getPrenom().substring(0,1).toUpperCase()
                + currentUser.getNom().substring(0,1).toUpperCase();

        Label avatar = new Label(initials);
        avatar.getStyleClass().add("profile-avatar");

        Label name = new Label(currentUser.getPrenom() + " " + currentUser.getNom());
        name.getStyleClass().add("profile-name");

        Label role = new Label(getRoleName(currentUser.getIdRole()));
        role.getStyleClass().add("profile-role");

        VBox header = new VBox(5, avatar, name, role);
        header.setAlignment(Pos.CENTER);

        // info section
        VBox info = new VBox(8);
        info.getChildren().addAll(
                createProfileItem("📧 Email", currentUser.getEmail()),
                createProfileItem("📞 Phone", currentUser.getTelephone()),
                createProfileItem("📍 Address", currentUser.getAdresse())
        );

        card.getChildren().addAll(header, new Separator(), info);

        pane.setContent(card);
        pane.getButtonTypes().add(ButtonType.CLOSE);

        dialog.showAndWait();
    }//new

    private String getRoleName(int idRole) {
        return switch (idRole) {
            case 1 -> "Admin";
            case 2 -> "User";
            case 3 -> "Manager";
            default -> "Unknown";
        };
    }

    private HBox createProfileItem(String label, String value) {
        Label l1 = new Label(label + ":");
        l1.getStyleClass().add("profile-label");

        Label l2 = new Label(value != null ? value : "-");
        l2.getStyleClass().add("profile-value");

        HBox row = new HBox(10, l1, l2);
        return row;
    }//new


    /**
     * Met à jour le style du bouton actif
     */
    private void updateActiveButton(Parent root, Button activeButton) {

        String[] btnIds = { "#btnUtilisateurs", "#btnEvenements", "#btnCoaching", "#btnBlog", "#btnProduits" };

        for (String id : btnIds) {
            Button btn = (Button) root.lookup(id);

            if (btn != null) {
                btn.getStyleClass().remove("active-menu-btn");
                if (btn == activeButton) {
                    if (!btn.getStyleClass().contains("active-menu-btn")) {
                        btn.getStyleClass().add("active-menu-btn");
                    }
                }
            } else {

            }
        }

        // Fallback simple : juste mettre la classe sur le bouton actif
        if (activeButton != null && !activeButton.getStyleClass().contains("active-menu-btn")) {
            activeButton.getStyleClass().add("active-menu-btn");
        }
    }

    private void loadData() {

        dataList = FXCollections.observableArrayList();

        try {

            switch (currentModule) {

                case "UTILISATEURS":
                    dataList.addAll(userService.getAllUsers());
                    break;

                case "EVENEMENTS":
                    dataList.addAll(EvenementService.afficher());
                    break;

                case "COACHING":
                    break;

                case "BLOG":
                    break;

                case "PRODUITS":
                    dataList.addAll(productService.getAllProducts());
                    break;
            }

        } catch (Exception e) {
            System.err.println("❌ Database loading error: " + e.getMessage());
            showDialog("Database Error", "Unable to load data from database.");
        }

        updateTable();
        updateResultsLabel();
    }
    private Parent createDashboardView() {

        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.getStyleClass().add("dashboard-root");

        // ===== Title =====
        Label title = new Label("Overview");
        title.getStyleClass().add("dashboard-title");

        // ===== KPI CARDS =====
        HBox cards = new HBox(20);

        VBox usersCard = createStatCard("Users", getUsersCount(), "👤");
        VBox productsCard = createStatCard("Products", getProductsCount(), "📦");
        VBox eventsCard = createStatCard("Events", "12", "📅"); // fake for now
        VBox revenueCard = createStatCard("Revenue", "12,400 DT", "💰"); // fake

        cards.getChildren().addAll(usersCard, productsCard, eventsCard, revenueCard);

        // ===== Simple chart (fake data for now) =====
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle("Activity");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("Mon", 5));
        series.getData().add(new XYChart.Data<>("Tue", 8));
        series.getData().add(new XYChart.Data<>("Wed", 6));
        series.getData().add(new XYChart.Data<>("Thu", 10));
        series.getData().add(new XYChart.Data<>("Fri", 4));

        chart.getData().add(series);
        chart.setPrefHeight(300);

        root.getChildren().addAll(title, cards, chart);

        return root;
    }

    private String getUsersCount() {
        try {
            return String.valueOf(userService.getAllUsers().size());
        } catch (Exception e) {
            return "0";
        }
    }

    private String getProductsCount() {
        try {
            return String.valueOf(productService.getAllProducts().size());
        } catch (Exception e) {
            return "0";
        }
    }

    private VBox createStatCard(String title, String value, String icon) {

        Label lblIcon = new Label(icon);
        lblIcon.getStyleClass().add("stat-icon");

        Label lblTitle = new Label(title);
        lblTitle.getStyleClass().add("stat-title");

        Label lblValue = new Label(value);
        lblValue.getStyleClass().add("stat-value");

        VBox box = new VBox(8, lblIcon, lblTitle, lblValue);
        box.getStyleClass().add("stat-card");
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPrefWidth(180);

        return box;
    }


    /**
     * Initialise les composants et événements de la vue module
     */
    @SuppressWarnings("unchecked")
    private void initializeModuleView(java.util.Map<String, Object> namespace, Parent view) {
        // Récupération des composants via namespace (fx:id)
        dataTable = (TableView<Object>) namespace.get("dataTable");
        searchField = (TextField) namespace.get("searchField");
        lblResults = (Label) namespace.get("lblResults");
        sortComboBox = (ComboBox<String>) namespace.get("sortComboBox");

        Button btnAdd = (Button) namespace.get("btnAdd");
        Button btnExport = (Button) namespace.get("btnExport");
        Button btnDelete = (Button) namespace.get("btnDelete");
        Button btnStats = (Button) namespace.get("btnStats");

        // Configuration des événements
        if (btnAdd != null)
            btnAdd.setOnAction(e -> handleAdd());

        if (btnExport != null)
            btnExport.setOnAction(e -> handleExport());
        if (btnDelete != null)
            btnDelete.setOnAction(e -> handleDeleteSelected());
        if (btnStats != null)
            btnStats.setOnAction(e -> handleStatistics());

        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> filterData(newVal));
        }

        if (sortComboBox != null) {
            sortComboBox.setItems(FXCollections.observableArrayList(
                    "Prix (Croissant)", "Prix (Décroissant)",
                    "Stock (Croissant)", "Stock (Décroissant)"));
            sortComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null)
                    sortProducts(newVal);
            });
        }

        // Initialiser les colonnes de la table
        initializeTable();
    }



    /**
     * Initialiser la table selon le module
     */
    private void initializeTable() {

        if (dataTable == null)
            return;

        dataTable.getColumns().clear();

        // Colonne de sélection
        TableColumn<Object, Boolean> selectCol = new TableColumn<>("");
        selectCol.setMaxWidth(50);
        selectCol.setMinWidth(50);
        // Utiliser une CheckBox simple pour la démo
        selectCol.setCellFactory(col -> new TableCell<>() {
            private final CheckBox checkBox = new CheckBox();
            {
                checkBox.setOnAction(e -> {
                    // Logique de sélection à implémenter si besoin
                    getTableView().getSelectionModel().select(getIndex());
                });
                setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setTextFill(Color.BLACK);

                } else {
                    setGraphic(checkBox);
                    setTextFill(Color.BLACK);

                }
            }
        });
        dataTable.getColumns().add(selectCol);

        switch (currentModule) {
            case "UTILISATEURS":
                createUserColumns();
                break;
            case "EVENEMENTS":
                createEventColumns();
                loadEventData();
                break;
            case "COACHING":
                createCoachingColumns();
                break;
            case "BLOG":
                createBlogColumns();
                break;
            case "PRODUITS":
                createProductColumns();

                break;
        }

        // Colonne Actions
        TableColumn<Object, Void> actionCol = new TableColumn<>("Action");
        actionCol.setMinWidth(150);
        actionCol.setMaxWidth(150);
        actionCol.setCellFactory(createActionButtons());
        dataTable.getColumns().add(actionCol);
    }

    // --- Colonnes ---

    private void createUserColumns() {
        addColumn("ID", "id", 60);
        addColumn("Prénom", "firstName", 120);
        addColumn("Nom", "lastName", 120);
        addColumn("Email", "email", 200);
        addColumn("Rôle", "role", 100);
        addColumn("Dernière Connexion", "lastUpdate", 180);
    }

    private void createEventColumns() {

        // Titre
        TableColumn<Object, String> colTitre = new TableColumn<>("Titre");
        colTitre.setCellValueFactory(cellData ->
                new SimpleStringProperty(((Evenement)cellData.getValue()).getTitre())
        );
        colTitre.setMinWidth(150);
        dataTable.getColumns().add(colTitre);

// Description
        TableColumn<Object, String> colDescription = new TableColumn<>("Description");
        colDescription.setCellValueFactory(cellData ->
                new SimpleStringProperty(((Evenement)cellData.getValue()).getDescription())
        );
        colDescription.setMinWidth(200);
        dataTable.getColumns().add(colDescription);

// Lieu
        TableColumn<Object, String> colLieu = new TableColumn<>("Lieu");
        colLieu.setCellValueFactory(cellData ->
                new SimpleStringProperty(((Evenement)cellData.getValue()).getLieu())
        );
        colLieu.setMinWidth(140);
        dataTable.getColumns().add(colLieu);

// Statut
        TableColumn<Object, String> colStatut = new TableColumn<>("Statut");
        colStatut.setCellValueFactory(cellData ->
                new SimpleStringProperty(((Evenement)cellData.getValue()).getStatut())
        );
        colStatut.setMinWidth(100);
        dataTable.getColumns().add(colStatut);

        // Capacité (int)
        TableColumn<Object, Integer> colCap = new TableColumn<>("Capacité");
        colCap.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(((Evenement)cellData.getValue()).getCapacite()));
        colCap.setMinWidth(80);
        dataTable.getColumns().add(colCap);

        // Prix (double)
        TableColumn<Object, Double> colPrix = new TableColumn<>("Prix");
        colPrix.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(((Evenement)cellData.getValue()).getPrix()));
        colPrix.setMinWidth(80);
        dataTable.getColumns().add(colPrix);

        // --- Date fields ---
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        TableColumn<Object, String> colDebut = new TableColumn<>("Date Début");
        colDebut.setCellValueFactory(cellData ->
                new SimpleStringProperty(((Evenement)cellData.getValue()).getDateDebut().format(formatter))
        );
        colDebut.setMinWidth(120);
        dataTable.getColumns().add(colDebut);

        TableColumn<Object, String> colFin = new TableColumn<>("Date Fin");
        colFin.setCellValueFactory(cellData ->
                new SimpleStringProperty(((Evenement)cellData.getValue()).getDateFin().format(formatter))
        );
        colFin.setMinWidth(120);
        dataTable.getColumns().add(colFin);
    }




    private void createCoachingColumns() {
        addColumn("ID", "id", 60);
        addColumn("Coach", "coachName", 150);
        addColumn("Spécialité", "specialty", 150);
        addColumn("Client", "clientName", 150);
        addColumn("Prochaine Session", "nextSession", 180);
        addColumn("Statut", "status", 100);
    }

    private void createBlogColumns() {
        addColumn("ID", "id", 60);
        addColumn("Titre", "title", 250);
        addColumn("Auteur", "author", 150);
        addColumn("Catégorie", "category", 120);
        addColumn("Date Publication", "publishDate", 150);
        addColumn("Vues", "views", 80);
    }

    private void createProductColumns() {
        addColumn("ID", "idProduit", 40);
        addColumn("Nom", "nomProduit", 130);
        addColumn("Description", "description", 160);
        addColumn("Prix", "prix", 70);
        addColumn("Stock", "stockDisponible", 60);
        addColumn("Catégorie", "categorie", 90);
        addColumn("Image", "imageUrl", 90);
        addColumn("Statut", "statut", 80);
    }

    private void addColumn(String title, String property, double width) {
        TableColumn<Object, String> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        column.setMinWidth(width);
        dataTable.getColumns().add(column);
    }


    private void loadEventData() {
        if (!"EVENEMENTS".equals(currentModule)) return;

        // ✅ Instantiate the service
        EvenementService evenementService = new EvenementService();

        // load from database
        List<Evenement> events = evenementService.afficher();

        // convert to ObservableList
        dataList = FXCollections.observableArrayList(events);

        // update the table
        updateTable();
        updateResultsLabel();
    }


    /**
     * Créer les boutons d'action pour chaque ligne
     */
    private Callback<TableColumn<Object, Void>, TableCell<Object, Void>> createActionButtons() {
        return param -> new TableCell<>() {
            private final Button viewBtn = new Button("👁");
            private final Button editBtn = new Button("✎");
            private final Button deleteBtn = new Button("🗑");
            private final HBox actionBox = new HBox(5);

            {
                viewBtn.getStyleClass().add("action-btn-view");
                editBtn.getStyleClass().add("action-btn-edit");
                deleteBtn.getStyleClass().add("action-btn-delete");

                viewBtn.setOnAction(e -> handleView(getTableRow().getItem()));
                editBtn.setOnAction(e -> handleEdit(getTableRow().getItem()));
                deleteBtn.setOnAction(e -> handleDelete(getTableRow().getItem()));

                actionBox.getChildren().addAll(viewBtn, editBtn, deleteBtn);
                actionBox.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : actionBox);
            }
        };
    }

    /**
     * Charger les données selon le module
     */


    private void updateTable() {
        if (dataTable != null) {
            dataTable.setItems(dataList);
        }
    }

    private void updateResultsLabel() {
        if (lblResults != null && dataList != null) {
            lblResults.setText(String.format("Résultats 1 à %d de %d",
                    Math.min(itemsPerPage, dataList.size()), dataList.size()));
        }
    }

    /**
     * Filtrer les données
     */
    private void filterData(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            updateTable();
        } else {
            ObservableList<Object> filteredList = FXCollections.observableArrayList();
            for (Object item : dataList) {
                if (item.toString().toLowerCase().contains(searchText.toLowerCase())) {
                    filteredList.add(item);
                }
            }
            if (dataTable != null)
                dataTable.setItems(filteredList);
            // updateResultsLabel pour filtré (optionnel)
        }
    }

    /**
     *
     * evenement
     */
    // Ajouter un événement
    private void handleAdd() {
        switch (currentModule) {
            case "EVENEMENTS":
                showEvenementDialog(null);
                break;
            case "PRODUITS":
                showProductDialog(null);
                break;
            default:
                showDialog("Ajouter", "Fonction d'ajout pour " + currentModule);
                break;
        }
    }


    // Voir les détails d'un événement
    private void handleView(Object item) {
        if (item == null) return;

        switch (currentModule) {
            case "EVENEMENTS":
                if (item instanceof Evenement) {
                    showEvenementDialog((Evenement) item); // Affiche ou modifie un événement
                }
                break;
            case "PRODUITS":
                if (item instanceof Product) {
                    showProductDialog((Product) item); // Affiche ou modifie un produit
                }
                break;
            default:
                showDialog("Voir", "Détails de l'élément: " + item.toString());
                break;
        }
    }


    // Modifier un événement
    private void handleEdit(Object item) {
        if ("EVENEMENTS".equals(currentModule) && item instanceof Evenement) {
            showEvenementDialog((Evenement) item);
            service.modifier((Evenement) item);

        } else if ("PRODUITS".equals(currentModule) && item instanceof Product) {
            showProductDialog((Product) item);
        } else {
            showDialog("Modifier", "Fonction de modification pour " + currentModule);
        }
    }
    /**
     * Afficher le dialogue d'événement avec design amélioré
     */
    private void showEvenementDialog(Evenement evenement) {


        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/EvenementView.fxml")
            );

            VBox root = loader.load();

            // Récupérer le controller
            EvenementController controller = loader.getController();


            // Si on passe un événement (mode modification)
            if (evenement != null) {
                controller.remplirFormulaire(evenement);
            }

            Stage stage = new Stage();
            stage.setTitle(evenement == null ? "Ajouter Événement" : "Modifier Événement");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();


        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erreur : impossible de charger EvenementView.fxml");
        }
    }


    @FXML
    private void modifier() {
        Evenement selected = tableEvenements.getSelectionModel().getSelectedItem();
        if (selected != null) {

            selected.setDescription(txtDescription.getText());
            selected.setLieu(txtLieu.getText());
            selected.setCapacite(Integer.parseInt(txtCapacite.getText()));
            selected.setPrix(Double.parseDouble(txtPrix.getText()));
            selected.setStatut(cbStatut.getValue());

            service.modifier(selected);
            data.clear();
            data.addAll(service.afficher());
            clearForm();
        }
    }

    @FXML
    private void annuler() {
        clearForm();
    }
    private void refreshTable() {
        data.clear();
        data.addAll(service.afficher());
        tableEvenements.setItems(data);
    }

    @FXML
    private void supprimer() {
        Evenement selected = tableEvenements.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Sélectionnez un événement !", "Vérifiez les champs !");
            return;
        }

        // Confirm deletion (optional)
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer l'événement ?");
        confirm.setContentText(selected.getTitre());
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        // Remove from database
        service.supprimer(selected.getIdEvenement());

        // Refresh table and clear form
        refreshTable();
        clearForm();
    }


    private void clearForm() {
        txtTitre.clear();
        txtDescription.clear();
        txtLieu.clear();
        txtCapacite.clear();
        txtPrix.clear();
        cbStatut.setValue(null);
        tableEvenements.getSelectionModel().clearSelection();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }






    /**
     * Trier les produits
     */
    private void sortProducts(String sortOption) {
        if (!"PRODUITS".equals(currentModule))
            return;

        java.util.Comparator<Object> comparator = null;

        switch (sortOption) {
            case "Prix (Croissant)":
                comparator = (o1, o2) -> {
                    try {
                        Double p1 = Double
                                .parseDouble(((Product) o1).getPrix().replace("€", "").replace(",", ".").trim());
                        Double p2 = Double
                                .parseDouble(((Product) o2).getPrix().replace("€", "").replace(",", ".").trim());
                        return p1.compareTo(p2);
                    } catch (Exception e) {
                        return 0;
                    }
                };
                break;
            case "Prix (Décroissant)":
                comparator = (o1, o2) -> {
                    try {
                        Double p1 = Double
                                .parseDouble(((Product) o1).getPrix().replace("€", "").replace(",", ".").trim());
                        Double p2 = Double
                                .parseDouble(((Product) o2).getPrix().replace("€", "").replace(",", ".").trim());
                        return p2.compareTo(p1);
                    } catch (Exception e) {
                        return 0;
                    }
                };
                break;
            case "Stock (Croissant)":
                comparator = (o1, o2) -> Integer.compare(((Product) o1).getStockDisponible(),
                        ((Product) o2).getStockDisponible());
                break;
            case "Stock (Décroissant)":
                comparator = (o1, o2) -> Integer.compare(((Product) o2).getStockDisponible(),
                        ((Product) o1).getStockDisponible());
                break;
        }

        if (comparator != null) {
            FXCollections.sort(dataList, comparator);
            updateTable();
        }
    }

    // --- Actions ---


    private void handleDeleteSelected() {

        // Activer la sélection multiple
        dataTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Copier la sélection pour éviter les problèmes pendant la suppression
        List<Object> itemsToDelete = new ArrayList<>(dataTable.getSelectionModel().getSelectedItems());

        if (itemsToDelete.isEmpty()) {
            showDialog("Erreur", "Aucun élément sélectionné !");
            return;
        }

        // Confirmation
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer les éléments sélectionnés ?");
        alert.setContentText("Êtes-vous sûr ?");
        alert.initOwner(dataTable.getScene().getWindow());

        if (alert.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK)
            return;

        // Supprimer tous les éléments
        for (Object item : itemsToDelete) {
            if ("PRODUITS".equals(currentModule) && item instanceof Product) {
                Product p = (Product) item;
                productService.deleteProduct(p.getIdProduit());
            } else if ("EVENEMENTS".equals(currentModule) && item instanceof Evenement) {
                Evenement e = (Evenement) item;
                service.supprimer(e.getIdEvenement());
            }

            // Retirer de la liste observable
            dataList.remove(item);
        }

        // Rafraîchir TableView et labels
        updateTable();
        updateResultsLabel();

        // Vider la sélection
        dataTable.getSelectionModel().clearSelection();

        // Message succès
        showDialog("Succès", "✅ Tous les éléments sélectionnés ont été supprimés !");
    }

    private void handleDelete(Object item) {
        if (item == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer l'élément ?");
        alert.setContentText("Êtes-vous sûr ?");
        alert.initOwner(dataTable.getScene().getWindow());

        if (alert.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK)
            return;

        if ("PRODUITS".equals(currentModule) && item instanceof Product) {
            Product p = (Product) item;
            if (productService.deleteProduct(p.getIdProduit())) {
                dataList.remove(item);
                updateTable();
                showDialog("Succès", "Produit supprimé !");
            }
        } else if ("EVENEMENTS".equals(currentModule) && item instanceof Evenement) {
            Evenement e = (Evenement) item;
            if (service.supprimer(e.getIdEvenement())) {
                dataList.remove(item);
                updateTable();
                showDialog("Succès", "Événement supprimé !");
            }
        }
    }

    /**
     * Gérer l'export PDF vers le dossier Téléchargements
     */
    private void handleExport() {
        if (!"PRODUITS".equals(currentModule)) {
            showDialog("Information", "L'export PDF est disponible uniquement pour les produits.");
            return;
        }

        try {
            String userHome = System.getProperty("user.home");
            String downloadsPath = userHome + File.separator + "Downloads" + File.separator;
            String fileName = downloadsPath + "Export_Produits_" + System.currentTimeMillis() + ".pdf";

            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(fileName));
            document.open();

            // Titre
            com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA,
                    18, com.itextpdf.text.Font.BOLD);
            Paragraph title = new Paragraph("Liste des Produits", titleFont);
            title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Table
            PdfPTable table = new PdfPTable(6); // 6 colonnes
            table.setWidthPercentage(100);
            String[] headers = { "ID", "Nom", "Prix", "Stock", "Catégorie", "Statut" };

            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header));
                cell.setBackgroundColor(com.itextpdf.text.BaseColor.LIGHT_GRAY);
                cell.setPadding(5);
                table.addCell(cell);
            }

            for (Object item : dataList) {
                if (item instanceof Product) {
                    Product p = (Product) item;
                    table.addCell(String.valueOf(p.getIdProduit()));
                    table.addCell(p.getNomProduit());
                    table.addCell(p.getPrix());
                    table.addCell(String.valueOf(p.getStockDisponible()));
                    table.addCell(p.getCategorie());
                    table.addCell(p.getStatut());
                }
            }

            document.add(table);
            document.close();

            showDialog("Succès", "✅ Export PDF réussi dans Téléchargements :\n" + fileName);

            // Ouvrir le fichier
            try {
                java.awt.Desktop.getDesktop().open(new java.io.File(fileName));
            } catch (Exception e) {
                // Ignorer
            }

        } catch (Exception e) {
            e.printStackTrace();
            showDialog("Erreur", "❌ Erreur lors de l'export PDF: " + e.getMessage());
        }
    }

    /**
     * Gérer les statistiques
     */
    private void handleStatistics() {
        if (!"PRODUITS".equals(currentModule)) {
            showDialog("Info", "Statistiques disponibles uniquement pour les produits.");
            return;
        }

        Stage statsStage = new Stage();
        statsStage.setTitle("📊 Statistiques Produits");

        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: white;");

        // 1. PieChart - Répartition par Catégorie
        javafx.scene.chart.PieChart pieChart = new javafx.scene.chart.PieChart();
        pieChart.setTitle("Répartition par Catégorie");

        java.util.Map<String, Integer> categoryCounts = new java.util.HashMap<>();
        java.util.Map<String, Integer> statusCounts = new java.util.HashMap<>();

        for (Object item : dataList) {
            if (item instanceof Product) {
                Product p = (Product) item;
                categoryCounts.put(p.getCategorie(), categoryCounts.getOrDefault(p.getCategorie(), 0) + 1);
                statusCounts.put(p.getStatut(), statusCounts.getOrDefault(p.getStatut(), 0) + 1);
            }
        }

        categoryCounts.forEach((cat, count) -> pieChart.getData()
                .add(new javafx.scene.chart.PieChart.Data(cat + " (" + count + ")", count)));

        // 2. BarChart - Répartition par Statut
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Statut");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Nombre");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("État du Stock");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Produits");
        statusCounts.forEach((stat, count) -> series.getData().add(new XYChart.Data<>(stat, count)));
        barChart.getData().add(series);

        // Disposition côte à côte
        HBox chartsBox = new HBox(20);
        chartsBox.getChildren().addAll(pieChart, barChart);
        chartsBox.setAlignment(Pos.CENTER);
        HBox.setHgrow(pieChart, Priority.ALWAYS);
        HBox.setHgrow(barChart, Priority.ALWAYS);

        root.getChildren().add(chartsBox);

        Scene scene = new Scene(root, 1000, 500);
        statsStage.setScene(scene);
        statsStage.show();
    }

    /**
     * Afficher le dialogue de produit avec design amélioré et harmonisé
     */
    private void showProductDialog(Product product) {
        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle(product == null ? "Nouveau Produit" : "Modifier Produit");
        dialog.setHeaderText(null);

        // Appliquer le CSS global au dialogue
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        dialogPane.getStyleClass().add("alert");

        ButtonType saveButtonType = new ButtonType(product == null ? "Ajouter" : "Enregistrer",
                ButtonBar.ButtonData.OK_DONE);
        dialogPane.getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Styliser les boutons
        Button saveBtn = (Button) dialogPane.lookupButton(saveButtonType);
        saveBtn.getStyleClass().add("btn-primary");

        Button cancelBtn = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
        cancelBtn.getStyleClass().add("btn-secondary");

        // Conteneur principal
        VBox mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(25));
        mainContainer.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        mainContainer.setPrefWidth(550);

        // Titre
        Label lblTitle = new Label(product == null ? "AJOUTER UN NOUVEAU PRODUIT" : "MODIFIER LE PRODUIT");
        lblTitle.setStyle(
                "-fx-font-size: 20px; -fx-font-weight: 800; -fx-text-fill: #17BB9C; -fx-font-family: 'Segoe UI', sans-serif;");
        mainContainer.getChildren().add(lblTitle);

        // Séparateur coloré
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #17BB9C; -fx-pref-height: 2px;");
        mainContainer.getChildren().add(sep);

        // ScrollPane pour le formulaire
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scrollPane.setPrefHeight(450); // Hauteur visible du formulaire

        // Grille pour les champs
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setPadding(new Insets(10)); // Marge interne

        // Définir les contraintes de colonnes pour éviter les "..."
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(140); // Largeur fixe suffisante pour les labels
        col1.setHalignment(javafx.geometry.HPos.RIGHT); // Aligner les labels à droite

        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS); // Le champ prend tout l'espace restant

        grid.getColumnConstraints().addAll(col1, col2);

        // Champs
        TextField txtNom = createStyledTextField("Nom du produit");
        TextArea txtDesc = createStyledTextArea("Description détaillée");
        TextField txtPrix = createStyledTextField("0.00");
        TextField txtStock = createStyledTextField("0");
        ComboBox<String> cmbCat = createStyledComboBox("Sélectionner une catégorie");
        ComboBox<String> cmbStat = createStyledComboBox("Sélectionner un statut");

        // Image Selection
        TextField txtImg = createStyledTextField("Chemin de l'image");
        txtImg.setEditable(false);
        Button btnChooseImg = new Button("📁 Parcourir");
        btnChooseImg.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #2d3748; -fx-cursor: hand;");

        ImageView imgPreview = new ImageView();
        imgPreview.setFitHeight(100);
        imgPreview.setFitWidth(100);
        imgPreview.setPreserveRatio(true);
        imgPreview.setStyle(
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0); -fx-background-color: #f7fafc;");

        // Wrapper pour l'aperçu avec bordure
        StackPane previewWrapper = new StackPane(imgPreview);
        previewWrapper
                .setStyle("-fx-border-color: #cbd5e0; -fx-border-radius: 5; -fx-border-style: dashed; -fx-padding: 5;");
        previewWrapper.setMaxSize(110, 110);
        previewWrapper.setAlignment(Pos.CENTER);

        btnChooseImg.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choisir l'image du produit");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"));
            File selectedFile = fileChooser.showOpenDialog(dialog.getOwner());
            if (selectedFile != null) {
                String imagePath = selectedFile.toURI().toString();
                txtImg.setText(imagePath);
                try {
                    imgPreview.setImage(new Image(imagePath));
                } catch (Exception ex) {
                    System.err.println("Impossible de charger l'image: " + ex.getMessage());
                }
            }
        });

        HBox imgBox = new HBox(10, txtImg, btnChooseImg);
        HBox.setHgrow(txtImg, Priority.ALWAYS);
        imgBox.setAlignment(Pos.CENTER_LEFT);

        cmbCat.setItems(FXCollections.observableArrayList(
                "Formation", "Livre", "Abonnement", "Kit", "Logiciel", "Service"));
        cmbStat.setItems(FXCollections.observableArrayList(
                "Disponible", "Rupture", "Actif", "Inactif"));

        // Remplir si modification
        if (product != null) {
            txtNom.setText(product.getNomProduit());
            txtDesc.setText(product.getDescription());
            txtPrix.setText(product.getPrix());
            txtStock.setText(String.valueOf(product.getStockDisponible()));
            cmbCat.setValue(product.getCategorie());
            txtImg.setText(product.getImageUrl());
            cmbStat.setValue(product.getStatut());

            if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                try {
                    imgPreview.setImage(new Image(product.getImageUrl()));
                } catch (Exception ex) {
                }
            }
        } else {
            // Valeurs par défaut pour nouveau produit
            cmbCat.getSelectionModel().selectFirst();
            cmbStat.getSelectionModel().selectFirst();
        }

        // Layout du formulaire (Labels + Champs)
        int row = 0;

        // On reconstruit l'ajout propre
        addFormRow(grid, "Nom du Produit", txtNom, row++);
        addFormRow(grid, "Catégorie", cmbCat, row++);
        addFormRow(grid, "Statut", cmbStat, row++);

        // Prix et Stock sur la même ligne (row) pour gagner de la place ? Non, gardons
        // simple pour l'instant.
        addFormRow(grid, "Prix (€)", txtPrix, row++);
        addFormRow(grid, "Stock", txtStock, row++);

        addFormRow(grid, "Image", imgBox, row++);

        // Aperçu décalé
        grid.add(previewWrapper, 1, row++);

        addFormRow(grid, "Description", txtDesc, row++);

        scrollPane.setContent(grid);
        mainContainer.getChildren().add(scrollPane);

        dialog.getDialogPane().setContent(mainContainer);

        // Validation et Conversion
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    // Validation simple
                    if (txtNom.getText().isEmpty() || txtPrix.getText().isEmpty()) {
                        showDialog("Validation", "Le nom et le prix du produit sont obligatoires.");
                        return null;
                    }

                    Product p = product == null ? new Product() : product;
                    p.setNomProduit(txtNom.getText());
                    p.setDescription(txtDesc.getText());
                    p.setPrix(txtPrix.getText());
                    p.setStockDisponible(Integer.parseInt(txtStock.getText()));
                    p.setCategorie(cmbCat.getValue());
                    p.setImageUrl(txtImg.getText());
                    p.setStatut(cmbStat.getValue());
                    return p;
                } catch (NumberFormatException e) {
                    showDialog("Erreur de saisie", "Veuillez vérifier les champs numériques (Prix, Stock).");
                    return null;
                } catch (Exception e) {
                    showDialog("Erreur", "Une erreur inattendue est survenue: " + e.getMessage());
                    return null;
                }
            }
            return null;
        });

        Optional<Product> result = dialog.showAndWait();
        result.ifPresent(p -> {
            boolean success;
            if (product == null) {
                success = productService.addProduct(p);
                if (success) {
                    dataList.add(p);
                    showDialog("Succès", "✅ Produit ajouté avec succès !");
                }
            } else {
                success = productService.updateProduct(p);
                if (success)
                    showDialog("Succès", "✅ Produit mis à jour avec succès !");
            }
            if (success) {
                updateTable();
                updateResultsLabel();
            } else {
                showDialog("Erreur", "❌ Une erreur est survenue lors de l'enregistrement.");
            }
        });
    }

    // Helper pour ajouter une ligne au formulaire proprement
    private void addFormRow(GridPane grid, String labelText, javafx.scene.Node field, int row) {
        Label lbl = new Label(labelText);
        lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #2d3748; -fx-font-size: 13px;");
        GridPane.setValignment(lbl, javafx.geometry.VPos.CENTER);

        // Si c'est une TextArea, on l'aligne en haut
        if (field instanceof TextArea) {
            GridPane.setValignment(lbl, javafx.geometry.VPos.TOP);
            GridPane.setMargin(lbl, new Insets(5, 0, 0, 0));
        }

        grid.add(lbl, 0, row);
        grid.add(field, 1, row);
    }

    // Méthodes utilitaires pour le style
    private Label createStyledLabel(String text) {
        Label lbl = new Label(text);
        lbl.getStyleClass().add("form-label");
        return lbl;
    }

    private TextField createStyledTextField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.getStyleClass().add("form-field");
        return tf;
    }


    private TextArea createStyledTextArea(String prompt) {
        TextArea ta = new TextArea();
        ta.setPromptText(prompt);
        ta.setPrefRowCount(3);
        ta.getStyleClass().add("text-area"); // Utiliser la classe CSS text-area existante
        return ta;
    }

    private ComboBox<String> createStyledComboBox(String prompt) {
        ComboBox<String> cb = new ComboBox<>();
        cb.setPromptText(prompt);
        cb.getStyleClass().add("combo-box");
        cb.setMaxWidth(Double.MAX_VALUE);
        return cb;
    }

    private void showDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String getCurrentDateTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm"));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
