package logbo.assy.automa.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import logbo.assy.automa.dao.MissionDAO;
import logbo.assy.automa.dao.PersonnelDAO;
import logbo.assy.automa.dao.VehiculeDAO;
import logbo.assy.automa.models.Mission;
import logbo.assy.automa.models.Personnel;
import logbo.assy.automa.models.Vehicule;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ControllerAjouterMission {
    private static final Logger LOGGER = Logger.getLogger(ControllerAjouterMission.class.getName());

    @FXML
    private TextField txtTitre;
    @FXML
    private ComboBox<Vehicule> comboVehicule;
    @FXML
    private ComboBox<Personnel> comboResponsable;

    @FXML
    private DatePicker dateDepart;
    @FXML
    private DatePicker dateRetourPrevu;

    @FXML
    private TextField txtKmDepart;
    @FXML
    private TextField txtKmRetour;
    @FXML
    private TextField txtDestination;

    @FXML
    private TextField txtBudgetCarburant;
    @FXML
    private TextField txtAutresFrais;
    @FXML
    private TextField txtDescription;

    @FXML
    private Button btnAjouter;

    private MissionDAO missionDAO;


    @FXML
    public void initialize() {
        try {
            // Initialisation des DAO
            missionDAO = new MissionDAO();
            VehiculeDAO vehiculeDAO = new VehiculeDAO();
            PersonnelDAO personnelDAO = new PersonnelDAO();

            // Configuration des DatePickers
            dateDepart.setValue(LocalDate.now());
            dateRetourPrevu.setValue(LocalDate.now().plusDays(1));

            // Remplissage des ComboBox
            comboVehicule.getItems().addAll(vehiculeDAO.getAllVehicules());
            comboVehicule.setConverter(new javafx.util.StringConverter<>() {
                @Override
                public String toString(Vehicule vehicule) {
                    return vehicule != null ? vehicule.getImmatriculation() + " - " + vehicule.getMarque() + " " + vehicule.getModele() : "";
                }

                @Override
                public Vehicule fromString(String string) {
                    return null; // Conversion non nécessaire pour l'utilisation actuelle
                }
            });

            comboResponsable.getItems().addAll(personnelDAO.getAllPersonnel());
            comboResponsable.setConverter(new javafx.util.StringConverter<>() {
                @Override
                public String toString(Personnel personnel) {
                    return personnel != null ? personnel.getNom() + " " + personnel.getPrenom() : "";
                }

                @Override
                public Personnel fromString(String string) {
                    return null; // Conversion non nécessaire pour l'utilisation actuelle
                }
            });

            // Ajout des validations pour les champs numériques
            txtKmDepart.textProperty().addListener((_, _, newValue) -> {
                if (!newValue.matches("\\d*")) {
                    txtKmDepart.setText(newValue.replaceAll("[^\\d]", ""));
                }
            });

            txtKmRetour.textProperty().addListener((_, _, newValue) -> {
                if (!newValue.matches("\\d*")) {
                    txtKmRetour.setText(newValue.replaceAll("[^\\d]", ""));
                }
            });

            txtBudgetCarburant.textProperty().addListener((_, _, newValue) -> {
                if (!newValue.matches("\\d*")) {
                    txtBudgetCarburant.setText(newValue.replaceAll("[^\\d]", ""));
                }
            });


            txtAutresFrais.textProperty().addListener((_, _, newValue) -> {
                if (!newValue.matches("\\d*")) {
                    txtAutresFrais.setText(newValue.replaceAll("[^\\d]", ""));
                }
            });

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur d'initialisation", e);
            afficherErreur("Erreur d'initialisation", "Impossible de se connecter à la base de données: " + e.getMessage());
        }
    }

    @FXML
    private void ajouterMission() {
        // Validation des champs VRAIMENT obligatoires selon la structure de la BDD
        if (comboVehicule.getValue() == null || dateDepart.getValue() == null ||
                txtDestination.getText().isEmpty()) {

            afficherErreur("Champs obligatoires",
                    "Veuillez remplir les champs obligatoires: véhicule, date de départ et destination.");
            return;
        }

        try {
            // Récupération des valeurs
            double coutCarburant = txtBudgetCarburant.getText().isEmpty() ? 0 :
                    Double.parseDouble(txtBudgetCarburant.getText());

            // La description/observation est importante à conserver
            String observation = txtDescription.getText();

            // Création de l'objet Mission
            Mission mission = new Mission(
                    dateDepart.getValue().toString(),
                    dateRetourPrevu.getValue() != null ? dateRetourPrevu.getValue().toString() : null,
                    // cout total (peut être null)
                    coutCarburant > 0 ? String.format("%.0f", coutCarburant) : null,
                    // cout_carburant (peut être null)
                    coutCarburant > 0 ? String.format("%.0f", coutCarburant) : null,
                    // observation (important à conserver)
                    observation,
                    // circuit/destination
                    txtDestination.getText(),
                    // id_vehicule (obligatoire)
                    comboVehicule.getValue().getIdVehicule()
            );

            // Enregistrement dans la base de données
            missionDAO.addMission(mission);

            afficherInfo("Mission ajoutée", "La mission a été ajoutée avec succès !");
            fermerModal();

        } catch (NumberFormatException e) {
            afficherErreur("Format incorrect", "Les champs de coût doivent contenir des nombres valides.");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'ajout de la mission", e);
            afficherErreur("Erreur d'enregistrement", "Impossible d'ajouter la mission: " + e.getMessage());
        }
    }

    @FXML
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