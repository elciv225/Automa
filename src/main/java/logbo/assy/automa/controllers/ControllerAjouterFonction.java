package logbo.assy.automa.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import logbo.assy.automa.AuditLogger;
import logbo.assy.automa.Main;
import logbo.assy.automa.dao.FonctionDAO;
import logbo.assy.automa.models.Fonction;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ControllerAjouterFonction {
    private static final Logger LOGGER = Logger.getLogger(ControllerAjouterFonction.class.getName());

    @FXML private TextField txtLibelleFonction;
    @FXML private Button btnAjouter;

    private FonctionDAO fonctionDAO;

    @FXML
    public void initialize() {
        try {
            // Initialisation du DAO
            fonctionDAO = new FonctionDAO();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur d'initialisation", e);
            afficherErreur("Erreur d'initialisation", "Impossible de se connecter à la base de données: " + e.getMessage());
        }

        // Appliquer Titre et icone
        Platform.runLater(() -> {
            Stage stage = (Stage) txtLibelleFonction.getScene().getWindow();
            stage.setTitle("AutoMA - Ajout de Fonction");
            Main.appliquerIcon(stage);
        });
    }

    @FXML
    private void ajouterFonction() {
        // Validation du champ obligatoire
        if (txtLibelleFonction.getText().trim().isEmpty()) {
            afficherErreur("Champ obligatoire", "Veuillez saisir le libellé de la fonction.");
            return;
        }

        String libelle = txtLibelleFonction.getText().trim();

        // Création d'un nouvel objet Fonction
        Fonction fonction = new Fonction(libelle);

        try {
            // Vérifier si l'ID fonction existe déjà
            boolean fonctionExiste = fonctionDAO.getAllFonctions().stream()
                    .anyMatch(f -> f.getIdFonction().equals(fonction.getIdFonction()));

            if (fonctionExiste) {
                // Modifier l'ID pour éviter le doublon
                String baseId = fonction.getIdFonction();
                int compteur = 1;
                while(fonctionExiste) {
                    fonction.setIdFonction(baseId + "_" + compteur);
                    fonctionExiste = fonctionDAO.getAllFonctions().stream()
                            .anyMatch(f -> f.getIdFonction().equals(fonction.getIdFonction()));
                    compteur++;
                }
            }

            // Ajouter la nouvelle fonction dans la base de données
            fonctionDAO.addFonction(fonction);

            // Journalisation de l'action
            AuditLogger.log(
                    "Administrateur", // Remplacer par l'utilisateur connecté
                    "Ajout d'une fonction",
                    "fonction",
                    fonction.getIdFonction(),
                    "Ajout de la fonction " + fonction.getLibelle()
            );

            afficherInfo("Fonction ajoutée", "La fonction a été ajoutée avec succès !");
            fermerModal();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'ajout de la fonction", e);
            afficherErreur("Erreur d'enregistrement", "Impossible d'ajouter la fonction: " + e.getMessage());
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
        Main.appliquerIconAlert(alert);
        alert.showAndWait();
    }

    private void afficherErreur(String titre, String contenu) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(contenu);
        Main.appliquerIconAlert(alert);
        alert.showAndWait();
    }
}