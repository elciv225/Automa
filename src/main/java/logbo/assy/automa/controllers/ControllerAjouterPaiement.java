package logbo.assy.automa.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import logbo.assy.automa.Main;
import logbo.assy.automa.dao.PaiementAttributionDAO;
import logbo.assy.automa.models.AttributionVehicule;
import logbo.assy.automa.models.PaiementAttribution;

import java.sql.SQLException;
import java.time.LocalDate;

public class ControllerAjouterPaiement {
    @FXML private TextField txtMontant;
    @FXML private DatePicker pickerDate;

    private AttributionVehicule attribution;
    private double montantRestant;

    @FXML
    public void initialize() {
        // Configuration du DatePicker
        pickerDate.setValue(LocalDate.now());

        // Désactiver les dates passées
        pickerDate.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate today = LocalDate.now();
                setDisable(empty || date.isBefore(today));
            }
        });

        // Validation du montant (seulement des chiffres)
        txtMontant.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                txtMontant.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        // Appliquer Titre et icone
        Platform.runLater(() -> {
            Stage stage = (Stage) txtMontant.getScene().getWindow();
            stage.setTitle("AutoMA - Ajout de Paiemennt");
            Main.appliquerIcon(stage);
        });
    }

    public void setAttribution(AttributionVehicule attribution) {
        this.attribution = attribution;

        try {
            // Récupérer le montant total et le montant déjà versé
            double montantTotal = Double.parseDouble(attribution.getMontantTotal().replaceAll("[^\\d.]", ""));

            PaiementAttributionDAO paiementDAO = new PaiementAttributionDAO();
            double montantDejaVerse = paiementDAO.getTotalVerse(
                    attribution.getVehicule().getIdVehicule(),
                    attribution.getPersonnel().getIdPersonnel()
            );

            // Calculer le montant restant
            this.montantRestant = montantTotal - montantDejaVerse;

            // Afficher l'information sur le montant restant comme prompting text
            txtMontant.setPromptText("Max: " + String.format("%.0f", montantRestant) + " FCFA");

            // Ajouter également un tooltip pour plus d'informations
            Tooltip tooltip = new Tooltip("Montant restant à payer: " + String.format("%.0f", montantRestant) + " FCFA");
            txtMontant.setTooltip(tooltip);

            // Limiter la saisie de montant pour ne pas dépasser le montant restant
            txtMontant.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.isEmpty()) {
                    try {
                        double montantSaisi = Double.parseDouble(newValue);
                        if (montantSaisi > montantRestant) {
                            txtMontant.setText(String.format("%.0f", montantRestant));
                        }
                    } catch (NumberFormatException e) {
                        // Ignorer les erreurs de conversion
                    }
                }
            });
        } catch (SQLException e) {
            afficherErreur("Erreur", "Impossible de récupérer les informations de paiement: " + e.getMessage());
        }
    }

    @FXML
    private void validerPaiement() {
        if (txtMontant.getText().isEmpty()) {
            afficherErreur("Champ manquant", "Veuillez saisir un montant.");
            return;
        }

        if (pickerDate.getValue() == null) {
            afficherErreur("Champ manquant", "Veuillez sélectionner une date de versement.");
            return;
        }

        // Vérifier que le montant ne dépasse pas le montant restant
        double montantSaisi = Double.parseDouble(txtMontant.getText());
        if (montantSaisi <= 0) {
            afficherErreur("Montant invalide", "Le montant doit être supérieur à zéro.");
            return;
        }

        if (montantSaisi > montantRestant) {
            afficherErreur("Montant invalide", "Le montant saisi dépasse le montant restant à payer.");
            return;
        }

        try {
            // Créer le mois de paiement automatiquement à partir de la date sélectionnée
            // Format: AAAA-MM-01 (premier jour du mois)
            LocalDate dateVersement = pickerDate.getValue();
            String moisPaiement = dateVersement.getYear() + "-" +
                    String.format("%02d", dateVersement.getMonthValue()) + "-01";

            // Créer et enregistrer le paiement
            PaiementAttribution paiement = new PaiementAttribution();
            paiement.setIdVehicule(attribution.getVehicule().getIdVehicule());
            paiement.setIdPersonnel(attribution.getPersonnel().getIdPersonnel());
            paiement.setMoisPaiement(moisPaiement);
            paiement.setMontantVerse(txtMontant.getText());
            paiement.setDateVersement(dateVersement.toString());

            PaiementAttributionDAO paiementDAO = new PaiementAttributionDAO();
            paiementDAO.ajouterPaiement(paiement);

            afficherInfo("Succès", "Paiement enregistré avec succès !");
            fermerModal();
        } catch (SQLException e) {
            afficherErreur("Erreur", "Impossible d'enregistrer le paiement: " + e.getMessage());
        }
    }

    @FXML
    private void fermerModal() {
        Stage stage = (Stage) txtMontant.getScene().getWindow();
        stage.close();
    }

    private void afficherInfo(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        Main.appliquerIconAlert(alert);
        alert.showAndWait();
    }

    private void afficherErreur(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        Main.appliquerIconAlert(alert);
        alert.showAndWait();
    }
}