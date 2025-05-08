package logbo.assy.automa.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import logbo.assy.automa.AuditLogger;
import logbo.assy.automa.dao.FonctionDAO;
import logbo.assy.automa.dao.PersonnelDAO;
import logbo.assy.automa.dao.ServiceDAO;
import logbo.assy.automa.models.Fonction;
import logbo.assy.automa.models.Personnel;
import logbo.assy.automa.models.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ControllerAjouterPersonnel {
    private static final Logger LOGGER = Logger.getLogger(ControllerAjouterPersonnel.class.getName());

    @FXML
    private TextField txtNom;
    @FXML
    private TextField txtPrenom;
    @FXML
    private TextField txtEmail;
    @FXML
    private TextField txtTelephone;
    @FXML
    private ComboBox<Fonction> comboFonction;
    @FXML
    private ComboBox<Service> comboService;
    @FXML
    private Button btnAjouter;

    private PersonnelDAO personnelDAO;
    private FonctionDAO fonctionDAO;
    private ServiceDAO serviceDAO;

    @FXML
    public void initialize() {
        try {
            // Initialisation des DAOs
            personnelDAO = new PersonnelDAO();
            fonctionDAO = new FonctionDAO();
            serviceDAO = new ServiceDAO();

            // Chargement des fonctions dans le ComboBox
            List<Fonction> fonctions = fonctionDAO.getAllFonctions();
            comboFonction.getItems().addAll(fonctions);
            comboFonction.setConverter(new javafx.util.StringConverter<Fonction>() {
                @Override
                public String toString(Fonction fonction) {
                    return fonction != null ? fonction.getLibelle() : "";
                }

                @Override
                public Fonction fromString(String string) {
                    return null;
                }
            });

            // Chargement des services dans le ComboBox
            List<Service> services = serviceDAO.getAllServices();
            comboService.getItems().addAll(services);
            comboService.setConverter(new javafx.util.StringConverter<Service>() {
                @Override
                public String toString(Service service) {
                    return service != null ? service.getLibelle() : "";
                }

                @Override
                public Service fromString(String string) {
                    return null;
                }
            });

            // Validation du numéro de téléphone (seulement des chiffres)
            txtTelephone.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.matches("\\d*")) {
                    txtTelephone.setText(newValue.replaceAll("[^\\d]", ""));
                }
            });

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur d'initialisation", e);
            afficherErreur("Erreur d'initialisation", "Impossible de se connecter à la base de données: " + e.getMessage());
        }
    }

    @FXML
    private void ajouterPersonnel() {
        // Validation des champs obligatoires
        if (txtNom.getText().isEmpty() || txtPrenom.getText().isEmpty() ||
                comboFonction.getValue() == null || comboService.getValue() == null) {

            afficherErreur("Champs obligatoires",
                    "Veuillez remplir tous les champs obligatoires: nom, prénom, fonction et service.");
            return;
        }

        // Création d'un nouvel objet Personnel
        Personnel personnel = new Personnel(
                txtNom.getText().trim(),
                txtPrenom.getText().trim(),
                "CDI", // Valeur par défaut pour le contrat
                comboFonction.getValue().getIdFonction(),
                comboService.getValue().getIdService()
        );

        // Ajout des informations supplémentaires
        personnel.setEmail(txtEmail.getText().trim());
        personnel.setTelephone(txtTelephone.getText().trim());

        try {
            // Vérifier si le login généré est déjà utilisé
            if (personnelDAO.loginExiste(personnel.getLogin())) {
                // Ajouter un numéro au login en cas de doublon
                int compteur = 1;
                String baseLogin = personnel.getLogin();
                while (personnelDAO.loginExiste(baseLogin + compteur)) {
                    compteur++;
                }
                personnel.setLogin(baseLogin + compteur);
            }

            // Ajouter le nouveau personnel dans la base de données
            personnelDAO.ajouterPersonnel(personnel);

            // Afficher le login généré à l'utilisateur
            String message = "Le personnel a été ajouté avec succès !\n\n" +
                    "Login généré: " + personnel.getLogin() + "\n" +
                    "Mot de passe par défaut: " + personnel.getMotDePasse();

            // Journalisation de l'action
            AuditLogger.log(
                    "Administrateur", // Remplacer par l'utilisateur connecté
                    "Ajout d'un personnel",
                    "personnel",
                    personnel.getIdPersonnel(),
                    "Ajout de " + personnel.getNom() + " " + personnel.getPrenom() + " (Login: " + personnel.getLogin() + ")"
            );

            afficherInfo("Personnel ajouté", message);
            fermerModal();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'ajout du personnel", e);
            afficherErreur("Erreur d'enregistrement", "Impossible d'ajouter le personnel: " + e.getMessage());
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