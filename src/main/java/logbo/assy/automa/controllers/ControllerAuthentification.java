package logbo.assy.automa.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import logbo.assy.automa.AuditLogger;
import logbo.assy.automa.Main;
import logbo.assy.automa.SessionManager;
import logbo.assy.automa.dao.PersonnelDAO;
import logbo.assy.automa.models.Personnel;

import java.util.logging.Logger;

public class ControllerAuthentification {
    private static final Logger LOGGER = Logger.getLogger(ControllerAuthentification.class.getName());
    @FXML public TextField txtLogin;
    @FXML public TextField txtPassword;
    @FXML public Button btnConnexion;

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
                SessionManager.setUtilisateurActuel(user); // stocker utilisateur

                FXMLLoader loader = new FXMLLoader();
                switch (user.getIdFonction()) {
                    case "FONC_RESPONSABLELOG" ->
                            loader.setLocation(getClass().getResource("/responsableLogistique.fxml"));
                    case "FONC_ADMIN" -> loader.setLocation(getClass().getResource("/administrateur.fxml"));
                    default -> {
                        popup("Fonction non reconnue. Accès refusé.", "erreur");
                        return;
                    }
                }

                Parent root = loader.load();

                // Créer une nouvelle fenêtre
                javafx.stage.Stage newStage = new javafx.stage.Stage();
                newStage.setTitle("Bienvenue " + user.getPrenom());
                newStage.setScene(new javafx.scene.Scene(root));
                newStage.show();

                // Fermer la fenêtre de connexion actuelle
                javafx.stage.Stage currentStage = (javafx.stage.Stage) txtLogin.getScene().getWindow();
                currentStage.close();

                LOGGER.info("Connexion réussie pour l'utilisateur : " + user.getNom());
                AuditLogger.log(user.getIdPersonnel(), "Connexion", "personnel", user.getIdPersonnel(), "Connexion réussie");
            }
        } catch (Exception e) {
            popup(e.getMessage(), "erreur");
            AuditLogger.log(txtLogin.getText(), "Échec connexion", "personnel", null, "Échec : " + e.getMessage());
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
            Main.appliquerIconAlert(alert);
            alert.showAndWait();
        });
    }


}
