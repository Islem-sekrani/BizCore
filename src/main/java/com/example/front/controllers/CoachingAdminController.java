package com.example.front.controllers;

import com.example.front.models.Coach;
import com.example.front.models.CoachingDomain;
import com.example.front.models.DataStore;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class CoachingAdminController implements Initializable {

    // --- DOMAINS TAB ---
    @FXML
    private TextField domainNameField;
    @FXML
    private TextField domainDescField;
    @FXML
    private TextField domainIconField;
    @FXML
    private TableView<CoachingDomain> domainsTable;
    @FXML
    private TableColumn<CoachingDomain, Integer> domainIdCol;
    @FXML
    private TableColumn<CoachingDomain, String> domainNameCol;
    @FXML
    private TableColumn<CoachingDomain, String> domainDescCol;
    @FXML
    private TableColumn<CoachingDomain, String> domainIconCol;

    // --- COACHES TAB ---
    @FXML
    private TableView<Coach> coachesTable;
    @FXML
    private TableColumn<Coach, String> coachNameCol;
    @FXML
    private TableColumn<Coach, String> coachDomainCol; // Display domain name
    @FXML
    private TableColumn<Coach, Integer> coachExpCol;
    @FXML
    private TableColumn<Coach, Double> coachRateCol;
    @FXML
    private TableColumn<Coach, String> coachAvailCol;

    @FXML
    private ComboBox<CoachingDomain> filterDomainCombo;

    // Form fields
    @FXML
    private TextField coachNameField;
    @FXML
    private ComboBox<CoachingDomain> coachDomainCombo;
    @FXML
    private TextField coachExpField;
    @FXML
    private TextField coachRateField;
    @FXML
    private TextField coachAvailField;
    @FXML
    private TextArea coachBioField;

    private DataStore dataStore;
    private FilteredList<Coach> filteredCoaches;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dataStore = DataStore.getInstance();

        // 1. Initialize Domains Table
        setupDomainsTable();

        // 2. Initialize Coaches Table
        setupCoachesTable();

        // 3. Setup ComboBoxes
        setupComboBoxes();
    }

    private void setupDomainsTable() {
        domainIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        domainNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        domainDescCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        domainIconCol.setCellValueFactory(new PropertyValueFactory<>("icon"));

        domainsTable.setItems(dataStore.getDomains());
    }

    private void setupCoachesTable() {
        coachNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        coachExpCol.setCellValueFactory(new PropertyValueFactory<>("yearsExperience"));
        coachRateCol.setCellValueFactory(new PropertyValueFactory<>("hourlyRate"));
        coachAvailCol.setCellValueFactory(new PropertyValueFactory<>("availability"));

        // Custom cell factory for Domain Name (since it's an object relationship)
        coachDomainCol.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDomainName()));

        // Wrap list in FilteredList
        filteredCoaches = new FilteredList<>(dataStore.getCoaches(), p -> true);
        coachesTable.setItems(filteredCoaches);
    }

    private void setupComboBoxes() {
        // Link combos to the ObservableList of domains
        coachDomainCombo.setItems(dataStore.getDomains());
        filterDomainCombo.setItems(dataStore.getDomains());

        // Handle Filtering
        filterDomainCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            filteredCoaches.setPredicate(coach -> {
                if (newVal == null)
                    return true; // Show all
                return coach.getDomain() == newVal; // Strict object equality or ID check
            });
        });
    }

    // --- DOMAIN ACTIONS ---

    @FXML
    private void handleAddDomain() {
        String name = domainNameField.getText().trim();
        String desc = domainDescField.getText().trim();
        String icon = domainIconField.getText().trim();

        if (name.isEmpty()) {
            showAlert("Erreur", "Le nom du domaine est requis.");
            return;
        }

        // Generate ID (simple auto-increment logic for demo)
        int newId = dataStore.getDomains().size() + 100; // Offset ID

        CoachingDomain newDomain = new CoachingDomain(newId, name, desc, icon);
        dataStore.addDomain(newDomain);
        handleClearDomainForm();
    }

    @FXML
    private void handleDeleteDomain() {
        CoachingDomain selected = domainsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            dataStore.removeDomain(selected);
        } else {
            showAlert("Attention", "Veuillez sélectionner un domaine à supprimer.");
        }
    }

    @FXML
    private void handleClearDomainForm() {
        domainNameField.clear();
        domainDescField.clear();
        domainIconField.clear();
    }

    // --- COACH ACTIONS ---

    @FXML
    private void handleSaveCoach() {
        String name = coachNameField.getText().trim();
        CoachingDomain domain = coachDomainCombo.getValue();
        String expStr = coachExpField.getText().trim();
        String rateStr = coachRateField.getText().trim();
        String avail = coachAvailField.getText().trim();
        String bio = coachBioField.getText().trim();

        if (name.isEmpty() || domain == null || expStr.isEmpty() || rateStr.isEmpty()) {
            showAlert("Erreur", "Veuillez remplir les champs obligatoires (Nom, Domaine, Exp, Tarif).");
            return;
        }

        try {
            int exp = Integer.parseInt(expStr);
            double rate = Double.parseDouble(rateStr);

            // Create new coach (Simple Add only for this demo)
            int newId = (int) (System.currentTimeMillis() % 100000);
            Coach newCoach = new Coach(newId, name, bio, exp, rate, avail, domain);

            dataStore.addCoach(newCoach);
            handleClearCoachForm();

        } catch (NumberFormatException e) {
            showAlert("Erreur de format", "L'expérience et le tarif doivent être des nombres.");
        }
    }

    @FXML
    private void handleDeleteCoach() {
        Coach selected = coachesTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            dataStore.removeCoach(selected);
        } else {
            showAlert("Attention", "Veuillez sélectionner un coach à supprimer.");
        }
    }

    @FXML
    private void handleClearCoachForm() {
        coachNameField.clear();
        coachExpField.clear();
        coachRateField.clear();
        coachAvailField.clear();
        coachBioField.clear();
        coachDomainCombo.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleResetFilter() {
        filterDomainCombo.getSelectionModel().clearSelection();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
