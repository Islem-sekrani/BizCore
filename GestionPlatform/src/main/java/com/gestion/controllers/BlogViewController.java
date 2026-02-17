package com.gestion.controllers;

import com.gestion.entities.Blog;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

public class BlogViewController {

    @FXML
    private TableView<Blog> dataTable;
    @FXML
    private TextField searchField;
    @FXML
    private Label lblResults;
    @FXML
    private Button btnAdd;

    private ObservableList<Blog> dataList;
    private ObservableList<Blog> filteredList;

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
        addColumn("Titre", "title", 250);
        addColumn("Auteur", "author", 150);
        addColumn("Catégorie", "category", 120);
        addColumn("Date Publication", "publishDate", 120);
        addColumn("Statut", "status", 100);

        // Actions Column
        TableColumn<Blog, Void> actionCol = new TableColumn<>("Actions");
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
        TableColumn<Blog, String> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        column.setMinWidth(width);
        dataTable.getColumns().add(column);
    }

    private void loadData() {
        // Données de démonstration
        dataList.clear();
        dataList.addAll(
                new Blog(1, "Introduction à JavaFX", "Marie Dupont", "Tutoriel", "2024-02-10", "Publié"),
                new Blog(2, "Les meilleures pratiques en développement", "Jean Martin", "Guide", "2024-02-12",
                        "Publié"),
                new Blog(3, "Nouveautés Java 17", "Sophie Bernard", "Actualité", "2024-02-15", "Brouillon"),
                new Blog(4, "Design Patterns essentiels", "Pierre Dubois", "Tutoriel", "2024-02-18", "Publié"),
                new Blog(5, "Optimisation des performances", "Luc Thomas", "Guide", "2024-02-20", "En révision"));
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
            lblResults.setText(String.format("Total: %d article(s)", filteredList.size()));
        }
    }

    private void applyFilters() {
        String searchText = (searchField != null) ? searchField.getText() : "";
        filterData(searchText);
    }

    private void filterData(String searchText) {
        filteredList.clear();

        for (Blog blog : dataList) {
            boolean matches = true;

            if (searchText != null && !searchText.isEmpty()) {
                String search = searchText.toLowerCase();
                matches = blog.getTitle().toLowerCase().contains(search) ||
                        blog.getAuthor().toLowerCase().contains(search) ||
                        blog.getCategory().toLowerCase().contains(search) ||
                        blog.getStatus().toLowerCase().contains(search);
            }

            if (matches) {
                filteredList.add(blog);
            }
        }
        updateTable();
        updateResultsLabel();
    }

    private void handleAdd() {
        showInfo("Ajouter", "Fonctionnalité d'ajout à implémenter");
    }

    private void handleView(Blog blog) {
        if (blog != null) {
            showInfo("Détails de l'article",
                    "Titre: " + blog.getTitle() + "\n" +
                            "Auteur: " + blog.getAuthor() + "\n" +
                            "Catégorie: " + blog.getCategory() + "\n" +
                            "Date: " + blog.getPublishDate() + "\n" +
                            "Statut: " + blog.getStatus());
        }
    }

    private void handleEdit(Blog blog) {
        if (blog != null) {
            showInfo("Modifier", "Fonctionnalité de modification à implémenter pour: " + blog.getTitle());
        }
    }

    private void handleDelete(Blog blog) {
        if (blog != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText("Supprimer l'article?");
            alert.setContentText("Voulez-vous vraiment supprimer " + blog.getTitle() + "?");
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
