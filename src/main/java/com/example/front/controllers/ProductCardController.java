package com.example.front.controllers;

import com.example.front.models.Product;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;

public class ProductCardController {

    @FXML
    private ImageView productImage;

    @FXML
    private Label productName;

    @FXML
    private Label productPrice;

    @FXML
    private Label productDescription;

    @FXML
    private Label placeholderIcon;

    @FXML
    private Label categoryBadge;

    private Product currentProduct;

    public void setData(Product product) {
        this.currentProduct = product;
        productName.setText(product.getName());
        productPrice.setText(String.format("%.2f TND", product.getPrice()));
        productDescription.setText(product.getDescription());

        // Set category badge based on product type
        String category = determineCategoryFromName(product.getName());
        if (categoryBadge != null) {
            categoryBadge.setText(category);
        }

        try {
            // Try to load image if URL is provided
            if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                Image image = new Image(product.getImageUrl(), true); // Background loading
                productImage.setImage(image);

                // Hide placeholder when image loads successfully
                if (placeholderIcon != null) {
                    image.progressProperty().addListener((obs, oldVal, newVal) -> {
                        if (newVal.doubleValue() >= 1.0 && !image.isError()) {
                            placeholderIcon.setVisible(false);
                            placeholderIcon.setManaged(false);
                        }
                    });

                    // If image fails to load, show placeholder with appropriate icon
                    image.errorProperty().addListener((obs, oldVal, newVal) -> {
                        if (newVal) {
                            showPlaceholder(product.getName());
                        }
                    });
                }
            } else {
                // No image URL provided, show placeholder
                showPlaceholder(product.getName());
            }
        } catch (Exception e) {
            System.out.println("Error loading image: " + e.getMessage());
            showPlaceholder(product.getName());
        }
    }

    @FXML
    private void handleComments() {
        if (currentProduct == null)
            return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/front/CommentsPopup.fxml"));
            Parent root = loader.load();

            CommentsPopupController controller = loader.getController();
            controller.setProduct(currentProduct);

            Stage stage = new Stage();
            stage.setTitle("Commentaires - " + currentProduct.getName());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL); // Bloque la fenêtre principale
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Show placeholder icon based on product type
     */
    private void showPlaceholder(String productName) {
        if (placeholderIcon != null) {
            placeholderIcon.setVisible(true);
            placeholderIcon.setManaged(true);

            // Set icon based on product type
            String icon = getIconForProduct(productName);
            placeholderIcon.setText(icon);
        }

        // Clear the image
        if (productImage != null) {
            productImage.setImage(null);
        }
    }

    /**
     * Determine icon based on product name/type
     */
    private String getIconForProduct(String productName) {
        String lowerName = productName.toLowerCase();

        if (lowerName.contains("formation") || lowerName.contains("cours")) {
            return "📚";
        } else if (lowerName.contains("logiciel") || lowerName.contains("software")) {
            return "💻";
        } else if (lowerName.contains("app") || lowerName.contains("mobile")) {
            return "📱";
        } else if (lowerName.contains("site") || lowerName.contains("web")) {
            return "🌐";
        } else if (lowerName.contains("design") || lowerName.contains("graphique")) {
            return "🎨";
        } else if (lowerName.contains("video") || lowerName.contains("vidéo")) {
            return "🎬";
        } else {
            return "📦";
        }
    }

    /**
     * Determine category badge text
     */
    private String determineCategoryFromName(String productName) {
        String lowerName = productName.toLowerCase();

        if (lowerName.contains("formation") || lowerName.contains("cours")) {
            return "Formation";
        } else if (lowerName.contains("logiciel") || lowerName.contains("software")) {
            return "Logiciel";
        } else if (lowerName.contains("app") || lowerName.contains("mobile")) {
            return "App Mobile";
        } else if (lowerName.contains("site") || lowerName.contains("web")) {
            return "Site Web";
        } else if (lowerName.contains("template")) {
            return "Template";
        } else {
            return "Digital";
        }
    }
}
