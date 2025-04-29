package logbo.assy.automa.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import logbo.assy.automa.dao.PersonnelDAO;
import logbo.assy.automa.models.Personnel;

import java.util.logging.Logger;

public class ControllerAuthentification {
    private static final Logger LOGGER = Logger.getLogger(ControllerAuthentification.class.getName());
    public TextField txtLogin;
    public TextField txtPassword;
    public Button btnConnexion;

    private PersonnelDAO dao;

    public ControllerAuthentification() {
        try {
            dao = new PersonnelDAO();
        } catch (Exception e) {
            LOGGER.info("Erreur de connexion à la base de données : " + e.getMessage());
        }
    }

    @FXML
    public void seConnecter() {
        try {
            Personnel user = dao.connexion(txtLogin.getText(), txtPassword.getText());
            if (user != null) {
                // TODO : Passer à la page d'accueil
                LOGGER.info("Connexion réussie pour l'utilisateur : " + user.getNom());
                popup("Connexion réussie ", "succes");
            }
        } catch (Exception e) {
            popup(e.getMessage(), "erreur");
        }
    }

  /**
     * Affiche une popup de type "erreur" ou "succès" avec le message fourni,
     * et logge l’événement.
     *
     * @param message Le texte à afficher dans la popup.
     * @param type    "erreur" pour une alerte d’erreur, tout autre valeur pour succès.
     */
    public void popup(String message, String type) {
        // Log dans la console
        if ("erreur".equalsIgnoreCase(type)) {
            LOGGER.info("Erreur : " + message);
        } else {
            LOGGER.info("Succès : " + message);
        }

        // Affichage de l’alert sur le JavaFX Application Thread
        Platform.runLater(() -> {
            Alert.AlertType alertType = "erreur".equalsIgnoreCase(type)
                                  ? Alert.AlertType.ERROR
                                  : Alert.AlertType.INFORMATION;
            Alert alert = new Alert(alertType);
            alert.setTitle("erreur".equalsIgnoreCase(type) ? "Erreur" : "Succès");
            alert.setHeaderText("Une erreur est survenue");
            alert.setContentText(message);
            alert.showAndWait();
        });
    }


}
