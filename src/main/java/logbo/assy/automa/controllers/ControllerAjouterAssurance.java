package logbo.assy.automa.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import logbo.assy.automa.Main;
import logbo.assy.automa.dao.AssuranceDAO;
import logbo.assy.automa.dao.VehiculeDAO;
import logbo.assy.automa.models.Assurance;
import logbo.assy.automa.models.Vehicule;

import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class ControllerAjouterAssurance implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(ControllerAjouterAssurance.class.getName());

    @FXML
    private TextField txtCompagnie;
    @FXML
    private TextField txtPrix;
    @FXML
    private ComboBox<String> comboType;
    @FXML
    private ComboBox<Vehicule> comboVehicule;
    @FXML
    private DatePicker dateDebut;
    @FXML
    private DatePicker dateFin;
    @FXML
    private Button btnAjouter;

    private final ObservableList<String> typesAssurance = FXCollections.observableArrayList(
            "RC", "Tous Risques", "Intermédiaire"
    );

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        comboType.setItems(typesAssurance);
        initComboVehicule();

        Platform.runLater(() -> {
            Stage stage = (Stage) txtCompagnie.getScene().getWindow();
            stage.setTitle("AutoMA - Ajout d'une Assurance");
            Main.appliquerIcon(stage);
        });
    }

    private void initComboVehicule() {
        try {
            VehiculeDAO vehiculeDAO = new VehiculeDAO();
            List<Vehicule> liste = vehiculeDAO.getAllVehicules();
            ObservableList<Vehicule> observableList = FXCollections.observableArrayList(liste);
            comboVehicule.setItems(observableList);

            comboVehicule.setCellFactory(_ -> new ListCell<>() {
                @Override
                protected void updateItem(Vehicule item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getImmatriculation());
                }
            });
            comboVehicule.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(Vehicule item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getImmatriculation());
                }
            });
        } catch (SQLException e) {
            LOGGER.severe("Erreur chargement véhicules : " + e.getMessage());
        }

        // Appliquer Titre et icone
        Platform.runLater(() -> {
            Stage stage = (Stage) comboVehicule.getScene().getWindow();
            stage.setTitle("AutoMA - Ajout Assurance");
            Main.appliquerIcon(stage);
        });
    }

    @FXML
    private void ajouterAssurance() {
        try {
            Vehicule vehicule = comboVehicule.getValue();
            String type = comboType.getValue();
            String agence = txtCompagnie.getText().trim();
            if (vehicule == null || type == null || agence.isEmpty()
                    || dateDebut.getValue() == null || dateFin.getValue() == null) {
                new Alert(Alert.AlertType.WARNING, "Veuillez remplir tous les champs requis.").showAndWait();
                return;
            }

            String formatDateDebut = dateDebut.getValue().format(DateTimeFormatter.ISO_DATE);
            String formatDateFin = dateFin.getValue().format(DateTimeFormatter.ISO_DATE);

            Assurance assurance = new Assurance(
                    agence.toUpperCase(),
                    type.toUpperCase(),
                    txtPrix.getText(),
                    formatDateDebut,
                    formatDateFin,
                    vehicule.getIdVehicule()
            );

            AssuranceDAO dao = new AssuranceDAO();
            dao.add(assurance);

            Alert b = new Alert(Alert.AlertType.INFORMATION, "Assurance ajoutée avec succès !");
            Main.appliquerIconAlert(b);
            b.showAndWait();
            btnAjouter.getScene().getWindow().hide();

        } catch (SQLException e) {
            if (e.getMessage().contains("possède déjà une assurance valide")) {
                Alert alert = new Alert(Alert.AlertType.WARNING, e.getMessage());
                Main.appliquerIconAlert(alert);
                alert.showAndWait();
            } else {
                LOGGER.severe("Erreur ajout assurance : " + e.getMessage());
                Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors de l'ajout de l'assurance.");
                Main.appliquerIconAlert(alert);
                alert.showAndWait();
            }
        } catch (Exception e) {
            LOGGER.severe("Erreur ajout assurance : " + e.getMessage());
            Alert a  = new Alert(Alert.AlertType.ERROR, "Erreur lors de l'ajout de l'assurance.");
            Main.appliquerIconAlert(a);
            a.showAndWait();
        }
    }
}
