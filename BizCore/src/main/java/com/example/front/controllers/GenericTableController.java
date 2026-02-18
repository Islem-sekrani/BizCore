package com.example.front.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class GenericTableController {

    @FXML
    private TextField searchField;

    @FXML
    private TableView<?> tableView;

    @FXML
    private void handleAddNew() {
        System.out.println("Add New clicked");
    }

    @FXML
    private void handleRefresh() {
        System.out.println("Refresh clicked");
    }

    @FXML
    private void handleEdit() {
        System.out.println("Edit clicked");
    }

    @FXML
    private void handleDelete() {
        System.out.println("Delete clicked");
    }
}
