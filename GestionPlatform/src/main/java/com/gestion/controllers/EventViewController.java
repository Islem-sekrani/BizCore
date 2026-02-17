package com.gestion.controllers;

import com.gestion.entities.Event;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

public class EventViewController {

    @FXML
    private TableView<Event> dataTable;
    @FXML
    private TextField searchField;
    @FXML
    private Label lblResults;
    @FXML
    private Button btnAdd;

    private ObservableList<Event> dataList;
    private ObservableList<Event> filteredList;

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
        addColumn("Titre", "title", 200);
        addColumn("Type", "type", 120);
        addColumn("Date", "eventDate", 120);
        addColumn("Lieu", "location", 150);
        addColumn("Statut", "status", 100);

        // Actions Column
        TableColumn<Event, Void> actionCol = new TableColumn<>("Actions");
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
        TableColumn<Event, String> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        column.setMinWidth(width);
        dataTable.getColumns().add(column);
    }

    private void loadData() {
        // Données de démonstration
        dataList.clear();
        dataList.addAll(
                new Event(1, "Conférence Tech 2024", "Conférence", "2024-03-15", "Paris", "Planifié"),
                new Event(2, "Workshop JavaFX", "Atelier", "2024-03-20", "Lyon", "Confirmé"),
                new Event(3, "Meetup Développeurs", "Meetup", "2024-03-25", "Marseille", "Planifié"),
                new Event(4, "Hackathon Innovation", "Hackathon", "2024-04-01", "Toulouse", "Ouvert"),
                new Event(5, "Webinaire IA", "Webinaire", "2024-04-10", "En ligne", "Confirmé"));
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
            lblResults.setText(String.format("Total: %d événement(s)", filteredList.size()));
        }
    }

    private void applyFilters() {
        String searchText = (searchField != null) ? searchField.getText() : "";
        filterData(searchText);
    }

    private void filterData(String searchText) {
        filteredList.clear();

        for (Event event : dataList) {
            boolean matches = true;

            if (searchText != null && !searchText.isEmpty()) {
                String search = searchText.toLowerCase();
                matches = event.getTitle().toLowerCase().contains(search) ||
                        event.getType().toLowerCase().contains(search) ||
                        event.getLocation().toLowerCase().contains(search) ||
                        event.getStatus().toLowerCase().contains(search);
            }

            if (matches) {
                filteredList.add(event);
            }
        }
        updateTable();
        updateResultsLabel();
    }

    private void handleAdd() {
        showInfo("Ajouter", "Fonctionnalité d'ajout à implémenter");
    }

    private void handleView(Event event) {
        if (event != null) {
            showInfo("Détails de l'événement",
                    "Titre: " + event.getTitle() + "\n" +
                            "Type: " + event.getType() + "\n" +
                            "Date: " + event.getEventDate() + "\n" +
                            "Lieu: " + event.getLocation() + "\n" +
                            "Statut: " + event.getStatus());
        }
    }

    private void handleEdit(Event event) {
        if (event != null) {
            showInfo("Modifier", "Fonctionnalité de modification à implémenter pour: " + event.getTitle());
        }
    }

    private void handleDelete(Event event) {
        if (event != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText("Supprimer l'événement?");
            alert.setContentText("Voulez-vous vraiment supprimer " + event.getTitle() + "?");
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
