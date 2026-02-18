package com.example.front.controllers;

import com.example.front.models.DataStore;
import com.example.front.models.Product;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class OffersController implements Initializable {

    @FXML
    private FlowPane productContainer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        refreshProducts();
    }

    public void refreshProducts() {
        productContainer.getChildren().clear();
        for (Product product : DataStore.getInstance().getProducts()) {
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
}
