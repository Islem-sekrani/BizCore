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
import javafx.stage.FileChooser;

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

    public ObservableList<Product> loadProducts() {
        productList.clear();
        productList.addAll(productService.getAllProducts());
        return productList;
    }

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

    public boolean updateProduct(Product product) {
        if (productService.updateProduct(product)) {
            loadProducts();
            return true;
        } else {
            showErrorDialog("Erreur BD", "Impossible de modifier le produit dans la base de données.");
            return false;
        }
    }

    public boolean deleteProduct(int productId) {
        if (productService.deleteProduct(productId)) {
            loadProducts();
            return true;
        } else {
            showErrorDialog("Erreur BD", "Impossible de supprimer le produit.");
            return false;
        }
    }

    public void showStatistics(ObservableList<Product> products) {
        Stage statsStage = new Stage();
        statsStage.setTitle("📊 Statistiques des Produits");

        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f5f7fa;");

        Label title = new Label("STATISTIQUES DES PRODUITS");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #17BB9C;");
        title.setAlignment(Pos.CENTER);

        Map<String, Integer> categoryCounts = new HashMap<>();
        Map<String, Integer> statusCounts = new HashMap<>();
        int totalProducts = products.size();
        int totalStock = 0;

        for (Product p : products) {
            categoryCounts.put(p.getCategorie(), categoryCounts.getOrDefault(p.getCategorie(), 0) + 1);
            statusCounts.put(p.getStatut(), statusCounts.getOrDefault(p.getStatut(), 0) + 1);
            totalStock += p.getStockDisponible();
        }

        HBox infoBox = new HBox(30);
        infoBox.setAlignment(Pos.CENTER);
        infoBox.setPadding(new Insets(10));
        infoBox.setStyle(
                "-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");

        VBox totalBox = createInfoCard("Total Produits", String.valueOf(totalProducts), "#3498db");
        VBox stockBox = createInfoCard("Stock Total", String.valueOf(totalStock), "#2ecc71");
        VBox categoriesBox = createInfoCard("Catégories", String.valueOf(categoryCounts.size()), "#e74c3c");
        infoBox.getChildren().addAll(totalBox, stockBox, categoriesBox);

        PieChart pieChart = new PieChart();
        pieChart.setTitle("Répartition par Catégorie");
        pieChart.setLegendVisible(true);
        pieChart.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        categoryCounts.forEach((cat, count) -> {
            PieChart.Data slice = new PieChart.Data(cat + " (" + count + ")", count);
            pieChart.getData().add(slice);
        });

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

        HBox chartsBox = new HBox(20);
        chartsBox.setAlignment(Pos.CENTER);
        chartsBox.getChildren().addAll(pieChart, barChart);
        HBox.setHgrow(pieChart, Priority.ALWAYS);
        HBox.setHgrow(barChart, Priority.ALWAYS);

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

    public void exportToPDF(ObservableList<Product> products) {
        try {
            String fileName = System.getProperty("user.home") + File.separator + "Downloads" + File.separator
                    + "Export_Produits_" + System.currentTimeMillis() + ".pdf";

            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(fileName));
            document.open();

            com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD);
            Paragraph title = new Paragraph("Liste des Produits", titleFont);
            title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

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
     * Affiche le formulaire d'ajout/modification de produit.
     * Inclut la sélection d'image locale, la prévisualisation,
     * et la génération d'image IA via Gemini (prompt libre ou description auto).
     */
    public Optional<Product> showProductDialog(Product product) {
        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle(product == null ? "Nouveau Produit" : "Modifier Produit");
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

        ButtonType saveButtonType = new ButtonType(product == null ? "Ajouter" : "Enregistrer",
                ButtonBar.ButtonData.OK_DONE);
        dialogPane.getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        javafx.scene.control.ScrollPane scrollPane = new javafx.scene.control.ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(620);

        VBox mainBox = new VBox(15);
        mainBox.setPadding(new Insets(20));
        mainBox.setPrefWidth(520);

        // ── Header ──
        Label lblHeader = new Label(product == null ? "AJOUTER UN PRODUIT" : "MODIFIER : " + product.getNomProduit());
        lblHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: #17BB9C;");

        // ── Nom ──
        Label lblNom = new Label("Nom du produit :");
        lblNom.setStyle("-fx-font-weight: bold;");
        TextField txtNom = new TextField(product != null ? product.getNomProduit() : "");
        txtNom.setPromptText("Ex: Formation Java");
        txtNom.setStyle("-fx-padding: 8px; -fx-font-size: 13px;");

        // TextField txtNom = ... (déjà là)

        txtNom.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.isEmpty() || !ProductValidator.validateProductName(newVal).isValid()) {
                txtNom.setStyle("-fx-padding: 8px; -fx-font-size: 13px; -fx-border-color: red; -fx-border-width: 2px;");
            } else {
                txtNom.setStyle(
                        "-fx-padding: 8px; -fx-font-size: 13px; -fx-border-color: green; -fx-border-width: 2px;");
            }
        });

        // ── Description ──
        Label lblDesc = new Label("Description :");
        lblDesc.setStyle("-fx-font-weight: bold;");
        TextArea txtDesc = new TextArea(product != null ? product.getDescription() : "");
        txtDesc.setPromptText("Décrivez le produit en détail...");
        txtDesc.setPrefHeight(100);
        txtDesc.setWrapText(true);
        txtDesc.setStyle("-fx-padding: 8px; -fx-font-size: 13px;");

        txtDesc.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.isEmpty() || !ProductValidator.validateDescription(newVal).isValid()) {
                txtDesc.setStyle(
                        "-fx-padding: 8px; -fx-font-size: 13px; -fx-border-color: red; -fx-border-width: 2px;");
            } else {
                txtDesc.setStyle(
                        "-fx-padding: 8px; -fx-font-size: 13px; -fx-border-color: green; -fx-border-width: 2px;");
            }
        });

        // ── Prix ──
        Label lblPrix = new Label("Prix :");
        lblPrix.setStyle("-fx-font-weight: bold;");
        TextField txtPrix = new TextField(product != null ? product.getPrix() : "");
        txtPrix.setPromptText("Ex: 25.99");
        txtPrix.setStyle("-fx-padding: 8px; -fx-font-size: 13px;");

        txtPrix.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.isEmpty() || !ProductValidator.validatePrice(newVal).isValid()) {
                txtPrix.setStyle(
                        "-fx-padding: 8px; -fx-font-size: 13px; -fx-border-color: red; -fx-border-width: 2px;");
            } else {
                txtPrix.setStyle(
                        "-fx-padding: 8px; -fx-font-size: 13px; -fx-border-color: green; -fx-border-width: 2px;");
            }
        });

        // ── Stock ──
        Label lblStock = new Label("Stock disponible :");
        lblStock.setStyle("-fx-font-weight: bold;");
        TextField txtStock = new TextField(product != null ? String.valueOf(product.getStockDisponible()) : "");
        txtStock.setPromptText("Ex: 10");
        txtStock.setStyle("-fx-padding: 8px; -fx-font-size: 13px;");

        txtStock.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.isEmpty() || !ProductValidator.validateStock(newVal).isValid()) {
                txtStock.setStyle(
                        "-fx-padding: 8px; -fx-font-size: 13px; -fx-border-color: red; -fx-border-width: 2px;");
            } else {
                txtStock.setStyle(
                        "-fx-padding: 8px; -fx-font-size: 13px; -fx-border-color: green; -fx-border-width: 2px;");
            }
        });

        // ── Catégorie ──
        Label lblCat = new Label("Catégorie :");
        lblCat.setStyle("-fx-font-weight: bold;");
        ComboBox<String> cmbCat = new ComboBox<>(FXCollections.observableArrayList(
                "Formation", "Livre", "Abonnement", "Logiciel", "Service"));
        cmbCat.setPromptText("Sélectionnez une catégorie");
        cmbCat.setPrefWidth(Double.MAX_VALUE);
        cmbCat.setStyle("-fx-font-size: 13px;");
        if (product != null)
            cmbCat.setValue(product.getCategorie());

        cmbCat.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                cmbCat.setStyle("-fx-font-size: 13px; -fx-border-color: red; -fx-border-width: 2px;");
            } else {
                cmbCat.setStyle("-fx-font-size: 13px; -fx-border-color: green; -fx-border-width: 2px;");
            }
        });

        // ── Image ──
        Label lblImage = new Label("Image du produit :");
        lblImage.setStyle("-fx-font-weight: bold;");

        HBox imageBox = new HBox(10);
        imageBox.setAlignment(Pos.CENTER_LEFT);

        TextField txtImagePath = new TextField(product != null ? product.getImageUrl() : "");
        txtImagePath.setPromptText("Chemin de l'image");
        txtImagePath.setEditable(false);
        txtImagePath.setPrefWidth(300);
        txtImagePath.setStyle("-fx-padding: 8px; -fx-font-size: 13px;");

        // Prévisualisation
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

        Button btnChooseImage = new Button("📁 Choisir une image");
        btnChooseImage.setStyle(
                "-fx-background-color: #17BB9C; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8px 15px;");

        imageBox.getChildren().addAll(txtImagePath, btnChooseImage);

        btnChooseImage.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Sélectionner une image");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"));
            File selectedFile = fileChooser.showOpenDialog(dialog.getOwner());
            if (selectedFile != null) {
                txtImagePath.setText(selectedFile.getAbsolutePath());
                try {
                    javafx.scene.image.Image image = new javafx.scene.image.Image(selectedFile.toURI().toString());
                    imagePreview.setImage(image);
                } catch (Exception ex) {
                    showErrorDialog("Erreur d'image", "Impossible de charger l'aperçu.");
                }
            }
        });

        if (product != null && product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            try {
                File imageFile = new File(product.getImageUrl());
                if (imageFile.exists()) {
                    javafx.scene.image.Image image = new javafx.scene.image.Image(imageFile.toURI().toString());
                    imagePreview.setImage(image);
                }
            } catch (Exception e) {
                /* ignorer */ }
        }

        // ── Statut ──
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

        cmbStatut.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                cmbStatut.setStyle("-fx-font-size: 13px; -fx-border-color: red; -fx-border-width: 2px;");
            } else {
                cmbStatut.setStyle("-fx-font-size: 13px; -fx-border-color: green; -fx-border-width: 2px;");
            }
        });

        // ── Assembly formulaire ──
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

        Button btnSave = (Button) dialogPane.lookupButton(saveButtonType);

        btnSave.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            try {
                String nom = txtNom.getText();
                String description = txtDesc.getText();
                String prix = txtPrix.getText();
                String stock = txtStock.getText();
                String categorie = cmbCat.getValue();
                String imagePath = txtImagePath.getText();
                String statut = cmbStatut.getValue();

                java.util.List<String> errors = ProductValidator.validateAllFields(
                        nom, description, prix, stock, categorie, imagePath, statut);

                if (!errors.isEmpty()) {
                    StringBuilder errorMessage = new StringBuilder("Veuillez corriger les erreurs suivantes :\n\n");
                    for (String error : errors) {
                        errorMessage.append(error).append("\n");
                    }
                    showErrorDialog("Erreurs de validation", errorMessage.toString());
                    event.consume();
                }
            } catch (Exception e) {
                showErrorDialog("Erreur", "Une erreur inattendue s'est produite : " + e.getMessage());
                event.consume();
            }
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    Product p = (product == null) ? new Product() : product;
                    p.setNomProduit(txtNom.getText().trim());
                    p.setDescription(txtDesc.getText().trim());
                    String prixNettoye = txtPrix.getText().trim().replace("€", "").replace(",", ".").trim();
                    p.setPrix(prixNettoye);
                    p.setStockDisponible(Integer.parseInt(txtStock.getText().trim()));
                    p.setCategorie(cmbCat.getValue());
                    p.setImageUrl(txtImagePath.getText().trim());
                    p.setStatut(cmbStatut.getValue());
                    return p;
                } catch (Exception e) {
                    System.err.println("ERREUR CRITIQUE : " + e.getMessage());
                    e.printStackTrace();
                    showErrorDialog("Erreur de conversion", "Les données saisies sont invalides.");
                    return null;
                }
            }
            return null;
        });

        return dialog.showAndWait();
    }

    // ──────────────────────────────────────────────────
    // Méthodes utilitaires
    // ──────────────────────────────────────────────────

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
