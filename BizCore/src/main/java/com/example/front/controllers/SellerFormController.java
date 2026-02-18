package com.example.front.controllers;

import com.example.front.models.DataStore;
import com.example.front.models.Product;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import java.net.URL;
import java.util.ResourceBundle;

public class SellerFormController implements Initializable, NavigationAware {

    @FXML
    private ComboBox<String> serviceType;
    @FXML
    private TextField serviceTitle;
    @FXML
    private TextField servicePrice;
    @FXML
    private TextField serviceImageUrl;
    @FXML
    private TextArea serviceDescription;

    private MainController mainController;

    @Override
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        serviceType.setItems(FXCollections.observableArrayList(
                "Formation", "Logiciel", "Application Mobile", "Site Web", "Application Desktop"));
    }

    @FXML
    private void handlePublish() {
        String title = serviceTitle.getText();
        String priceStr = servicePrice.getText();
        String desc = serviceDescription.getText();
        String type = serviceType.getValue();
        String img = serviceImageUrl.getText();

        if (title != null && !title.isEmpty() && priceStr != null && !priceStr.isEmpty()) {
            try {
                double price = Double.parseDouble(priceStr);
                Product newProduct = new Product(title, price, img, desc + " [" + type + "]");

                // Add to Global DataStore
                DataStore.getInstance().addProduct(newProduct);

                System.out.println("Product Added: " + title);

                // Redirect to Offers Page
                if (mainController != null) {
                    mainController.showOffers();
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid price");
            }
        }
    }
}
