package logbo.assy.automa.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import logbo.assy.automa.Main;
import logbo.assy.automa.dao.AuditLogDAO;
import logbo.assy.automa.models.AuditLog;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ControllerLogs {
    private static final Logger LOGGER = Logger.getLogger(ControllerLogs.class.getName());

    @FXML private DatePicker dateDebut;
    @FXML private DatePicker dateFin;
    @FXML private TextField motCle;
    @FXML private Button btnRechercher;

    @FXML private TableView<AuditLog> tableLogs;
    @FXML private TableColumn<AuditLog, String> colUtilisateur;
    @FXML private TableColumn<AuditLog, String> colAction;
    @FXML private TableColumn<AuditLog, String> colDate;
    @FXML private TableColumn<AuditLog, String> colHeure;

    private AuditLogDAO auditLogDAO;
    private final ObservableList<AuditLog> logsAffiches = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        try {
            // Initialisation du DAO
            auditLogDAO = new AuditLogDAO();

            // Configuration des colonnes du tableau
            colUtilisateur.setCellValueFactory(new PropertyValueFactory<>("utilisateur"));
            colAction.setCellValueFactory(new PropertyValueFactory<>("action"));
            colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
            colHeure.setCellValueFactory(new PropertyValueFactory<>("heure"));

            // Configuration de la tooltip pour afficher les détails au survol
            tableLogs.setRowFactory(tv -> {
                TableRow<AuditLog> row = new TableRow<>();
                row.setOnMouseEntered(event -> {
                    if (!row.isEmpty()) {
                        AuditLog log = row.getItem();
                        if (log.getDetails() != null && !log.getDetails().isEmpty()) {
                            Tooltip tooltip = new Tooltip("Détails: " + log.getDetails() +
                                    "\nEntité: " + (log.getEntite() != null ? log.getEntite() : "N/A") +
                                    "\nID: " + (log.getIdEntite() != null ? log.getIdEntite() : "N/A"));
                            Tooltip.install(row, tooltip);
                        }
                    }
                });
                return row;
            });

            // Liaison du tableau à la liste observable
            tableLogs.setItems(logsAffiches);

            // Configuration du bouton de recherche
            btnRechercher.setOnAction(event -> rechercherLogs());

            // Chargement initial des logs
            chargerTousLesLogs();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur d'initialisation", e);
            afficherErreur("Erreur d'initialisation", "Impossible de se connecter à la base de données: " + e.getMessage());
        }
    }

    private void chargerTousLesLogs() {
        try {
            List<AuditLog> logs = auditLogDAO.getAllLogs();
            logsAffiches.setAll(logs);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur de chargement des logs", e);
            afficherErreur("Erreur de chargement", "Impossible de charger les logs: " + e.getMessage());
        }
    }

    private void rechercherLogs() {
        try {
            LocalDate debut = dateDebut.getValue();
            LocalDate fin = dateFin.getValue();
            String recherche = motCle.getText().trim().isEmpty() ? null : motCle.getText().trim();

            // Validation des dates
            if (debut != null && fin != null && debut.isAfter(fin)) {
                afficherErreur("Dates invalides", "La date de début doit être antérieure à la date de fin.");
                return;
            }

            // Recherche des logs
            List<AuditLog> logs = auditLogDAO.getLogs(debut, fin, recherche);
            logsAffiches.setAll(logs);

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur de recherche", e);
            afficherErreur("Erreur de recherche", "Impossible d'effectuer la recherche: " + e.getMessage());
        }
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