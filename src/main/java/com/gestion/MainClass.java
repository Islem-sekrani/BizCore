package com.gestion;

import com.gestion.entities.*;

import java.util.Map;

import com.gestion.services.AiService;
import com.gestion.services.ProductService;
import com.gestion.interfaces.IProductService;
import com.gestion.services.UserService; //new
import com.gestion.entities.User;//new
import com.gestion.tools.EmailService;
import com.gestion.tools.PasswordStrengthUtil;
import java.awt.Desktop;
import com.gestion.tools.PasswordUtil;
import javafx.collections.FXCollections;//new
import com.gestion.interfaces.IUserService;//new
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Callback;

import javafx.scene.control.*;

import java.util.UUID;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

// imports near top of file
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

// Imports supplémentaires pour PDF et UI
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.FileOutputStream;

import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.geometry.Insets;
import javafx.stage.FileChooser;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;//new


/**
 * Classe principale de l'application GestionPlatform
 * Gère l'interface utilisateur et la logique métier
 * Charge les fichiers FXML pour conserver le design original
 */
public class MainClass extends Application {

    // Services
    private IProductService productService;
    private IUserService userService; //new

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

    private boolean sortAsc = true;//new

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

    private String recaptchaToken = null;
    private com.sun.net.httpserver.HttpServer server;

    private void startLocalServer() {

        if (server != null) return;

        try {

            server = com.sun.net.httpserver.HttpServer.create(
                    new java.net.InetSocketAddress(8080), 0);

            // main page
            server.createContext("/", exchange -> {

                java.io.InputStream is =
                        getClass().getResourceAsStream("/recaptcha.html");

                byte[] response = is.readAllBytes();

                exchange.sendResponseHeaders(200, response.length);
                exchange.getResponseBody().write(response);
                exchange.close();
            });

            // success callback
            server.createContext("/success", exchange -> {

                String query = exchange.getRequestURI().getQuery();

                if (query != null && query.contains("token=")) {
                    recaptchaToken = query.split("token=")[1];
                    System.out.println("Token received: " + recaptchaToken);
                }

                String response = "Verification complete. You can close this tab.";
                exchange.sendResponseHeaders(200, response.length());
                exchange.getResponseBody().write(response.getBytes());
                exchange.close();
            });

            server.start();
            System.out.println("Local server started on 8080");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean verifyRecaptcha(String token) {

        try {
            String secret = "6LedT34sAAAAAFTMJzLiiZ4vuipDQGjoXebvUt3Q";

            String params = "secret=" + secret + "&response=" + token;

            java.net.URL url = new java.net.URL("https://www.google.com/recaptcha/api/siteverify");
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            try (java.io.OutputStream os = conn.getOutputStream()) {
                os.write(params.getBytes());
            }

            java.io.InputStream is = conn.getInputStream();
            String result = new String(is.readAllBytes());
            System.out.println("Google response: " + result);
            return result.contains("\"success\": true");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private boolean showRecaptcha() {

        try {

            startLocalServer();  // start server once

            final boolean[] verified = {false};

            // open system browser instead of WebView
            java.awt.Desktop.getDesktop().browse(
                    new java.net.URI("http://localhost:8080/")
            );

            // wait until token is received
            while (!verified[0]) {
                Thread.sleep(500);
                if (recaptchaToken != null) {
                    verified[0] = verifyRecaptcha(recaptchaToken);
                    break;
                }
            }

            return verified[0];

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void showResetDialog() {

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Reset Password");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField txtToken = new TextField();
        PasswordField txtNewPassword = new PasswordField();
        Label lblStrength = new Label();

        grid.add(new Label("Token:"), 0, 0);
        grid.add(txtToken, 1, 0);

        grid.add(new Label("New Password:"), 0, 1);
        grid.add(txtNewPassword, 1, 1);

        grid.add(lblStrength, 1, 2);

        // 🔥 SAME STRENGTH LOGIC
        txtNewPassword.textProperty().addListener((obs, oldVal, newVal) -> {

            int score = PasswordStrengthUtil.calculateScore(newVal);
            String strength = PasswordStrengthUtil.getStrengthLabel(score);

            lblStrength.setText("Strength: " + strength);

            switch (strength) {
                case "Weak":
                    lblStrength.setStyle("-fx-text-fill: red;");
                    break;
                case "Medium":
                    lblStrength.setStyle("-fx-text-fill: orange;");
                    break;
                case "Strong":
                    lblStrength.setStyle("-fx-text-fill: green;");
                    break;
            }
        });

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<Void> result = dialog.showAndWait();

        if (result.isPresent()) {

            // 🔥 BLOCK IF WEAK
            if (PasswordStrengthUtil.calculateScore(txtNewPassword.getText()) <= 2) {
                showDialog("Validation", "Password too weak!");
                return;
            }

            String hashed = PasswordUtil.hashPassword(txtNewPassword.getText());

            boolean reset = userService.resetPassword(txtToken.getText(), hashed);

            if (reset) {
                showDialog("Success", "Password updated successfully!");
            } else {
                showDialog("Error", "Invalid or expired token.");
            }
        }
    }

    private void showForgotDialog() {

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Forgot Password");
        dialog.setHeaderText("Enter your email");

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(email -> {

            User user = userService.getUserByEmail(email);

            if (user == null) {
                showDialog("Error", "Email not found");
                return;
            }

            String token = java.util.UUID.randomUUID().toString();
            Timestamp expiry = new Timestamp(System.currentTimeMillis() + (15 * 60 * 1000));

            boolean created = userService.createResetToken(email, token, expiry);

            if (created) {

                EmailService.sendEmail(
                        email,
                        "Password Reset",
                        "Your reset code is:\n\n" + token +
                                "\n\nValid for 15 minutes."
                );

                showResetDialog();

            }
        });
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
            Button btnForgot = (Button) root.lookup("#btnForgot");

            if (btnForgot != null) {
                btnForgot.setOnAction(e -> showForgotDialog());
            }

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

                if (!user.isEmailConfirmed()) {
                    lblMessage.setText("Please confirm your email first");
                    return;
                }

                if (!PasswordUtil.checkPassword(password, user.getPassword())) {
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

            Scene scene = new Scene(root, 400, 400);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("Login");
            stage.show();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }//new

    private void showConfirmDialog(User user) {

        while (true) {

            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Confirm Account");
            dialog.setHeaderText("Enter the confirmation code sent to your email");

            Optional<String> result = dialog.showAndWait();

            // Si user ferme la fenêtre
            if (result.isEmpty()) {
                showDialog("Confirmation Required", "You must confirm your account.");
                continue;
            }

            String token = result.get().trim();
            if (!showRecaptcha()) {
                showDialog("Security Error", "reCAPTCHA validation failed.");
                return;
            }
            boolean confirmed = userService.confirmUser(token);

            if (confirmed) {

                showDialog("Success", "Account confirmed successfully!");

                currentUser = user;

                Timestamp now = Timestamp.from(java.time.Instant.now());
                userService.updateLastConnection(user.getIdUser(), now);

                openDashboard(primaryStageRef);

                break; // 🔥 on sort de la boucle seulement si correct
            } else {
                showDialog("Error", "Invalid confirmation code. Try again.");
            }
        }
    }

    private void showSignupDialog() {

        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Create account");

        DialogPane pane = dialog.getDialogPane();
        pane.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        pane.setPrefHeight(300);
        dialog.setResizable(true);

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

        Label lblStrength = new Label();
        lblStrength.setWrapText(true);
        lblStrength.setPrefWidth(400);
        grid.add(lblStrength, 0, 4, 2, 1);

        txtPassword.textProperty().addListener((obs, oldVal, newVal) -> {

            int score = PasswordStrengthUtil.calculateScore(newVal);
            String strength = PasswordStrengthUtil.getStrengthLabel(score);

            lblStrength.setText("Strength: " + strength);

            switch (strength) {
                case "Weak":
                    lblStrength.setStyle("-fx-text-fill: red;");
                    break;
                case "Medium":
                    lblStrength.setStyle("-fx-text-fill: orange;");
                    break;
                case "Strong":
                    lblStrength.setStyle("-fx-text-fill: green;");
                    break;
            }

            // 🔥 Detailed feedback
            List<String> missing = PasswordStrengthUtil.getMissingCriteria(newVal);
            if (!missing.isEmpty()) {
                lblStrength.setText(
                        "Strength: " + strength +
                                "\nMissing: " + String.join(", ", missing)
                );
            }
        });

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
                String hashed = PasswordUtil.hashPassword(txtPassword.getText());

                User u = new User(
                        txtNom.getText(),
                        txtPrenom.getText(),
                        txtEmail.getText(),
                        hashed,
                        roleId
                );
                String token = java.util.UUID.randomUUID().toString();
                u.setConfirmationToken(token);

                return u;
            }
            return null;
        });

        Optional<User> result = dialog.showAndWait();

        result.ifPresent(u -> {

            String token = java.util.UUID.randomUUID().toString();
            u.setConfirmationToken(token);
            u.setEmailConfirmed(false);

            boolean success = userService.addUser(u);

            if (success) {

                // envoyer email
                EmailService.sendEmail(
                        u.getEmail(),
                        "Confirm your account",
                        "Your confirmation code is:\n\n" + token +
                                "\n\nEnter this code inside the application."
                );

                showDialog("Email sent", "A confirmation code was sent to your email.");

                // 🔥 LANCER DIRECTEMENT LA FENETRE DE CONFIRMATION
                showConfirmDialog(u);

            } else {
                showDialog("Error", "Error creating account (email may already exist)");
            }
        });
    }//new

    private void openDashboard(Stage primaryStage) {
        try {
            // save reference
            this.primaryStageRef = primaryStage;

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainDashboard.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 1400, 800);

            initializeDashboard(loader.getNamespace(), root);
            loadModule("DASHBOARD");

            primaryStage.setTitle("Admin Panel");
            primaryStage.setScene(scene);
            primaryStage.setMaximized(true);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }//new

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
        contentArea = (AnchorPane) namespace.get("contentArea");

        lblUserName = (Label) namespace.get("lblUserName");
        lblUserRole = (Label) namespace.get("lblUserRole");

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

    /**
     * Configure un bouton de menu
     */
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

                // ✅ If allowed → load module
                loadModule(moduleName);
            });
        }
    }//new

    private void showUserProfileDialog() {

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("My Profile");

        DialogPane pane = dialog.getDialogPane();
        pane.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

        // main container
        VBox card = new VBox(12);
        card.getStyleClass().add("profile-card");

        // avatar circle (initials)
        String initials = currentUser.getPrenom().substring(0, 1).toUpperCase()
                + currentUser.getNom().substring(0, 1).toUpperCase();

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
        // Pour gérer les classes CSS, on peut parcourir tous les boutons du namespace
        // si on l'avait stocké,
        // ou utiliser un lookup CSS sur la racine si les boutons n'ont pas d'ID CSS.
        // Ici, on va utiliser lookup car les boutons ont des styleClass.
        String[] btnIds = {"#btnUtilisateurs", "#btnEvenements", "#btnCoaching", "#btnBlog", "#btnProduits"};

        for (String id : btnIds) {
            Button btn = (Button) root.lookup(id);
            // Si lookup échoue (pas d'ID CSS), on peut essayer de retrouver le composant
            // d'une autre manière,
            // mais ici on suppose que fx:id génère un ID CSS par défaut avec FXMLLoader, ce
            // qui n'est pas garanti.
            // Si fx:id="toto", FXMLLoader fait souvent node.setId("toto") si aucun
            // contrôleur n'est défini.
            // Vérifions si c'est le cas. Sinon, on devra stocker les références.

            if (btn != null) {
                btn.getStyleClass().remove("active-menu-btn");
                if (btn == activeButton) {
                    if (!btn.getStyleClass().contains("active-menu-btn")) {
                        btn.getStyleClass().add("active-menu-btn");
                    }
                }
            } else {
                // Si lookup échoue, on peut iterer sur les enfants du conteneur de menu si on
                // le connait.
                // Pour simplifier, on va supposer que lookup fonctionne ou que ça n'est pas
                // critique pour l'instant.
            }
        }

        // Fallback simple : juste mettre la classe sur le bouton actif
        if (activeButton != null && !activeButton.getStyleClass().contains("active-menu-btn")) {
            activeButton.getStyleClass().add("active-menu-btn");
        }
    }

    /**
     * Charge un module dans la zone de contenu
     */
    private void loadModule(String moduleName) {
        try {
            currentModule = moduleName;

            if ("DASHBOARD".equals(moduleName)) {
                Parent dashboardView = createDashboardView();  // we’ll create this next

                contentArea.getChildren().clear();
                contentArea.getChildren().add(dashboardView);

                AnchorPane.setTopAnchor(dashboardView, 0.0);
                AnchorPane.setBottomAnchor(dashboardView, 0.0);
                AnchorPane.setLeftAnchor(dashboardView, 0.0);
                AnchorPane.setRightAnchor(dashboardView, 0.0);

                if (lblTitle != null) lblTitle.setText("Dashboard");
                return;
            }

            if (lblTitle != null) {
                lblTitle.setText("GESTION " + moduleName);
            }

            // Charger le FXML du module
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ModuleView.fxml"));
            Parent moduleView = loader.load();

            // Initialiser les composants du module via namespace
            initializeModuleView(loader.getNamespace(), moduleView);

            // Charger les données
            if (dataTable != null) {
                dataTable.getItems().clear();
            }
            loadData();

            // Ajouter à la zone de contenu
            if (contentArea != null) {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(moduleView);

                // Ancrer aux 4 coins
                AnchorPane.setTopAnchor(moduleView, 0.0);
                AnchorPane.setBottomAnchor(moduleView, 0.0);
                AnchorPane.setLeftAnchor(moduleView, 0.0);
                AnchorPane.setRightAnchor(moduleView, 0.0);
            }

        } catch (IOException e) {
            e.printStackTrace();
            showDialog("Erreur", "Impossible de charger le module: " + moduleName);
        }
    }

    private String buildSystemContext() {

        int totalUsers = userService.getAllUsers().size();
        int totalProducts = productService.getAllProducts().size();

        return """
    System Data:
    Total users: %d
    Total products: %d
    """.formatted(totalUsers, totalProducts);
    }

    private void showAiChat() {

        Stage stage = new Stage();
        stage.setTitle("AI Assistant");

        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        TextArea chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setPrefHeight(300);

        TextField inputField = new TextField();
        inputField.setPromptText("Ask something...");

        Button sendBtn = new Button("Send");

        sendBtn.setOnAction(e -> {

            String question = inputField.getText();
            if (question.isEmpty()) return;

            chatArea.appendText("You: " + question + "\n");
            inputField.clear();

            String context = buildSystemContext();
            String fullPrompt = context + "\nUser question: " + question;

            // 🔥 Run AI call in background thread
            new Thread(() -> {

                String aiResponse = AiService.askAI(fullPrompt);

                // 🔥 Update UI safely
                javafx.application.Platform.runLater(() -> {
                    chatArea.appendText("AI: " + aiResponse + "\n\n");
                });

            }).start();
        });

        root.getChildren().addAll(chatArea, inputField, sendBtn);

        stage.setScene(new Scene(root, 400, 400));
        stage.show();
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

        Button btnAI = new Button("💬 AI Assistant");
        btnAI.setStyle("-fx-background-color:#17BB9C; -fx-text-fill:white;");
        btnAI.setOnAction(e -> showAiChat());

        root.getChildren().add(btnAI);
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

        Button btnSort = (Button) namespace.get("btnSort");//new
        HBox sortBox = (HBox) namespace.get("sortBox");//new
        Button btnAdd = (Button) namespace.get("btnAdd");
        Button btnExport = (Button) namespace.get("btnExport");
        Button btnDelete = (Button) namespace.get("btnDelete");
        Button btnStats = (Button) namespace.get("btnStats");
        // 🔥 hide delete selected button only for users
        if ("UTILISATEURS".equals(currentModule) && btnDelete != null) {
            btnDelete.setVisible(false);
            btnDelete.setManaged(false); // removes empty space
        }//new

        if ("UTILISATEURS".equals(currentModule)) {

            // hide ComboBox sorting
            if (sortBox != null) {
                sortBox.setVisible(false);
                sortBox.setManaged(false);
            }

            // show icon sort button
            if (btnSort != null) {
                btnSort.setVisible(true);
                btnSort.setManaged(true);
                btnSort.setOnAction(e -> sortUsersAlphabetically());
            }

        } else {

            // show ComboBox for products
            if (sortBox != null) {
                sortBox.setVisible(true);
                sortBox.setManaged(true);
            }

            // hide icon button
            if (btnSort != null) {
                btnSort.setVisible(false);
                btnSort.setManaged(false);
            }
        }//new

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

        // 🔥 Only add checkbox column for modules other than UTILISATEURS
        if (!"UTILISATEURS".equals(currentModule)) {

            TableColumn<Object, Boolean> selectCol = new TableColumn<>("");
            selectCol.setMaxWidth(50);
            selectCol.setMinWidth(50);

            selectCol.setCellFactory(col -> new TableCell<>() {
                private final CheckBox checkBox = new CheckBox();

                {
                    checkBox.setOnAction(e -> {
                        getTableView().getSelectionModel().select(getIndex());
                    });
                    setAlignment(Pos.CENTER);
                }

                @Override
                protected void updateItem(Boolean item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : checkBox);
                }
            });

            dataTable.getColumns().add(selectCol);
        }//new

        switch (currentModule) {
            case "UTILISATEURS":
                createUserColumns();
                break;
            case "EVENEMENTS":
                createEventColumns();
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
        actionCol.setPrefWidth(140);
        actionCol.setMinWidth(140);
        actionCol.setMaxWidth(140);//new
        actionCol.setCellFactory(createActionButtons());
        dataTable.getColumns().add(actionCol);
    }

    // --- Colonnes ---

    private void createUserColumns() {
        addColumn("ID", "idUser", 50);
        addColumn("Prénom", "prenom", 100);
        addColumn("Nom", "nom", 100);
        addColumn("Email", "email", 160);
        addColumn("Rôle", "nomRole", 100);
        addColumn("Statut", "statut", 100);
        addColumn("Dernière Connexion", "derniereConnexion", 150);

    }//new

    private void createEventColumns() {
        addColumn("ID", "id", 60);
        addColumn("Titre", "title", 200);
        addColumn("Type", "type", 120);
        addColumn("Date", "eventDate", 150);
        addColumn("Lieu", "location", 150);
        addColumn("Statut", "status", 100);
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

        column.setPrefWidth(width);     // ✅ important
        column.setMinWidth(width);      // optional but safe

        dataTable.getColumns().add(column);
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
                editBtn.getStyleClass().add("action-btn-edit");
                deleteBtn.getStyleClass().add("action-btn-delete");

                editBtn.setOnAction(e -> handleEdit(getTableRow().getItem()));
                deleteBtn.setOnAction(e -> handleDelete(getTableRow().getItem()));

                // 🔥 only add view button for non-user modules
                if (!"UTILISATEURS".equals(currentModule)) {
                    viewBtn.getStyleClass().add("action-btn-view");
                    viewBtn.setOnAction(e -> handleView(getTableRow().getItem()));
                    actionBox.getChildren().addAll(viewBtn, editBtn, deleteBtn);
                } else {
                    // for users → only edit + delete
                    actionBox.getChildren().addAll(editBtn, deleteBtn);
                }

                actionBox.setAlignment(Pos.CENTER);
            }//new

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
    private void loadData() {
        dataList = FXCollections.observableArrayList();

        switch (currentModule) {
            case "UTILISATEURS":
                // load from DB (falls back to empty list if there's an error)
                try {
                    dataList.clear();
                    dataList.addAll(userService.getAllUsers());
                } catch (Exception e) {
                    System.err.println("❌ Erreur lors du chargement des utilisateurs: " + e.getMessage());
                }
                break; //new
            case "EVENEMENTS":
                dataList.addAll(
                        new Event(1, "Conférence Tech 2024", "Conférence", "15 Mars 2024", "Paris", "Confirmé"),
                        new Event(2, "Workshop IA", "Atelier", "20 Mars 2024", "Lyon", "En attente"));
                break;
            case "COACHING":
                dataList.addAll(
                        new Coaching(1, "Dr. Sarah Johnson", "Développement Personnel", "Marc Leroy",
                                "18 Fév 2024 14:00", "Actif"),
                        new Coaching(2, "Jean-Paul Dubois", "Business Coaching", "Claire Martin", "20 Fév 2024 10:00",
                                "Actif"));
                break;
            case "BLOG":
                dataList.addAll(
                        new Blog(1, "Les tendances IA en 2024", "Jean Tech", "Technologie", "05 Fév 2024", 1250),
                        new Blog(2, "Guide du développeur moderne", "Marie Code", "Développement", "03 Fév 2024", 890));
                break;
            case "PRODUITS":
                // Charger depuis la base de données
                dataList.addAll(productService.getAllProducts());
                break;
        }

        updateTable();
        updateResultsLabel();
    }

    private void updateTable() {
        if (dataTable != null) {
            dataTable.getItems().clear();   // 🔥 clear old rows (very important)
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

    private void sortUsersAlphabetically() {

        if (!"UTILISATEURS".equals(currentModule)) return;

        FXCollections.sort(dataList, (o1, o2) -> {

            // safety check
            if (!(o1 instanceof User) || !(o2 instanceof User)) return 0;

            User u1 = (User) o1;
            User u2 = (User) o2;

            return sortAsc
                    ? u1.getNom().compareToIgnoreCase(u2.getNom())
                    : u2.getNom().compareToIgnoreCase(u1.getNom());
        });

        sortAsc = !sortAsc;

        dataTable.refresh();   // 🔥 force refresh UI
    }
//new

    // --- Actions ---

    private void handleAdd() {
        if ("PRODUITS".equals(currentModule)) {
            showProductDialog(null);
        } else if ("UTILISATEURS".equals(currentModule)) {
            showUserDialog(null); //new
        } else {
            showDialog("Ajouter", "Fonction d'ajout pour " + currentModule);
        }
    }

    private void handleView(Object item) {
        if (item != null) {
            showDialog("Voir", "Détails de l'élément: " + item.toString());
        }
    }

    private void handleEdit(Object item) {
        if ("PRODUITS".equals(currentModule) && item instanceof Product) {
            showProductDialog((Product) item);
        } else if ("UTILISATEURS".equals(currentModule) && item instanceof User) {
            showUserDialog((User) item); //new
        } else {
            showDialog("Modifier", "Fonction de modification pour " + currentModule);
        }
    }

    private void handleDeleteSelected() {
        showDialog("Suppression", "Suppression de la sélection (à implémenter)");
    }

    private void handleDelete(Object item) {
        if (item == null)
            return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer l'élément");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cet élément?");
        alert.initOwner(dataTable.getScene().getWindow());

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {

            boolean success = false;

            if ("PRODUITS".equals(currentModule) && item instanceof Product) {
                Product product = (Product) item;
                success = productService.deleteProduct(product.getIdProduit());

            } else if ("UTILISATEURS".equals(currentModule) && item instanceof User) {
                User user = (User) item;
                success = userService.deleteUser(user.getIdUser());  // 🔥 IMPORTANT
            }

            if (success) {
                loadData();
                showDialog("Succès", "Élément supprimé avec succès !");
            } else {
                showDialog("Erreur", "❌ Erreur lors de la suppression !");
            }
        }
    }//new

    /**
     * Gérer l'export PDF vers le dossier Téléchargements
     */
    private void handleExport() {

        if (!"PRODUITS".equals(currentModule) && !"UTILISATEURS".equals(currentModule)) {
            showDialog("Information", "Export disponible uniquement pour Produits ou Utilisateurs.");
            return;
        }

        try {
            String userHome = System.getProperty("user.home");
            String downloadsPath = userHome + File.separator + "Downloads" + File.separator;

            String fileName;

            if ("PRODUITS".equals(currentModule)) {
                fileName = downloadsPath + "Export_Produits_" + System.currentTimeMillis() + ".pdf";
            } else {
                fileName = downloadsPath + "Export_Utilisateurs_" + System.currentTimeMillis() + ".pdf";
            }

            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(fileName));
            document.open();

            // ===== TITLE =====
            com.itextpdf.text.Font titleFont =
                    new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD);

            String titleText = "PRODUITS".equals(currentModule) ? "Liste des Produits" : "Liste des Utilisateurs";

            Paragraph title = new Paragraph(titleText, titleFont);
            title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // ===== TABLE =====
            PdfPTable table;

            if ("PRODUITS".equals(currentModule)) {

                table = new PdfPTable(6);
                table.setWidthPercentage(100);

                String[] headers = {"ID", "Nom", "Prix", "Stock", "Catégorie", "Statut"};

                for (String header : headers) {
                    PdfPCell cell = new PdfPCell(new Phrase(header));
                    cell.setBackgroundColor(com.itextpdf.text.BaseColor.LIGHT_GRAY);
                    cell.setPadding(5);
                    table.addCell(cell);
                }

                for (Object item : dataList) {
                    if (item instanceof Product p) {
                        table.addCell(String.valueOf(p.getIdProduit()));
                        table.addCell(p.getNomProduit());
                        table.addCell(p.getPrix());
                        table.addCell(String.valueOf(p.getStockDisponible()));
                        table.addCell(p.getCategorie());
                        table.addCell(p.getStatut());
                    }
                }

            } else {
                // ===== USERS EXPORT =====

                table = new PdfPTable(6);
                table.setWidthPercentage(100);

                String[] headers = {"ID", "Nom", "Prénom", "Email", "Rôle", "Statut"};

                for (String header : headers) {
                    PdfPCell cell = new PdfPCell(new Phrase(header));
                    cell.setBackgroundColor(com.itextpdf.text.BaseColor.LIGHT_GRAY);
                    cell.setPadding(5);
                    table.addCell(cell);
                }

                for (Object item : dataList) {
                    if (item instanceof User u) {
                        table.addCell(String.valueOf(u.getIdUser()));
                        table.addCell(u.getNom());
                        table.addCell(u.getPrenom());
                        table.addCell(u.getEmail());
                        table.addCell(getRoleName(u.getIdRole()));
                        table.addCell(u.getStatut());
                    }
                }
            }

            document.add(table);
            document.close();

            showDialog("Succès", "✅ Export PDF réussi :\n" + fileName);

            try {
                java.awt.Desktop.getDesktop().open(new java.io.File(fileName));
            } catch (Exception ignored) {
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

        if (!"PRODUITS".equals(currentModule) && !"UTILISATEURS".equals(currentModule)) {
            showDialog("Info", "Statistiques disponibles uniquement pour Produits ou Utilisateurs.");
            return;
        }

        Stage statsStage = new Stage();
        statsStage.setTitle("📊 Statistiques - " + currentModule);

        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: white;");

        HBox chartsBox = new HBox(20);
        chartsBox.setAlignment(Pos.CENTER);

        if ("PRODUITS".equals(currentModule)) {

            // ===== PRODUCTS STATS =====

            javafx.scene.chart.PieChart pieChart = new javafx.scene.chart.PieChart();
            pieChart.setTitle("Répartition par Catégorie");

            CategoryAxis xAxis = new CategoryAxis();
            xAxis.setLabel("Statut");

            NumberAxis yAxis = new NumberAxis();
            yAxis.setLabel("Nombre");

            BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
            barChart.setTitle("État du Stock");

            Map<String, Integer> categoryCounts = new java.util.HashMap<>();
            Map<String, Integer> statusCounts = new java.util.HashMap<>();

            for (Object item : dataList) {
                if (item instanceof Product p) {
                    categoryCounts.put(p.getCategorie(), categoryCounts.getOrDefault(p.getCategorie(), 0) + 1);
                    statusCounts.put(p.getStatut(), statusCounts.getOrDefault(p.getStatut(), 0) + 1);
                }
            }

            categoryCounts.forEach((cat, count) ->
                    pieChart.getData().add(new javafx.scene.chart.PieChart.Data(cat + " (" + count + ")", count)));

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Produits");

            statusCounts.forEach((stat, count) ->
                    series.getData().add(new XYChart.Data<>(stat, count)));

            barChart.getData().add(series);

            chartsBox.getChildren().addAll(pieChart, barChart);

        } else {

            // ===== USERS STATS =====

            javafx.scene.chart.PieChart roleChart = new javafx.scene.chart.PieChart();
            roleChart.setTitle("Répartition par Rôle");

            CategoryAxis xAxis = new CategoryAxis();
            xAxis.setLabel("Statut");

            NumberAxis yAxis = new NumberAxis();
            yAxis.setLabel("Nombre d'utilisateurs");

            BarChart<String, Number> statusChart = new BarChart<>(xAxis, yAxis);
            statusChart.setTitle("Statut des utilisateurs");

            Map<String, Integer> roleCounts = new java.util.HashMap<>();
            Map<String, Integer> statusCounts = new java.util.HashMap<>();

            for (Object item : dataList) {
                if (item instanceof User u) {

                    String role = getRoleName(u.getIdRole());
                    roleCounts.put(role, roleCounts.getOrDefault(role, 0) + 1);

                    String statut = u.getStatut() != null ? u.getStatut() : "Inconnu";
                    statusCounts.put(statut, statusCounts.getOrDefault(statut, 0) + 1);
                }
            }

            // Pie chart roles
            roleCounts.forEach((role, count) ->
                    roleChart.getData().add(new javafx.scene.chart.PieChart.Data(role + " (" + count + ")", count)));

            // Bar chart status
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Utilisateurs");

            statusCounts.forEach((stat, count) ->
                    series.getData().add(new XYChart.Data<>(stat, count)));

            statusChart.getData().add(series);

            chartsBox.getChildren().addAll(roleChart, statusChart);
        }

        HBox.setHgrow(chartsBox, Priority.ALWAYS);

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
                    loadData(); // reload from DB instead of modifying shared list
                    showDialog("Succès", "✅ Produit ajouté avec succès !");
                }
            } else {
                success = productService.updateProduct(p);
                if (success)
                    loadData();
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

    private void showUserDialog(User user) {

        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle(user == null ? "Nouvel Utilisateur" : "Modifier Utilisateur");

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

        ButtonType saveButtonType = new ButtonType(user == null ? "Ajouter" : "Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialogPane.getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // ===== FORM =====
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField txtNom = new TextField();
        txtNom.setPromptText("Nom");

        TextField txtPrenom = new TextField();
        txtPrenom.setPromptText("Prénom");

        // 🔤 allow only letters + space in Nom
        txtNom.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("[a-zA-Z ]*")) {
                txtNom.setText(oldVal);
            }
        });

        // 🔤 allow only letters + space in Prenom
        txtPrenom.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("[a-zA-Z ]*")) {
                txtPrenom.setText(oldVal);
            }
        });


        TextField txtEmail = new TextField();
        txtEmail.setPromptText("Email");

        PasswordField txtPassword = new PasswordField();
        txtPassword.setPromptText("Mot de passe");

        ComboBox<String> cmbRole = new ComboBox<>();
        cmbRole.getItems().addAll("Admin", "User", "Manager");
        cmbRole.setPromptText("Choisir un rôle");

        grid.add(new Label("Nom:"), 0, 0);
        grid.add(txtNom, 1, 0);

        grid.add(new Label("Prénom:"), 0, 1);
        grid.add(txtPrenom, 1, 1);

        grid.add(new Label("Email:"), 0, 2);
        grid.add(txtEmail, 1, 2);

        grid.add(new Label("Mot de passe:"), 0, 3);
        grid.add(txtPassword, 1, 3);

        grid.add(new Label("Rôle:"), 0, 4);
        grid.add(cmbRole, 1, 4);

        // ===== Prefill if editing =====
        if (user != null) {
            txtNom.setText(user.getNom());
            txtPrenom.setText(user.getPrenom());
            txtEmail.setText(user.getEmail());
            txtPassword.setText(user.getPassword());

            switch (user.getIdRole()) {
                case 1 -> cmbRole.setValue("Admin");
                case 2 -> cmbRole.setValue("User");
                case 3 -> cmbRole.setValue("Manager");
            }
        }

        dialogPane.setContent(grid);

        // =========================================================
        // 🔥 VALIDATION BEFORE DIALOG CLOSE (IMPORTANT PART)
        // =========================================================
        Button saveBtn = (Button) dialogPane.lookupButton(saveButtonType);

        saveBtn.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {

            String nom = txtNom.getText();
            String prenom = txtPrenom.getText();
            String email = txtEmail.getText();
            String password = txtPassword.getText();

            if (!isNotEmpty(nom) || !isNotEmpty(prenom) || !isNotEmpty(email) || !isNotEmpty(password) || cmbRole.getValue() == null) {
                showDialog("Validation", "Tous les champs sont obligatoires !");
                event.consume();
                return;
            }

            if (!isValidEmail(email)) {
                showDialog("Validation", "Email invalide !");
                event.consume();
                return;
            }

            if (!minLength(password, 4)) {
                showDialog("Validation", "Le mot de passe doit contenir au moins 4 caractères !");
                event.consume();
                return;
            }

            User existing = userService.getUserByEmail(email);
            if (existing != null && (user == null || existing.getIdUser() != user.getIdUser())) {
                showDialog("Validation", "Cet email est déjà utilisé !");
                event.consume();
            }
        });

        // =========================================================
        // RESULT CONVERTER (NO VALIDATION HERE)
        // =========================================================
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {

                int roleId = switch (cmbRole.getValue()) {
                    case "Admin" -> 1;
                    case "User" -> 2;
                    case "Manager" -> 3;
                    default -> 3;
                };

                User u;

                if (user == null) {
                    u = new User(
                            txtNom.getText(),
                            txtPrenom.getText(),
                            txtEmail.getText(),
                            txtPassword.getText(),
                            roleId
                    );
                } else {
                    u = user;
                    u.setNom(txtNom.getText());
                    u.setPrenom(txtPrenom.getText());
                    u.setEmail(txtEmail.getText());
                    u.setPassword(txtPassword.getText());
                    u.setIdRole(roleId);
                }

                return u;
            }
            return null;
        });

        // ===== SHOW RESULT =====
        Optional<User> result = dialog.showAndWait();

        result.ifPresent(u -> {

            boolean success;

            if (user == null) {
                success = userService.addUser(u);
            } else {
                u.setIdUser(user.getIdUser());
                success = userService.updateUser(u);
            }

            if (success) {
                loadData();
                showDialog("Succès", user == null ?
                        "Utilisateur ajouté avec succès !" :
                        "Utilisateur modifié avec succès !");
            } else {
                showDialog("Erreur", "Erreur lors de l'enregistrement !");
            }
        });
    }//new

    // ===== USER VALIDATION HELPERS =====

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");
    }

    private boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private boolean minLength(String value, int len) {
        return value != null && value.trim().length() >= len;
    }//new

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
