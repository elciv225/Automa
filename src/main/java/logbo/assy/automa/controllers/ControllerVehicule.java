package logbo.assy.automa.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import logbo.assy.automa.dao.VehiculeDAO;
import logbo.assy.automa.models.Vehicule;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

public class ControllerVehicule {
    private static final Logger LOGGER = Logger.getLogger(ControllerVehicule.class.getName());
    @FXML
    public TableView<Vehicule> tabVehicule;
    @FXML
    public TableColumn<Vehicule, String> celNumChassis;
    @FXML
    public TableColumn<Vehicule, String> celImmatriculation;
    @FXML
    public TableColumn<Vehicule, String> celMarque;
    @FXML
    public TableColumn<Vehicule, String> celModele;
    @FXML
    public TableColumn<Vehicule, String> celEnergie;
    @FXML
    public TableColumn<Vehicule, String> celPuissance;
    @FXML
    public TableColumn<Vehicule, String> celCategorie;
    @FXML
    public TableColumn<Vehicule, String> celCouleur;
    @FXML
    public TableColumn<Vehicule, String> celPrixAchat;
    @FXML
    public TableColumn<Vehicule, String> celDateAqui;
    @FXML
    public TableColumn<Vehicule, String> celDateMiseServ;
    @FXML
    public TableColumn<Vehicule, String> celDateAmmor;
    @FXML
    public Button btnSuivant;
    @FXML
    public Button btnPrecedent;
    @FXML
    public Label lblPage;
    @FXML
    public Label lblTotal;
    public TableColumn celCase;
    public Button btnImprim;
    public Button btnSupp;
    public ComboBox comboFilter;
    public Button btnAjouter;
    // Element de la pour la pagination
    private int currentPage = 1;
    private static final int TOTAL_PAGE = 10;
    private int totalItems;
    private int totalPages;

    @FXML
    public void initialize() {
        LOGGER.info("→ Initialisation de la page des véhicules");

        // Initialisation des colonnes de la table
        LOGGER.info("→ Initialisation des colonnes de la table");
        setupColonnes();

        try {
            VehiculeDAO dao = new VehiculeDAO();
            totalItems = dao.getTotalVehicules();
            totalPages = (int) Math.ceil((double) totalItems / TOTAL_PAGE);
            if (totalPages == 0) totalPages = 1;

        } catch (SQLException e) {
            LOGGER.severe("Erreur lors du calcul du total des véhicules : " + e.getMessage());
        }

        chargerPage(currentPage);

        btnSuivant.setOnAction(_ -> {
            if (currentPage < totalPages) {
                currentPage++;
                chargerPage(currentPage);
            }
        });

        btnPrecedent.setOnAction(_ -> {
            if (currentPage > 1) {
                currentPage--;
                chargerPage(currentPage);
            }
        });
    }

    private void setupColonnes() {
        celNumChassis.setCellValueFactory(new PropertyValueFactory<>("numeroChassis"));
        celImmatriculation.setCellValueFactory(new PropertyValueFactory<>("immatriculation"));
        celMarque.setCellValueFactory(new PropertyValueFactory<>("marque"));
        celModele.setCellValueFactory(new PropertyValueFactory<>("modele"));
        celEnergie.setCellValueFactory(new PropertyValueFactory<>("energie"));
        celPuissance.setCellValueFactory(new PropertyValueFactory<>("puissance"));
        celCategorie.setCellValueFactory(new PropertyValueFactory<>("idCategorie"));
        celCouleur.setCellValueFactory(new PropertyValueFactory<>("couleur"));
        celPrixAchat.setCellValueFactory(new PropertyValueFactory<>("prixAchat"));
        celDateAqui.setCellValueFactory(new PropertyValueFactory<>("dateAchat"));
        celDateMiseServ.setCellValueFactory(new PropertyValueFactory<>("dateMiseEnService"));
        celDateAmmor.setCellValueFactory(new PropertyValueFactory<>("dateAmmortissement"));
    }

    private void chargerPage(int page) {
        try {
            VehiculeDAO dao = new VehiculeDAO();
            List<Vehicule> vehicules = dao.getVehiculesPagines(page, TOTAL_PAGE);
            ObservableList<Vehicule> data = FXCollections.observableArrayList(vehicules);

            tabVehicule.setItems(data);

            // Affichage pagination
            if (totalItems == 0) {
                lblPage.setText("Page 0 / 0");
                lblTotal.setText("Aucun véhicule trouvé.");
            } else {
                lblPage.setText("Page " + currentPage + " / " + totalPages);
                lblTotal.setText("Total : " + totalItems + " véhicules");
            }

            // Gestion des boutons suivant / précédent
            btnSuivant.setDisable(currentPage >= totalPages || totalPages == 0);
            btnPrecedent.setDisable(currentPage <= 1 || totalPages == 0);
        } catch (SQLException e) {
            LOGGER.severe("Erreur lors du chargement des véhicules paginés : " + e.getMessage());
            lblTotal.setText("Erreur de chargement des données.");
        }

    }

    /**
     * Méthode pour ajouter un véhicule en utilisant une modal
     */
    @FXML
    private void ajouterVehicule() {
        LOGGER.info("→ Ouverture de la modal d'ajout de véhicule");
         try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/layouts/modals/modalAddVehicule.fxml")
            );
            Parent root = loader.load();

            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initOwner(btnAjouter.getScene().getWindow());
            modalStage.setTitle("Ajouter un véhicule");
            modalStage.setScene(new Scene(root));
            modalStage.showAndWait();

        } catch (IOException e) {
            LOGGER.severe("Erreur lors de l'ouverture de la modal d'ajout de véhicule : " + e.getMessage());
        }
    }
}
