package logbo.assy.automa.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import logbo.assy.automa.AuditLogger;
import logbo.assy.automa.dao.PersonnelDAO;
import logbo.assy.automa.dao.ServiceDAO;
import logbo.assy.automa.dao.FonctionDAO;
import logbo.assy.automa.models.Personnel;
import logbo.assy.automa.models.Service;
import logbo.assy.automa.models.Fonction;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ControllerCompte {
    private static final Logger LOGGER = Logger.getLogger(ControllerCompte.class.getName());

    // Champs modifiables
    @FXML private TextField txtNom;
    @FXML private TextField txtPrenom;
    @FXML private TextField txtEmail;
    @FXML private TextField txtTelephone;

    // Champs de changement de mot de passe
    @FXML private PasswordField txtAncienMotDePasse;
    @FXML private PasswordField txtNouveauMotDePasse;
    @FXML private PasswordField txtConfirmationMotDePasse;

    // Champs non modifiables (affichage seulement)
    @FXML private TextField txtLogin;
    @FXML private TextField txtFonction;
    @FXML private TextField txtService;

    // Boutons
    @FXML private Button btnEnregistrer;
    @FXML private Button btnChangerMotDePasse;

    // Section d'affichage des erreurs/succès
    @FXML private Label lblMessage;
    @FXML private VBox vboxChangementMotDePasse;

    // DAOs
    private PersonnelDAO personnelDAO;
    private ServiceDAO serviceDAO;
    private FonctionDAO fonctionDAO;

    // Utilisateur courant
    private Personnel utilisateurCourant;
    private Service serviceUtilisateur;
    private Fonction fonctionUtilisateur;

    @FXML
    public void initialize() {
        try {
            // Initialisation des DAOs
            personnelDAO = new PersonnelDAO();
            serviceDAO = new ServiceDAO();
            fonctionDAO = new FonctionDAO();

            // Par défaut, cacher la section de changement de mot de passe
            vboxChangementMotDePasse.setVisible(false);
            vboxChangementMotDePasse.setManaged(false);

            // Désactiver les champs en lecture seule
            txtLogin.setEditable(false);
            txtFonction.setEditable(false);
            txtService.setEditable(false);

            // Charger les données de l'utilisateur courant
            chargerUtilisateurCourant();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur d'initialisation", e);
            afficherMessage("Erreur de connexion à la base de données", true);
        }
    }

    /**
     * Charge les données de l'utilisateur actuellement connecté.
     * Cette méthode devrait être appelée après la connexion.
     */
    public void chargerUtilisateurCourant() throws SQLException {
        // Dans un système réel, vous récupéreriez l'utilisateur connecté depuis une session
        // Ici, je vais simuler en récupérant un utilisateur spécifique
        String loginUtilisateur = "admin"; // À remplacer par la récupération du login de l'utilisateur connecté

        utilisateurCourant = personnelDAO.getPersonnelByLogin(loginUtilisateur);
        if (utilisateurCourant == null) {
            afficherMessage("Utilisateur non trouvé", true);
            return;
        }

        // Récupérer les informations de service et fonction
        serviceUtilisateur = serviceDAO.getServiceById(utilisateurCourant.getIdService());
        fonctionUtilisateur = fonctionDAO.getFonctionById(utilisateurCourant.getIdFonction());

        // Remplir les champs du formulaire
        txtNom.setText(utilisateurCourant.getNom());
        txtPrenom.setText(utilisateurCourant.getPrenom());
        txtEmail.setText(utilisateurCourant.getEmail());
        txtTelephone.setText(utilisateurCourant.getTelephone());

        // Remplir les champs en lecture seule
        txtLogin.setText(utilisateurCourant.getLogin());
        txtFonction.setText(fonctionUtilisateur != null ? fonctionUtilisateur.getLibelle() : "");
        txtService.setText(serviceUtilisateur != null ? serviceUtilisateur.getLibelle() : "");
    }

    @FXML
    private void enregistrerModifications() {
        // Validation des champs
        if (txtNom.getText().trim().isEmpty() || txtPrenom.getText().trim().isEmpty()) {
            afficherMessage("Les champs Nom et Prénom sont obligatoires", true);
            return;
        }

        // Mettre à jour l'objet utilisateur
        utilisateurCourant.setNom(txtNom.getText().trim());
        utilisateurCourant.setPrenom(txtPrenom.getText().trim());
        utilisateurCourant.setEmail(txtEmail.getText().trim());
        utilisateurCourant.setTelephone(txtTelephone.getText().trim());

        try {
            // Enregistrer les modifications
            personnelDAO.updatePersonnel(utilisateurCourant);

            // Journalisation de l'action
            AuditLogger.log(
                    utilisateurCourant.getLogin(),
                    "Modification de profil",
                    "personnel",
                    utilisateurCourant.getIdPersonnel(),
                    "Mise à jour des informations personnelles"
            );

            afficherMessage("Vos informations ont été mises à jour avec succès", false);

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la mise à jour du profil", e);
            afficherMessage("Erreur lors de l'enregistrement des modifications: " + e.getMessage(), true);
        }
    }

    @FXML
    private void afficherFormChangementMotDePasse() {
        // Afficher/masquer le formulaire de changement de mot de passe
        boolean visible = !vboxChangementMotDePasse.isVisible();
        vboxChangementMotDePasse.setVisible(visible);
        vboxChangementMotDePasse.setManaged(visible);

        // Réinitialiser les champs
        if (visible) {
            txtAncienMotDePasse.clear();
            txtNouveauMotDePasse.clear();
            txtConfirmationMotDePasse.clear();
        }
    }

    @FXML
    private void changerMotDePasse() {
        // Validation des champs
        if (txtAncienMotDePasse.getText().isEmpty() ||
                txtNouveauMotDePasse.getText().isEmpty() ||
                txtConfirmationMotDePasse.getText().isEmpty()) {

            afficherMessage("Tous les champs de mot de passe sont obligatoires", true);
            return;
        }

        // Vérifier que l'ancien mot de passe est correct
        if (!txtAncienMotDePasse.getText().equals(utilisateurCourant.getMotDePasse())) {
            afficherMessage("L'ancien mot de passe est incorrect", true);
            return;
        }

        // Vérifier que les nouveaux mots de passe correspondent
        if (!txtNouveauMotDePasse.getText().equals(txtConfirmationMotDePasse.getText())) {
            afficherMessage("Les nouveaux mots de passe ne correspondent pas", true);
            return;
        }

        // Vérifier que le nouveau mot de passe est différent de l'ancien
        if (txtNouveauMotDePasse.getText().equals(txtAncienMotDePasse.getText())) {
            afficherMessage("Le nouveau mot de passe doit être différent de l'ancien", true);
            return;
        }

        // Vérifier que le nouveau mot de passe est assez fort
        if (txtNouveauMotDePasse.getText().length() < 8) {
            afficherMessage("Le nouveau mot de passe doit contenir au moins 8 caractères", true);
            return;
        }

        try {
            // Mettre à jour le mot de passe
            utilisateurCourant.setMotDePasse(txtNouveauMotDePasse.getText());
            personnelDAO.updateMotDePasse(utilisateurCourant);

            // Journalisation de l'action
            AuditLogger.log(
                    utilisateurCourant.getLogin(),
                    "Changement de mot de passe",
                    "personnel",
                    utilisateurCourant.getIdPersonnel(),
                    "Modification du mot de passe"
            );

            afficherMessage("Votre mot de passe a été changé avec succès", false);

            // Masquer le formulaire de changement de mot de passe
            vboxChangementMotDePasse.setVisible(false);
            vboxChangementMotDePasse.setManaged(false);

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du changement de mot de passe", e);
            afficherMessage("Erreur lors du changement de mot de passe: " + e.getMessage(), true);
        }
    }

    private void afficherMessage(String message, boolean estErreur) {
        lblMessage.setText(message);

        if (estErreur) {
            lblMessage.getStyleClass().clear();
            lblMessage.getStyleClass().add("erreur-message");
        } else {
            lblMessage.getStyleClass().clear();
            lblMessage.getStyleClass().add("succes-message");
        }

        lblMessage.setVisible(true);
    }
}