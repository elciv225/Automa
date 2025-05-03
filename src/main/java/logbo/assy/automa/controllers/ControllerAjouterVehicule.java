package logbo.assy.automa.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import logbo.assy.automa.Main;
import logbo.assy.automa.dao.CategorieVehiculeDAO;
import logbo.assy.automa.dao.VehiculeDAO;
import logbo.assy.automa.models.CategorieVehicule;
import logbo.assy.automa.models.Vehicule;

import java.net.URL;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class ControllerAjouterVehicule implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(ControllerAjouterVehicule.class.getName());

    @FXML
    public TextField txtNumChassis;
    @FXML
    public TextField txtImmat;
    @FXML
    public TextField txtMarque;
    @FXML
    public TextField txtModele;
    @FXML
    public ComboBox<CategorieVehicule> comboCategorie;
    @FXML
    public ComboBox<String> comboEnergie;
    @FXML
    public TextField txtPuissance;
    @FXML
    public TextField txtCouleur;
    @FXML
    public TextField txtPrixAchat;
    @FXML
    public DatePicker dateAquisition;
    @FXML
    public DatePicker dateMiseService;
    @FXML
    public TextField nbAmmor;
    @FXML
    public TextField txtDateAmmor;
    @FXML
    public Button btnAjouter;

    private CategorieVehiculeDAO categorieDAO;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initComboBox();
        setupDateAmmortissement();
        setupFormatListeners();

        // Appliquer le titre et l'icone
        Platform.runLater(() -> {
            Stage stage = (Stage) txtNumChassis.getScene().getWindow();
            stage.setTitle("AutoMA - Ajout de Vehicule");
            Main.appliquerIcon(stage);
        });
    }

    private void initComboBox() {
        LOGGER.info("Initialisation de la ComboBox des catégories de véhicule");
        try {
            categorieDAO = new CategorieVehiculeDAO();
            List<CategorieVehicule> list = categorieDAO.getAllCategories();
            ObservableList<CategorieVehicule> obs = FXCollections.observableArrayList(list);
            comboCategorie.setItems(obs);

            comboCategorie.setCellFactory(_ -> new ListCell<>() {
                @Override
                protected void updateItem(CategorieVehicule item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getLibelle());
                }
            });
            comboCategorie.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(CategorieVehicule item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getLibelle());
                }
            });

        } catch (SQLException e) {
            LOGGER.severe("Erreur lors de l'initialisation des catégories : " + e.getMessage());
        }
    }

    private void setupDateAmmortissement() {
        nbAmmor.textProperty().addListener((obs, oldVal, newVal) -> calculerDateAmmortissement());
        dateAquisition.valueProperty().addListener((obs, oldVal, newVal) -> calculerDateAmmortissement());
    }

    private void calculerDateAmmortissement() {
        String nb = nbAmmor.getText();
        LocalDate acquisition = dateAquisition.getValue();

        if (acquisition != null && nb.matches("\\d+")) {
            int nbAnnees = Integer.parseInt(nb);
            LocalDate dateAmortie = acquisition.plusYears(nbAnnees);
            txtDateAmmor.setText(dateAmortie.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        } else {
            txtDateAmmor.clear();
        }
    }

   private void setupFormatListeners() {
    txtImmat.textProperty().addListener((obs, oldVal, newVal) -> {
        String cleaned = newVal.toUpperCase().replaceAll("[^A-Z0-9]", "");
        if (cleaned.length() == 9) {
            String formatted = String.format("%s-%s-%s-CI",
                    cleaned.substring(0, 2),
                    cleaned.substring(2, 5),
                    cleaned.substring(5, 7));
            if (!formatted.equals(oldVal)) {
                txtImmat.setText(formatted);
            }
        }
    });

    txtPrixAchat.textProperty().addListener((obs, oldVal, newVal) -> {
        String digits = newVal.replaceAll("[^0-9]", "");
        if (!digits.isEmpty()) {
            try {
                long value = Long.parseLong(digits);
                String formatted = NumberFormat.getInstance(Locale.FRANCE).format(value) + " FCFA";
                if (!formatted.equals(newVal)) {
                    txtPrixAchat.setText(formatted);
                }
            } catch (NumberFormatException ignored) {}
        } else {
            txtPrixAchat.clear();
        }
    });

    txtPuissance.textProperty().addListener((obs, oldVal, newVal) -> {
        String digits = newVal.replaceAll("[^0-9]", "");
        if (!digits.isEmpty()) {
            String formatted = digits + " CV";
            if (!formatted.equals(newVal)) {
                txtPuissance.setText(formatted);
            }
        } else {
            txtPuissance.clear();
        }
    });
}


    @FXML
    private void ajouterVehicule() {
        try {
            Vehicule vehicule = new Vehicule();
            vehicule.setNumeroChassis(txtNumChassis.getText().toUpperCase());
            vehicule.setImmatriculation(txtImmat.getText().toUpperCase().replace("-", ""));
            vehicule.setMarque(txtMarque.getText().toUpperCase());
            vehicule.setModele(txtModele.getText().toUpperCase());
            vehicule.setIdCategorie(comboCategorie.getSelectionModel().getSelectedItem().getIdCategorie());
            vehicule.setEnergie(comboEnergie.getSelectionModel().getSelectedItem().toUpperCase());
            vehicule.setPuissance(txtPuissance.getText());
            vehicule.setCouleur(txtCouleur.getText().toUpperCase());
            vehicule.setPrixAchat(txtPrixAchat.getText());
            vehicule.setDateAchat(dateAquisition.getValue() != null ? dateAquisition.getValue().toString() : null);
            vehicule.setDateMiseEnService(dateMiseService.getValue() != null ? dateMiseService.getValue().toString() : null);
            vehicule.setDateAmmortissement(txtDateAmmor.getText());

            VehiculeDAO dao = new VehiculeDAO();
            dao.addVehicule(vehicule);

            Alert success = new Alert(Alert.AlertType.INFORMATION, "Véhicule ajouté avec succès !", ButtonType.OK);
            success.showAndWait();

            btnAjouter.getScene().getWindow().hide();

        } catch (Exception e) {
            LOGGER.severe("Erreur lors de l'ajout du véhicule : " + e.getMessage());
            Alert alert = new Alert(Alert.AlertType.ERROR, "Échec de l'ajout du véhicule.", ButtonType.OK);
            alert.showAndWait();
        }
    }
}
