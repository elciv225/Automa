package logbo.assy.automa.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import logbo.assy.automa.AuditLogger;
import logbo.assy.automa.dao.ServiceDAO;
import logbo.assy.automa.models.Service;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ControllerAjouterService {
    private static final Logger LOGGER = Logger.getLogger(ControllerAjouterService.class.getName());

    @FXML private TextField txtLibelleService;
    @FXML private Button btnAjouter;

    private ServiceDAO serviceDAO;

    @FXML
    public void initialize() {
        try {
            // Initialisation du DAO
            serviceDAO = new ServiceDAO();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur d'initialisation", e);
            afficherErreur("Erreur d'initialisation", "Impossible de se connecter à la base de données: " + e.getMessage());
        }
    }

    @FXML
    private void ajouterService() {
        // Validation du champ obligatoire
        if (txtLibelleService.getText().trim().isEmpty()) {
            afficherErreur("Champ obligatoire", "Veuillez saisir le nom du service.");
            return;
        }

        String libelle = txtLibelleService.getText().trim();
        String idService = genererIdService(libelle);

        // Création d'un nouvel objet Service
        Service service = new Service();
        service.setIdService(idService);
        service.setLibelle(libelle);
        service.setlocalisation("Abidjan"); // Valeur par défaut

        try {
            // Vérifier si l'ID service existe déjà
            boolean serviceExiste = serviceDAO.getAllServices().stream()
                    .anyMatch(s -> s.getIdService().equals(idService));

            if (serviceExiste) {
                afficherErreur("Service existant", "Un service avec cet identifiant existe déjà.");
                return;
            }

            // Ajouter le nouveau service dans la base de données
            serviceDAO.addService(service);

            // Journalisation de l'action
            AuditLogger.log(
                    "Administrateur", // Remplacer par l'utilisateur connecté
                    "Ajout d'un service",
                    "service",
                    service.getIdService(),
                    "Ajout du service " + service.getLibelle()
            );

            afficherInfo("Service ajouté", "Le service a été ajouté avec succès !");
            fermerModal();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'ajout du service", e);
            afficherErreur("Erreur d'enregistrement", "Impossible d'ajouter le service: " + e.getMessage());
        }
    }

    /**
     * Génère un ID de service à partir du libellé.
     * Format: SERV_LIBELLE (en majuscules, sans espaces)
     */
    private String genererIdService(String libelle) {
        // Convertir en majuscules, enlever les espaces
        String id = "SERV_" + libelle.toUpperCase().trim()
                .replaceAll("\\s+", "_")   // Remplacer les espaces par des underscores
                .replaceAll("[^A-Z0-9_]", ""); // Enlever les caractères spéciaux

        // Limiter la longueur de l'ID à 50 caractères (comme dans la base de données)
        if (id.length() > 50) {
            id = id.substring(0, 50);
        }

        return id;
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