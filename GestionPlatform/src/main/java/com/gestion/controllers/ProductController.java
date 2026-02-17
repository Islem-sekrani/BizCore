package com.gestion.controllers;

import com.gestion.entities.Product;
import com.gestion.interfaces.IProductService;
import com.gestion.services.ProductService;
import com.gestion.utils.ProductValidator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

// PDF Imports
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * Contrôleur dédié à la gestion des produits
 * Gère les opérations CRUD, statistiques, et export PDF
 */
public class ProductController {

    private IProductService productService;
    private ObservableList<Product> productList;

    public ProductController() {
        this.productService = new ProductService();
        this.productList = FXCollections.observableArrayList();
    }

    /**
     * Charge tous les produits depuis la base de données
     */
    public ObservableList<Product> loadProducts() {
        productList.clear();
        productList.addAll(productService.getAllProducts());
        return productList;
    }

    /**
     * Ajoute un nouveau produit
     */
    public boolean addProduct(Product product) {
        if (productService.addProduct(product)) {
            loadProducts();
            return true;
        } else {
            showErrorDialog("Erreur BD",
                    "Impossible d'ajouter le produit dans la base de données. Vérifiez la connexion.");
            return false;
        }
    }

    /**
     * Met à jour un produit existant
     */
    public boolean updateProduct(Product product) {
        if (productService.updateProduct(product)) {
            loadProducts();
            return true;
        } else {
            showErrorDialog("Erreur BD", "Impossible de modifier le produit dans la base de données.");
            return false;
        }
    }

    /**
     * Supprime un produit
     */
    public boolean deleteProduct(int productId) {
        if (productService.deleteProduct(productId)) {
            loadProducts();
            return true;
        } else {
            showErrorDialog("Erreur BD", "Impossible de supprimer le produit.");
            return false;
        }
    }

    /**
     * Affiche les statistiques complètes (Catégories + Statuts)
     */
    public void showStatistics(ObservableList<Product> products) {
        Stage statsStage = new Stage();
        statsStage.setTitle("📊 Statistiques des Produits");

        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f5f7fa;");

        // Titre principal
        Label title = new Label("STATISTIQUES DES PRODUITS");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #17BB9C;");
        title.setAlignment(Pos.CENTER);

        // Calcul des statistiques
        Map<String, Integer> categoryCounts = new HashMap<>();
        Map<String, Integer> statusCounts = new HashMap<>();
        int totalProducts = products.size();
        int totalStock = 0;

        for (Product p : products) {
            categoryCounts.put(p.getCategorie(), categoryCounts.getOrDefault(p.getCategorie(), 0) + 1);
            statusCounts.put(p.getStatut(), statusCounts.getOrDefault(p.getStatut(), 0) + 1);
            totalStock += p.getStockDisponible();
        }

        // Informations générales
        HBox infoBox = new HBox(30);
        infoBox.setAlignment(Pos.CENTER);
        infoBox.setPadding(new Insets(10));
        infoBox.setStyle(
                "-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");

        VBox totalBox = createInfoCard("Total Produits", String.valueOf(totalProducts), "#3498db");
        VBox stockBox = createInfoCard("Stock Total", String.valueOf(totalStock), "#2ecc71");
        VBox categoriesBox = createInfoCard("Catégories", String.valueOf(categoryCounts.size()), "#e74c3c");

        infoBox.getChildren().addAll(totalBox, stockBox, categoriesBox);

        // PieChart - Répartition par Catégorie
        PieChart pieChart = new PieChart();
        pieChart.setTitle("Répartition par Catégorie");
        pieChart.setLegendVisible(true);
        pieChart.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

        categoryCounts.forEach((cat, count) -> {
            PieChart.Data slice = new PieChart.Data(cat + " (" + count + ")", count);
            pieChart.getData().add(slice);
        });

        // BarChart - Répartition par Statut
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Statut");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Nombre de produits");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("État du Stock par Statut");
        barChart.setLegendVisible(false);
        barChart.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Produits");
        statusCounts.forEach((stat, count) -> series.getData().add(new XYChart.Data<>(stat, count)));
        barChart.getData().add(series);

        // Disposition des graphiques côte à côte
        HBox chartsBox = new HBox(20);
        chartsBox.setAlignment(Pos.CENTER);
        chartsBox.getChildren().addAll(pieChart, barChart);
        HBox.setHgrow(pieChart, Priority.ALWAYS);
        HBox.setHgrow(barChart, Priority.ALWAYS);

        // Bouton de fermeture
        Button closeBtn = new Button("Fermer");
        closeBtn.setStyle(
                "-fx-background-color: #17BB9C; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 30;");
        closeBtn.setOnAction(e -> statsStage.close());

        HBox btnBox = new HBox(closeBtn);
        btnBox.setAlignment(Pos.CENTER);
        btnBox.setPadding(new Insets(10, 0, 0, 0));

        root.getChildren().addAll(title, infoBox, chartsBox, btnBox);

        Scene scene = new Scene(root, 1000, 600);
        statsStage.setScene(scene);
        statsStage.show();
    }

    /**
     * Crée une carte d'information stylisée
     */
    private VBox createInfoCard(String label, String value, String color) {
        VBox card = new VBox(5);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 8;");

        Label lblValue = new Label(value);
        lblValue.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label lblLabel = new Label(label);
        lblLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");

        card.getChildren().addAll(lblValue, lblLabel);
        return card;
    }

    /**
     * Exporte les produits en PDF
     */
    public void exportToPDF(ObservableList<Product> products) {
        try {
            String fileName = System.getProperty("user.home") + File.separator + "Downloads" + File.separator
                    + "Export_Produits_" + System.currentTimeMillis() + ".pdf";

            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(fileName));
            document.open();

            // Titre
            com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD);
            Paragraph title = new Paragraph("Liste des Produits", titleFont);
            title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Table
            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            String[] headers = { "ID", "Nom", "Description", "Prix", "Stock", "Catégorie", "Statut" };

            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h));
                cell.setBackgroundColor(com.itextpdf.text.BaseColor.LIGHT_GRAY);
                cell.setPadding(5);
                table.addCell(cell);
            }

            for (Product p : products) {
                table.addCell(String.valueOf(p.getIdProduit()));
                table.addCell(p.getNomProduit());
                table.addCell(p.getDescription());
                table.addCell(p.getPrix());
                table.addCell(String.valueOf(p.getStockDisponible()));
                table.addCell(p.getCategorie());
                table.addCell(p.getStatut());
            }

            document.add(table);
            document.close();

            showSuccessDialog("Export PDF réussi !", "Le fichier a été exporté vers :\n" + fileName);

            // Ouvrir le fichier
            try {
                java.awt.Desktop.getDesktop().open(new File(fileName));
            } catch (Exception e) {
                // Ignorer si impossible d'ouvrir
            }

        } catch (Exception e) {
            e.printStackTrace();
            showErrorDialog("Erreur d'export", "Impossible d'exporter le PDF : " + e.getMessage());
        }
    }

    /**
     * Affiche une boîte de dialogue de produit pour ajout/modification
     * Avec sélection d'image locale et prévisualisation
     */
    public Optional<Product> showProductDialog(Product product) {
        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle(product == null ? "Nouveau Produit" : "Modifier Produit");
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

        ButtonType saveButtonType = new ButtonType(product == null ? "Ajouter" : "Enregistrer",
                ButtonBar.ButtonData.OK_DONE);
        dialogPane.getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Container principal avec scroll
        javafx.scene.control.ScrollPane scrollPane = new javafx.scene.control.ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(600);

        VBox mainBox = new VBox(15);
        mainBox.setPadding(new Insets(20));
        mainBox.setPrefWidth(500);

        // Header
        Label lblHeader = new Label(product == null ? "AJOUTER UN PRODUIT" : "MODIFIER : " + product.getNomProduit());
        lblHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: #17BB9C;");

        // Nom du produit
        Label lblNom = new Label("Nom du produit :");
        lblNom.setStyle("-fx-font-weight: bold;");
        TextField txtNom = new TextField(product != null ? product.getNomProduit() : "");
        txtNom.setPromptText("Ex: Formation");
        txtNom.setStyle("-fx-padding: 8px; -fx-font-size: 13px;");

        // Validation en temps réel pour le nom
        txtNom.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.isEmpty() || !ProductValidator.validateProductName(newVal).isValid()) {
                txtNom.setStyle("-fx-padding: 8px; -fx-font-size: 13px; -fx-border-color: red; -fx-border-width: 2px;");
            } else {
                txtNom.setStyle(
                        "-fx-padding: 8px; -fx-font-size: 13px; -fx-border-color: green; -fx-border-width: 2px;");
            }
        });

        // Description
        Label lblDesc = new Label("Description :");
        lblDesc.setStyle("-fx-font-weight: bold;");
        TextArea txtDesc = new TextArea(product != null ? product.getDescription() : "");
        txtDesc.setPromptText("Décrivez le produit en détail...");
        txtDesc.setPrefHeight(100);
        txtDesc.setWrapText(true);
        txtDesc.setStyle("-fx-padding: 8px; -fx-font-size: 13px;");

        // Validation en temps réel pour la description
        txtDesc.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.isEmpty() || !ProductValidator.validateDescription(newVal).isValid()) {
                txtDesc.setStyle(
                        "-fx-padding: 8px; -fx-font-size: 13px; -fx-border-color: red; -fx-border-width: 2px;");
            } else {
                txtDesc.setStyle(
                        "-fx-padding: 8px; -fx-font-size: 13px; -fx-border-color: green; -fx-border-width: 2px;");
            }
        });

        // Prix
        Label lblPrix = new Label("Prix :");
        lblPrix.setStyle("-fx-font-weight: bold;");
        TextField txtPrix = new TextField(product != null ? product.getPrix() : "");
        txtPrix.setPromptText("Ex: 25.99");
        txtPrix.setStyle("-fx-padding: 8px; -fx-font-size: 13px;");

        // Validation en temps réel pour le prix
        txtPrix.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.isEmpty() || !ProductValidator.validatePrice(newVal).isValid()) {
                txtPrix.setStyle(
                        "-fx-padding: 8px; -fx-font-size: 13px; -fx-border-color: red; -fx-border-width: 2px;");
            } else {
                txtPrix.setStyle(
                        "-fx-padding: 8px; -fx-font-size: 13px; -fx-border-color: green; -fx-border-width: 2px;");
            }
        });

        // Stock
        Label lblStock = new Label("Stock disponible :");
        lblStock.setStyle("-fx-font-weight: bold;");
        TextField txtStock = new TextField(product != null ? String.valueOf(product.getStockDisponible()) : "");
        txtStock.setPromptText("Ex: 10");
        txtStock.setStyle("-fx-padding: 8px; -fx-font-size: 13px;");

        // Validation en temps réel pour le stock
        txtStock.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.isEmpty() || !ProductValidator.validateStock(newVal).isValid()) {
                txtStock.setStyle(
                        "-fx-padding: 8px; -fx-font-size: 13px; -fx-border-color: red; -fx-border-width: 2px;");
            } else {
                txtStock.setStyle(
                        "-fx-padding: 8px; -fx-font-size: 13px; -fx-border-color: green; -fx-border-width: 2px;");
            }
        });

        // Catégorie
        Label lblCat = new Label("Catégorie :");
        lblCat.setStyle("-fx-font-weight: bold;");
        ComboBox<String> cmbCat = new ComboBox<>(FXCollections.observableArrayList(
                "Formation", "Livre", "Abonnement", "Logiciel", "Service"));
        cmbCat.setPromptText("Sélectionnez une catégorie");
        cmbCat.setPrefWidth(Double.MAX_VALUE);
        cmbCat.setStyle("-fx-font-size: 13px;");
        if (product != null)
            cmbCat.setValue(product.getCategorie());

        // Validation en temps réel pour la catégorie
        cmbCat.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                cmbCat.setStyle("-fx-font-size: 13px; -fx-border-color: red; -fx-border-width: 2px;");
            } else {
                cmbCat.setStyle("-fx-font-size: 13px; -fx-border-color: green; -fx-border-width: 2px;");
            }
        });

        // Image - Sélection de fichier local
        Label lblImage = new Label("Image du produit :");
        lblImage.setStyle("-fx-font-weight: bold;");

        HBox imageBox = new HBox(10);
        imageBox.setAlignment(Pos.CENTER_LEFT);

        TextField txtImagePath = new TextField(product != null ? product.getImageUrl() : "");
        txtImagePath.setPromptText("Chemin de l'image");
        txtImagePath.setEditable(false);
        txtImagePath.setPrefWidth(300);
        txtImagePath.setStyle("-fx-padding: 8px; -fx-font-size: 13px;");

        Button btnChooseImage = new Button("📁 Choisir une image");
        btnChooseImage.setStyle(
                "-fx-background-color: #17BB9C; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8px 15px;");

        // Prévisualisation de l'image
        javafx.scene.image.ImageView imagePreview = new javafx.scene.image.ImageView();
        imagePreview.setFitWidth(150);
        imagePreview.setFitHeight(150);
        imagePreview.setPreserveRatio(true);
        imagePreview.setStyle("-fx-border-color: #ddd; -fx-border-width: 2; -fx-background-color: #f5f5f5;");

        VBox previewBox = new VBox(5);
        previewBox.setAlignment(Pos.CENTER);
        Label lblPreview = new Label("Aperçu");
        lblPreview.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        previewBox.getChildren().addAll(lblPreview, imagePreview);

        // Charger l'image existante si disponible
        if (product != null && product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            try {
                File imageFile = new File(product.getImageUrl());
                if (imageFile.exists()) {
                    javafx.scene.image.Image image = new javafx.scene.image.Image(imageFile.toURI().toString());
                    imagePreview.setImage(image);
                }
            } catch (Exception e) {
                // Ignorer si l'image ne peut pas être chargée
            }
        }

        // Action du bouton de sélection d'image
        btnChooseImage.setOnAction(e -> {
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Sélectionner une image");
            fileChooser.getExtensionFilters().addAll(
                    new javafx.stage.FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif",
                            "*.bmp"),
                    new javafx.stage.FileChooser.ExtensionFilter("Tous les fichiers", "*.*"));

            File selectedFile = fileChooser.showOpenDialog(dialog.getOwner());
            if (selectedFile != null) {
                txtImagePath.setText(selectedFile.getAbsolutePath());
                try {
                    javafx.scene.image.Image image = new javafx.scene.image.Image(selectedFile.toURI().toString());
                    imagePreview.setImage(image);
                    // Validation après sélection
                    txtImagePath.setStyle(
                            "-fx-padding: 8px; -fx-font-size: 13px; -fx-border-color: green; -fx-border-width: 2px;");
                } catch (Exception ex) {
                    showErrorDialog("Erreur", "Impossible de charger l'image : " + ex.getMessage());
                    txtImagePath.setStyle(
                            "-fx-padding: 8px; -fx-font-size: 13px; -fx-border-color: red; -fx-border-width: 2px;");
                }
            }
        });

        // Validation initiale de l'image
        txtImagePath.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty()) {
                txtImagePath.setStyle(
                        "-fx-padding: 8px; -fx-font-size: 13px; -fx-border-color: red; -fx-border-width: 2px;");
            } else {
                txtImagePath.setStyle(
                        "-fx-padding: 8px; -fx-font-size: 13px; -fx-border-color: green; -fx-border-width: 2px;");
            }
        });

        imageBox.getChildren().addAll(txtImagePath, btnChooseImage);

        // Statut
        Label lblStatut = new Label("Statut :");
        lblStatut.setStyle("-fx-font-weight: bold;");
        ComboBox<String> cmbStatut = new ComboBox<>(FXCollections.observableArrayList(
                "Disponible", "Rupture", "En commande", "Bientôt disponible"));
        cmbStatut.setPromptText("Sélectionnez un statut");
        cmbStatut.setPrefWidth(Double.MAX_VALUE);
        cmbStatut.setStyle("-fx-font-size: 13px;");
        if (product != null) {
            cmbStatut.setValue(product.getStatut());
        } else {
            cmbStatut.setValue("Disponible");
        }

        // Validation en temps réel pour le statut
        cmbStatut.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                cmbStatut.setStyle("-fx-font-size: 13px; -fx-border-color: red; -fx-border-width: 2px;");
            } else {
                cmbStatut.setStyle("-fx-font-size: 13px; -fx-border-color: green; -fx-border-width: 2px;");
            }
        });

        // Ajout de tous les éléments au formulaire
        mainBox.getChildren().addAll(
                lblHeader,
                new javafx.scene.control.Separator(),
                lblNom, txtNom,
                lblDesc, txtDesc,
                lblPrix, txtPrix,
                lblStock, txtStock,
                lblCat, cmbCat,
                lblImage, imageBox,
                previewBox,
                lblStatut, cmbStatut);

        scrollPane.setContent(mainBox);
        dialogPane.setContent(scrollPane);

        // Récupération du bouton Enregistrer pour intercepter l'événement
        Button btnSave = (Button) dialogPane.lookupButton(saveButtonType);

        // Ajout d'un filtre d'événement pour empêcher la fermeture si validation échoue
        btnSave.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            try {
                // Récupération des valeurs
                String nom = txtNom.getText();
                String description = txtDesc.getText();
                String prix = txtPrix.getText();
                String stock = txtStock.getText();
                String categorie = cmbCat.getValue();
                String imagePath = txtImagePath.getText();
                String statut = cmbStatut.getValue();

                // Validation complète de tous les champs
                java.util.List<String> errors = ProductValidator.validateAllFields(
                        nom, description, prix, stock, categorie, imagePath, statut);

                // Si des erreurs existent, les afficher et bloquer la fermeture
                if (!errors.isEmpty()) {
                    StringBuilder errorMessage = new StringBuilder("Veuillez corriger les erreurs suivantes :\n\n");
                    for (String error : errors) {
                        errorMessage.append(error).append("\n");
                    }
                    showErrorDialog("Erreurs de validation", errorMessage.toString());

                    // EMPÊCHER LA FERMETURE DU DIALOGUE
                    event.consume();
                }
            } catch (Exception e) {
                showErrorDialog("Erreur", "Une erreur inattendue s'est produite : " + e.getMessage());
                event.consume();
            }
        });

        // Conversion du résultat (exécuté uniquement si la validation passe et
        // l'événement n'est pas consommé)
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    // Si on est ici, c'est que le EventFilter a déjà validé les données
                    Product p = (product == null) ? new Product() : product;

                    // Nettoyage final des données
                    p.setNomProduit(txtNom.getText().trim());
                    p.setDescription(txtDesc.getText().trim());

                    // On enregistre le prix sans le symbole €, au cas où la BD soit stricte
                    String prixNettoye = txtPrix.getText().trim().replace("€", "").replace(",", ".").trim();
                    p.setPrix(prixNettoye);

                    p.setStockDisponible(Integer.parseInt(txtStock.getText().trim()));
                    p.setCategorie(cmbCat.getValue());
                    p.setImageUrl(txtImagePath.getText().trim());
                    p.setStatut(cmbStatut.getValue());

                    return p;
                } catch (Exception e) {
                    System.err.println("ERREUR CRITIQUE lors de la création du produit: " + e.getMessage());
                    e.printStackTrace();
                    showErrorDialog("Erreur de conversion", "Les données saisies sont invalides malgré la validation.");
                    return null;
                }
            }
            return null;
        });

        return dialog.showAndWait();
    }

    private void showSuccessDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public ObservableList<Product> getProductList() {
        return productList;
    }
}
