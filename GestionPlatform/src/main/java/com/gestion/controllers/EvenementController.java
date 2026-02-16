package com.gestion.controllers;

import com.gestion.entities.Evenement;
import com.gestion.services.EvenementService;
import com.google.protobuf.BoolValue;
import com.sun.javafx.charts.Legend;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class EvenementController {

    public Legend.LegendItem txtImageUrl;
    public BoolValue.Builder dpDateDebut;
    public BoolValue.Builder dpDateFin;
    private EvenementService service = new EvenementService();
    private ObservableList<Evenement> data = FXCollections.observableArrayList();

    @FXML private TextField txtTitre, txtLieu, txtCapacite, txtPrix;
    @FXML private TextArea txtDescription;
    @FXML private ComboBox<String> cbStatut;

    @FXML private TableView<Evenement> tableEvenements;
    @FXML private TableColumn<Evenement, String> colTitre, colDescription, colLieu, colStatut;
    @FXML private TableColumn<Evenement, Integer> colCapacite;
    @FXML private TableColumn<Evenement, Double> colPrix;

    @FXML
    public void initialize() {

        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colLieu.setCellValueFactory(new PropertyValueFactory<>("lieu"));
        colCapacite.setCellValueFactory(new PropertyValueFactory<>("capacite"));
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prix"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        //data.addAll(service.afficher());
        tableEvenements.setItems(data);

        tableEvenements.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                txtTitre.setText(newSel.getTitre());
                txtDescription.setText(newSel.getDescription());
                txtLieu.setText(newSel.getLieu());
                txtCapacite.setText(String.valueOf(newSel.getCapacite()));
                txtPrix.setText(String.valueOf(newSel.getPrix()));
                cbStatut.setValue(newSel.getStatut());
            }
        });
    }

    @FXML
    private void ajouter() {
        try {
            Evenement e = new Evenement(
                    txtTitre.getText(),
                    txtDescription.getText(),
                    txtLieu.getText(),
                    Integer.parseInt(txtCapacite.getText()),
                    Double.parseDouble(txtPrix.getText()),
                    cbStatut.getValue()
            );

            service.ajouter(e);
            data.clear();
            //data.addAll(service.afficher());
            clearForm();

        } catch (Exception ex) {
            showAlert("Erreur", "Vérifiez les champs !");
        }
    }

    @FXML
    private void modifier() {
        Evenement selected = tableEvenements.getSelectionModel().getSelectedItem();
        if (selected != null) {

            selected.setDescription(txtDescription.getText());
            selected.setLieu(txtLieu.getText());
            selected.setCapacite(Integer.parseInt(txtCapacite.getText()));
            selected.setPrix(Double.parseDouble(txtPrix.getText()));
            selected.setStatut(cbStatut.getValue());

            //service.modifier(selected);
            data.clear();
            //data.addAll(service.afficher());
            clearForm();
        }
    }

    @FXML
    private void annuler() {
        clearForm();
    }


    public TableColumn<Evenement, Double> getColPrix() {
        return colPrix;
    }

    public void setColPrix(TableColumn<Evenement, Double> colPrix) {
        this.colPrix = colPrix;
    }

    public TableColumn<Evenement, Integer> getColCapacite() {
        return colCapacite;
    }

    public void setColCapacite(TableColumn<Evenement, Integer> colCapacite) {
        this.colCapacite = colCapacite;
    }

    public TableColumn<Evenement, String> getColStatut() {
        return colStatut;
    }

    public void setColStatut(TableColumn<Evenement, String> colStatut) {
        this.colStatut = colStatut;
    }

    public TableColumn<Evenement, String> getColLieu() {
        return colLieu;
    }

    public void setColLieu(TableColumn<Evenement, String> colLieu) {
        this.colLieu = colLieu;
    }

    public TableColumn<Evenement, String> getColDescription() {
        return colDescription;
    }

    public void setColDescription(TableColumn<Evenement, String> colDescription) {
        this.colDescription = colDescription;
    }

    public TableColumn<Evenement, String> getColTitre() {
        return colTitre;
    }

    public void setColTitre(TableColumn<Evenement, String> colTitre) {
        this.colTitre = colTitre;
    }

    public TableView<Evenement> getTableEvenements() {
        return tableEvenements;
    }

    public void setTableEvenements(TableView<Evenement> tableEvenements) {
        this.tableEvenements = tableEvenements;
    }

    public ComboBox<String> getCbStatut() {
        return cbStatut;
    }

    public void setCbStatut(ComboBox<String> cbStatut) {
        this.cbStatut = cbStatut;
    }

    public TextArea getTxtDescription() {
        return txtDescription;
    }

    public void setTxtDescription(TextArea txtDescription) {
        this.txtDescription = txtDescription;
    }

    public TextField getTxtPrix() {
        return txtPrix;
    }

    public void setTxtPrix(TextField txtPrix) {
        this.txtPrix = txtPrix;
    }

    public TextField getTxtCapacite() {
        return txtCapacite;
    }

    public void setTxtCapacite(TextField txtCapacite) {
        this.txtCapacite = txtCapacite;
    }

    public TextField getTxtLieu() {
        return txtLieu;
    }

    public void setTxtLieu(TextField txtLieu) {
        this.txtLieu = txtLieu;
    }

    public TextField getTxtTitre() {
        return txtTitre;
    }

    public void setTxtTitre(TextField txtTitre) {
        this.txtTitre = txtTitre;
    }

    public ObservableList<Evenement> getData() {
        return data;
    }

    public void setData(ObservableList<Evenement> data) {
        this.data = data;
    }

    public EvenementService getService() {
        return service;
    }

    public void setService(EvenementService service) {
        this.service = service;
    }

    @FXML
    private void supprimer() {
        Evenement selected = tableEvenements.getSelectionModel().getSelectedItem();
        if (selected != null) {
            //service.supprimer(selected);
            data.remove(selected);
            clearForm();
        }
    }

    private void clearForm() {
        txtTitre.clear();
        txtDescription.clear();
        txtLieu.clear();
        txtCapacite.clear();
        txtPrix.clear();
        cbStatut.setValue(null);
        tableEvenements.getSelectionModel().clearSelection();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
