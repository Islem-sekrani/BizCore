package com.gestion;

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

    // Validation style constants
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
            System.err.println("Erreur fatale: " + e.getMessage());
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

            initializeModuleView(moduleLoader.getNamespace(), moduleView);
            loadData();

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
                    "Prix (Croissant)", "Prix (Decroissant)", "Stock (Croissant)", "Stock (Decroissant)"));
            sortComboBox.valueProperty().addListener((obs, ov, nv) -> { if (nv != null) sortProducts(nv); });
        }

        initializeTable();
    }

    private void initializeTable() {
        if (dataTable == null) return;
        dataTable.getColumns().clear();

        TableColumn<Object, Boolean> selectCol = new TableColumn<>("");
        selectCol.setMaxWidth(50); selectCol.setMinWidth(50);
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
            case "BLOG"         -> createBlogColumns();
            case "PRODUITS"     -> createProductColumns();
        }

        TableColumn<Object, Void> actionCol = new TableColumn<>("Action");
        actionCol.setMinWidth(150); actionCol.setMaxWidth(150);
        actionCol.setCellFactory(createActionButtons());
        dataTable.getColumns().add(actionCol);
    }

    private void createUserColumns()    { addColumn("ID","id",60); addColumn("Prenom","firstName",120); addColumn("Nom","lastName",120); addColumn("Email","email",200); addColumn("Role","role",100); addColumn("Connexion","lastUpdate",180); }
    private void createEventColumns()   { addColumn("ID","id",60); addColumn("Titre","title",200); addColumn("Type","type",120); addColumn("Date","eventDate",150); addColumn("Lieu","location",150); addColumn("Statut","status",100); }
    private void createCoachingColumns(){ addColumn("ID","id",60); addColumn("Coach","coachName",150); addColumn("Specialite","specialty",150); addColumn("Client","clientName",150); addColumn("Session","nextSession",180); addColumn("Statut","status",100); }
    private void createBlogColumns()    { addColumn("ID","idArticle",60); addColumn("Titre","titre",140); addColumn("Contenu","contenu",180); addColumn("Image","imagePrincipale",100); addColumn("Categorie","categorie",100); addColumn("Statut","statut",90); addColumn("Vues","nombreVues",70); }
    private void createProductColumns() { addColumn("ID","idProduit",40); addColumn("Nom","nomProduit",130); addColumn("Description","description",160); addColumn("Prix","prix",70); addColumn("Stock","stockDisponible",60); addColumn("Categorie","categorie",90); addColumn("Image","imageUrl",90); addColumn("Statut","statut",80); }

    private void addColumn(String title, String property, double width) {
        TableColumn<Object, String> col = new TableColumn<>(title);
        col.setCellValueFactory(new PropertyValueFactory<>(property));
        col.setMinWidth(width);
        dataTable.getColumns().add(col);
    }

    private Callback<TableColumn<Object, Void>, TableCell<Object, Void>> createActionButtons() {
        return param -> new TableCell<>() {
            private final Button vBtn = new Button("o"), eBtn = new Button("E"), dBtn = new Button("D");
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
                new User(3,"Pierre","Bernard","pierre@email.com","Moderateur",getCurrentDateTime()));
            case "EVENEMENTS" -> dataList.addAll(
                new Event(1,"Conference Tech 2024","Conference","15 Mars 2024","Paris","Confirme"),
                new Event(2,"Workshop IA","Atelier","20 Mars 2024","Lyon","En attente"));
            case "COACHING" -> dataList.addAll(
                new Coaching(1,"Dr. Sarah Johnson","Dev Personnel","Marc Leroy","18 Fev 2024 14:00","Actif"),
                new Coaching(2,"Jean-Paul Dubois","Business","Claire Martin","20 Fev 2024 10:00","Actif"));
            case "BLOG"     -> dataList.addAll(blogService.getAllArticles());
            case "PRODUITS" -> dataList.addAll(productService.getAllProducts());
        }
        updateTable();
        updateResultsLabel();
    }

    private void updateTable() { if (dataTable != null) dataTable.setItems(dataList); }

    private void updateResultsLabel() {
        if (lblResults != null && dataList != null)
            lblResults.setText(String.format("Resultats 1 a %d de %d", Math.min(itemsPerPage, dataList.size()), dataList.size()));
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
            case "Prix (Croissant)"  -> (a,b) -> { try { return Double.compare(Double.parseDouble(((Product)a).getPrix().replace(",",".").replaceAll("[^0-9.]","")), Double.parseDouble(((Product)b).getPrix().replace(",",".").replaceAll("[^0-9.]",""))); } catch(Exception e){return 0;} };
            case "Prix (Decroissant)"-> (a,b) -> { try { return Double.compare(Double.parseDouble(((Product)b).getPrix().replace(",",".").replaceAll("[^0-9.]","")), Double.parseDouble(((Product)a).getPrix().replace(",",".").replaceAll("[^0-9.]",""))); } catch(Exception e){return 0;} };
            case "Stock (Croissant)" -> (a,b) -> Integer.compare(((Product)a).getStockDisponible(),((Product)b).getStockDisponible());
            case "Stock (Decroissant)"-> (a,b)-> Integer.compare(((Product)b).getStockDisponible(),((Product)a).getStockDisponible());
            default -> null;
        };
        if (cmp != null) { FXCollections.sort(dataList, cmp); updateTable(); }
    }

    private void handleAdd() {
        if ("PRODUITS".equals(currentModule))  showProductDialog(null);
        else if ("BLOG".equals(currentModule)) showBlogDialog(null);
        else showDialog("Ajouter", "Fonction d'ajout pour " + currentModule);
    }

    private void handleView(Object item)   { if (item != null) showDialog("Voir", item.toString()); }

    private void handleEdit(Object item) {
        if ("PRODUITS".equals(currentModule) && item instanceof Product) showProductDialog((Product) item);
        else if ("BLOG".equals(currentModule) && item instanceof Blog)   showBlogDialog((Blog) item);
        else if (item != null) showDialog("Modifier", item.toString());
    }

    private void handleDelete(Object item) {
        if (item == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation"); confirm.setHeaderText("Supprimer l'element");
        confirm.setContentText("Etes-vous sur de vouloir supprimer cet element?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean deleted = false;
            if ("PRODUITS".equals(currentModule) && item instanceof Product)
                deleted = productService.deleteProduct(((Product) item).getIdProduit());
            else if ("BLOG".equals(currentModule) && item instanceof Blog)
                deleted = blogService.deleteArticle(((Blog) item).getIdArticle());
            if (deleted) { loadData(); showDialog("Succes", "Element supprime avec succes"); }
            else showDialog("Erreur", "Erreur lors de la suppression");
        }
    }

    private void handleDeleteSelected() { showDialog("Supprimer", "Suppression multiple"); }

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
            title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER); title.setSpacingAfter(20);
            doc.add(title);
            PdfPTable table = new PdfPTable(6); table.setWidthPercentage(100);
            for (String h : new String[]{"ID","Nom","Prix","Stock","Categorie","Statut"}) {
                PdfPCell cell = new PdfPCell(new Phrase(h));
                cell.setBackgroundColor(com.itextpdf.text.BaseColor.LIGHT_GRAY); cell.setPadding(5);
                table.addCell(cell);
            }
            for (Object item : dataList) {
                if (item instanceof Product p) {
                    table.addCell(String.valueOf(p.getIdProduit())); table.addCell(p.getNomProduit());
                    table.addCell(p.getPrix()); table.addCell(String.valueOf(p.getStockDisponible()));
                    table.addCell(p.getCategorie()); table.addCell(p.getStatut());
                }
            }
            doc.add(table); doc.close();
            showDialog("Succes", "Export PDF reussi:\n" + fileName);
            try { java.awt.Desktop.getDesktop().open(new File(fileName)); } catch (Exception ignored) {}
        } catch (Exception e) { e.printStackTrace(); showDialog("Erreur", "Erreur export PDF: " + e.getMessage()); }
    }

    private void handleStatistics() {
        if (!"PRODUITS".equals(currentModule)) { showDialog("Info", "Statistiques disponibles pour les produits."); return; }
        Stage statsStage = new Stage();
        statsStage.setTitle("Statistiques Produits");
        VBox root = new VBox(20); root.setPadding(new Insets(20)); root.setStyle("-fx-background-color: white;");
        javafx.scene.chart.PieChart pie = new javafx.scene.chart.PieChart();
        pie.setTitle("Repartition par Categorie");
        java.util.Map<String,Integer> cats = new java.util.HashMap<>(), stats = new java.util.HashMap<>();
        for (Object item : dataList) {
            if (item instanceof Product p) {
                cats.merge(p.getCategorie(), 1, Integer::sum);
                stats.merge(p.getStatut(), 1, Integer::sum);
            }
        }
        cats.forEach((c,n) -> pie.getData().add(new javafx.scene.chart.PieChart.Data(c+" ("+n+")", n)));
        BarChart<String,Number> bar = new BarChart<>(new CategoryAxis(), new NumberAxis());
        bar.setTitle("Etat du Stock");
        XYChart.Series<String,Number> series = new XYChart.Series<>(); series.setName("Produits");
        stats.forEach((s,n) -> series.getData().add(new XYChart.Data<>(s, n)));
        bar.getData().add(series);
        HBox charts = new HBox(20, pie, bar); charts.setAlignment(Pos.CENTER);
        HBox.setHgrow(pie, Priority.ALWAYS); HBox.setHgrow(bar, Priority.ALWAYS);
        root.getChildren().add(charts);
        statsStage.setScene(new Scene(root, 1000, 500));
        statsStage.show();
    }

    // =========================================================================
    //  PRODUCT DIALOG (unchanged logic)
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

        VBox main = new VBox(20); main.setPadding(new Insets(25));
        main.setStyle("-fx-background-color: white; -fx-background-radius: 10;"); main.setPrefWidth(550);
        Label lbl = new Label(product == null ? "AJOUTER UN NOUVEAU PRODUIT" : "MODIFIER LE PRODUIT");
        lbl.setStyle("-fx-font-size: 20px; -fx-font-weight: 800; -fx-text-fill: #17BB9C;");
        Separator sep = new Separator(); sep.setStyle("-fx-background-color: #17BB9C; -fx-pref-height: 2px;");
        main.getChildren().addAll(lbl, sep);

        ScrollPane scroll = new ScrollPane(); scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;"); scroll.setPrefHeight(450);

        GridPane grid = new GridPane(); grid.setHgap(20); grid.setVgap(20); grid.setPadding(new Insets(10));
        ColumnConstraints c1 = new ColumnConstraints(); c1.setMinWidth(140); c1.setHalignment(javafx.geometry.HPos.RIGHT);
        ColumnConstraints c2 = new ColumnConstraints(); c2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(c1, c2);

        TextField tNom = createStyledTextField("Nom du produit"), tPrix = createStyledTextField("0.00"), tStock = createStyledTextField("0"), tImg = createStyledTextField("Chemin image");
        TextArea tDesc = createStyledTextArea("Description");
        ComboBox<String> cCat = createStyledComboBox("Selectionner une categorie"), cStat = createStyledComboBox("Selectionner un statut");
        tImg.setEditable(false);
        Button btnImg = new Button("Parcourir"); btnImg.setStyle("-fx-background-color: #e2e8f0; -fx-cursor: hand;");
        ImageView imgPrev = new ImageView(); imgPrev.setFitHeight(100); imgPrev.setFitWidth(100); imgPrev.setPreserveRatio(true);
        HBox imgBox = new HBox(10, tImg, btnImg); HBox.setHgrow(tImg, Priority.ALWAYS);
        btnImg.setOnAction(e -> {
            FileChooser fc = new FileChooser(); fc.setTitle("Choisir image");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images","*.png","*.jpg","*.jpeg","*.gif"));
            File f = fc.showOpenDialog(dialog.getOwner());
            if (f != null) { tImg.setText(f.getAbsolutePath()); try { imgPrev.setImage(new Image(f.toURI().toString())); } catch (Exception ex) { ex.printStackTrace(); } }
        });
        cCat.setItems(FXCollections.observableArrayList("Electronique","Vetements","Alimentation","Maison & Jardin","Sports","Jouets","Livres","Beaute","Automobile"));
        cStat.setItems(FXCollections.observableArrayList("Disponible","Rupture de stock","Precommande","Archive"));

        if (product != null) {
            tNom.setText(product.getNomProduit()); tDesc.setText(product.getDescription()); tPrix.setText(product.getPrix());
            tStock.setText(String.valueOf(product.getStockDisponible())); cCat.setValue(product.getCategorie());
            tImg.setText(product.getImageUrl()); cStat.setValue(product.getStatut());
            if (product.getImageUrl() != null && !product.getImageUrl().isEmpty())
                try { File f = new File(product.getImageUrl()); if (f.exists()) imgPrev.setImage(new Image(f.toURI().toString())); } catch (Exception ignored) {}
        }
        int r = 0;
        grid.add(createStyledLabel("Nom *"),0,r); grid.add(tNom,1,r++);
        grid.add(createStyledLabel("Description *"),0,r); grid.add(tDesc,1,r++);
        grid.add(createStyledLabel("Prix *"),0,r); grid.add(tPrix,1,r++);
        grid.add(createStyledLabel("Stock *"),0,r); grid.add(tStock,1,r++);
        grid.add(createStyledLabel("Categorie *"),0,r); grid.add(cCat,1,r++);
        grid.add(createStyledLabel("Image"),0,r); grid.add(imgBox,1,r++);
        grid.add(createStyledLabel("Apercu"),0,r); grid.add(imgPrev,1,r++);
        grid.add(createStyledLabel("Statut *"),0,r); grid.add(cStat,1,r++);
        scroll.setContent(grid); main.getChildren().add(scroll); dp.setContent(main);

        dialog.setResultConverter(db -> {
            if (db == saveType) {
                try {
                    String nom=tNom.getText().trim(),desc=tDesc.getText().trim(),prix=tPrix.getText().trim(),stock=tStock.getText().trim(),cat=cCat.getValue(),img=tImg.getText().trim(),stat=cStat.getValue();
                    if (nom.isEmpty()||desc.isEmpty()||prix.isEmpty()||stock.isEmpty()||cat==null||stat==null) { showDialog("Erreur","Veuillez remplir tous les champs obligatoires (*)"); return null; }
                    int stockInt = Integer.parseInt(stock);
                    if (product == null) return new Product(0,nom,desc,prix,stockInt,cat,img,stat);
                    product.setNomProduit(nom); product.setDescription(desc); product.setPrix(prix); product.setStockDisponible(stockInt); product.setCategorie(cat); product.setImageUrl(img); product.setStatut(stat);
                    return product;
                } catch (NumberFormatException e) { showDialog("Erreur","Le stock doit etre un entier valide"); return null; }
            }
            return null;
        });
        Optional<Product> res = dialog.showAndWait();
        res.ifPresent(p -> {
            boolean ok = product == null ? productService.addProduct(p) : productService.updateProduct(p);
            if (ok) { loadData(); showDialog("Succes","Produit "+(product==null?"ajoute":"modifie")+" avec succes!"); }
            else showDialog("Erreur","Erreur lors de l'operation");
        });
    }

    // =========================================================================
    //  BLOG DIALOG — PROFESSIONAL VALIDATION
    // =========================================================================
    private void showBlogDialog(Blog article) {
        Stage modal = new Stage();
        modal.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        modal.setTitle(article == null ? "Nouvel Article" : "Modifier Article");
        modal.setResizable(false);

        // Fields
        TextField        txtTitre    = createStyledTextField("Titre de l'article");
        TextArea         txtContenu  = createStyledTextArea("Contenu de l'article");
        TextField        txtImage    = createStyledTextField("https://exemple.com/image.jpg  (optionnel)");
        ComboBox<String> cmbCategorie = createStyledComboBox("Selectionner une categorie");
        ComboBox<String> cmbStatut   = createStyledComboBox("Selectionner un statut");
        TextField        txtVues     = createStyledTextField("0");

        cmbCategorie.setItems(FXCollections.observableArrayList(
                "Technologie","Developpement","Bien-etre","Business","Management",
                "Innovation","Coaching","Marketing","Finance","Education"));
        cmbStatut.setItems(FXCollections.observableArrayList("Brouillon","Publie","Archive","En revision"));

        // Error labels
        Label eTitre     = buildErrorLabel();
        Label eContenu   = buildErrorLabel();
        Label eImage     = buildErrorLabel();
        Label eCategorie = buildErrorLabel();
        Label eStatut    = buildErrorLabel();
        Label eVues      = buildErrorLabel();

        // Pre-fill for EDIT
        if (article != null) {
            txtTitre.setText(article.getTitre());
            txtContenu.setText(article.getContenu());
            txtImage.setText(article.getImagePrincipale() != null ? article.getImagePrincipale() : "");
            cmbCategorie.setValue(article.getCategorie());
            cmbStatut.setValue(article.getStatut());
            txtVues.setText(String.valueOf(article.getNombreVues()));
        }

        // Live border-clearing listeners
        txtTitre.textProperty().addListener((o,ov,nv) -> { if (nv!=null && nv.trim().length()>=3 && !nv.trim().matches("\\d+")) clearValidationError(txtTitre,eTitre); });
        txtContenu.textProperty().addListener((o,ov,nv) -> { if (nv!=null && nv.trim().length()>=20) clearValidationError(txtContenu,eContenu); });
        txtImage.textProperty().addListener((o,ov,nv) -> { if (nv==null||nv.trim().isEmpty()||isValidUrl(nv.trim())) clearValidationError(txtImage,eImage); });
        cmbCategorie.valueProperty().addListener((o,ov,nv) -> { if (nv!=null && !nv.equals("Selectionner une categorie")) clearValidationError(cmbCategorie,eCategorie); });
        cmbStatut.valueProperty().addListener((o,ov,nv) -> { if (nv!=null && !nv.equals("Selectionner un statut")) clearValidationError(cmbStatut,eStatut); });
        txtVues.textProperty().addListener((o,ov,nv) -> { if (isValidNombreVues(nv)) clearValidationError(txtVues,eVues); });

        // Layout
        Label titleLbl = new Label(article == null ? "AJOUTER UN NOUVEL ARTICLE" : "MODIFIER L'ARTICLE");
        titleLbl.setStyle("-fx-font-size: 20px; -fx-font-weight: 800; -fx-text-fill: #17BB9C; -fx-font-family: 'Segoe UI', sans-serif;");
        Separator sep = new Separator(); sep.setStyle("-fx-background-color: #17BB9C; -fx-pref-height: 2px;");

        GridPane grid = new GridPane(); grid.setHgap(20); grid.setVgap(6); grid.setPadding(new Insets(10));
        ColumnConstraints c1 = new ColumnConstraints(); c1.setMinWidth(150); c1.setHalignment(javafx.geometry.HPos.RIGHT);
        ColumnConstraints c2 = new ColumnConstraints(); c2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(c1, c2);

        int row = 0;
        grid.add(createStyledLabel("Titre *"),          0, row); grid.add(txtTitre,    1, row++);
        grid.add(new Label(),                           0, row); grid.add(eTitre,      1, row++);
        grid.add(createStyledLabel("Contenu *"),        0, row); grid.add(txtContenu,  1, row++);
        grid.add(new Label(),                           0, row); grid.add(eContenu,    1, row++);
        grid.add(createStyledLabel("Image principale"), 0, row); grid.add(txtImage,    1, row++);
        grid.add(new Label(),                           0, row); grid.add(eImage,      1, row++);
        grid.add(createStyledLabel("Categorie *"),      0, row); grid.add(cmbCategorie,1, row++);
        grid.add(new Label(),                           0, row); grid.add(eCategorie,  1, row++);
        grid.add(createStyledLabel("Statut *"),         0, row); grid.add(cmbStatut,   1, row++);
        grid.add(new Label(),                           0, row); grid.add(eStatut,     1, row++);
        grid.add(createStyledLabel("Nombre de vues"),   0, row); grid.add(txtVues,     1, row++);
        grid.add(new Label(),                           0, row); grid.add(eVues,       1, row++);

        Button btnSave   = new Button(article == null ? "Ajouter" : "Enregistrer");
        Button btnCancel = new Button("Annuler");
        btnSave.setStyle("-fx-background-color: #17BB9C; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 10 25; -fx-cursor: hand; -fx-background-radius: 6px;");
        btnCancel.setStyle("-fx-background-color: #e53e3e; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 10 25; -fx-cursor: hand; -fx-background-radius: 6px;");
        btnCancel.setOnAction(e -> modal.close());

        HBox buttons = new HBox(15, btnSave, btnCancel);
        buttons.setAlignment(Pos.CENTER_RIGHT); buttons.setPadding(new Insets(15,10,5,0));

        ScrollPane scroll = new ScrollPane(grid); scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;"); scroll.setPrefHeight(460);

        VBox mainContainer = new VBox(12, titleLbl, sep, scroll, buttons);
        mainContainer.setPadding(new Insets(25)); mainContainer.setStyle("-fx-background-color: white;"); mainContainer.setPrefWidth(580);

        // Save with full validation
        btnSave.setOnAction(e -> {
            // Reset all
            clearValidationError(txtTitre, eTitre); clearValidationError(txtContenu, eContenu);
            clearValidationError(txtImage, eImage);   clearValidationError(cmbCategorie, eCategorie);
            clearValidationError(cmbStatut, eStatut); clearValidationError(txtVues, eVues);

            boolean valid = true;

            // 1. Titre
            String titre = txtTitre.getText();
            if (titre == null || titre.trim().isEmpty()) {
                setValidationError(txtTitre, eTitre, "Le titre est obligatoire."); valid = false;
            } else if (titre.trim().length() < 3) {
                setValidationError(txtTitre, eTitre, "Le titre doit contenir au moins 3 caracteres."); valid = false;
            } else if (titre.trim().length() > 255) {
                setValidationError(txtTitre, eTitre, "Le titre ne doit pas depasser 255 caracteres."); valid = false;
            } else if (titre.trim().matches("\\d+")) {
                setValidationError(txtTitre, eTitre, "Le titre ne peut pas contenir uniquement des chiffres."); valid = false;
            }

            // 2. Contenu
            String contenu = txtContenu.getText();
            if (contenu == null || contenu.trim().isEmpty()) {
                setValidationError(txtContenu, eContenu, "Le contenu est obligatoire."); valid = false;
            } else if (contenu.trim().length() < 20) {
                setValidationError(txtContenu, eContenu, "Le contenu doit contenir au moins 20 caracteres."); valid = false;
            }

            // 3. Image (optionnel)
            String image = txtImage.getText();
            if (image != null && !image.trim().isEmpty() && !isValidUrl(image.trim())) {
                setValidationError(txtImage, eImage, "URL invalide. Doit commencer par http:// ou https://"); valid = false;
            }

            // 4. Categorie
            String categorie = cmbCategorie.getValue();
            if (categorie == null || categorie.equals("Selectionner une categorie")) {
                setValidationError(cmbCategorie, eCategorie, "Veuillez selectionner une categorie."); valid = false;
            }

            // 5. Statut
            String statut = cmbStatut.getValue();
            if (statut == null || statut.equals("Selectionner un statut")) {
                setValidationError(cmbStatut, eStatut, "Veuillez selectionner un statut."); valid = false;
            }

            // 6. Nombre de vues
            String vuesStr = txtVues.getText();
            int nombreVues = 0;
            if (vuesStr != null && !vuesStr.trim().isEmpty()) {
                if (!isValidNombreVues(vuesStr.trim())) {
                    setValidationError(txtVues, eVues, "Le nombre de vues doit etre un entier >= 0."); valid = false;
                } else {
                    nombreVues = Integer.parseInt(vuesStr.trim());
                }
            }

            // STOP if invalid — DB never touched
            if (!valid) return;

            // Persist
            boolean success;
            if (article == null) {
                Blog newArt = new Blog(0, titre.trim(), contenu.trim(),
                        (image != null && !image.trim().isEmpty()) ? image.trim() : null,
                        categorie, statut, nombreVues);
                success = blogService.addArticle(newArt);
            } else {
                article.setTitre(titre.trim()); article.setContenu(contenu.trim());
                article.setImagePrincipale((image != null && !image.trim().isEmpty()) ? image.trim() : null);
                article.setCategorie(categorie); article.setStatut(statut); article.setNombreVues(nombreVues);
                success = blogService.updateArticle(article);
            }

            if (success) {
                modal.close(); loadData();
                showDialog("Succes", "Article " + (article == null ? "ajoute" : "modifie") + " avec succes!");
            } else {
                showDialog("Erreur", "Erreur lors de l'operation. Verifiez votre connexion WAMP/MySQL.");
            }
        });

        modal.setScene(new Scene(mainContainer));
        modal.showAndWait();
    }

    // =========================================================================
    //  VALIDATION HELPERS
    // =========================================================================
    private void setValidationError(Control field, Label lbl, String msg) {
        if (field instanceof ComboBox) field.setStyle(COMBO_ERROR);
        else field.setStyle(FIELD_ERROR);
        lbl.setText("  " + msg);
        lbl.setVisible(true); lbl.setManaged(true);
    }

    private void clearValidationError(Control field, Label lbl) {
        if (field instanceof ComboBox) field.setStyle(COMBO_NORMAL);
        else field.setStyle(FIELD_NORMAL);
        lbl.setText(""); lbl.setVisible(false); lbl.setManaged(false);
    }

    private boolean isValidUrl(String url) {
        return url != null && (url.startsWith("http://") || url.startsWith("https://")) && url.length() > 10;
    }

    private boolean isValidNombreVues(String val) {
        if (val == null || val.trim().isEmpty()) return true;
        try { return Integer.parseInt(val.trim()) >= 0; } catch (NumberFormatException e) { return false; }
    }

    private Label buildErrorLabel() {
        Label lbl = new Label(); lbl.setStyle(ERR_LABEL_STYLE);
        lbl.setVisible(false); lbl.setManaged(false); lbl.setWrapText(true); lbl.setMaxWidth(340);
        return lbl;
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
