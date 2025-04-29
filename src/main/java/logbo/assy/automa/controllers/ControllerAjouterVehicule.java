package logbo.assy.automa.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import logbo.assy.automa.dao.CategorieVehiculeDAO;
import logbo.assy.automa.models.CategorieVehicule;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
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
    public TextField txtEnergie;
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
    }

    private void initComboBox() {
        LOGGER.info("Initialisation de la ComboBox des catégories de véhicule");
        try {
            // Instanciation du DAO
            categorieDAO = new CategorieVehiculeDAO();

            // Récupération des catégories et transformation en ObservableList
            List<CategorieVehicule> list = categorieDAO.getAllCategories();
            ObservableList<CategorieVehicule> obs = FXCollections.observableArrayList(list);
            comboCategorie.setItems(obs);

            // Affichage du libellé de la catégorie (plutôt que CategorieVehicule.toString())
            comboCategorie.setCellFactory(_ -> new ListCell<>() {
                @Override
                protected void updateItem(CategorieVehicule item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getLibelle());
                }
            });
            // Affiche aussi le libellé dans la partie “sélectionnée”
            comboCategorie.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(CategorieVehicule item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getLibelle());
                }
            });

        } catch (SQLException e) {
            LOGGER.severe("Erreur lors de l'initialisation des catégories : " + e.getMessage());
            // Vous pouvez afficher une alerte ici pour prévenir l'utilisateur
        }
    }

    /**
     * Méthode liée à votre bouton Ajouter (définie dans le FXML)
     */
    @FXML
    private void ajouterVehicule() {
        // Récupérer la catégorie sélectionnée
        CategorieVehicule selectedCat = comboCategorie.getSelectionModel().getSelectedItem();
        // Puis gérer l'insertion en base...
    }
}
