package logbo.assy.automa.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import logbo.assy.automa.AuditLogger;
import logbo.assy.automa.Main;
import logbo.assy.automa.dao.CategorieVehiculeDAO;
import logbo.assy.automa.models.CategorieVehicule;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ControllerAjouterCategorieVehicule {
    private static final Logger LOGGER = Logger.getLogger(ControllerAjouterCategorieVehicule.class.getName());

    @FXML private TextField txtLibelleCategorie;
    @FXML private TextField txtNombrePlaces;
    @FXML private Button btnAjouter;

    private CategorieVehiculeDAO categorieDAO;

    @FXML
    public void initialize() {
        try {
            // Initialisation du DAO
            categorieDAO = new CategorieVehiculeDAO();

            // Validation du nombre de places (seulement des chiffres)
            txtNombrePlaces.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.matches("\\d*")) {
                    txtNombrePlaces.setText(newValue.replaceAll("[^\\d]", ""));
                }
            });
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur d'initialisation", e);
            afficherErreur("Erreur d'initialisation", "Impossible de se connecter à la base de données: " + e.getMessage());
        }

        // Appliquer Titre et icone
        Platform.runLater(() -> {
            Stage stage = (Stage) txtLibelleCategorie.getScene().getWindow();
            stage.setTitle("AutoMA - Ajout de Mission");
            Main.appliquerIcon(stage);
        });
    }

    @FXML
    private void ajouterCategorie() {
        // Validation des champs obligatoires
        if (txtLibelleCategorie.getText().trim().isEmpty()) {
            afficherErreur("Champ obligatoire", "Veuillez saisir le libellé de la catégorie.");
            return;
        }

        if (txtNombrePlaces.getText().trim().isEmpty()) {
            afficherErreur("Champ obligatoire", "Veuillez saisir le nombre de places.");
            return;
        }

        String libelle = txtLibelleCategorie.getText().trim();
        String nombrePlaces = txtNombrePlaces.getText().trim();

        try {
            // Vérifier que le nombre de places est un entier valide
            int places = Integer.parseInt(nombrePlaces);
            if (places <= 0) {
                afficherErreur("Valeur invalide", "Le nombre de places doit être supérieur à zéro.");
                return;
            }

            // Création d'un nouvel objet CategorieVehicule
            CategorieVehicule categorie = new CategorieVehicule(libelle, nombrePlaces);

            // Vérifier si l'ID catégorie existe déjà
            boolean categorieExiste = categorieDAO.getAllCategories().stream()
                    .anyMatch(c -> c.getIdCategorie().equals(categorie.getIdCategorie()));

            if (categorieExiste) {
                // Modifier l'ID pour éviter le doublon
                String baseId = categorie.getIdCategorie();
                int compteur = 1;
                while(categorieExiste) {
                    categorie.setIdCategorie(baseId + "_" + compteur);
                    categorieExiste = categorieDAO.getAllCategories().stream()
                            .anyMatch(c -> c.getIdCategorie().equals(categorie.getIdCategorie()));
                    compteur++;
                }
            }

            // Ajouter la nouvelle catégorie dans la base de données
            categorieDAO.addCategory(categorie);

            // Journalisation de l'action
            AuditLogger.log(
                    "Administrateur", // Remplacer par l'utilisateur connecté
                    "Ajout d'une catégorie de véhicule",
                    "categorie_vehicule",
                    categorie.getIdCategorie(),
                    "Ajout de la catégorie " + categorie.getLibelle() + " avec " + categorie.getNombrePlace() + " places"
            );

            afficherInfo("Catégorie ajoutée", "La catégorie a été ajoutée avec succès !");
            fermerModal();

        } catch (NumberFormatException _) {
            afficherErreur("Format incorrect", "Le nombre de places doit être un nombre entier.");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'ajout de la catégorie", e);
            afficherErreur("Erreur d'enregistrement", "Impossible d'ajouter la catégorie: " + e.getMessage());
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