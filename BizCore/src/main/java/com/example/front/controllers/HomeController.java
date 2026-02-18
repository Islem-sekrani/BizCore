package com.example.front.controllers;

import javafx.fxml.FXML;

public class HomeController implements NavigationAware {

    private MainController mainController;

    @Override
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void handleDiscover() {
        if (mainController != null) {
            mainController.showOffers();
        }
    }

    @FXML
    private void handleSubmit() {
        if (mainController != null) {
            mainController.showSellerForm();
        }
    }

    @FXML
    private void handleTestBlog() {
        if (mainController != null) {
            mainController.showArticleDetail();
        }
    }

    @FXML
    private void handleAdmin() {
        if (mainController != null) {
            mainController.showAdmin();
        }
    }
}
