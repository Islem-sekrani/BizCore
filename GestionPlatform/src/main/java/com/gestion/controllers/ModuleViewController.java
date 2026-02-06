package com.gestion.controllers;

import com.gestion.dao.ProductDAO;
import com.gestion.models.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Callback;

import java.io.File;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;

public class ModuleViewController implements Initializable {

    @FXML
    private TableView<Object> dataTable;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<Integer> resultsComboBox;
    @FXML
    private Button btnAdd;
    @FXML
    private Button btnExport;
    @FXML
    private Button btnDelete;
    @FXML
    private Label lblResults;
    @FXML
    private Pagination pagination;

    private ObservableList<Object> dataList;
    private String moduleType;
    private int itemsPerPage = 5;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        resultsComboBox.setItems(FXCollections.observableArrayList(5, 10, 20, 50, 100));
        resultsComboBox.setValue(5);

        resultsComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            itemsPerPage = newVal;
            updateTable();
        });

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filterData(newVal);
        });
    }

    public void setModuleType(String moduleType) {
        this.moduleType = moduleType;
        initializeTable();
        loadSampleData();
    }

    private void initializeTable() {
        dataTable.getColumns().clear();
        dataTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Colonne de sélection
        TableColumn<Object, Boolean> selectCol = new TableColumn<>("");
        selectCol.setMaxWidth(50);
        selectCol.setMinWidth(50);
        selectCol.setCellFactory(col -> new CheckBoxTableCell());
        dataTable.getColumns().add(selectCol);

        switch (moduleType) {
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
        actionCol.setMinWidth(150);
        actionCol.setMaxWidth(150);
        actionCol.setCellFactory(createActionButtons());
        dataTable.getColumns().add(actionCol);
    }

    private void createUserColumns() {
        addColumn("ID", "id", 60);
        addColumn("Prénom", "firstName", 120);
        addColumn("Nom", "lastName", 120);
        addColumn("Email", "email", 200);
        addColumn("Rôle", "role", 100);
        addColumn("Dernière Connexion", "lastUpdate", 180);
    }

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

    /* 🔥 PRODUITS (COLONNES OPTIMISÉES COMPACTES) */
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

    private Callback<TableColumn<Object, Void>, TableCell<Object, Void>> createActionButtons() {
        return param -> new TableCell<>() {
            private final Button viewBtn = new Button();
            private final Button editBtn = new Button();
            private final Button deleteBtn = new Button();
            private final HBox actionBox = new HBox(5);

            {
                viewBtn.getStyleClass().add("action-btn-view");
                editBtn.getStyleClass().add("action-btn-edit");
                deleteBtn.getStyleClass().add("action-btn-delete");

                viewBtn.setText("👁");
                editBtn.setText("✎");
                deleteBtn.setText("🗑");

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

    private void loadSampleData() {
        dataList = FXCollections.observableArrayList();

        switch (moduleType) {
            case "UTILISATEURS":
                dataList.addAll(
                        new User(1, "Jean", "Dupont", "jean.dupont@email.com", "Administrateur", getCurrentDateTime()),
                        new User(2, "Marie", "Martin", "marie.martin@email.com", "Utilisateur", getCurrentDateTime()),
                        new User(3, "Pierre", "Bernard", "pierre.bernard@email.com", "Modérateur",
                                getCurrentDateTime()),
                        new User(4, "Sophie", "Dubois", "sophie.dubois@email.com", "Utilisateur", getCurrentDateTime()),
                        new User(5, "Luc", "Thomas", "luc.thomas@email.com", "Coach", getCurrentDateTime()));
                break;
            case "EVENEMENTS":
                dataList.addAll(
                        new Event(1, "Conférence Tech 2024", "Conférence", "15 Mars 2024", "Paris", "Confirmé"),
                        new Event(2, "Workshop IA", "Atelier", "20 Mars 2024", "Lyon", "En attente"),
                        new Event(3, "Networking Event", "Networking", "25 Mars 2024", "Marseille", "Confirmé"),
                        new Event(4, "Formation DevOps", "Formation", "01 Avril 2024", "Toulouse", "Annulé"),
                        new Event(5, "Hackathon 2024", "Compétition", "10 Avril 2024", "Bordeaux", "Confirmé"));
                break;
            case "COACHING":
                dataList.addAll(
                        new Coaching(1, "Dr. Sarah Johnson", "Développement Personnel", "Marc Leroy",
                                "18 Fév 2024 14:00", "Actif"),
                        new Coaching(2, "Jean-Paul Dubois", "Business Coaching", "Claire Martin", "20 Fév 2024 10:00",
                                "Actif"),
                        new Coaching(3, "Marie Lambert", "Coaching Carrière", "Thomas Bernard", "22 Fév 2024 16:00",
                                "Planifié"),
                        new Coaching(4, "Pierre Moreau", "Leadership", "Sophie Petit", "25 Fév 2024 09:00", "Actif"),
                        new Coaching(5, "Anne Rousseau", "Gestion Stress", "Luc Durand", "28 Fév 2024 11:00",
                                "Terminé"));
                break;
            case "BLOG":
                dataList.addAll(
                        new Blog(1, "Les tendances IA en 2024", "Jean Tech", "Technologie", "05 Fév 2024", 1250),
                        new Blog(2, "Guide du développeur moderne", "Marie Code", "Développement", "03 Fév 2024", 890),
                        new Blog(3, "L'importance du coaching", "Sophie Well", "Bien-être", "01 Fév 2024", 645),
                        new Blog(4, "Marketing digital efficace", "Pierre Biz", "Business", "28 Jan 2024", 1580),
                        new Blog(5, "Réussir sa transformation", "Luc Change", "Management", "25 Jan 2024", 720));
                break;
            case "PRODUITS":
                // 🔥 LOAD PRODUCTS FROM DATABASE
                dataList.addAll(ProductDAO.getAllProducts());
                break;

        }

        updateTable();
        updateResultsLabel();
    }

    private void updateTable() {
        dataTable.setItems(dataList);
    }

    private void updateResultsLabel() {
        lblResults.setText(String.format("Résultats 1 à %d de %d",
                Math.min(itemsPerPage, dataList.size()), dataList.size()));
    }

    private void filterData(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            updateTable();
        } else {
            ObservableList<Object> filteredList = FXCollections.observableArrayList();
            for (Object item : dataList) {
                if (itemContainsText(item, searchText.toLowerCase())) {
                    filteredList.add(item);
                }
            }
            dataTable.setItems(filteredList);
            updateResultsLabel();
        }
    }

    private boolean itemContainsText(Object item, String text) {
        String itemString = item.toString().toLowerCase();
        return itemString.contains(text);
    }

    @FXML
    private void handleAdd() {
        openAddDialog();
    }

    @FXML
    private void handleExport() {
        showDialog("Export", "Exporter les données de: " + moduleType);
    }

    @FXML
    private void handleDeleteSelected() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer les éléments sélectionnés");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer les éléments sélectionnés?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            showDialog("Succès", "Éléments supprimés avec succès");
        }
    }

    private void handleView(Object item) {
        if (item != null) {
            showDialog("Voir", "Détails de l'élément: " + item.toString());
        }
    }

    private void handleEdit(Object item) {
        if (item != null) {
            openEditDialog(item);
        }
    }

    private void handleDelete(Object item) {
        if (item != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText("Supprimer l'élément");
            alert.setContentText("Êtes-vous sûr de vouloir supprimer cet élément?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // 🔥 DELETE FROM DATABASE IF PRODUCT
                if ("PRODUITS".equals(moduleType) && item instanceof Product) {
                    Product product = (Product) item;
                    if (ProductDAO.deleteProduct(product.getIdProduit())) {
                        dataList.remove(item);
                        updateTable();
                        updateResultsLabel();
                        showDialog("Succès", "✅ Produit supprimé avec succès de la base de données!");
                    } else {
                        showDialog("Erreur", "❌ Erreur lors de la suppression du produit de la base de données");
                    }
                } else {
                    dataList.remove(item);
                    updateTable();
                    updateResultsLabel();
                    showDialog("Succès", "Élément supprimé avec succès");
                }
            }
        }
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

    // ==================== DIALOG METHODS ====================

    private void openAddDialog() {
        if ("PRODUITS".equals(moduleType)) {
            showProductDialog(null);
        } else {
            showDialog("Ajouter", "Fonction d'ajout pour " + moduleType);
        }
    }

    private void openEditDialog(Object item) {
        if ("PRODUITS".equals(moduleType) && item instanceof Product) {
            showProductDialog((Product) item);
        } else {
            showDialog("Modifier", "Fonction de modification pour " + moduleType);
        }
    }

    private void showProductDialog(Product product) {
        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle(product == null ? "➕ Ajouter un Produit" : "✎ Modifier le Produit");
        dialog.setHeaderText(null); // We'll create a custom header

        ButtonType saveButtonType = new ButtonType(product == null ? "Ajouter" : "Modifier",
                ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Main container with ultra-professional styling
        VBox mainContainer = new VBox(0);
        mainContainer.setPadding(new javafx.geometry.Insets(0));
        mainContainer.setStyle("-fx-background-color: white;");

        // Custom Header with Turquoise and Navy Blue Gradient
        VBox header = new VBox(10);
        header.setPadding(new javafx.geometry.Insets(25, 30, 25, 30));
        header.setStyle(
                "-fx-background-color: linear-gradient(to right, #17BB9C 0%, #1e3a8a 100%);" +
                        "-fx-background-radius: 0;");

        Label headerTitle = new Label(product == null ? "➕ Nouveau Produit" : "✎ Modifier le Produit");
        headerTitle.setStyle(
                "-fx-font-size: 22px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: white;");
        Label headerSubtitle = new Label(product == null ? "Ajoutez un nouveau produit à votre catalogue"
                : "Modifiez les informations du produit");
        headerSubtitle.setStyle(
                "-fx-font-size: 13px;" +
                        "-fx-text-fill: rgba(255, 255, 255, 0.9);");

        header.getChildren().addAll(headerTitle, headerSubtitle);

        // Form Container with shadow (scrollable)
        VBox formContainer = new VBox(18);
        formContainer.setPadding(new javafx.geometry.Insets(30, 30, 30, 30));
        formContainer.setStyle("-fx-background-color: #ffffff;");

        // Styled text fields with modern design
        TextField txtNom = new TextField();
        txtNom.setPromptText("Ex: Formation Java Avancé");
        txtNom.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-padding: 12px 15px;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-width: 2px;" +
                        "-fx-border-radius: 8px;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-background-color: #fafafa;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.03), 3, 0, 0, 1);");
        txtNom.setPrefWidth(450);
        txtNom.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                txtNom.setStyle(txtNom.getStyle() + "-fx-border-color: #17BB9C; -fx-background-color: white;");
            } else {
                txtNom.setStyle(txtNom.getStyle().replace("-fx-border-color: #17BB9C;", "-fx-border-color: #e0e0e0;")
                        .replace("-fx-background-color: white;", "-fx-background-color: #fafafa;"));
            }
        });

        TextArea txtDesc = new TextArea();
        txtDesc.setPromptText("Description détaillée du produit...");
        txtDesc.setPrefRowCount(4);
        txtDesc.setWrapText(true);
        txtDesc.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-padding: 12px 15px;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-width: 2px;" +
                        "-fx-border-radius: 8px;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-background-color: #fafafa;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.03), 3, 0, 0, 1);");
        txtDesc.setPrefWidth(450);

        // Two-column layout for Prix and Stock
        HBox priceStockBox = new HBox(15);

        TextField txtPrixDialog = new TextField();
        txtPrixDialog.setPromptText("Ex: 299.99€");
        txtPrixDialog.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-padding: 12px 15px;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-width: 2px;" +
                        "-fx-border-radius: 8px;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-background-color: #fafafa;");
        txtPrixDialog.setPrefWidth(217);

        TextField txtStockDialog = new TextField();
        txtStockDialog.setPromptText("Ex: 45");
        txtStockDialog.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-padding: 12px 15px;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-width: 2px;" +
                        "-fx-border-radius: 8px;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-background-color: #fafafa;");
        txtStockDialog.setPrefWidth(217);

        ComboBox<String> cmbCat = new ComboBox<>(FXCollections.observableArrayList(
                "Formation", "Livre", "Abonnement", "Kit", "Logiciel", "Service"));
        cmbCat.setPromptText("Sélectionner une catégorie");
        cmbCat.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-padding: 8px 12px;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-width: 2px;" +
                        "-fx-border-radius: 8px;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-background-color: #fafafa;");
        cmbCat.setPrefWidth(450);

        // Image selection with file picker
        HBox imageBox = new HBox(10);
        imageBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        TextField txtImg = new TextField();
        txtImg.setPromptText("Chemin de l'image...");
        txtImg.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-padding: 12px 15px;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-width: 2px;" +
                        "-fx-border-radius: 8px 0 0 8px;" +
                        "-fx-background-radius: 8px 0 0 8px;" +
                        "-fx-background-color: #fafafa;");
        txtImg.setPrefWidth(330);

        Button btnBrowse = new Button("📁 Parcourir");
        btnBrowse.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #17BB9C 0%, #1e3a8a 100%);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 13px;" +
                        "-fx-padding: 12px 20px;" +
                        "-fx-border-radius: 0 8px 8px 0;" +
                        "-fx-background-radius: 0 8px 8px 0;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(23, 187, 156, 0.4), 8, 0, 0, 2);");
        btnBrowse.setPrefWidth(110);

        btnBrowse.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Sélectionner une image");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"),
                    new FileChooser.ExtensionFilter("Tous les fichiers", "*.*"));

            File selectedFile = fileChooser.showOpenDialog(dialog.getOwner());
            if (selectedFile != null) {
                txtImg.setText(selectedFile.getAbsolutePath());
            }
        });

        imageBox.getChildren().addAll(txtImg, btnBrowse);

        ComboBox<String> cmbStat = new ComboBox<>(FXCollections.observableArrayList(
                "Disponible", "Rupture", "Actif", "Inactif"));
        cmbStat.setPromptText("Sélectionner un statut");
        cmbStat.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-padding: 8px 12px;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-width: 2px;" +
                        "-fx-border-radius: 8px;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-background-color: #fafafa;");
        cmbStat.setPrefWidth(450);

        // Populate if editing
        if (product != null) {
            txtNom.setText(product.getNomProduit());
            txtDesc.setText(product.getDescription());
            txtPrixDialog.setText(product.getPrix());
            txtStockDialog.setText(String.valueOf(product.getStockDisponible()));
            cmbCat.setValue(product.getCategorie());
            txtImg.setText(product.getImageUrl());
            cmbStat.setValue(product.getStatut());
        }

        // Create ultra-professional labels with icons
        Label lblNom = new Label("📝 Nom du Produit *");
        lblNom.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #2d3748;");

        Label lblDesc = new Label("📄 Description *");
        lblDesc.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #2d3748;");

        Label lblPrix = new Label("💰 Prix *");
        lblPrix.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #2d3748;");

        Label lblStock = new Label("📦 Stock Disponible *");
        lblStock.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #2d3748;");

        Label lblCat = new Label("🏷️ Catégorie *");
        lblCat.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #2d3748;");

        Label lblImg = new Label("🖼️ Image du Produit");
        lblImg.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #2d3748;");

        Label lblStat = new Label("✓ Statut *");
        lblStat.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #2d3748;");

        // Add two-column layout for price and stock
        VBox priceBox = new VBox(8);
        priceBox.getChildren().addAll(lblPrix, txtPrixDialog);

        VBox stockBox = new VBox(8);
        stockBox.getChildren().addAll(lblStock, txtStockDialog);

        priceStockBox.getChildren().addAll(priceBox, stockBox);

        // Add all fields to form
        formContainer.getChildren().addAll(
                lblNom, txtNom,
                lblDesc, txtDesc,
                priceStockBox,
                lblCat, cmbCat,
                lblImg, imageBox,
                lblStat, cmbStat);

        // Wrap form in ScrollPane with max height
        ScrollPane scrollPane = new ScrollPane(formContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setMaxHeight(400);
        scrollPane.setStyle("-fx-background: white; -fx-background-color: white;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        mainContainer.getChildren().addAll(header, scrollPane);
        dialog.getDialogPane().setContent(mainContainer);

        // Style the dialog pane and buttons
        dialog.getDialogPane().setStyle("-fx-background-color: white; -fx-padding: 0;");

        // Ultra-professional button styling with #17BB9C
        dialog.getDialogPane().lookupButton(saveButtonType).setStyle(
                "-fx-background-color: linear-gradient(to bottom, #17BB9C 0%, #0d9488 100%);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 14px;" +
                        "-fx-padding: 12px 30px;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(23, 187, 156, 0.4), 10, 0, 0, 3);");

        dialog.getDialogPane().lookupButton(ButtonType.CANCEL).setStyle(
                "-fx-background-color: #f3f4f6;" +
                        "-fx-text-fill: #6b7280;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 14px;" +
                        "-fx-padding: 12px 30px;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-cursor: hand;");

        // Convert result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    // Validation
                    if (txtNom.getText().trim().isEmpty() || txtDesc.getText().trim().isEmpty() ||
                            txtPrixDialog.getText().trim().isEmpty() || txtStockDialog.getText().trim().isEmpty() ||
                            cmbCat.getValue() == null || cmbStat.getValue() == null) {
                        showDialog("Erreur", "⚠️ Veuillez remplir tous les champs obligatoires (*)");
                        return null;
                    }

                    if (product == null) {
                        // Add new product
                        int newId = dataList.size() + 1;
                        Product newProduct = new Product(
                                newId,
                                txtNom.getText().trim(),
                                txtDesc.getText().trim(),
                                txtPrixDialog.getText().trim(),
                                Integer.parseInt(txtStockDialog.getText().trim()),
                                cmbCat.getValue(),
                                txtImg.getText().trim(),
                                cmbStat.getValue());
                        return newProduct; // Return the new product
                    } else {
                        // Update existing product
                        product.setNomProduit(txtNom.getText().trim());
                        product.setDescription(txtDesc.getText().trim());
                        product.setPrix(txtPrixDialog.getText().trim());
                        product.setStockDisponible(Integer.parseInt(txtStockDialog.getText().trim()));
                        product.setCategorie(cmbCat.getValue());
                        product.setImageUrl(txtImg.getText().trim());
                        product.setStatut(cmbStat.getValue());
                        return product; // Return the updated product
                    }
                } catch (NumberFormatException e) {
                    showDialog("Erreur", "❌ Le stock doit être un nombre valide");
                    return null;
                }
            }
            return null;
        });

        // Show dialog and process result
        Optional<Product> result = dialog.showAndWait();

        if (result.isPresent()) {
            Product resultProduct = result.get();

            if (product == null) {
                // 🔥 ADD NEW PRODUCT TO DATABASE
                if (ProductDAO.addProduct(resultProduct)) {
                    dataList.add(resultProduct);

                    // Clear search filter to show new product
                    if (searchField != null && !searchField.getText().isEmpty()) {
                        searchField.clear();
                    }

                    showDialog("Succès", "✅ Produit ajouté avec succès à la base de données!");
                } else {
                    showDialog("Erreur", "❌ Erreur lors de l'ajout du produit à la base de données");
                    return; // Don't update table if database operation failed
                }
            } else {
                // 🔥 UPDATE EXISTING PRODUCT IN DATABASE
                if (ProductDAO.updateProduct(resultProduct)) {
                    showDialog("Succès", "✅ Produit modifié avec succès dans la base de données!");
                } else {
                    showDialog("Erreur", "❌ Erreur lors de la modification du produit dans la base de données");
                    return; // Don't update table if database operation failed
                }
            }

            // Update table and refresh
            updateTable();
            updateResultsLabel();
            dataTable.refresh();
        }
    }

    private class CheckBoxTableCell extends TableCell<Object, Boolean> {
        private final CheckBox checkBox = new CheckBox();

        public CheckBoxTableCell() {
            checkBox.setOnAction(e -> {
                // Handle checkbox selection
            });
        }

        @Override
        protected void updateItem(Boolean item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setGraphic(null);
            } else {
                setGraphic(checkBox);
            }
        }
    }
}
