package logbo.assy.automa.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import logbo.assy.automa.dao.AttributionVehiculeDAO;
import logbo.assy.automa.dao.PersonnelDAO;
import logbo.assy.automa.dao.VehiculeDAO;
import logbo.assy.automa.models.AttributionVehicule;
import logbo.assy.automa.models.Personnel;
import logbo.assy.automa.models.Vehicule;

import java.sql.SQLException;
import java.time.LocalDate;

public class ControllerAjouterAffectation {

    @FXML
    private ComboBox<Personnel> comboPersonnel;
    @FXML
    private ComboBox<Vehicule> comboVehicule;

    private AttributionVehiculeDAO dao;

    public void initialize() {
        try {
            dao = new AttributionVehiculeDAO();
            comboPersonnel.getItems().addAll(new PersonnelDAO().getAllPersonnel());
            comboVehicule.getItems().addAll(new VehiculeDAO().getAllVehicules());
        } catch (SQLException e) {
            showError("Erreur de chargement", e.getMessage());
        }
    }

    @FXML
    public void affecterVehicule() {
        Personnel pers = comboPersonnel.getValue();
        Vehicule veh = comboVehicule.getValue();

        if (pers == null || veh == null) {
            showError("Champs requis", "Veuillez sélectionner un personnel et un véhicule.");
            return;
        }

        AttributionVehicule attribution = new AttributionVehicule(veh, pers, LocalDate.now().toString());

        // Définir le montant total à partir du prix d'achat du véhicule
        attribution.setMontantTotal(veh.getPrixAchat());

        // Définir la date de début de remboursement (par exemple, premier jour du mois prochain)
        LocalDate dateDebut = LocalDate.now().plusMonths(1).withDayOfMonth(1);
        attribution.setDateDebutRemboursement(dateDebut.toString());

        try {
            dao.addAttribution(attribution);
            showInfo("Succès", "Véhicule affecté avec succès !");
            closeWindow();
        } catch (SQLException e) {
            showError("Erreur SQL", e.getMessage());
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) comboVehicule.getScene().getWindow();
        stage.close();
    }

    private void showInfo(String titre, String contenu) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setContentText(contenu);
        alert.showAndWait();
    }

    private void showError(String titre, String contenu) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(titre);
        alert.setContentText(contenu);
        alert.showAndWait();
    }
}
