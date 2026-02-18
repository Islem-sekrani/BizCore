package com.example.front.controllers;

import com.example.front.models.Product;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class MarketplaceController implements Initializable {

    @FXML
    private FlowPane productContainer;

    @FXML
    private ComboBox<String> serviceType;
    @FXML
    private TextField serviceTitle;
    @FXML
    private TextField servicePrice;
    @FXML
    private TextArea serviceDescription;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Init Combobox
        serviceType.setItems(FXCollections.observableArrayList(
                "Formation", "Logiciel", "Application Mobile", "Site Web", "Application Desktop"));

        List<Product> products = getMockProducts();
        for (Product product : products) {
            addProductCard(product);
        }
    }

    private void addProductCard(Product product) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("/com/example/front/ProductCard.fxml"));
            VBox productCard = fxmlLoader.load();

            ProductCardController cardController = fxmlLoader.getController();
            cardController.setData(product);

            productContainer.getChildren().add(productCard);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<Product> getMockProducts() {
        List<Product> ls = new ArrayList<>();
        ls.add(new Product("Formation JavaFX Complète", 120.0, "",
                "Maîtrisez JavaFX de A à Z avec ce cours intensif."));
        ls.add(new Product("Template Site E-commerce", 250.0, "",
                "Site web moderne prêt à l'emploi pour votre boutique."));
        ls.add(new Product("Logiciel de Gestion de Stock", 500.0, "",
                "Solution performante pour gérer vos stocks pme."));
        ls.add(new Product("App Mobile Fitness", 350.0, "", "Code source complet application Android/iOS."));
        return ls;
    }

    @FXML
    private void handlePublishService() {
        String title = serviceTitle.getText();
        String priceStr = servicePrice.getText();
        String desc = serviceDescription.getText();
        String type = serviceType.getValue();

        if (title != null && !title.isEmpty() && priceStr != null && !priceStr.isEmpty()) {
            try {
                double price = Double.parseDouble(priceStr);
                Product newProduct = new Product(title, price, "", desc + " [" + type + "]");

                // Add to view directly
                addProductCard(newProduct);

                // Clear form
                serviceTitle.clear();
                servicePrice.clear();
                serviceDescription.clear();
                serviceType.getSelectionModel().clearSelection();

                System.out.println("Service Published: " + title);
            } catch (NumberFormatException e) {
                System.out.println("Invalid price format");
            }
        }
    }

    @FXML
    private void handleCartButton() {
        System.out.println("Cart clicked");
    }
}
