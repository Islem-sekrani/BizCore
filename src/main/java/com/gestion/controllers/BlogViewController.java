package com.gestion.controllers;

import com.gestion.analyzers.BlogAnalysisResult;
import com.gestion.analyzers.TrendingResult;
import com.gestion.entities.Blog;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * BlogViewController
 * ──────────────────
 * JavaFX UI controller for the Blog module (database table: article).
 * Uses Blog.java as the entity and BlogController for business logic.
 *
 * Wired into MainClass via:
 *
 *   BlogViewController blogVC = new BlogViewController();
 *   blogVC.initialize(dataTable, searchField, sortComboBox, lblResults);
 *   blogVC.bindAddButton(btnAdd);
 *   blogVC.bindDeleteButton(btnDelete);
 */
public class BlogViewController {

    // ── Delegate ──────────────────────────────────────────────────────────────
    private final BlogController blogController = new BlogController();

    // ── UI references — injected from MainClass namespace ──────────────────────
    private TableView<Blog>    dataTable;
    private TextField          searchField;
    private ComboBox<String>   sortComboBox;
    private Label              lblResults;

    // ── Data ──────────────────────────────────────────────────────────────────
    private ObservableList<Blog> masterList  = FXCollections.observableArrayList();
    private ObservableList<Blog> displayList = FXCollections.observableArrayList();

    // ── Validation style constants (match project palette) ─────────────────────
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
    private static final String ERR_STYLE =
            "-fx-text-fill: #e53e3e; -fx-font-size: 11px; -fx-font-weight: 600;";

    // =========================================================================
    //  PUBLIC API
    // =========================================================================

    /**
     * Wire UI components from the ModuleView.fxml namespace and load data.
     * Called by MainClass immediately after FXMLLoader.load() for the BLOG module.
     */
    @SuppressWarnings("unchecked")
    public void initialize(TableView<?> table,
                           TextField search,
                           ComboBox<?> sort,
                           Label results) {

        this.dataTable   = (TableView<Blog>) table;
        this.searchField = search;
        this.sortComboBox = (ComboBox<String>) sort;
        this.lblResults  = results;

        setupColumns();
        setupSortComboBox();
        setupSearchListener();
        loadData();
    }

    /** Wire the ADD NEW button from the ModuleView toolbar. */
    public void bindAddButton(Button btnAdd) {
        if (btnAdd != null)
            btnAdd.setOnAction(e -> showBlogDialog(null));
    }

    /** Wire the DELETE SELECTED button from the ModuleView toolbar. */
    public void bindDeleteButton(Button btnDelete) {
        if (btnDelete != null)
            btnDelete.setOnAction(e -> handleDeleteSelected());
    }

    /** Wire the EXPORT PDF button from the ModuleView toolbar. */
    public void bindExportButton(Button btnExport) {
        if (btnExport != null)
            btnExport.setOnAction(e -> handleExportPdf());
    }

    /** Wire the STATISTICS icon button from the ModuleView toolbar. */
    public void bindStatsButton(Button btnStats) {
        if (btnStats != null)
            btnStats.setOnAction(e -> handleStatistics());
    }

    // =========================================================================
    //  TABLE COLUMNS — ID, Titre, Contenu, Image, Catégorie, Statut, Vues
    // =========================================================================

    @SuppressWarnings("unchecked")
    private void setupColumns() {
        if (dataTable == null) return;
        dataTable.getColumns().clear();

        // Checkbox column — matches existing project style
        TableColumn<Blog, Boolean> colCheck = new TableColumn<>("");
        colCheck.setMaxWidth(50);
        colCheck.setMinWidth(50);
        colCheck.setCellFactory(col -> new TableCell<>() {
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

        // ID  →  Blog.idArticle  →  DB: id_article
        TableColumn<Blog, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("idArticle"));
        colId.setMinWidth(60);
        colId.setMaxWidth(70);

        // Titre  →  Blog.titre  →  DB: titre
        TableColumn<Blog, String> colTitre = new TableColumn<>("Titre");
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colTitre.setMinWidth(140);

        // Contenu  →  Blog.contenu  →  DB: contenu
        TableColumn<Blog, String> colContenu = new TableColumn<>("Contenu");
        colContenu.setCellValueFactory(new PropertyValueFactory<>("contenu"));
        colContenu.setMinWidth(180);

        // Image  →  Blog.imagePrincipale  →  DB: image_principale
        TableColumn<Blog, String> colImage = new TableColumn<>("Image");
        colImage.setCellValueFactory(new PropertyValueFactory<>("imagePrincipale"));
        colImage.setMinWidth(100);

        // Catégorie  →  Blog.categorie  →  DB: categorie
        TableColumn<Blog, String> colCategorie = new TableColumn<>("Catégorie");
        colCategorie.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        colCategorie.setMinWidth(100);

        // Statut  →  Blog.statut  →  DB: statut
        TableColumn<Blog, String> colStatut = new TableColumn<>("Statut");
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colStatut.setMinWidth(90);
        colStatut.setMaxWidth(110);

        // Vues  →  Blog.nombreVues  →  DB: nombre_vues
        TableColumn<Blog, Integer> colVues = new TableColumn<>("Vues");
        colVues.setCellValueFactory(new PropertyValueFactory<>("nombreVues"));
        colVues.setMinWidth(70);
        colVues.setMaxWidth(90);

        // Actions column — 👁 view  ✎ edit  🗑 delete  🔬 analyze
        TableColumn<Blog, Void> colActions = new TableColumn<>("Action");
        colActions.setMinWidth(195);
        colActions.setMaxWidth(195);
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnView    = new Button("👁");
            private final Button btnEdit    = new Button("✎");
            private final Button btnDelete  = new Button("🗑");
            private final Button btnAnalyze = new Button("🔬");
            private final HBox   box        = new HBox(4, btnView, btnEdit, btnDelete, btnAnalyze);
            {
                btnView.getStyleClass().add("action-btn-view");
                btnEdit.getStyleClass().add("action-btn-edit");
                btnDelete.getStyleClass().add("action-btn-delete");
                btnAnalyze.setStyle(
                        "-fx-background-color: #17BB9C; -fx-text-fill: white; " +
                        "-fx-font-size: 10px; -fx-font-weight: bold; " +
                        "-fx-padding: 4 7; -fx-cursor: hand; -fx-background-radius: 4;");
                btnAnalyze.setTooltip(new Tooltip("Analyser la qualité de l'article"));
                box.setAlignment(Pos.CENTER);

                btnView.setOnAction(e -> {
                    Blog b = getTableView().getItems().get(getIndex());
                    showDetailDialog(b);
                });
                btnEdit.setOnAction(e -> {
                    Blog b = getTableView().getItems().get(getIndex());
                    showBlogDialog(b);
                });
                btnDelete.setOnAction(e -> {
                    Blog b = getTableView().getItems().get(getIndex());
                    handleDeleteOne(b);
                });
                btnAnalyze.setOnAction(e -> {
                    Blog b = getTableView().getItems().get(getIndex());
                    handleAnalyze(b);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        dataTable.getColumns().addAll(
                colCheck, colId, colTitre, colContenu,
                colImage, colCategorie, colStatut, colVues, colActions);

        dataTable.setPlaceholder(new Label("Aucun article trouvé."));
    }

    // =========================================================================
    //  DATA
    // =========================================================================

    private void loadData() {
        masterList  = blogController.getAllBlogs();
        displayList = FXCollections.observableArrayList(masterList);
        applyCurrentSort();
        updateTable();
        updateResultsLabel();
    }

    private void updateTable() {
        if (dataTable != null) dataTable.setItems(displayList);
    }

    private void updateResultsLabel() {
        if (lblResults != null)
            lblResults.setText("Résultats : " + displayList.size() + " article(s)");
    }

    // =========================================================================
    //  SORT — Vues (Croissant/Décroissant), Titre (A-Z / Z-A)
    // =========================================================================

    private void setupSortComboBox() {
        if (sortComboBox == null) return;

        sortComboBox.setItems(FXCollections.observableArrayList(
                "-- Trier Articles --",
                "Vues (Croissant)",
                "Vues (Décroissant)",
                "Titre (A-Z)",
                "Titre (Z-A)"));
        sortComboBox.getSelectionModel().selectFirst();

        sortComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.startsWith("--")) {
                applyCurrentSort();
                updateTable();
                updateResultsLabel();
            }
        });
    }

    private void applyCurrentSort() {
        if (sortComboBox == null) return;
        String selected = sortComboBox.getValue();
        if (selected == null || selected.startsWith("--")) return;
        displayList = blogController.sortBlogs(displayList, selected);
    }

    // =========================================================================
    //  SEARCH / FILTER — by titre, categorie, statut
    // =========================================================================

    private void setupSearchListener() {
        if (searchField == null) return;

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty()) {
                displayList = FXCollections.observableArrayList(masterList);
            } else {
                displayList = blogController.filterBlogs(newVal);
            }
            applyCurrentSort();
            updateTable();
            updateResultsLabel();
        });
    }

    // =========================================================================
    //  DELETE
    // =========================================================================

    private void handleDeleteOne(Blog blog) {
        if (blog == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer la suppression");
        confirm.setHeaderText(null);
        confirm.setContentText("Supprimer l'article « " + blog.getTitre() + " » ?\nCette action est irréversible.");
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = blogController.deleteBlog(blog.getIdArticle());
            if (success) {
                loadData();
                showInfo("Succès", "✅ Article supprimé avec succès.");
            } else {
                showError("Erreur", "❌ Impossible de supprimer l'article.\nVérifiez votre connexion WAMP/MySQL.");
            }
        }
    }

    private void handleDeleteSelected() {
        Blog selected = dataTable != null ? dataTable.getSelectionModel().getSelectedItem() : null;
        if (selected == null) {
            showInfo("Aucune sélection", "Veuillez sélectionner un article dans la table.");
            return;
        }
        handleDeleteOne(selected);
    }

    // =========================================================================
    //  DETAIL DIALOG  (View 👁)
    // =========================================================================

    private void showDetailDialog(Blog blog) {
        if (blog == null) return;

        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.setTitle("Détails de l'Article #" + blog.getIdArticle());
        modal.setResizable(false);

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(14);
        grid.setPadding(new Insets(20));
        ColumnConstraints c1 = new ColumnConstraints(); c1.setMinWidth(130); c1.setHalignment(javafx.geometry.HPos.RIGHT);
        ColumnConstraints c2 = new ColumnConstraints(); c2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(c1, c2);

        int row = 0;
        grid.add(detailLabel("ID"),              0, row); grid.add(valueLabel(String.valueOf(blog.getIdArticle())), 1, row++);
        grid.add(detailLabel("Titre"),           0, row); grid.add(valueLabel(blog.getTitre()),           1, row++);
        grid.add(detailLabel("Catégorie"),       0, row); grid.add(valueLabel(blog.getCategorie()),       1, row++);
        grid.add(detailLabel("Statut"),          0, row); grid.add(valueLabel(blog.getStatut()),          1, row++);
        grid.add(detailLabel("Nombre de vues"),  0, row); grid.add(valueLabel(String.valueOf(blog.getNombreVues())), 1, row++);
        grid.add(detailLabel("Image principale"),0, row); grid.add(valueLabel(blog.getImagePrincipale() != null ? blog.getImagePrincipale() : "—"), 1, row++);

        TextArea taContenu = new TextArea(blog.getContenu());
        taContenu.setEditable(false);
        taContenu.setWrapText(true);
        taContenu.setPrefRowCount(5);
        taContenu.setStyle("-fx-background-color: #f7fafc; -fx-border-color: #cbd5e0; " +
                           "-fx-border-width: 1; -fx-border-radius: 5; -fx-background-radius: 5;");
        grid.add(detailLabel("Contenu"), 0, row);
        grid.add(taContenu,              1, row++);

        Button btnClose = new Button("✖  Fermer");
        btnClose.setStyle("-fx-background-color: #1a2332; -fx-text-fill: white; -fx-font-weight: bold; " +
                          "-fx-font-size: 13px; -fx-padding: 10 25; -fx-cursor: hand; -fx-background-radius: 6px;");
        btnClose.setOnAction(e -> modal.close());
        HBox footer = new HBox(btnClose);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(10, 20, 20, 0));

        Label titleLbl = new Label("📄  Article #" + blog.getIdArticle());
        titleLbl.setStyle("-fx-font-size: 20px; -fx-font-weight: 800; -fx-text-fill: #17BB9C;");
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #17BB9C; -fx-pref-height: 2px;");

        VBox root = new VBox(12, titleLbl, sep, grid, footer);
        root.setStyle("-fx-background-color: white;");
        root.setPadding(new Insets(20, 0, 0, 0));

        modal.setScene(new Scene(root, 580, 500));
        modal.showAndWait();
    }

    // =========================================================================
    //  ADD / EDIT DIALOG — full professional validation
    // =========================================================================

    private void showBlogDialog(Blog existing) {
        boolean isEdit = (existing != null);

        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.setTitle(isEdit ? "Modifier l'Article" : "Nouvel Article");
        modal.setResizable(false);

        // ── Form fields ───────────────────────────────────────────────────────
        TextField        txtTitre     = createField("Titre de l'article");
        TextArea         txtContenu   = createArea("Contenu de l'article");
        TextField        txtImage     = createField("https://exemple.com/image.jpg  (optionnel)");
        ComboBox<String> cmbCategorie = createCombo("Sélectionner une catégorie");
        ComboBox<String> cmbStatut    = createCombo("Sélectionner un statut");
        TextField        txtVues      = createField("0");

        cmbCategorie.setItems(FXCollections.observableArrayList(
                "Technologie", "Développement", "Bien-être", "Business", "Management",
                "Innovation", "Coaching", "Marketing", "Finance", "Éducation"));

        cmbStatut.setItems(FXCollections.observableArrayList(
                "Brouillon", "Publié", "Archivé", "En révision"));

        // ── Error labels ──────────────────────────────────────────────────────
        Label eTitre     = errLabel();
        Label eContenu   = errLabel();
        Label eImage     = errLabel();
        Label eCategorie = errLabel();
        Label eStatut    = errLabel();
        Label eVues      = errLabel();

        // ── Pre-fill for EDIT ─────────────────────────────────────────────────
        if (isEdit) {
            txtTitre.setText(existing.getTitre());
            txtContenu.setText(existing.getContenu());
            txtImage.setText(existing.getImagePrincipale() != null ? existing.getImagePrincipale() : "");
            cmbCategorie.setValue(existing.getCategorie());
            cmbStatut.setValue(existing.getStatut());
            txtVues.setText(String.valueOf(existing.getNombreVues()));
        }

        // ── Live border-clearing listeners — red border disappears as user corrects
        txtTitre.textProperty().addListener((o, ov, nv) -> {
            if (nv != null && nv.trim().length() >= 3 && !nv.trim().matches("\\d+"))
                clearErr(txtTitre, eTitre);
        });
        txtContenu.textProperty().addListener((o, ov, nv) -> {
            if (nv != null && nv.trim().length() >= 20)
                clearErr(txtContenu, eContenu);
        });
        txtImage.textProperty().addListener((o, ov, nv) -> {
            if (nv == null || nv.trim().isEmpty() || isValidUrl(nv.trim()))
                clearErr(txtImage, eImage);
        });
        cmbCategorie.valueProperty().addListener((o, ov, nv) -> {
            if (nv != null && !nv.equals("Sélectionner une catégorie"))
                clearErr(cmbCategorie, eCategorie);
        });
        cmbStatut.valueProperty().addListener((o, ov, nv) -> {
            if (nv != null && !nv.equals("Sélectionner un statut"))
                clearErr(cmbStatut, eStatut);
        });
        txtVues.textProperty().addListener((o, ov, nv) -> {
            if (isValidVues(nv)) clearErr(txtVues, eVues);
        });

        // ── Grid layout ───────────────────────────────────────────────────────
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(6);
        grid.setPadding(new Insets(10));
        ColumnConstraints c1 = new ColumnConstraints(); c1.setMinWidth(150); c1.setHalignment(javafx.geometry.HPos.RIGHT);
        ColumnConstraints c2 = new ColumnConstraints(); c2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(c1, c2);

        int row = 0;
        grid.add(formLabel("Titre *"),           0, row); grid.add(txtTitre,     1, row++);
        grid.add(new Label(),                    0, row); grid.add(eTitre,       1, row++);
        grid.add(formLabel("Contenu *"),         0, row); grid.add(txtContenu,   1, row++);
        grid.add(new Label(),                    0, row); grid.add(eContenu,     1, row++);
        grid.add(formLabel("Image principale"),  0, row); grid.add(txtImage,     1, row++);
        grid.add(new Label(),                    0, row); grid.add(eImage,       1, row++);
        grid.add(formLabel("Catégorie *"),       0, row); grid.add(cmbCategorie, 1, row++);
        grid.add(new Label(),                    0, row); grid.add(eCategorie,   1, row++);
        grid.add(formLabel("Statut *"),          0, row); grid.add(cmbStatut,    1, row++);
        grid.add(new Label(),                    0, row); grid.add(eStatut,      1, row++);
        grid.add(formLabel("Nombre de vues"),    0, row); grid.add(txtVues,      1, row++);
        grid.add(new Label(),                    0, row); grid.add(eVues,        1, row++);

        // ── Buttons ───────────────────────────────────────────────────────────
        Button btnSave   = new Button(isEdit ? "💾  Enregistrer" : "✅  Ajouter");
        Button btnCancel = new Button("✖  Annuler");
        btnSave.setStyle(
                "-fx-background-color: #17BB9C; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-font-size: 13px; -fx-padding: 10 25; -fx-cursor: hand; -fx-background-radius: 6px;");
        btnCancel.setStyle(
                "-fx-background-color: #e53e3e; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-font-size: 13px; -fx-padding: 10 25; -fx-cursor: hand; -fx-background-radius: 6px;");
        btnCancel.setOnAction(e -> modal.close());

        HBox buttons = new HBox(15, btnSave, btnCancel);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        buttons.setPadding(new Insets(15, 10, 5, 0));

        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scroll.setPrefHeight(470);

        Label titleLbl = new Label(isEdit ? "✏  Modifier l'Article" : "➕  Ajouter un Article");
        titleLbl.setStyle("-fx-font-size: 20px; -fx-font-weight: 800; -fx-text-fill: #17BB9C; " +
                          "-fx-font-family: 'Segoe UI', sans-serif;");
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #17BB9C; -fx-pref-height: 2px;");

        VBox root = new VBox(12, titleLbl, sep, scroll, buttons);
        root.setPadding(new Insets(25));
        root.setStyle("-fx-background-color: white;");
        root.setPrefWidth(590);

        // ── Save: validate all fields, then persist only if all pass ──────────
        btnSave.setOnAction(e -> {

            // Reset all error states
            clearErr(txtTitre, eTitre);       clearErr(txtContenu, eContenu);
            clearErr(txtImage, eImage);        clearErr(cmbCategorie, eCategorie);
            clearErr(cmbStatut, eStatut);      clearErr(txtVues, eVues);

            boolean valid = true;

            // 1. Titre — required, min 3 chars, max 255, not all-digits
            String titre = txtTitre.getText();
            if (titre == null || titre.trim().isEmpty()) {
                setErr(txtTitre, eTitre, "Le titre est obligatoire.");
                valid = false;
            } else if (titre.trim().length() < 3) {
                setErr(txtTitre, eTitre, "Le titre doit contenir au moins 3 caractères.");
                valid = false;
            } else if (titre.trim().length() > 255) {
                setErr(txtTitre, eTitre, "Le titre ne doit pas dépasser 255 caractères.");
                valid = false;
            } else if (titre.trim().matches("\\d+")) {
                setErr(txtTitre, eTitre, "Le titre ne peut pas contenir uniquement des chiffres.");
                valid = false;
            }

            // 2. Contenu — required, min 20 chars, not blank-only
            String contenu = txtContenu.getText();
            if (contenu == null || contenu.trim().isEmpty()) {
                setErr(txtContenu, eContenu, "Le contenu est obligatoire.");
                valid = false;
            } else if (contenu.trim().length() < 20) {
                setErr(txtContenu, eContenu, "Le contenu doit contenir au moins 20 caractères.");
                valid = false;
            }

            // 3. Image principale — optional; if filled must be a valid URL
            String image = txtImage.getText();
            if (image != null && !image.trim().isEmpty()) {
                if (!isValidUrl(image.trim())) {
                    setErr(txtImage, eImage, "URL invalide. Doit commencer par http:// ou https://");
                    valid = false;
                }
            }

            // 4. Catégorie — required
            String categorie = cmbCategorie.getValue();
            if (categorie == null || categorie.equals("Sélectionner une catégorie")) {
                setErr(cmbCategorie, eCategorie, "Veuillez sélectionner une catégorie.");
                valid = false;
            }

            // 5. Statut — required
            String statut = cmbStatut.getValue();
            if (statut == null || statut.equals("Sélectionner un statut")) {
                setErr(cmbStatut, eStatut, "Veuillez sélectionner un statut.");
                valid = false;
            }

            // 6. Nombre de vues — numeric, >= 0; empty defaults to 0
            String vuesStr = txtVues.getText();
            int nombreVues = 0;
            if (vuesStr != null && !vuesStr.trim().isEmpty()) {
                if (!isValidVues(vuesStr.trim())) {
                    setErr(txtVues, eVues, "Le nombre de vues doit être un entier ≥ 0.");
                    valid = false;
                } else {
                    nombreVues = Integer.parseInt(vuesStr.trim());
                }
            }

            // STOP — database is NOT touched if any field is invalid
            if (!valid) return;

            // ── Build Blog entity and persist ─────────────────────────────────
            boolean success;
            if (isEdit) {
                existing.setTitre(titre.trim());
                existing.setContenu(contenu.trim());
                existing.setImagePrincipale(image != null && !image.trim().isEmpty() ? image.trim() : null);
                existing.setCategorie(categorie);
                existing.setStatut(statut);
                existing.setNombreVues(nombreVues);
                success = blogController.updateBlog(existing);
            } else {
                Blog newBlog = new Blog(
                        0,
                        titre.trim(),
                        contenu.trim(),
                        (image != null && !image.trim().isEmpty()) ? image.trim() : null,
                        categorie,
                        statut,
                        nombreVues);
                success = blogController.addBlog(newBlog);
            }

            if (success) {
                modal.close();
                loadData();
                showInfo("Succès",
                         "✅ Article " + (isEdit ? "modifié" : "ajouté") + " avec succès !");
            } else {
                showError("Erreur base de données",
                          "❌ L'opération a échoué.\n" +
                          "Vérifiez que WAMP est démarré et que la base 'bizcore' est accessible.");
            }
        });

        modal.setScene(new Scene(root));
        modal.showAndWait();
    }

    // =========================================================================
    //  VALIDATION HELPERS
    // =========================================================================

    private void setErr(Control field, Label lbl, String msg) {
        field.setStyle(field instanceof ComboBox ? COMBO_ERROR : FIELD_ERROR);
        lbl.setText("⚠  " + msg);
        lbl.setVisible(true);
        lbl.setManaged(true);
    }

    private void clearErr(Control field, Label lbl) {
        field.setStyle(field instanceof ComboBox ? COMBO_NORMAL : FIELD_NORMAL);
        lbl.setText("");
        lbl.setVisible(false);
        lbl.setManaged(false);
    }

    private boolean isValidUrl(String url) {
        return url != null
               && (url.startsWith("http://") || url.startsWith("https://"))
               && url.length() > 10;
    }

    private boolean isValidVues(String val) {
        if (val == null || val.trim().isEmpty()) return true;
        try { return Integer.parseInt(val.trim()) >= 0; }
        catch (NumberFormatException e) { return false; }
    }

    // =========================================================================
    //  UI FACTORY HELPERS — match project style
    // =========================================================================

    private TextField createField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setStyle(FIELD_NORMAL);
        tf.setMaxWidth(Double.MAX_VALUE);
        return tf;
    }

    private TextArea createArea(String prompt) {
        TextArea ta = new TextArea();
        ta.setPromptText(prompt);
        ta.setPrefRowCount(5);
        ta.setWrapText(true);
        ta.setStyle(FIELD_NORMAL);
        ta.setMaxWidth(Double.MAX_VALUE);
        return ta;
    }

    private ComboBox<String> createCombo(String prompt) {
        ComboBox<String> cb = new ComboBox<>();
        cb.setPromptText(prompt);
        cb.setMaxWidth(Double.MAX_VALUE);
        cb.setStyle(COMBO_NORMAL);
        return cb;
    }

    private Label errLabel() {
        Label lbl = new Label();
        lbl.setStyle(ERR_STYLE);
        lbl.setVisible(false);
        lbl.setManaged(false);
        lbl.setWrapText(true);
        lbl.setMaxWidth(350);
        return lbl;
    }

    private Label formLabel(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-weight: 600; -fx-text-fill: #2d3748; -fx-font-size: 13px;");
        return lbl;
    }

    private Label detailLabel(String text) {
        Label lbl = new Label(text + " :");
        lbl.setStyle("-fx-font-weight: 700; -fx-text-fill: #1a2332; -fx-font-size: 13px;");
        return lbl;
    }

    private Label valueLabel(String text) {
        Label lbl = new Label(text != null ? text : "—");
        lbl.setStyle("-fx-text-fill: #4a5568; -fx-font-size: 13px;");
        lbl.setWrapText(true);
        return lbl;
    }

    // =========================================================================
    //  EXPORT PDF — exports currently displayed list (respects filter/sort)
    // =========================================================================

    private void handleExportPdf() {
        if (displayList == null || displayList.isEmpty()) {
            showInfo("Export PDF", "Aucun article à exporter.\nVeuillez vérifier vos filtres.");
            return;
        }

        // Build filename with timestamp in Downloads folder
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String downloadsPath = System.getProperty("user.home") + File.separator + "Downloads";
        String fileName = downloadsPath + File.separator + "Export_Blog_" + timestamp + ".pdf";

        try {
            Document document = new Document(com.itextpdf.text.PageSize.A4.rotate());
            PdfWriter.getInstance(document, new FileOutputStream(fileName));
            document.open();

            // ── Title ─────────────────────────────────────────────────────────
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD,
                    new BaseColor(23, 187, 156));          // #17BB9C — project's primary colour
            Paragraph title = new Paragraph("Liste des Articles – Blog", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(6);
            document.add(title);

            // Export date subtitle
            Font subFont = new Font(Font.FontFamily.HELVETICA, 9, Font.ITALIC, BaseColor.GRAY);
            Paragraph sub = new Paragraph(
                    "Exporté le " + LocalDateTime.now().format(
                            DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")) +
                    "  |  " + displayList.size() + " article(s)", subFont);
            sub.setAlignment(Element.ALIGN_CENTER);
            sub.setSpacingAfter(16);
            document.add(sub);

            // ── Table header colours ───────────────────────────────────────────
            BaseColor headerBg   = new BaseColor(26, 35, 50);   // #1a2332 — dark navy
            BaseColor headerText = BaseColor.WHITE;
            BaseColor rowAlt     = new BaseColor(247, 250, 252); // #f7fafc — very light grey
            BaseColor borderCol  = new BaseColor(203, 213, 224); // #cbd5e0

            // 7 columns: ID | Titre | Contenu | Image | Catégorie | Statut | Vues
            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{4f, 16f, 28f, 18f, 12f, 10f, 6f});

            // Header row
            Font hFont = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, headerText);
            for (String header : new String[]{"ID", "Titre", "Contenu", "Image", "Catégorie", "Statut", "Vues"}) {
                PdfPCell cell = new PdfPCell(new Phrase(header, hFont));
                cell.setBackgroundColor(headerBg);
                cell.setPadding(7);
                cell.setBorderColor(headerBg);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            // Data rows
            Font rowFont = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL, BaseColor.DARK_GRAY);
            boolean alt = false;
            for (Blog b : displayList) {
                BaseColor rowBg = alt ? rowAlt : BaseColor.WHITE;
                alt = !alt;

                String[] values = {
                    String.valueOf(b.getIdArticle()),
                    safe(b.getTitre()),
                    truncate(safe(b.getContenu()), 120),
                    safe(b.getImagePrincipale()),
                    safe(b.getCategorie()),
                    safe(b.getStatut()),
                    String.valueOf(b.getNombreVues())
                };

                for (int i = 0; i < values.length; i++) {
                    PdfPCell cell = new PdfPCell(new Phrase(values[i], rowFont));
                    cell.setBackgroundColor(rowBg);
                    cell.setPadding(5);
                    cell.setBorderColor(borderCol);
                    cell.setHorizontalAlignment(i == 0 || i == 6
                            ? Element.ALIGN_CENTER : Element.ALIGN_LEFT);
                    table.addCell(cell);
                }
            }

            document.add(table);
            document.close();

            showInfo("Export PDF réussi ✅",
                     "Le fichier a été enregistré dans :\n" + fileName);

            // Auto-open the PDF
            try {
                java.awt.Desktop.getDesktop().open(new File(fileName));
            } catch (Exception ignored) { /* desktop open not supported on all OS */ }

        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Erreur Export PDF",
                      "❌ Impossible de générer le PDF.\nDétail : " + ex.getMessage());
        }
    }

    /** Return the string or "—" if null/blank. */
    private String safe(String s) {
        return (s != null && !s.isBlank()) ? s : "—";
    }

    /** Truncate long strings to keep the PDF cell readable. */
    private String truncate(String s, int max) {
        return (s.length() > max) ? s.substring(0, max - 1) + "…" : s;
    }

    // =========================================================================
    //  STATISTICS — modal dashboard using real data from displayList / masterList
    // =========================================================================

    private void handleStatistics() {
        // Always base statistics on the full master list so that they reflect the
        // real database state, not just whatever is currently filtered.
        ObservableList<Blog> data = masterList;

        if (data == null || data.isEmpty()) {
            showInfo("Statistiques", "Aucune donnée disponible pour générer les statistiques.");
            return;
        }

        // ── Compute aggregates ─────────────────────────────────────────────────
        Map<String, Integer> byCategory = new LinkedHashMap<>();
        Map<String, Integer> byStatus   = new LinkedHashMap<>();
        long totalViews = 0;
        Blog mostViewed = data.get(0);

        for (Blog b : data) {
            byCategory.merge(safe(b.getCategorie()), 1, Integer::sum);
            byStatus.merge(safe(b.getStatut()), 1, Integer::sum);
            totalViews += b.getNombreVues();
            if (b.getNombreVues() > mostViewed.getNombreVues()) mostViewed = b;
        }

        // ── Build the stats window ─────────────────────────────────────────────
        Stage statsStage = new Stage();
        statsStage.initModality(Modality.APPLICATION_MODAL);
        statsStage.setTitle("📊 Statistiques – Blog");
        statsStage.setResizable(true);

        // Colour palette matching project
        String PRIMARY    = "#17BB9C";
        String DARK_NAVY  = "#1a2332";
        String BG_LIGHT   = "#f7fafc";
        String TEXT_DARK  = "#2d3748";
        String TEXT_MUTED = "#718096";

        // ── Header bar ─────────────────────────────────────────────────────────
        Label headerLbl = new Label("📊  Statistiques des Articles");
        headerLbl.setStyle("-fx-font-size: 20px; -fx-font-weight: 800; -fx-text-fill: white;");
        HBox header = new HBox(headerLbl);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(18, 25, 18, 25));
        header.setStyle("-fx-background-color: " + DARK_NAVY + ";");

        Separator headerSep = new Separator();
        headerSep.setStyle("-fx-background-color: " + PRIMARY + "; -fx-pref-height: 3px;");

        // ── KPI cards row ──────────────────────────────────────────────────────
        HBox kpiRow = new HBox(15,
            kpiCard("📝  Total Articles", String.valueOf(data.size()), PRIMARY, BG_LIGHT),
            kpiCard("👁  Total Vues",     String.valueOf(totalViews),  "#4299e1", BG_LIGHT),
            kpiCard("🏆  Article le plus vu",
                    truncate(safe(mostViewed.getTitre()), 28)
                    + "  (" + mostViewed.getNombreVues() + " vues)",
                    "#ed8936", BG_LIGHT),
            kpiCard("📂  Catégories",     String.valueOf(byCategory.size()), "#9f7aea", BG_LIGHT)
        );
        kpiRow.setPadding(new Insets(20, 25, 10, 25));
        for (javafx.scene.Node n : kpiRow.getChildren())
            HBox.setHgrow(n, Priority.ALWAYS);

        // ── Pie chart — articles per category ─────────────────────────────────
        PieChart pieChart = new PieChart();
        pieChart.setTitle("Articles par Catégorie");
        pieChart.setLabelsVisible(true);
        pieChart.setLegendVisible(true);
        byCategory.forEach((cat, count) ->
            pieChart.getData().add(new PieChart.Data(cat + " (" + count + ")", count)));
        pieChart.setPrefSize(420, 320);

        // ── Bar chart — articles per status ───────────────────────────────────
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis   yAxis = new NumberAxis();
        xAxis.setLabel("Statut");
        yAxis.setLabel("Nombre d'Articles");
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Articles par Statut");
        barChart.setLegendVisible(false);
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Articles");
        byStatus.forEach((status, count) ->
            series.getData().add(new XYChart.Data<>(status, count)));
        barChart.getData().add(series);
        barChart.setPrefSize(420, 320);

        HBox chartsRow = new HBox(20, pieChart, barChart);
        chartsRow.setPadding(new Insets(10, 25, 10, 25));
        chartsRow.setAlignment(Pos.CENTER);
        HBox.setHgrow(pieChart,  Priority.ALWAYS);
        HBox.setHgrow(barChart,  Priority.ALWAYS);

        // ── Top 5 most-viewed table ────────────────────────────────────────────
        Label top5Lbl = new Label("🔝  Top 5 Articles les Plus Vus");
        top5Lbl.setStyle("-fx-font-size: 14px; -fx-font-weight: 700; -fx-text-fill: " + TEXT_DARK + ";");

        TableView<Blog> top5Table = new TableView<>();
        top5Table.setMaxHeight(150);
        top5Table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Blog, Integer> colId   = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("idArticle"));
        colId.setMaxWidth(50);

        TableColumn<Blog, String>  colTitre = new TableColumn<>("Titre");
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));

        TableColumn<Blog, String>  colCat  = new TableColumn<>("Catégorie");
        colCat.setCellValueFactory(new PropertyValueFactory<>("categorie"));

        TableColumn<Blog, String>  colStat = new TableColumn<>("Statut");
        colStat.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colStat.setMaxWidth(100);

        TableColumn<Blog, Integer> colVues = new TableColumn<>("Vues");
        colVues.setCellValueFactory(new PropertyValueFactory<>("nombreVues"));
        colVues.setMaxWidth(70);

        top5Table.getColumns().addAll(colId, colTitre, colCat, colStat, colVues);

        ObservableList<Blog> top5 = FXCollections.observableArrayList(
            data.stream()
                .sorted(Comparator.comparingInt(Blog::getNombreVues).reversed())
                .limit(5)
                .toList());
        top5Table.setItems(top5);

        VBox top5Box = new VBox(8, top5Lbl, top5Table);
        top5Box.setPadding(new Insets(5, 25, 10, 25));

        // ── Close button ───────────────────────────────────────────────────────
        Button btnClose = new Button("✖  Fermer");
        btnClose.setStyle("-fx-background-color: " + DARK_NAVY + "; -fx-text-fill: white; " +
                          "-fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 10 30; " +
                          "-fx-cursor: hand; -fx-background-radius: 6px;");
        btnClose.setOnAction(e -> statsStage.close());
        HBox footer = new HBox(btnClose);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(10, 25, 20, 25));

        // ── Assemble ───────────────────────────────────────────────────────────
        VBox root = new VBox(0,
            header, headerSep,
            kpiRow, chartsRow,
            top5Box, footer);
        root.setStyle("-fx-background-color: " + BG_LIGHT + ";");

        ScrollPane sp = new ScrollPane(root);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background-color: " + BG_LIGHT + "; -fx-background: " + BG_LIGHT + ";");

        statsStage.setScene(new Scene(sp, 920, 750));
        statsStage.show();
    }

    /** Builds a small KPI card matching the project's design language. */
    private VBox kpiCard(String label, String value, String accentColour, String bg) {
        Label valueLbl = new Label(value);
        valueLbl.setStyle("-fx-font-size: 15px; -fx-font-weight: 800; " +
                          "-fx-text-fill: " + accentColour + "; -fx-wrap-text: true;");
        valueLbl.setWrapText(true);
        valueLbl.setMaxWidth(200);

        Label nameLbl = new Label(label);
        nameLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #718096; -fx-font-weight: 600;");

        Separator accent = new Separator();
        accent.setStyle("-fx-background-color: " + accentColour + "; -fx-pref-height: 3px;");

        VBox card = new VBox(6, accent, valueLbl, nameLbl);
        card.setPadding(new Insets(14, 16, 14, 16));
        card.setStyle("-fx-background-color: white; " +
                      "-fx-border-color: #e2e8f0; -fx-border-width: 1; " +
                      "-fx-border-radius: 8; -fx-background-radius: 8;");
        return card;
    }

    // =========================================================================
    //  CONTENT QUALITY ANALYSIS — 🔬 per-row Analyze button
    // =========================================================================

    /**
     * Called when the 🔬 button is clicked on a specific table row.
     *
     * The Blog object from that row is passed directly — its idArticle is used
     * to call blogController.analyzeBlog(id), which fetches the article fresh
     * from the database, runs the internal algorithm, and returns a result.
     *
     * The article is NEVER modified. If the result is null (DB unreachable),
     * an error alert is shown instead.
     */
    private void handleAnalyze(Blog blog) {
        if (blog == null) return;

        BlogAnalysisResult result = blogController.analyzeBlog(blog.getIdArticle());

        if (result == null) {
            showError("Analyse impossible",
                    "❌ Impossible d'analyser l'article #" + blog.getIdArticle() + ".\n"
                    + "Vérifiez que WAMP est démarré et que la base 'bizcore' est accessible.");
            return;
        }

        showAnalysisDialog(result);
    }

    /**
     * Renders the analysis result in a modal window.
     * Colours and layout are consistent with the existing project design.
     */
    private void showAnalysisDialog(BlogAnalysisResult r) {

        final String PRIMARY  = "#17BB9C";
        final String NAVY     = "#1a2332";
        final String BG       = "#f7fafc";
        final String TXT_DARK = "#2d3748";
        final String TXT_GREY = "#718096";

        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.setTitle("🔬 Analyse Qualité — Article #" + r.getArticleId());
        modal.setResizable(true);

        // ── Header bar ────────────────────────────────────────────────────────
        Label hTitle = new Label("🔬  Analyse Qualité du Contenu");
        hTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: 800; -fx-text-fill: white;");
        Label hSub = new Label("Article #" + r.getArticleId() + " — " + r.getArticleTitre());
        hSub.setStyle("-fx-font-size: 11px; -fx-text-fill: #a0aec0;");
        hSub.setWrapText(true);
        HBox header = new HBox(new VBox(3, hTitle, hSub));
        header.setPadding(new Insets(16, 24, 16, 24));
        header.setStyle("-fx-background-color: " + NAVY + ";");

        Separator topSep = new Separator();
        topSep.setStyle("-fx-background-color: " + PRIMARY + "; -fx-pref-height: 3px;");

        // ── Score cards ───────────────────────────────────────────────────────
        HBox scoreRow = new HBox(12,
            anaScoreCard("Lisibilité",   r.getReadabilityScore(), PRIMARY,   "/ 100"),
            anaScoreCard("Score SEO",    r.getSeoScore(),         "#4299e1", "/ 100"),
            anaScoreCard("Mots",         r.getWordCount(),        "#ed8936", "mots"),
            anaScoreCard("Phrases",      r.getSentenceCount(),    "#9f7aea", "phrases")
        );
        scoreRow.setPadding(new Insets(18, 24, 10, 24));
        for (javafx.scene.Node n : scoreRow.getChildren())
            HBox.setHgrow(n, Priority.ALWAYS);

        // ── Progress bars ─────────────────────────────────────────────────────
        Label barTitle = anaSectionLabel("📈  Progression des Scores", TXT_DARK);

        ProgressBar readBar = new ProgressBar(r.getReadabilityScore() / 100.0);
        readBar.setPrefWidth(Double.MAX_VALUE);
        readBar.setPrefHeight(12);
        readBar.setStyle("-fx-accent: " + anaScoreColour(r.getReadabilityScore(), PRIMARY) + ";");

        ProgressBar seoBar = new ProgressBar(r.getSeoScore() / 100.0);
        seoBar.setPrefWidth(Double.MAX_VALUE);
        seoBar.setPrefHeight(12);
        seoBar.setStyle("-fx-accent: " + anaScoreColour(r.getSeoScore(), "#4299e1") + ";");

        GridPane barGrid = new GridPane();
        barGrid.setHgap(14); barGrid.setVgap(8);
        barGrid.setPadding(new Insets(8, 0, 4, 0));
        ColumnConstraints bc1 = new ColumnConstraints(); bc1.setMinWidth(130);
        ColumnConstraints bc2 = new ColumnConstraints(); bc2.setHgrow(Priority.ALWAYS);
        barGrid.getColumnConstraints().addAll(bc1, bc2);
        barGrid.add(anaMetricLabel("Lisibilité", TXT_GREY), 0, 0);
        barGrid.add(readBar,                                1, 0);
        barGrid.add(anaMetricLabel("SEO",        TXT_GREY), 0, 1);
        barGrid.add(seoBar,                                 1, 1);

        VBox barsBox = new VBox(6, barTitle, barGrid);
        barsBox.setPadding(new Insets(8, 24, 8, 24));

        // ── Content metrics ───────────────────────────────────────────────────
        Label metTitle = anaSectionLabel("📊  Métriques du Contenu", TXT_DARK);
        GridPane metGrid = new GridPane();
        metGrid.setHgap(20); metGrid.setVgap(10);
        metGrid.setPadding(new Insets(8, 0, 4, 0));
        ColumnConstraints mc1 = new ColumnConstraints(); mc1.setMinWidth(220);
        ColumnConstraints mc2 = new ColumnConstraints(); mc2.setHgrow(Priority.ALWAYS);
        metGrid.getColumnConstraints().addAll(mc1, mc2);
        int mr = 0;
        metGrid.add(anaMetricLabel("Longueur du contenu",       TXT_GREY), 0, mr);
        metGrid.add(anaMetricValue(r.getContentLengthCategory()
                + " (" + r.getCharacterCount() + " car.)", TXT_DARK),     1, mr++);
        metGrid.add(anaMetricLabel("Moy. mots / phrase",        TXT_GREY), 0, mr);
        metGrid.add(anaMetricValue(
                String.format("%.1f mots", r.getAvgWordsPerSentence()), TXT_DARK), 1, mr++);
        metGrid.add(anaMetricLabel("Densité mot-clé principal", TXT_GREY), 0, mr);
        metGrid.add(anaMetricValue(
                String.format("%.1f %%", r.getKeywordDensity()), TXT_DARK), 1, mr++);

        VBox metBox = new VBox(6, metTitle, metGrid);
        metBox.setPadding(new Insets(8, 24, 8, 24));

        // ── Keywords ──────────────────────────────────────────────────────────
        Label kwTitle = anaSectionLabel("🔑  Top 5 Mots-Clés Extraits", TXT_DARK);
        HBox kwRow = new HBox(8);
        kwRow.setPadding(new Insets(8, 0, 4, 0));
        String[] tagCols = {PRIMARY, "#4299e1", "#ed8936", "#9f7aea", "#e53e3e"};
        java.util.List<String> kws = r.getTopKeywords();
        if (kws.isEmpty()) {
            Label none = new Label("Aucun mot-clé significatif extrait.");
            none.setStyle("-fx-text-fill: " + TXT_GREY + "; -fx-font-style: italic;");
            kwRow.getChildren().add(none);
        } else {
            for (int i = 0; i < kws.size() && i < 5; i++) {
                String c = tagCols[i];
                Label tag = new Label("  " + kws.get(i) + "  ");
                tag.setStyle("-fx-background-color:" + c + "22; -fx-text-fill:" + c
                        + "; -fx-font-weight:700; -fx-font-size:12px; -fx-padding:5 10;"
                        + " -fx-background-radius:20; -fx-border-color:" + c + "55;"
                        + " -fx-border-radius:20; -fx-border-width:1;");
                kwRow.getChildren().add(tag);
            }
        }
        VBox kwBox = new VBox(6, kwTitle, kwRow);
        kwBox.setPadding(new Insets(8, 24, 8, 24));

        // ── Suggestions ───────────────────────────────────────────────────────
        Label sugTitle = anaSectionLabel("💡  Suggestions d'Amélioration", TXT_DARK);
        VBox sugList = new VBox(8);
        sugList.setPadding(new Insets(8, 0, 4, 0));
        java.util.List<String> sugs = r.getSuggestions();
        for (int i = 0; i < sugs.size(); i++) {
            Label num = new Label((i + 1) + ".");
            num.setStyle("-fx-font-weight:800; -fx-text-fill:" + PRIMARY
                    + "; -fx-font-size:13px; -fx-min-width:22;");
            Label txt = new Label(sugs.get(i));
            txt.setStyle("-fx-text-fill:" + TXT_DARK + "; -fx-font-size:12px;");
            txt.setWrapText(true);
            HBox sugRow = new HBox(8, num, txt);
            sugRow.setAlignment(Pos.TOP_LEFT);
            HBox.setHgrow(txt, Priority.ALWAYS);
            sugList.getChildren().add(sugRow);
        }
        VBox sugBox = new VBox(6, sugTitle, sugList);
        sugBox.setPadding(new Insets(8, 24, 14, 24));

        // ── Close button ──────────────────────────────────────────────────────
        Button btnClose = new Button("✖  Fermer");
        btnClose.setStyle("-fx-background-color:" + NAVY + "; -fx-text-fill:white;"
                + " -fx-font-weight:bold; -fx-font-size:13px;"
                + " -fx-padding:10 30; -fx-cursor:hand; -fx-background-radius:6;");
        btnClose.setOnAction(e -> modal.close());
        HBox footer = new HBox(btnClose);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(10, 24, 20, 24));

        // ── Assemble ──────────────────────────────────────────────────────────
        VBox content = new VBox(0,
                header, topSep,
                scoreRow,
                anaDivider(), barsBox,
                anaDivider(), metBox,
                anaDivider(), kwBox,
                anaDivider(), sugBox,
                footer);
        content.setStyle("-fx-background-color:" + BG + ";");

        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background-color:" + BG + "; -fx-background:" + BG + ";");

        modal.setScene(new Scene(sp, 720, 680));
        modal.showAndWait();
    }

    // ── Analysis dialog private helpers ──────────────────────────────────────

    private VBox anaScoreCard(String label, int value, String colour, String unit) {
        Label v = new Label(String.valueOf(value));
        v.setStyle("-fx-font-size:28px; -fx-font-weight:900; -fx-text-fill:" + colour + ";");
        Label u = new Label(unit);
        u.setStyle("-fx-font-size:10px; -fx-text-fill:#a0aec0;");
        Label n = new Label(label);
        n.setStyle("-fx-font-size:11px; -fx-font-weight:700; -fx-text-fill:#4a5568;");
        Separator top = new Separator();
        top.setStyle("-fx-background-color:" + colour + "; -fx-pref-height:3px;");
        VBox card = new VBox(3, top, v, u, n);
        card.setPadding(new Insets(12, 14, 12, 14));
        card.setStyle("-fx-background-color:white; -fx-border-color:#e2e8f0;"
                + " -fx-border-width:1; -fx-border-radius:8; -fx-background-radius:8;");
        return card;
    }

    private String anaScoreColour(int score, String good) {
        if (score >= 75) return good;
        if (score >= 50) return "#ed8936";
        return "#e53e3e";
    }

    private Label anaSectionLabel(String text, String colour) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size:13px; -fx-font-weight:800; -fx-text-fill:" + colour + ";");
        return l;
    }

    private Label anaMetricLabel(String text, String colour) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size:12px; -fx-font-weight:600; -fx-text-fill:" + colour + ";");
        return l;
    }

    private Label anaMetricValue(String text, String colour) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size:12px; -fx-font-weight:700; -fx-text-fill:" + colour + ";");
        return l;
    }

    private Separator anaDivider() {
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color:#e2e8f0; -fx-pref-height:1px;");
        sep.setPadding(new Insets(2, 24, 2, 24));
        return sep;
    }

    // =========================================================================
    //  TRENDING ALGORITHM  —  GET /api/blogs/trending
    // =========================================================================

    /**
     * Wire the Trending button from the ModuleView toolbar.
     * Called by MainClass.loadBlogModule() after initialize().
     */
    public void bindTrendingButton(Button btnTrending) {
        if (btnTrending != null)
            btnTrending.setOnAction(e -> handleTrending());
    }

    /**
     * Called when the 🔥 Trending button is clicked.
     * Delegates to blogController.getTrendingArticles() which implements
     * GET /api/blogs/trending — reads only, never writes.
     */
    private void handleTrending() {
        java.util.List<TrendingResult> results = blogController.getTrendingArticles();

        if (results == null || results.isEmpty()) {
            showInfo("Trending",
                    "Aucun article publié trouvé.\n" +
                    "Assurez-vous que des articles ont le statut « Publié » " +
                    "et que WAMP/MySQL est démarré.");
            return;
        }

        showTrendingDialog(results);
    }

    /**
     * Displays the Top 5 Trending results in a modal window.
     * Style matches the existing project design (navy #1a2332 + teal #17BB9C).
     * No layout changes to the main view.
     */
    private void showTrendingDialog(java.util.List<TrendingResult> results) {

        final String PRIMARY  = "#17BB9C";
        final String NAVY     = "#1a2332";
        final String BG       = "#f7fafc";
        final String TXT_DARK = "#2d3748";
        final String TXT_GREY = "#718096";
        final String GOLD     = "#D4AF37";

        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.setTitle("🔥 Top 5 Articles Trending");
        modal.setResizable(false);

        // ── Header ────────────────────────────────────────────────────────────
        Label hTitle = new Label("🔥  Top 5 Articles Trending");
        hTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: 800; -fx-text-fill: white;");
        Label hSub = new Label("Score = (Vues × 0.6) + (Récence × 0.4)  —  Articles publiés uniquement");
        hSub.setStyle("-fx-font-size: 11px; -fx-text-fill: #a0aec0;");
        VBox headerContent = new VBox(3, hTitle, hSub);
        HBox header = new HBox(headerContent);
        header.setPadding(new Insets(16, 24, 16, 24));
        header.setStyle("-fx-background-color: " + NAVY + ";");

        Separator topSep = new Separator();
        topSep.setStyle("-fx-background-color: " + PRIMARY + "; -fx-pref-height: 3px;");

        // ── Ranking cards ─────────────────────────────────────────────────────
        VBox cardList = new VBox(10);
        cardList.setPadding(new Insets(20, 24, 10, 24));

        String[] medalColours = {GOLD, "#C0C0C0", "#CD7F32", TXT_GREY, TXT_GREY};
        String[] medals       = {"🥇", "🥈", "🥉", "4", "5"};

        for (int i = 0; i < results.size(); i++) {
            TrendingResult r = results.get(i);
            String colour    = medalColours[i];

            // Medal / rank badge
            Label rank = new Label(medals[i]);
            rank.setStyle("-fx-font-size: 22px; -fx-min-width: 36; -fx-alignment: CENTER;");

            // Title + meta
            Label titre = new Label(r.getTitre());
            titre.setStyle("-fx-font-size: 14px; -fx-font-weight: 800; -fx-text-fill: " + TXT_DARK + ";");
            titre.setWrapText(true);
            titre.setMaxWidth(340);

            Label meta = new Label(
                    "ID #" + r.getId()
                    + "   👁 " + r.getVues() + " vues"
                    + "   📅 " + r.getCreatedAt());
            meta.setStyle("-fx-font-size: 11px; -fx-text-fill: " + TXT_GREY + ";");

            VBox textBox = new VBox(3, titre, meta);
            HBox.setHgrow(textBox, Priority.ALWAYS);

            // Score badge
            Label scoreLbl = new Label(String.format("%.1f", r.getTrendingScore()));
            scoreLbl.setStyle(
                    "-fx-font-size: 20px; -fx-font-weight: 900; -fx-text-fill: " + colour + ";"
                    + " -fx-min-width: 65; -fx-alignment: CENTER_RIGHT;");

            Label scoreUnit = new Label("score");
            scoreUnit.setStyle("-fx-font-size: 10px; -fx-text-fill: #a0aec0;");

            VBox scoreBox = new VBox(1, scoreLbl, scoreUnit);
            scoreBox.setAlignment(Pos.CENTER_RIGHT);

            HBox card = new HBox(12, rank, textBox, scoreBox);
            card.setAlignment(Pos.CENTER_LEFT);
            card.setPadding(new Insets(12, 16, 12, 16));

            // Colour the top bar of each card with its medal colour
            Separator cardAccent = new Separator();
            cardAccent.setStyle("-fx-background-color: " + colour
                    + "; -fx-pref-height: 3px;");

            VBox cardWrapper = new VBox(0, cardAccent, card);
            cardWrapper.setStyle(
                    "-fx-background-color: white;"
                    + " -fx-border-color: #e2e8f0; -fx-border-width: 1;"
                    + " -fx-border-radius: 8; -fx-background-radius: 8;");

            cardList.getChildren().add(cardWrapper);
        }

        // ── Formula note ──────────────────────────────────────────────────────
        Label formulaNote = new Label(
                "Formule :  TrendingScore = (Vues × 0.6) + (Récence × 0.4)\n" +
                "Récence : 0–3j → ~100  |  4–10j → ~50–89  |  11–30j → ~20–49  |  31j+ → ≤19");
        formulaNote.setStyle(
                "-fx-font-size: 11px; -fx-text-fill: " + TXT_GREY + ";"
                + " -fx-background-color: #edf2f7; -fx-padding: 10 14;"
                + " -fx-background-radius: 6;");
        formulaNote.setWrapText(true);
        VBox noteBox = new VBox(formulaNote);
        noteBox.setPadding(new Insets(4, 24, 10, 24));

        // ── Close button ──────────────────────────────────────────────────────
        Button btnClose = new Button("✖  Fermer");
        btnClose.setStyle(
                "-fx-background-color: " + NAVY + "; -fx-text-fill: white;"
                + " -fx-font-weight: bold; -fx-font-size: 13px;"
                + " -fx-padding: 10 30; -fx-cursor: hand; -fx-background-radius: 6;");
        btnClose.setOnAction(e -> modal.close());
        HBox footer = new HBox(btnClose);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(6, 24, 20, 24));

        // ── Assemble ──────────────────────────────────────────────────────────
        Separator noteSep = new Separator();
        noteSep.setStyle("-fx-background-color: #e2e8f0;");
        noteSep.setPadding(new Insets(0, 24, 0, 24));

        VBox root = new VBox(0,
                header, topSep,
                cardList,
                noteSep, noteBox,
                footer);
        root.setStyle("-fx-background-color: " + BG + ";");

        modal.setScene(new Scene(root, 620, 620));
        modal.showAndWait();
    }

    // =========================================================================
    //  ALERT HELPERS
    // =========================================================================

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
