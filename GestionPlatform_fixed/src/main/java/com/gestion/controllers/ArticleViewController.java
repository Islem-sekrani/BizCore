package com.gestion.controllers;

import com.gestion.entities.Article;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * ArticleViewController
 * ─────────────────────
 * JavaFX controller for the Article module.
 * Integrates with the existing ModuleView.fxml namespace pattern used by MainClass.
 *
 * Usage from MainClass (same pattern as BLOG module):
 *
 *   ArticleViewController articleVC = new ArticleViewController();
 *   articleVC.initialize(dataTable, searchField, sortComboBox, lblResults);
 *   articleVC.bindAddButton(btnAdd);
 *   articleVC.bindDeleteButton(btnDelete);
 */
public class ArticleViewController {

    // ── Delegate ──────────────────────────────────────────────────────────────
    private final ArticleController articleController = new ArticleController();

    // ── UI references (injected from MainClass namespace) ──────────────────────
    private TableView<Article> dataTable;
    private TextField          searchField;
    private ComboBox<String>   sortComboBox;
    private Label              lblResults;

    // ── Live data ─────────────────────────────────────────────────────────────
    private ObservableList<Article> masterList   = FXCollections.observableArrayList();
    private ObservableList<Article> displayList  = FXCollections.observableArrayList();

    // ── Style constants (match project palette) ────────────────────────────────
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
    //  PUBLIC API  — called by MainClass after loading ModuleView.fxml
    // =========================================================================

    /**
     * Wire the UI components from ModuleView.fxml namespace and load data.
     * Call this immediately after FXMLLoader.load() inside MainClass.loadModule().
     */
    @SuppressWarnings("unchecked")
    public void initialize(TableView<?> table,
                           TextField search,
                           ComboBox<?> sort,
                           Label results) {

        this.dataTable   = (TableView<Article>) table;
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
            btnAdd.setOnAction(e -> showArticleDialog(null));
    }

    /** Wire the DELETE SELECTED button from the ModuleView toolbar. */
    public void bindDeleteButton(Button btnDelete) {
        if (btnDelete != null)
            btnDelete.setOnAction(e -> handleDeleteSelected());
    }

    // =========================================================================
    //  TABLE COLUMNS
    // =========================================================================

    @SuppressWarnings("unchecked")
    private void setupColumns() {
        if (dataTable == null) return;
        dataTable.getColumns().clear();

        // Checkbox column (matches existing project style)
        TableColumn<Article, Boolean> colCheck = new TableColumn<>("");
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

        // ID
        TableColumn<Article, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("idArticle"));
        colId.setMinWidth(60);
        colId.setMaxWidth(70);

        // Titre
        TableColumn<Article, String> colTitre = new TableColumn<>("Titre");
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colTitre.setMinWidth(140);

        // Contenu
        TableColumn<Article, String> colContenu = new TableColumn<>("Contenu");
        colContenu.setCellValueFactory(new PropertyValueFactory<>("contenu"));
        colContenu.setMinWidth(180);

        // Image
        TableColumn<Article, String> colImage = new TableColumn<>("Image");
        colImage.setCellValueFactory(new PropertyValueFactory<>("imagePrincipale"));
        colImage.setMinWidth(100);

        // Catégorie
        TableColumn<Article, String> colCategorie = new TableColumn<>("Catégorie");
        colCategorie.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        colCategorie.setMinWidth(100);

        // Statut
        TableColumn<Article, String> colStatut = new TableColumn<>("Statut");
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colStatut.setMinWidth(90);
        colStatut.setMaxWidth(110);

        // Vues
        TableColumn<Article, Integer> colVues = new TableColumn<>("Vues");
        colVues.setCellValueFactory(new PropertyValueFactory<>("nombreVues"));
        colVues.setMinWidth(70);
        colVues.setMaxWidth(90);

        // Actions
        TableColumn<Article, Void> colActions = new TableColumn<>("Action");
        colActions.setMinWidth(150);
        colActions.setMaxWidth(150);
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnView   = new Button("👁");
            private final Button btnEdit   = new Button("✎");
            private final Button btnDelete = new Button("🗑");
            private final HBox   box       = new HBox(5, btnView, btnEdit, btnDelete);

            {
                btnView.getStyleClass().add("action-btn-view");
                btnEdit.getStyleClass().add("action-btn-edit");
                btnDelete.getStyleClass().add("action-btn-delete");
                box.setAlignment(Pos.CENTER);

                btnView.setOnAction(e -> {
                    Article a = getTableView().getItems().get(getIndex());
                    showDetailDialog(a);
                });
                btnEdit.setOnAction(e -> {
                    Article a = getTableView().getItems().get(getIndex());
                    showArticleDialog(a);
                });
                btnDelete.setOnAction(e -> {
                    Article a = getTableView().getItems().get(getIndex());
                    handleDeleteOne(a);
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
        masterList  = articleController.getAllArticles();
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
    //  SORT
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
        String selected = sortComboBox != null ? sortComboBox.getValue() : null;
        if (selected == null || selected.startsWith("--")) return;
        displayList = articleController.sortArticles(displayList, selected);
    }

    // =========================================================================
    //  SEARCH / FILTER  (titre, categorie, statut)
    // =========================================================================

    private void setupSearchListener() {
        if (searchField == null) return;

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty()) {
                displayList = FXCollections.observableArrayList(masterList);
            } else {
                displayList = articleController.filterArticles(newVal);
            }
            applyCurrentSort();
            updateTable();
            updateResultsLabel();
        });
    }

    // =========================================================================
    //  DELETE HANDLERS
    // =========================================================================

    private void handleDeleteOne(Article article) {
        if (article == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer la suppression");
        confirm.setHeaderText(null);
        confirm.setContentText("Supprimer l'article « " + article.getTitre() + " » ?\nCette action est irréversible.");
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = articleController.deleteArticle(article.getIdArticle());
            if (success) {
                loadData();
                showInfo("Succès", "✅ Article supprimé avec succès.");
            } else {
                showError("Erreur", "❌ Impossible de supprimer l'article. Vérifiez votre connexion WAMP/MySQL.");
            }
        }
    }

    private void handleDeleteSelected() {
        Article selected = dataTable != null ? dataTable.getSelectionModel().getSelectedItem() : null;
        if (selected == null) {
            showInfo("Aucune sélection", "Veuillez sélectionner un article dans la table.");
            return;
        }
        handleDeleteOne(selected);
    }

    // =========================================================================
    //  DETAIL DIALOG  (View)
    // =========================================================================

    private void showDetailDialog(Article article) {
        if (article == null) return;

        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.setTitle("Détails de l'Article #" + article.getIdArticle());
        modal.setResizable(false);

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(14);
        grid.setPadding(new Insets(25));

        ColumnConstraints c1 = new ColumnConstraints(); c1.setMinWidth(130); c1.setHalignment(javafx.geometry.HPos.RIGHT);
        ColumnConstraints c2 = new ColumnConstraints(); c2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(c1, c2);

        int row = 0;
        grid.add(detailLabel("ID"),               0, row); grid.add(valueLabel(String.valueOf(article.getIdArticle())), 1, row++);
        grid.add(detailLabel("Titre"),             0, row); grid.add(valueLabel(article.getTitre()),           1, row++);
        grid.add(detailLabel("Catégorie"),         0, row); grid.add(valueLabel(article.getCategorie()),       1, row++);
        grid.add(detailLabel("Statut"),            0, row); grid.add(valueLabel(article.getStatut()),          1, row++);
        grid.add(detailLabel("Nombre de vues"),    0, row); grid.add(valueLabel(String.valueOf(article.getNombreVues())), 1, row++);
        grid.add(detailLabel("Image principale"),  0, row); grid.add(valueLabel(article.getImagePrincipale() != null ? article.getImagePrincipale() : "—"), 1, row++);

        Label lblContenu = detailLabel("Contenu");
        TextArea taContenu = new TextArea(article.getContenu());
        taContenu.setEditable(false);
        taContenu.setWrapText(true);
        taContenu.setPrefRowCount(5);
        taContenu.setStyle("-fx-background-color: #f7fafc; -fx-border-color: #cbd5e0; " +
                           "-fx-border-width: 1; -fx-border-radius: 5; -fx-background-radius: 5;");
        grid.add(lblContenu, 0, row);
        grid.add(taContenu,  1, row++);

        Button btnClose = new Button("✖  Fermer");
        btnClose.setStyle("-fx-background-color: #1a2332; -fx-text-fill: white; -fx-font-weight: bold; " +
                          "-fx-font-size: 13px; -fx-padding: 10 25; -fx-cursor: hand; -fx-background-radius: 6px;");
        btnClose.setOnAction(e -> modal.close());

        HBox footer = new HBox(btnClose);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(10, 25, 20, 0));

        Label title = new Label("📄  Article #" + article.getIdArticle());
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: 800; -fx-text-fill: #17BB9C;");
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #17BB9C; -fx-pref-height: 2px;");
        sep.setPadding(new Insets(0, 25, 0, 25));

        VBox root = new VBox(12, title, sep, grid, footer);
        root.setStyle("-fx-background-color: white;");
        root.setPadding(new Insets(20, 0, 0, 0));

        modal.setScene(new Scene(root, 580, 480));
        modal.showAndWait();
    }

    // =========================================================================
    //  ADD / EDIT DIALOG  (with full validation)
    // =========================================================================

    private void showArticleDialog(Article existing) {
        boolean isEdit = (existing != null);

        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.setTitle(isEdit ? "Modifier l'Article" : "Nouvel Article");
        modal.setResizable(false);

        // ── Form fields ───────────────────────────────────────────────────────
        TextField        txtTitre    = createField("Titre de l'article");
        TextArea         txtContenu  = createArea("Contenu de l'article");
        TextField        txtImage    = createField("https://exemple.com/image.jpg  (optionnel)");
        ComboBox<String> cmbCategorie = createCombo("Sélectionner une catégorie");
        ComboBox<String> cmbStatut   = createCombo("Sélectionner un statut");
        TextField        txtVues     = createField("0");

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

        // ── Live border-clearing listeners ────────────────────────────────────
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

        // ── Layout ────────────────────────────────────────────────────────────
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(6);
        grid.setPadding(new Insets(10));
        ColumnConstraints c1 = new ColumnConstraints(); c1.setMinWidth(150); c1.setHalignment(javafx.geometry.HPos.RIGHT);
        ColumnConstraints c2 = new ColumnConstraints(); c2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(c1, c2);

        int row = 0;
        grid.add(formLabel("Titre *"),            0, row); grid.add(txtTitre,     1, row++);
        grid.add(new Label(),                     0, row); grid.add(eTitre,       1, row++);
        grid.add(formLabel("Contenu *"),          0, row); grid.add(txtContenu,   1, row++);
        grid.add(new Label(),                     0, row); grid.add(eContenu,     1, row++);
        grid.add(formLabel("Image principale"),   0, row); grid.add(txtImage,     1, row++);
        grid.add(new Label(),                     0, row); grid.add(eImage,       1, row++);
        grid.add(formLabel("Catégorie *"),        0, row); grid.add(cmbCategorie, 1, row++);
        grid.add(new Label(),                     0, row); grid.add(eCategorie,   1, row++);
        grid.add(formLabel("Statut *"),           0, row); grid.add(cmbStatut,    1, row++);
        grid.add(new Label(),                     0, row); grid.add(eStatut,      1, row++);
        grid.add(formLabel("Nombre de vues"),     0, row); grid.add(txtVues,      1, row++);
        grid.add(new Label(),                     0, row); grid.add(eVues,        1, row++);

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

        // ── Save handler — validation then persist ────────────────────────────
        btnSave.setOnAction(e -> {

            // Reset all error states first
            clearErr(txtTitre, eTitre);     clearErr(txtContenu, eContenu);
            clearErr(txtImage, eImage);     clearErr(cmbCategorie, eCategorie);
            clearErr(cmbStatut, eStatut);   clearErr(txtVues, eVues);

            boolean valid = true;

            // 1. Titre — required, min 3, max 255, not all-digits
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

            // 3. Image principale — optional; if filled must be valid URL
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

            // STOP — do NOT touch the database if any field is invalid
            if (!valid) return;

            // ── Build entity and persist ──────────────────────────────────────
            boolean success;
            if (isEdit) {
                existing.setTitre(titre.trim());
                existing.setContenu(contenu.trim());
                existing.setImagePrincipale(image != null && !image.trim().isEmpty() ? image.trim() : null);
                existing.setCategorie(categorie);
                existing.setStatut(statut);
                existing.setNombreVues(nombreVues);
                success = articleController.updateArticle(existing);
            } else {
                Article newArticle = new Article(
                        0,
                        titre.trim(),
                        contenu.trim(),
                        (image != null && !image.trim().isEmpty()) ? image.trim() : null,
                        categorie,
                        statut,
                        nombreVues);
                success = articleController.addArticle(newArticle);
            }

            if (success) {
                modal.close();
                loadData();
                showInfo("Succès",
                         "✅ Article " + (isEdit ? "modifié" : "ajouté") + " avec succès !");
            } else {
                showError("Erreur base de données",
                          "❌ L'opération a échoué.\nVérifiez que WAMP est démarré et que la base 'bizcore' est accessible.");
            }
        });

        modal.setScene(new Scene(root));
        modal.showAndWait();
    }

    // =========================================================================
    //  VALIDATION HELPERS
    // =========================================================================

    private void setErr(Control field, Label lbl, String msg) {
        if (field instanceof ComboBox) field.setStyle(COMBO_ERROR);
        else                           field.setStyle(FIELD_ERROR);
        lbl.setText("⚠  " + msg);
        lbl.setVisible(true);
        lbl.setManaged(true);
    }

    private void clearErr(Control field, Label lbl) {
        if (field instanceof ComboBox) field.setStyle(COMBO_NORMAL);
        else                           field.setStyle(FIELD_NORMAL);
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
    //  UI FACTORY HELPERS  (match project style)
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
