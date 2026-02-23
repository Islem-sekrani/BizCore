package com.gestion.controllers;

import com.gestion.entities.Evenement;
import com.gestion.services.EvenementService;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class EvenementController {

    private EvenementService service = new EvenementService();
    private ObservableList<Evenement> data = FXCollections.observableArrayList();
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private Evenement evenementEnCours;
    @FXML private TableView<Evenement> tableEvenements;
    @FXML private TableColumn<Evenement, String> colTitre, colDescription, colLieu, colStatut, colDateDebut, colDateFin;
    @FXML private TableColumn<Evenement, Integer> colCapacite;
    @FXML private TableColumn<Evenement, Double> colPrix;

    @FXML private TextField txtTitre, txtLieu, txtCapacite, txtPrix;
    @FXML private TextArea txtDescription;
    @FXML private ComboBox<String> cbStatut;
    @FXML private DatePicker dpDateDebut, dpDateFin;

    @FXML
    private void refreshTable() {
        data.clear();
        data.addAll(service.afficher());
        tableEvenements.setItems(data);
    }

    private void clearForm() {
        txtTitre.clear(); txtDescription.clear(); txtLieu.clear();
        txtCapacite.clear(); txtPrix.clear(); cbStatut.setValue(null);
        dpDateDebut.setValue(null); dpDateFin.setValue(null);
        tableEvenements.getSelectionModel().clearSelection();
    }

    private boolean champsValides() {
        try {
            if (txtTitre.getText().isEmpty() || txtDescription.getText().isEmpty() || txtLieu.getText().isEmpty()) return false;
            Integer.parseInt(txtCapacite.getText());
            Double.parseDouble(txtPrix.getText());
            if (cbStatut.getValue() == null || dpDateDebut.getValue() == null || dpDateFin.getValue() == null) return false;
        } catch (Exception e) { return false; }
        return true;
    }

    private void showAlert(String message) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setContentText(message);
        a.showAndWait();
    }

    public void remplirFormulaire(Evenement e) {
        evenementEnCours = e;
        txtTitre.setText(e.getTitre());
        txtDescription.setText(e.getDescription());
        txtLieu.setText(e.getLieu());
        txtCapacite.setText(String.valueOf(e.getCapacite()));
        txtPrix.setText(String.valueOf(e.getPrix()));
        cbStatut.setValue(e.getStatut());
        dpDateDebut.setValue(e.getDateDebut().toLocalDate());
        dpDateFin.setValue(e.getDateFin().toLocalDate());
    }

    @FXML

    private void ajouter() {
        try {
            // ✅ Contrôle des champs obligatoires
            if (txtTitre.getText().isEmpty() || txtDescription.getText().isEmpty()
                    || txtLieu.getText().isEmpty() || txtCapacite.getText().isEmpty()
                    || txtPrix.getText().isEmpty()
                    || dpDateDebut.getValue() == null || dpDateFin.getValue() == null) {

                showAlert("Erreur", "Veuillez remplir tous les champs !");
                return;
            }

            // ✅ Vérifier que la capacité et le prix sont corrects
            int capacite;
            double prix;
            try {
                capacite = Integer.parseInt(txtCapacite.getText());
                prix = Double.parseDouble(txtPrix.getText());
            } catch (NumberFormatException nfe) {
                showAlert("Erreur", "Capacité ou prix invalide !");
                return;
            }

            // ✅ Créer l'événement
            Evenement e = new Evenement();
            e.setTitre(txtTitre.getText());
            e.setDescription(txtDescription.getText());
            e.setLieu(txtLieu.getText());
            e.setCapacite(capacite);
            e.setPrix(prix);
            e.setStatut(cbStatut.getValue());
            e.setDateDebut(dpDateDebut.getValue().atStartOfDay());
            e.setDateFin(dpDateFin.getValue().atStartOfDay());
            e.setImageUrl("");
            e.setIdOrganisateur(1);
            e.setIdCategorie(1);

            // ✅ Ajouter via le service
            service.ajouter(e);

            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Succès");
            success.setHeaderText(null);
            success.setContentText("✅ Événement ajouter avec succès !");
            success.showAndWait();

            // ✅ Fermer la fenêtre après ajout
            Stage stage = (Stage) txtTitre.getScene().getWindow();
            stage.close();
            refreshTable();

        } catch (Exception ex) {
        }
    }

    // Méthode utilitaire pour afficher une alerte simple
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    @FXML
    private void modifier() {

        if (evenementEnCours == null) {
            showAlert("Aucun événement chargé !");
            return;
        }


        evenementEnCours.setTitre(txtTitre.getText());
        evenementEnCours.setDescription(txtDescription.getText());
        evenementEnCours.setLieu(txtLieu.getText());
        evenementEnCours.setCapacite(Integer.parseInt(txtCapacite.getText()));
        evenementEnCours.setPrix(Double.parseDouble(txtPrix.getText()));
        evenementEnCours.setStatut(cbStatut.getValue());
        evenementEnCours.setDateDebut(dpDateDebut.getValue().atStartOfDay());
        evenementEnCours.setDateFin(dpDateFin.getValue().atStartOfDay());

        service.modifier(evenementEnCours);

        Alert success = new Alert(Alert.AlertType.INFORMATION);
        success.setTitle("Succès");
        success.setHeaderText(null);
        success.setContentText("✅ Événement mofifier avec succès !");
        success.showAndWait();

        // ✅ Fermer la fenêtre après ajout
        Stage stage = (Stage) txtTitre.getScene().getWindow();
        stage.close();
        refreshTable();

    }

    @FXML
    private void supprimer() {
        Evenement selected = tableEvenements.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Sélectionnez un événement !"); return; }
        service.supprimer(selected.getIdEvenement());
        refreshTable(); clearForm();
    }

    @FXML
    private void annuler() {
        clearForm();
    }
}
