package com.gestion.controllers;

import com.gestion.entities.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

public class UserViewController {

    @FXML
    private TableView<User> dataTable;
    @FXML
    private TextField searchField;
    @FXML
    private Label lblResults;
    @FXML
    private Button btnAdd;

    private ObservableList<User> dataList;
    private ObservableList<User> filteredList;

    @FXML
    public void initialize() {
        dataList = FXCollections.observableArrayList();
        filteredList = FXCollections.observableArrayList();

        setupEvents();
        initializeTable();
        loadData();
    }

    private void setupEvents() {
        if (btnAdd != null)
            btnAdd.setOnAction(e -> handleAdd());

        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        }
    }

    private void initializeTable() {
        if (dataTable == null)
            return;
        dataTable.getColumns().clear();

        addColumn("ID", "id", 50);
        addColumn("Prénom", "firstName", 120);
        addColumn("Nom", "lastName", 120);
        addColumn("Email", "email", 200);
        addColumn("Rôle", "role", 100);
        addColumn("Dernière MAJ", "lastUpdate", 150);

        // Actions Column
        TableColumn<User, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setMinWidth(120);
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button viewBtn = new Button("👁");
            private final Button editBtn = new Button("✎");
            private final Button deleteBtn = new Button("🗑");
            private final HBox actionBox = new HBox(5);
            {
                viewBtn.getStyleClass().add("action-btn-view");
                editBtn.getStyleClass().add("action-btn-edit");
                deleteBtn.getStyleClass().add("action-btn-delete");

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
        });
        dataTable.getColumns().add(actionCol);
    }

    private void addColumn(String title, String property, double width) {
        TableColumn<User, String> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        column.setMinWidth(width);
        dataTable.getColumns().add(column);
    }

    private void loadData() {
        // Données de démonstration
        dataList.clear();
        dataList.addAll(
                new User(1, "Jean", "Dupont", "jean.dupont@email.com", "Admin", "2024-02-15"),
                new User(2, "Marie", "Martin", "marie.martin@email.com", "Utilisateur", "2024-02-14"),
                new User(3, "Pierre", "Bernard", "pierre.bernard@email.com", "Modérateur", "2024-02-13"),
                new User(4, "Sophie", "Dubois", "sophie.dubois@email.com", "Utilisateur", "2024-02-12"),
                new User(5, "Luc", "Thomas", "luc.thomas@email.com", "Admin", "2024-02-11"));
        filteredList.clear();
        filteredList.addAll(dataList);
        updateTable();
        updateResultsLabel();
    }

    private void updateTable() {
        if (dataTable != null) {
            dataTable.setItems(filteredList);
            dataTable.refresh();
        }
    }

    private void updateResultsLabel() {
        if (lblResults != null && filteredList != null) {
            lblResults.setText(String.format("Total: %d utilisateur(s)", filteredList.size()));
        }
    }

    private void applyFilters() {
        String searchText = (searchField != null) ? searchField.getText() : "";
        filterData(searchText);
    }

    private void filterData(String searchText) {
        filteredList.clear();

        for (User user : dataList) {
            boolean matches = true;

            if (searchText != null && !searchText.isEmpty()) {
                String search = searchText.toLowerCase();
                matches = user.getFirstName().toLowerCase().contains(search) ||
                        user.getLastName().toLowerCase().contains(search) ||
                        user.getEmail().toLowerCase().contains(search) ||
                        user.getRole().toLowerCase().contains(search);
            }

            if (matches) {
                filteredList.add(user);
            }
        }
        updateTable();
        updateResultsLabel();
    }

    private void handleAdd() {
        showInfo("Ajouter", "Fonctionnalité d'ajout à implémenter");
    }

    private void handleView(User user) {
        if (user != null) {
            showInfo("Détails de l'utilisateur",
                    "Nom: " + user.getFirstName() + " " + user.getLastName() + "\n" +
                            "Email: " + user.getEmail() + "\n" +
                            "Rôle: " + user.getRole());
        }
    }

    private void handleEdit(User user) {
        if (user != null) {
            showInfo("Modifier", "Fonctionnalité de modification à implémenter pour: " + user.getFirstName());
        }
    }

    private void handleDelete(User user) {
        if (user != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText("Supprimer l'utilisateur?");
            alert.setContentText(
                    "Voulez-vous vraiment supprimer " + user.getFirstName() + " " + user.getLastName() + "?");
            alert.showAndWait();
        }
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
