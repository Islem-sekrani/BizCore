package com.gestion.controllers;

import com.gestion.entities.*;
import com.gestion.interfaces.IProductService;
import com.gestion.services.ProductService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.util.Optional;

/**
 * Contrôleur pour la vue des produits avec toutes les fonctionnalités CRUD
 */
public class ProductViewController {

    @FXML
    private TableView<Object> dataTable;
    @FXML
    private TextField searchField;
    @FXML
    private Label lblResults;
    @FXML
    private ComboBox<String> sortComboBox;
    @FXML
    private Button btnAdd;
    @FXML
    private Button btnExport;
    @FXML
    private Button btnDelete;
    @FXML
    private Button btnStats;

    private IProductService productService;
    private ProductController productController;
    private ObservableList<Object> dataList;
    private ObservableList<Object> filteredList;
    private int itemsPerPage = 5;

    @FXML
    public void initialize() {
        productService = new ProductService();
        productController = new ProductController();
        dataList = FXCollections.observableArrayList();
        filteredList = FXCollections.observableArrayList();

        setupEvents();
        setupSortComboBox();
        initializeTable();
        loadData();
    }

    private void setupEvents() {
        if (btnAdd != null)
            btnAdd.setOnAction(e -> handleAdd());
        if (btnExport != null)
            btnExport.setOnAction(e -> handleExport());
        if (btnDelete != null)
            btnDelete.setOnAction(e -> handleDeleteSelected());
        if (btnStats != null)
            btnStats.setOnAction(e -> handleStatistics());

        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> filterData(newVal));
        }
    }

    private void setupSortComboBox() {
        if (sortComboBox != null) {
            sortComboBox.setItems(FXCollections.observableArrayList(
                    "Prix (Croissant)", "Prix (Décroissant)",
                    "Stock (Croissant)", "Stock (Décroissant)"));
            sortComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null)
                    sortProducts(newVal);
            });
        }
    }

    private void initializeTable() {
        if (dataTable == null)
            return;
        dataTable.getColumns().clear();

        // Selection Column
        TableColumn<Object, Boolean> selectCol = new TableColumn<>("");
        selectCol.setMaxWidth(50);
        selectCol.setMinWidth(50);
        selectCol.setCellFactory(col -> new TableCell<>() {
            private final CheckBox checkBox = new CheckBox();
            {
                checkBox.setOnAction(e -> getTableView().getSelectionModel().select(getIndex()));
                setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : checkBox);
            }
        });
        dataTable.getColumns().add(selectCol);

        // Product columns
        addColumn("ID", "idProduit", 40);
        addColumn("Nom", "nomProduit", 130);
        addColumn("Description", "description", 160);
        addColumn("Prix", "prix", 70);
        addColumn("Stock", "stockDisponible", 60);
        addColumn("Catégorie", "categorie", 90);
        addColumn("Image", "imageUrl", 90);
        addColumn("Statut", "statut", 80);

        // Actions Column
        TableColumn<Object, Void> actionCol = new TableColumn<>("Action");
        actionCol.setMinWidth(150);
        actionCol.setMaxWidth(150);
        actionCol.setCellFactory(createActionButtons());
        dataTable.getColumns().add(actionCol);
    }

    private void addColumn(String title, String property, double width) {
        TableColumn<Object, String> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        column.setMinWidth(width);
        dataTable.getColumns().add(column);
    }

    private Callback<TableColumn<Object, Void>, TableCell<Object, Void>> createActionButtons() {
        return param -> new TableCell<>() {
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
        };
    }

    private void loadData() {
        dataList.clear();
        dataList.addAll(productService.getAllProducts());
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
            lblResults.setText(String.format("Résultats 1 à %d de %d",
                    Math.min(itemsPerPage, filteredList.size()), filteredList.size()));
        }
    }

    private void filterData(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            filteredList.clear();
            filteredList.addAll(dataList);
        } else {
            filteredList.clear();
            for (Object item : dataList) {
                if (item instanceof Product) {
                    Product p = (Product) item;
                    String search = searchText.toLowerCase();
                    if (p.getNomProduit().toLowerCase().contains(search) ||
                            p.getDescription().toLowerCase().contains(search) ||
                            p.getCategorie().toLowerCase().contains(search) ||
                            p.getStatut().toLowerCase().contains(search)) {
                        filteredList.add(item);
                    }
                }
            }
        }
        updateTable();
        updateResultsLabel();
    }

    private void sortProducts(String sortOption) {
        java.util.Comparator<Object> comparator = null;
        switch (sortOption) {
            case "Prix (Croissant)":
                comparator = (o1, o2) -> comparePrices((Product) o1, (Product) o2, true);
                break;
            case "Prix (Décroissant)":
                comparator = (o1, o2) -> comparePrices((Product) o1, (Product) o2, false);
                break;
            case "Stock (Croissant)":
                comparator = (o1, o2) -> Integer.compare(((Product) o1).getStockDisponible(),
                        ((Product) o2).getStockDisponible());
                break;
            case "Stock (Décroissant)":
                comparator = (o1, o2) -> Integer.compare(((Product) o2).getStockDisponible(),
                        ((Product) o1).getStockDisponible());
                break;
        }

        if (comparator != null) {
            FXCollections.sort(filteredList, comparator);
            updateTable();
        }
    }

    private int comparePrices(Product p1, Product p2, boolean ascending) {
        try {
            Double val1 = Double.parseDouble(p1.getPrix().replace("€", "").replace(",", ".").trim());
            Double val2 = Double.parseDouble(p2.getPrix().replace("€", "").replace(",", ".").trim());
            return ascending ? val1.compareTo(val2) : val2.compareTo(val1);
        } catch (Exception e) {
            return 0;
        }
    }

    private void handleAdd() {
        Optional<Product> result = productController.showProductDialog(null);
        result.ifPresent(product -> {
            if (productController.addProduct(product)) {
                loadData();
                showSuccess("Produit ajouté",
                        "Le produit '" + product.getNomProduit() + "' a été ajouté avec succès !");
            }
        });
    }

    private void handleView(Object item) {
        if (item instanceof Product) {
            Product p = (Product) item;
            showInfo("Détails du produit",
                    "Nom: " + p.getNomProduit() + "\n" +
                            "Description: " + p.getDescription() + "\n" +
                            "Prix: " + p.getPrix() + "\n" +
                            "Stock: " + p.getStockDisponible() + "\n" +
                            "Catégorie: " + p.getCategorie() + "\n" +
                            "Statut: " + p.getStatut());
        }
    }

    private void handleEdit(Object item) {
        if (item instanceof Product) {
            Product product = (Product) item;
            Optional<Product> result = productController.showProductDialog(product);
            result.ifPresent(p -> {
                if (productController.updateProduct(p)) {
                    loadData();
                    showSuccess("Produit modifié",
                            "Le produit '" + p.getNomProduit() + "' a été modifié avec succès !");
                }
            });
        }
    }

    private void handleDeleteSelected() {
        showInfo("Information", "Fonction de suppression multiple à implémenter");
    }

    private void handleDelete(Object item) {
        if (item == null || !(item instanceof Product))
            return;

        Product product = (Product) item;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer : " + product.getNomProduit());
        alert.setContentText("Êtes-vous sûr de vouloir supprimer ce produit ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (productController.deleteProduct(product.getIdProduit())) {
                loadData();
                showSuccess("Succès", "Produit '" + product.getNomProduit() + "' supprimé avec succès !");
            }
        }
    }

    private void handleExport() {
        ObservableList<Product> products = FXCollections.observableArrayList();
        for (Object item : filteredList) {
            if (item instanceof Product)
                products.add((Product) item);
        }
        productController.exportToPDF(products);
    }

    private void handleStatistics() {
        ObservableList<Product> products = FXCollections.observableArrayList();
        for (Object item : dataList) {
            if (item instanceof Product)
                products.add((Product) item);
        }
        productController.showStatistics(products);
    }

    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
