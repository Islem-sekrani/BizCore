package com.gestion.controllers;

import com.gestion.entities.Coaching;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

public class CoachingViewController {

    @FXML
    private TableView<Coaching> dataTable;
    @FXML
    private TextField searchField;
    @FXML
    private Label lblResults;
    @FXML
    private Button btnAdd;

    private ObservableList<Coaching> dataList;
    private ObservableList<Coaching> filteredList;

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
        addColumn("Coach", "coachName", 150);
        addColumn("Spécialité", "specialty", 150);
        addColumn("Client", "clientName", 150);
        addColumn("Prochaine Session", "nextSession", 150);
        addColumn("Statut", "status", 100);

        // Actions Column
        TableColumn<Coaching, Void> actionCol = new TableColumn<>("Actions");
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
        TableColumn<Coaching, String> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        column.setMinWidth(width);
        dataTable.getColumns().add(column);
    }

    private void loadData() {
        // Données de démonstration
        dataList.clear();
        dataList.addAll(
                new Coaching(1, "Dr. Sophie Martin", "Leadership", "Jean Dupont", "2024-03-18 10:00", "Actif"),
                new Coaching(2, "Marc Leblanc", "Développement Personnel", "Marie Bernard", "2024-03-19 14:00",
                        "Actif"),
                new Coaching(3, "Claire Rousseau", "Gestion du Stress", "Pierre Thomas", "2024-03-20 09:00",
                        "Planifié"),
                new Coaching(4, "Paul Durand", "Communication", "Sophie Petit", "2024-03-21 15:00", "Actif"),
                new Coaching(5, "Emma Moreau", "Productivité", "Luc Robert", "2024-03-22 11:00", "Planifié"));
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
            lblResults.setText(String.format("Total: %d session(s)", filteredList.size()));
        }
    }

    private void applyFilters() {
        String searchText = (searchField != null) ? searchField.getText() : "";
        filterData(searchText);
    }

    private void filterData(String searchText) {
        filteredList.clear();

        for (Coaching coaching : dataList) {
            boolean matches = true;

            if (searchText != null && !searchText.isEmpty()) {
                String search = searchText.toLowerCase();
                matches = coaching.getCoachName().toLowerCase().contains(search) ||
                        coaching.getSpecialty().toLowerCase().contains(search) ||
                        coaching.getClientName().toLowerCase().contains(search) ||
                        coaching.getStatus().toLowerCase().contains(search);
            }

            if (matches) {
                filteredList.add(coaching);
            }
        }
        updateTable();
        updateResultsLabel();
    }

    private void handleAdd() {
        showInfo("Ajouter", "Fonctionnalité d'ajout à implémenter");
    }

    private void handleView(Coaching coaching) {
        if (coaching != null) {
            showInfo("Détails de la session",
                    "Coach: " + coaching.getCoachName() + "\n" +
                            "Spécialité: " + coaching.getSpecialty() + "\n" +
                            "Client: " + coaching.getClientName() + "\n" +
                            "Prochaine session: " + coaching.getNextSession() + "\n" +
                            "Statut: " + coaching.getStatus());
        }
    }

    private void handleEdit(Coaching coaching) {
        if (coaching != null) {
            showInfo("Modifier", "Fonctionnalité de modification à implémenter pour: " + coaching.getCoachName());
        }
    }

    private void handleDelete(Coaching coaching) {
        if (coaching != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText("Supprimer la session?");
            alert.setContentText("Voulez-vous vraiment supprimer cette session de coaching?");
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
