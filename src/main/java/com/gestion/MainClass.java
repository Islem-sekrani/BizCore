package com.gestion;

import com.gestion.controllers.BlogViewController;
import com.gestion.entities.*;
import com.gestion.services.ProductService;
import com.gestion.services.BlogService;
import com.gestion.interfaces.IProductService;
import com.gestion.interfaces.IBlogService;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.geometry.Insets;
import javafx.stage.FileChooser;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.File;

public class MainClass extends Application {

    private IProductService productService;
    private IBlogService blogService;

    private Label lblTitle;
    private Label lblUserName;
    private AnchorPane contentArea;

    private Button btnUsers;
    private Button btnEvents;
    private Button btnCoaching;
    private Button btnBlog;
    private Button btnProducts;

    private TableView<Object> dataTable;
    private TextField searchField;
    private Label lblResults;
    private ComboBox<String> sortComboBox;

    private ObservableList<Object> dataList;
    private String currentModule = "PRODUITS";
    private int itemsPerPage = 5;

    // Blog module — delegated entirely to BlogViewController
    private BlogViewController blogViewController;

    // Validation style constants (used by Product dialog only — Blog uses BlogViewController)
    private static final String FIELD_NORMAL =
        "-fx-background-color: #f7fafc; -fx-border-color: #cbd5e0; " +
        "-fx-border-width: 1; -fx-border-radius: 5; -fx-background-radius: 5; " +
        "-fx-padding: 10; -fx-font-size: 13px;";
    private static final String FIELD_ERROR =
        "-fx-background-color: #fff5f5; -fx-border-color: #e53e3e; " +
        "-fx-border-width: 2; -fx-border-radius: 5; -fx-background-radius: 5; " +
        "-fx-padding: 10; -fx-font-size: 13px;";
    private static final String COMBO_NORMAL =
        "-fx-background-color: #f7fafc; -fx-border-color: #cbd5e0; " +
        "-fx-border-width: 1; -fx-border-radius: 5; -fx-background-radius: 5;";
    private static final String COMBO_ERROR =
        "-fx-background-color: #fff5f5; -fx-border-color: #e53e3e; " +
        "-fx-border-width: 2; -fx-border-radius: 5; -fx-background-radius: 5;";
    private static final String ERR_LABEL_STYLE =
        "-fx-text-fill: #e53e3e; -fx-font-size: 11px; -fx-font-weight: 600;";

    @Override
    public void start(Stage primaryStage) {
        try {
            productService = new ProductService();
            blogService = new BlogService();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainDashboard.fxml"));

            Parent root = loader.load();

            Scene scene = new Scene(root, 1400, 800);
            initializeDashboard(loader.getNamespace(), root);
            loadModule("PRODUITS");

            primaryStage.setTitle("Admin Panel - Gestion Platform");
            primaryStage.setScene(scene);
            primaryStage.setMaximized(true);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Erreur fatale lors du démarrage de l'application: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void initializeDashboard(java.util.Map<String, Object> namespace, Parent root) {
        lblTitle    = (Label)      namespace.get("lblTitle");
        lblUserName = (Label)      namespace.get("lblUserName");
        contentArea = (AnchorPane) namespace.get("contentArea");

        btnUsers    = (Button) namespace.get("btnUtilisateurs");
        btnEvents   = (Button) namespace.get("btnEvenements");
        btnCoaching = (Button) namespace.get("btnCoaching");
        btnBlog     = (Button) namespace.get("btnBlog");
        btnProducts = (Button) namespace.get("btnProduits");

        if (btnUsers    != null) btnUsers.setOnAction(e    -> { setActiveButton(btnUsers);    loadModule("UTILISATEURS"); });
        if (btnEvents   != null) btnEvents.setOnAction(e   -> { setActiveButton(btnEvents);   loadModule("EVENEMENTS"); });
        if (btnCoaching != null) btnCoaching.setOnAction(e -> { setActiveButton(btnCoaching); loadModule("COACHING"); });
        if (btnBlog     != null) btnBlog.setOnAction(e     -> { setActiveButton(btnBlog);     loadModule("BLOG"); });
        if (btnProducts != null) btnProducts.setOnAction(e -> { setActiveButton(btnProducts); loadModule("PRODUITS"); });
    }

    private void setActiveButton(Button clicked) {
        for (Button b : new Button[]{btnUsers, btnEvents, btnCoaching, btnBlog, btnProducts})
            if (b != null) b.getStyleClass().remove("active-menu-btn");
        if (clicked != null && !clicked.getStyleClass().contains("active-menu-btn"))
            clicked.getStyleClass().add("active-menu-btn");
    }

    private void loadModule(String moduleName) {
        try {
            currentModule = moduleName;
            if (lblTitle != null) lblTitle.setText("Gestion " + moduleName);

            FXMLLoader moduleLoader = new FXMLLoader(getClass().getResource("/fxml/ModuleView.fxml"));
            Parent moduleView = moduleLoader.load();

            // ── BLOG module: delegate entirely to BlogViewController ────────────
            if ("BLOG".equals(moduleName)) {
                loadBlogModule(moduleLoader.getNamespace(), moduleView);
            } else {
                initializeModuleView(moduleLoader.getNamespace(), moduleView);
                loadData();
            }

            if (contentArea != null) {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(moduleView);
                AnchorPane.setTopAnchor(moduleView, 0.0);
                AnchorPane.setBottomAnchor(moduleView, 0.0);
                AnchorPane.setLeftAnchor(moduleView, 0.0);
                AnchorPane.setRightAnchor(moduleView, 0.0);
            }

        } catch (IOException e) {
            e.printStackTrace();
            showDialog("Erreur", "Impossible de charger le module: " + moduleName);
        }
    }

    /**
     * Hands off the BLOG module entirely to BlogViewController.
     * BlogViewController manages its own TableView columns, data loading,
     * search, sort, and all CRUD dialogs using Blog.java → table: article.
     */
    @SuppressWarnings("unchecked")
    private void loadBlogModule(java.util.Map<String, Object> namespace, Parent moduleView) {
        TableView<?>   table   = (TableView<?>)   namespace.get("dataTable");
        TextField      search  = (TextField)       namespace.get("searchField");
        ComboBox<?>    sort    = (ComboBox<?>)     namespace.get("sortComboBox");
        Label          results = (Label)           namespace.get("lblResults");
        Button         btnAdd  = (Button)          namespace.get("btnAdd");
        Button         btnDel  = (Button)          namespace.get("btnDelete");
        Button         btnExp  = (Button)          namespace.get("btnExport");
        Button         btnStat = (Button)          namespace.get("btnStats");
        Button         btnTrend= (Button)          namespace.get("btnTrending");

        blogViewController = new BlogViewController();
        blogViewController.initialize(table, search, sort, results);
        blogViewController.bindAddButton(btnAdd);
        blogViewController.bindDeleteButton(btnDel);
        blogViewController.bindExportButton(btnExp);
        blogViewController.bindStatsButton(btnStat);
        blogViewController.bindTrendingButton(btnTrend);
    }

    @SuppressWarnings("unchecked")
    private void initializeModuleView(java.util.Map<String, Object> namespace, Parent view) {
        dataTable    = (TableView<Object>) namespace.get("dataTable");
        searchField  = (TextField)         namespace.get("searchField");
        lblResults   = (Label)             namespace.get("lblResults");
        sortComboBox = (ComboBox<String>)  namespace.get("sortComboBox");

        Button btnAdd    = (Button) namespace.get("btnAdd");
        Button btnExport = (Button) namespace.get("btnExport");
        Button btnDelete = (Button) namespace.get("btnDelete");
        Button btnStats  = (Button) namespace.get("btnStats");

        if (btnAdd    != null) btnAdd.setOnAction(e    -> handleAdd());
        if (btnExport != null) btnExport.setOnAction(e -> handleExport());
        if (btnDelete != null) btnDelete.setOnAction(e -> handleDeleteSelected());
        if (btnStats  != null) btnStats.setOnAction(e  -> handleStatistics());

        if (searchField != null)
            searchField.textProperty().addListener((obs, ov, nv) -> filterData(nv));

        if (sortComboBox != null) {
            sortComboBox.setItems(FXCollections.observableArrayList(
                    "Prix (Croissant)", "Prix (Décroissant)",
                    "Stock (Croissant)", "Stock (Décroissant)"));
            sortComboBox.valueProperty().addListener((obs, ov, nv) -> { if (nv != null) sortProducts(nv); });
        }

        initializeTable();
    }

    private void initializeTable() {
        if (dataTable == null) return;
        dataTable.getColumns().clear();

        TableColumn<Object, Boolean> selectCol = new TableColumn<>("");
        selectCol.setMaxWidth(50);
        selectCol.setMinWidth(50);
        selectCol.setCellFactory(col -> new TableCell<>() {
            private final CheckBox cb = new CheckBox();
            { cb.setOnAction(e -> getTableView().getSelectionModel().select(getIndex())); setAlignment(Pos.CENTER); }
            @Override protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty); setGraphic(empty ? null : cb);
            }
        });
        dataTable.getColumns().add(selectCol);

        switch (currentModule) {
            case "UTILISATEURS" -> createUserColumns();
            case "EVENEMENTS"   -> createEventColumns();
            case "COACHING"     -> createCoachingColumns();
            case "PRODUITS"     -> createProductColumns();
        }

        TableColumn<Object, Void> actionCol = new TableColumn<>("Action");
        actionCol.setMinWidth(150);
        actionCol.setMaxWidth(150);
        actionCol.setCellFactory(createActionButtons());
        dataTable.getColumns().add(actionCol);
    }

    private void createUserColumns()    { addColumn("ID","id",60); addColumn("Prénom","firstName",120); addColumn("Nom","lastName",120); addColumn("Email","email",200); addColumn("Rôle","role",100); addColumn("Connexion","lastUpdate",180); }
    private void createEventColumns()   { addColumn("ID","id",60); addColumn("Titre","title",200); addColumn("Type","type",120); addColumn("Date","eventDate",150); addColumn("Lieu","location",150); addColumn("Statut","status",100); }
    private void createCoachingColumns(){ addColumn("ID","id",60); addColumn("Coach","coachName",150); addColumn("Spécialité","specialty",150); addColumn("Client","clientName",150); addColumn("Session","nextSession",180); addColumn("Statut","status",100); }
    private void createProductColumns() { addColumn("ID","idProduit",40); addColumn("Nom","nomProduit",130); addColumn("Description","description",160); addColumn("Prix","prix",70); addColumn("Stock","stockDisponible",60); addColumn("Catégorie","categorie",90); addColumn("Image","imageUrl",90); addColumn("Statut","statut",80); }

    private void addColumn(String title, String property, double width) {
        TableColumn<Object, String> col = new TableColumn<>(title);
        col.setCellValueFactory(new PropertyValueFactory<>(property));
        col.setMinWidth(width);
        dataTable.getColumns().add(col);
    }

    private Callback<TableColumn<Object, Void>, TableCell<Object, Void>> createActionButtons() {
        return param -> new TableCell<>() {
            private final Button vBtn = new Button("👁");
            private final Button eBtn = new Button("✎");
            private final Button dBtn = new Button("🗑");
            private final HBox box = new HBox(5, vBtn, eBtn, dBtn);
            {
                vBtn.getStyleClass().add("action-btn-view");
                eBtn.getStyleClass().add("action-btn-edit");
                dBtn.getStyleClass().add("action-btn-delete");
                vBtn.setOnAction(e -> handleView(getTableRow().getItem()));
                eBtn.setOnAction(e -> handleEdit(getTableRow().getItem()));
                dBtn.setOnAction(e -> handleDelete(getTableRow().getItem()));
                box.setAlignment(Pos.CENTER);
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty); setGraphic(empty ? null : box);
            }
        };
    }

    private void loadData() {
        dataList = FXCollections.observableArrayList();
        switch (currentModule) {
            case "UTILISATEURS" -> dataList.addAll(
                new User(1,"Jean","Dupont","jean@email.com","Administrateur",getCurrentDateTime()),
                new User(2,"Marie","Martin","marie@email.com","Utilisateur",getCurrentDateTime()),
                new User(3,"Pierre","Bernard","pierre@email.com","Modérateur",getCurrentDateTime()));
            case "EVENEMENTS" -> dataList.addAll(
                new Event(1,"Conférence Tech 2024","Conférence","15 Mars 2024","Paris","Confirmé"),
                new Event(2,"Workshop IA","Atelier","20 Mars 2024","Lyon","En attente"));
            case "COACHING" -> dataList.addAll(
                new Coaching(1,"Dr. Sarah Johnson","Développement Personnel","Marc Leroy","18 Fév 2024 14:00","Actif"),
                new Coaching(2,"Jean-Paul Dubois","Business Coaching","Claire Martin","20 Fév 2024 10:00","Actif"));
            case "PRODUITS" -> dataList.addAll(productService.getAllProducts());
        }
        updateTable();
        updateResultsLabel();
    }

    private void updateTable() { if (dataTable != null) dataTable.setItems(dataList); }

    private void updateResultsLabel() {
        if (lblResults != null && dataList != null)
            lblResults.setText(String.format("Résultats 1 à %d de %d",
                    Math.min(itemsPerPage, dataList.size()), dataList.size()));
    }

    private void filterData(String searchText) {
        if (searchText == null || searchText.isEmpty()) { updateTable(); return; }
        ObservableList<Object> filtered = FXCollections.observableArrayList();
        for (Object item : dataList)
            if (item.toString().toLowerCase().contains(searchText.toLowerCase()))
                filtered.add(item);
        if (dataTable != null) dataTable.setItems(filtered);
    }

    private void sortProducts(String opt) {
        if (!"PRODUITS".equals(currentModule)) return;
        java.util.Comparator<Object> cmp = switch (opt) {
            case "Prix (Croissant)"   -> (a, b) -> { try { return Double.compare(Double.parseDouble(((Product)a).getPrix().replace(",",".").replaceAll("[^0-9.]","")), Double.parseDouble(((Product)b).getPrix().replace(",",".").replaceAll("[^0-9.]",""))); } catch(Exception e){return 0;} };
            case "Prix (Décroissant)" -> (a, b) -> { try { return Double.compare(Double.parseDouble(((Product)b).getPrix().replace(",",".").replaceAll("[^0-9.]","")), Double.parseDouble(((Product)a).getPrix().replace(",",".").replaceAll("[^0-9.]",""))); } catch(Exception e){return 0;} };
            case "Stock (Croissant)"  -> (a, b) -> Integer.compare(((Product)a).getStockDisponible(), ((Product)b).getStockDisponible());
            case "Stock (Décroissant)"-> (a, b) -> Integer.compare(((Product)b).getStockDisponible(), ((Product)a).getStockDisponible());
            default -> null;
        };
        if (cmp != null) { FXCollections.sort(dataList, cmp); updateTable(); }
    }

    private void handleAdd() {
        if ("PRODUITS".equals(currentModule)) showProductDialog(null);
        else showDialog("Ajouter", "Fonction d'ajout pour " + currentModule);
    }

    private void handleView(Object item)   { if (item != null) showDialog("Voir", item.toString()); }

    private void handleEdit(Object item) {
        if ("PRODUITS".equals(currentModule) && item instanceof Product) showProductDialog((Product) item);
        else if (item != null) showDialog("Modifier", item.toString());
    }

    private void handleDelete(Object item) {
        if (item == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer l'élément");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer cet élément?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean deleted = false;
            if ("PRODUITS".equals(currentModule) && item instanceof Product)
                deleted = productService.deleteProduct(((Product) item).getIdProduit());
            if (deleted) { loadData(); showDialog("Succès", "✅ Élément supprimé avec succès"); }
            else showDialog("Erreur", "❌ Erreur lors de la suppression");
        }
    }

    private void handleDeleteSelected() { showDialog("Supprimer", "Fonction de suppression multiple"); }

    private void handleExport() {
        if (!"PRODUITS".equals(currentModule)) { showDialog("Export", "Export disponible pour les produits uniquement."); return; }
        try {
            String fileName = System.getProperty("user.home") + File.separator + "Downloads"
                    + File.separator + "Export_Produits_" + System.currentTimeMillis() + ".pdf";
            Document doc = new Document();
            PdfWriter.getInstance(doc, new FileOutputStream(fileName));
            doc.open();
            com.itextpdf.text.Font f = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD);
            Paragraph title = new Paragraph("Liste des Produits", f);
            title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            doc.add(title);
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            for (String h : new String[]{"ID","Nom","Prix","Stock","Catégorie","Statut"}) {
                PdfPCell cell = new PdfPCell(new Phrase(h));
                cell.setBackgroundColor(com.itextpdf.text.BaseColor.LIGHT_GRAY);
                cell.setPadding(5);
                table.addCell(cell);
            }
            for (Object item : dataList) {
                if (item instanceof Product p) {
                    table.addCell(String.valueOf(p.getIdProduit()));
                    table.addCell(p.getNomProduit());
                    table.addCell(p.getPrix());
                    table.addCell(String.valueOf(p.getStockDisponible()));
                    table.addCell(p.getCategorie());
                    table.addCell(p.getStatut());
                }
            }
            doc.add(table);
            doc.close();
            showDialog("Succès", "✅ Export PDF réussi:\n" + fileName);
            try { java.awt.Desktop.getDesktop().open(new File(fileName)); } catch (Exception ignored) {}
        } catch (Exception e) { e.printStackTrace(); showDialog("Erreur", "❌ Erreur lors de l'export PDF: " + e.getMessage()); }
    }

    private void handleStatistics() {
        if (!"PRODUITS".equals(currentModule)) { showDialog("Info", "Statistiques disponibles pour les produits."); return; }
        Stage statsStage = new Stage();
        statsStage.setTitle("📊 Statistiques Produits");
        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: white;");
        javafx.scene.chart.PieChart pie = new javafx.scene.chart.PieChart();
        pie.setTitle("Répartition par Catégorie");
        java.util.Map<String,Integer> cats = new java.util.HashMap<>(), stats = new java.util.HashMap<>();
        for (Object item : dataList) {
            if (item instanceof Product p) {
                cats.merge(p.getCategorie(), 1, Integer::sum);
                stats.merge(p.getStatut(), 1, Integer::sum);
            }
        }
        cats.forEach((c,n) -> pie.getData().add(new javafx.scene.chart.PieChart.Data(c+" ("+n+")", n)));
        BarChart<String,Number> bar = new BarChart<>(new CategoryAxis(), new NumberAxis());
        bar.setTitle("État du Stock");
        XYChart.Series<String,Number> series = new XYChart.Series<>();
        series.setName("Produits");
        stats.forEach((s,n) -> series.getData().add(new XYChart.Data<>(s, n)));
        bar.getData().add(series);
        HBox charts = new HBox(20, pie, bar);
        charts.setAlignment(Pos.CENTER);
        HBox.setHgrow(pie, Priority.ALWAYS);
        HBox.setHgrow(bar, Priority.ALWAYS);
        root.getChildren().add(charts);
        statsStage.setScene(new Scene(root, 1000, 500));
        statsStage.show();
    }

    // =========================================================================
    //  PRODUCT DIALOG (unchanged)
    // =========================================================================
    private void showProductDialog(Product product) {
        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle(product == null ? "Nouveau Produit" : "Modifier Produit");
        dialog.setHeaderText(null);
        DialogPane dp = dialog.getDialogPane();
        dp.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        dp.getStyleClass().add("alert");
        ButtonType saveType = new ButtonType(product == null ? "Ajouter" : "Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dp.getButtonTypes().addAll(saveType, ButtonType.CANCEL);
        ((Button) dp.lookupButton(saveType)).getStyleClass().add("btn-primary");
        ((Button) dp.lookupButton(ButtonType.CANCEL)).getStyleClass().add("btn-secondary");

        VBox main = new VBox(20);
        main.setPadding(new Insets(25));
        main.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        main.setPrefWidth(550);
        Label lbl = new Label(product == null ? "AJOUTER UN NOUVEAU PRODUIT" : "MODIFIER LE PRODUIT");
        lbl.setStyle("-fx-font-size: 20px; -fx-font-weight: 800; -fx-text-fill: #17BB9C;");
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #17BB9C; -fx-pref-height: 2px;");
        main.getChildren().addAll(lbl, sep);

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scroll.setPrefHeight(450);

        GridPane grid = new GridPane();
        grid.setHgap(20); grid.setVgap(20); grid.setPadding(new Insets(10));
        ColumnConstraints c1 = new ColumnConstraints(); c1.setMinWidth(140); c1.setHalignment(javafx.geometry.HPos.RIGHT);
        ColumnConstraints c2 = new ColumnConstraints(); c2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(c1, c2);

        TextField tNom   = createStyledTextField("Nom du produit");
        TextArea  tDesc  = createStyledTextArea("Description détaillée");
        TextField tPrix  = createStyledTextField("0.00");
        TextField tStock = createStyledTextField("0");
        ComboBox<String> cCat  = createStyledComboBox("Sélectionner une catégorie");
        ComboBox<String> cStat = createStyledComboBox("Sélectionner un statut");
        TextField tImg   = createStyledTextField("Chemin de l'image");
        tImg.setEditable(false);

        Button btnImg = new Button("📁 Parcourir");
        btnImg.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #2d3748; -fx-cursor: hand;");
        ImageView imgPrev = new ImageView();
        imgPrev.setFitHeight(100); imgPrev.setFitWidth(100); imgPrev.setPreserveRatio(true);
        HBox imgBox = new HBox(10, tImg, btnImg);
        HBox.setHgrow(tImg, Priority.ALWAYS);
        btnImg.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Choisir une image");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images","*.png","*.jpg","*.jpeg","*.gif"));
            File f = fc.showOpenDialog(dialog.getOwner());
            if (f != null) {
                tImg.setText(f.getAbsolutePath());
                try { imgPrev.setImage(new Image(f.toURI().toString())); } catch (Exception ex) { ex.printStackTrace(); }
            }
        });

        cCat.setItems(FXCollections.observableArrayList("Électronique","Vêtements","Alimentation","Maison & Jardin","Sports","Jouets","Livres","Beauté","Automobile"));
        cStat.setItems(FXCollections.observableArrayList("Disponible","Rupture de stock","Précommande","Archivé"));

        if (product != null) {
            tNom.setText(product.getNomProduit()); tDesc.setText(product.getDescription()); tPrix.setText(product.getPrix());
            tStock.setText(String.valueOf(product.getStockDisponible())); cCat.setValue(product.getCategorie());
            tImg.setText(product.getImageUrl()); cStat.setValue(product.getStatut());
            if (product.getImageUrl() != null && !product.getImageUrl().isEmpty())
                try { File f = new File(product.getImageUrl()); if (f.exists()) imgPrev.setImage(new Image(f.toURI().toString())); } catch (Exception ignored) {}
        }

        int r = 0;
        grid.add(createStyledLabel("Nom du produit *"), 0, r); grid.add(tNom,  1, r++);
        grid.add(createStyledLabel("Description *"),    0, r); grid.add(tDesc, 1, r++);
        grid.add(createStyledLabel("Prix (€) *"),       0, r); grid.add(tPrix, 1, r++);
        grid.add(createStyledLabel("Stock disponible *"),0,r); grid.add(tStock,1, r++);
        grid.add(createStyledLabel("Catégorie *"),      0, r); grid.add(cCat,  1, r++);
        grid.add(createStyledLabel("Image"),            0, r); grid.add(imgBox,1, r++);
        grid.add(createStyledLabel("Aperçu image"),     0, r); grid.add(imgPrev,1,r++);
        grid.add(createStyledLabel("Statut *"),         0, r); grid.add(cStat, 1, r++);
        scroll.setContent(grid);
        main.getChildren().add(scroll);
        dp.setContent(main);

        dialog.setResultConverter(db -> {
            if (db == saveType) {
                try {
                    String nom=tNom.getText().trim(), desc=tDesc.getText().trim(), prix=tPrix.getText().trim(),
                           stock=tStock.getText().trim(), cat=cCat.getValue(), img=tImg.getText().trim(), stat=cStat.getValue();
                    if (nom.isEmpty()||desc.isEmpty()||prix.isEmpty()||stock.isEmpty()||cat==null||stat==null) {
                        showDialog("Erreur","❌ Veuillez remplir tous les champs obligatoires (*)"); return null;
                    }
                    int stockInt = Integer.parseInt(stock);
                    if (product == null) return new Product(0,nom,desc,prix,stockInt,cat,img,stat);
                    product.setNomProduit(nom); product.setDescription(desc); product.setPrix(prix);
                    product.setStockDisponible(stockInt); product.setCategorie(cat);
                    product.setImageUrl(img); product.setStatut(stat);
                    return product;
                } catch (NumberFormatException e) { showDialog("Erreur","❌ Le stock doit être un nombre entier valide"); return null; }
            }
            return null;
        });

        Optional<Product> res = dialog.showAndWait();
        res.ifPresent(p -> {
            boolean ok = product == null ? productService.addProduct(p) : productService.updateProduct(p);
            if (ok) { loadData(); showDialog("Succès","✅ Produit "+(product==null?"ajouté":"modifié")+" avec succès!"); }
            else showDialog("Erreur","❌ Erreur lors de l'opération");
        });
    }

    // =========================================================================
    //  SHARED UI HELPERS
    // =========================================================================
    private TextField createStyledTextField(String prompt) {
        TextField tf = new TextField(); tf.setPromptText(prompt); tf.setStyle(FIELD_NORMAL); return tf;
    }
    private TextArea createStyledTextArea(String prompt) {
        TextArea ta = new TextArea(); ta.setPromptText(prompt); ta.setPrefRowCount(4); ta.setWrapText(true); ta.setStyle(FIELD_NORMAL); return ta;
    }
    private ComboBox<String> createStyledComboBox(String prompt) {
        ComboBox<String> cb = new ComboBox<>(); cb.setPromptText(prompt); cb.setMaxWidth(Double.MAX_VALUE); cb.setStyle(COMBO_NORMAL); return cb;
    }
    private Label createStyledLabel(String text) {
        Label lbl = new Label(text); lbl.setStyle("-fx-font-weight: 600; -fx-text-fill: #2d3748; -fx-font-size: 13px;"); return lbl;
    }
    private void showDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(message); alert.showAndWait();
    }
    private String getCurrentDateTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm"));
    }

    public static void main(String[] args) { launch(args); }
}
