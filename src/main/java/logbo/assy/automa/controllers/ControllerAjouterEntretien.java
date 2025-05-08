package logbo.assy.automa.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import logbo.assy.automa.dao.EntretienDAO;
import logbo.assy.automa.dao.VehiculeDAO;
import logbo.assy.automa.models.Entretien;
import logbo.assy.automa.models.Vehicule;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ControllerAjouterEntretien {
    private static final Logger LOGGER = Logger.getLogger(ControllerAjouterEntretien.class.getName());

    @FXML private ComboBox<Vehicule> comboVehicule;
    @FXML private ComboBox<String> comboType;
    @FXML private TextField txtDescription;
    @FXML private DatePicker dateDebut;
    @FXML private DatePicker dateFin;
    @FXML private Button btnAjouter;

    private EntretienDAO entretienDAO;
    private VehiculeDAO vehiculeDAO;

    @FXML
    public void initialize() {
        try {
            // Initialisation des DAOs
            entretienDAO = new EntretienDAO();
            vehiculeDAO = new VehiculeDAO();

            // Configuration des DatePickers
            dateDebut.setValue(LocalDate.now());

            // Chargement des véhicules dans le ComboBox
            comboVehicule.getItems().addAll(vehiculeDAO.getAllVehicules());
            comboVehicule.setConverter(new javafx.util.StringConverter<>() {
                @Override
                public String toString(Vehicule vehicule) {
                    return vehicule != null ? vehicule.getImmatriculation() + " - " + vehicule.getMarque() + " " + vehicule.getModele() : "";
                }

                @Override
                public Vehicule fromString(String string) {
                    return null;
                }
            });

            // Chargement des types d'entretien
            comboType.getItems().addAll(
                    "Préventif",
                    "Curatif",
                    "Révision périodique",
                    "Réparation",
                    "Vidange",
                    "Changement pneus",
                    "Autre"
            );

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur d'initialisation", e);
            afficherErreur("Erreur d'initialisation", "Impossible de se connecter à la base de données: " + e.getMessage());
        }
    }

    @FXML
    private void ajouterEntretien() {
        // Validation des champs obligatoires
        if (comboVehicule.getValue() == null || dateDebut.getValue() == null ||
                txtDescription.getText().isEmpty() || comboType.getValue() == null) {

            afficherErreur("Champs obligatoires",
                    "Veuillez remplir tous les champs obligatoires: véhicule, date de début, type et description.");
            return;
        }

        // Validation des dates
        if (dateFin.getValue() != null && dateFin.getValue().isBefore(dateDebut.getValue())) {
            afficherErreur("Dates invalides", "La date de fin ne peut pas être antérieure à la date de début.");
            return;
        }

        try {
            // Création de l'entretien
            Entretien entretien = new Entretien();
            entretien.setIdVehicule(comboVehicule.getValue().getIdVehicule());
            entretien.setDateEntree(dateDebut.getValue().toString());
            entretien.setDateSortie(dateFin.getValue() != null ? dateFin.getValue().toString() : null);
            entretien.setMotif(comboType.getValue());
            entretien.setObservation(txtDescription.getText());
            // Ces champs peuvent être null pour le moment
            entretien.setPrix(null);
            entretien.setLieu(null);

            // Génération d'un ID unique
            entretien.setIdEntretien("ENT_" + System.currentTimeMillis());

            // Enregistrement dans la base de données
            entretienDAO.addEntretien(entretien);

            afficherInfo("Entretien ajouté", "L'entretien a été ajouté avec succès !");
            fermerModal();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'ajout de l'entretien", e);
            afficherErreur("Erreur d'enregistrement", "Impossible d'ajouter l'entretien: " + e.getMessage());
        }
    }

    private void fermerModal() {
        Stage stage = (Stage) btnAjouter.getScene().getWindow();
        stage.close();
    }

    private void afficherInfo(String titre, String contenu) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(contenu);
        alert.showAndWait();
    }

    private void afficherErreur(String titre, String contenu) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(contenu);
        alert.showAndWait();
    }
}