package com.gestion.controllers;

import com.gestion.models.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;

public class ModuleViewController implements Initializable {

    @FXML private TableView<Object> dataTable;
    @FXML private TextField searchField;
    @FXML private ComboBox<Integer> resultsComboBox;
    @FXML private Button btnAdd;
    @FXML private Button btnExport;
    @FXML private Button btnDelete;
    @FXML private Label lblResults;
    @FXML private Pagination pagination;

    private ObservableList<Object> dataList;
    private String moduleType;
    private int itemsPerPage = 5;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        resultsComboBox.setItems(FXCollections.observableArrayList(5, 10, 20, 50, 100));
        resultsComboBox.setValue(5);
        
        resultsComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            itemsPerPage = newVal;
            updateTable();
        });
        
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filterData(newVal);
        });
    }

    public void setModuleType(String moduleType) {
        this.moduleType = moduleType;
        initializeTable();
        loadSampleData();
    }

    private void initializeTable() {
        dataTable.getColumns().clear();
        dataTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Colonne de sélection
        TableColumn<Object, Boolean> selectCol = new TableColumn<>("");
        selectCol.setMaxWidth(50);
        selectCol.setMinWidth(50);
        selectCol.setCellFactory(col -> new CheckBoxTableCell());
        dataTable.getColumns().add(selectCol);

        switch (moduleType) {
            case "UTILISATEURS":
                createUserColumns();
                break;
            case "EVENEMENTS":
                createEventColumns();
                break;
            case "COACHING":
                createCoachingColumns();
                break;
            case "BLOG":
                createBlogColumns();
                break;
            case "PRODUITS":
                createProductColumns();
                break;
        }

        // Colonne Actions
        TableColumn<Object, Void> actionCol = new TableColumn<>("Action");
        actionCol.setMinWidth(150);
        actionCol.setMaxWidth(150);
        actionCol.setCellFactory(createActionButtons());
        dataTable.getColumns().add(actionCol);
    }

    private void createUserColumns() {
        addColumn("ID", "id", 60);
        addColumn("Prénom", "firstName", 120);
        addColumn("Nom", "lastName", 120);
        addColumn("Email", "email", 200);
        addColumn("Rôle", "role", 100);
        addColumn("Dernière Connexion", "lastUpdate", 180);
    }

    private void createEventColumns() {
        addColumn("ID", "id", 60);
        addColumn("Titre", "title", 200);
        addColumn("Type", "type", 120);
        addColumn("Date", "eventDate", 150);
        addColumn("Lieu", "location", 150);
        addColumn("Statut", "status", 100);
    }

    private void createCoachingColumns() {
        addColumn("ID", "id", 60);
        addColumn("Coach", "coachName", 150);
        addColumn("Spécialité", "specialty", 150);
        addColumn("Client", "clientName", 150);
        addColumn("Prochaine Session", "nextSession", 180);
        addColumn("Statut", "status", 100);
    }

    private void createBlogColumns() {
        addColumn("ID", "id", 60);
        addColumn("Titre", "title", 250);
        addColumn("Auteur", "author", 150);
        addColumn("Catégorie", "category", 120);
        addColumn("Date Publication", "publishDate", 150);
        addColumn("Vues", "views", 80);
    }

    private void createProductColumns() {
        addColumn("ID", "id", 60);
        addColumn("Nom Produit", "productName", 200);
        addColumn("Catégorie", "category", 120);
        addColumn("Prix", "price", 100);
        addColumn("Stock", "stock", 80);
        addColumn("Commandes", "orders", 100);
    }

    private void addColumn(String title, String property, double width) {
        TableColumn<Object, String> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        column.setMinWidth(width);
        dataTable.getColumns().add(column);
    }

    private Callback<TableColumn<Object, Void>, TableCell<Object, Void>> createActionButtons() {
        return param -> new TableCell<>() {
            private final Button viewBtn = new Button();
            private final Button editBtn = new Button();
            private final Button deleteBtn = new Button();
            private final HBox actionBox = new HBox(5);

            {
                viewBtn.getStyleClass().add("action-btn-view");
                editBtn.getStyleClass().add("action-btn-edit");
                deleteBtn.getStyleClass().add("action-btn-delete");
                
                viewBtn.setText("👁");
                editBtn.setText("✎");
                deleteBtn.setText("🗑");
                
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
        };
    }

    private void loadSampleData() {
        dataList = FXCollections.observableArrayList();
        
        switch (moduleType) {
            case "UTILISATEURS":
                dataList.addAll(
                    new User(1, "Jean", "Dupont", "jean.dupont@email.com", "Administrateur", getCurrentDateTime()),
                    new User(2, "Marie", "Martin", "marie.martin@email.com", "Utilisateur", getCurrentDateTime()),
                    new User(3, "Pierre", "Bernard", "pierre.bernard@email.com", "Modérateur", getCurrentDateTime()),
                    new User(4, "Sophie", "Dubois", "sophie.dubois@email.com", "Utilisateur", getCurrentDateTime()),
                    new User(5, "Luc", "Thomas", "luc.thomas@email.com", "Coach", getCurrentDateTime())
                );
                break;
            case "EVENEMENTS":
                dataList.addAll(
                    new Event(1, "Conférence Tech 2024", "Conférence", "15 Mars 2024", "Paris", "Confirmé"),
                    new Event(2, "Workshop IA", "Atelier", "20 Mars 2024", "Lyon", "En attente"),
                    new Event(3, "Networking Event", "Networking", "25 Mars 2024", "Marseille", "Confirmé"),
                    new Event(4, "Formation DevOps", "Formation", "01 Avril 2024", "Toulouse", "Annulé"),
                    new Event(5, "Hackathon 2024", "Compétition", "10 Avril 2024", "Bordeaux", "Confirmé")
                );
                break;
            case "COACHING":
                dataList.addAll(
                    new Coaching(1, "Dr. Sarah Johnson", "Développement Personnel", "Marc Leroy", "18 Fév 2024 14:00", "Actif"),
                    new Coaching(2, "Jean-Paul Dubois", "Business Coaching", "Claire Martin", "20 Fév 2024 10:00", "Actif"),
                    new Coaching(3, "Marie Lambert", "Coaching Carrière", "Thomas Bernard", "22 Fév 2024 16:00", "Planifié"),
                    new Coaching(4, "Pierre Moreau", "Leadership", "Sophie Petit", "25 Fév 2024 09:00", "Actif"),
                    new Coaching(5, "Anne Rousseau", "Gestion Stress", "Luc Durand", "28 Fév 2024 11:00", "Terminé")
                );
                break;
            case "BLOG":
                dataList.addAll(
                    new Blog(1, "Les tendances IA en 2024", "Jean Tech", "Technologie", "05 Fév 2024", 1250),
                    new Blog(2, "Guide du développeur moderne", "Marie Code", "Développement", "03 Fév 2024", 890),
                    new Blog(3, "L'importance du coaching", "Sophie Well", "Bien-être", "01 Fév 2024", 645),
                    new Blog(4, "Marketing digital efficace", "Pierre Biz", "Business", "28 Jan 2024", 1580),
                    new Blog(5, "Réussir sa transformation", "Luc Change", "Management", "25 Jan 2024", 720)
                );
                break;
            case "PRODUITS":
                dataList.addAll(
                    new Product(1, "Formation Java Avancé", "Formation", "299.99€", 45, 127),
                    new Product(2, "Livre: Clean Code", "Livre", "39.99€", 120, 89),
                    new Product(3, "Abonnement Premium", "Abonnement", "29.99€/mois", 999, 456),
                    new Product(4, "Kit Développeur", "Kit", "149.99€", 30, 67),
                    new Product(5, "Cours Python Débutant", "Formation", "199.99€", 80, 234)
                );
                break;
        }
        
        updateTable();
        updateResultsLabel();
    }

    private void updateTable() {
        dataTable.setItems(dataList);
    }

    private void updateResultsLabel() {
        lblResults.setText(String.format("Résultats 1 à %d de %d", 
            Math.min(itemsPerPage, dataList.size()), dataList.size()));
    }

    private void filterData(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            updateTable();
        } else {
            ObservableList<Object> filteredList = FXCollections.observableArrayList();
            for (Object item : dataList) {
                if (itemContainsText(item, searchText.toLowerCase())) {
                    filteredList.add(item);
                }
            }
            dataTable.setItems(filteredList);
            updateResultsLabel();
        }
    }

    private boolean itemContainsText(Object item, String text) {
        String itemString = item.toString().toLowerCase();
        return itemString.contains(text);
    }

    @FXML
    private void handleAdd() {
        showDialog("Ajouter", "Ajouter un nouvel élément pour: " + moduleType);
    }

    @FXML
    private void handleExport() {
        showDialog("Export", "Exporter les données de: " + moduleType);
    }

    @FXML
    private void handleDeleteSelected() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer les éléments sélectionnés");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer les éléments sélectionnés?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            showDialog("Succès", "Éléments supprimés avec succès");
        }
    }

    private void handleView(Object item) {
        if (item != null) {
            showDialog("Voir", "Détails de l'élément: " + item.toString());
        }
    }

    private void handleEdit(Object item) {
        if (item != null) {
            showDialog("Modifier", "Modifier l'élément: " + item.toString());
        }
    }

    private void handleDelete(Object item) {
        if (item != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText("Supprimer l'élément");
            alert.setContentText("Êtes-vous sûr de vouloir supprimer cet élément?");
            
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                dataList.remove(item);
                updateTable();
                updateResultsLabel();
                showDialog("Succès", "Élément supprimé avec succès");
            }
        }
    }

    private void showDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String getCurrentDateTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm"));
    }

    private class CheckBoxTableCell extends TableCell<Object, Boolean> {
        private final CheckBox checkBox = new CheckBox();

        public CheckBoxTableCell() {
            checkBox.setOnAction(e -> {
                // Handle checkbox selection
            });
        }

        @Override
        protected void updateItem(Boolean item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setGraphic(null);
            } else {
                setGraphic(checkBox);
            }
        }
    }
}
