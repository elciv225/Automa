package logbo.assy.automa.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.stage.Modality;
import javafx.stage.Stage;
import logbo.assy.automa.Main;
import logbo.assy.automa.dao.AttributionVehiculeDAO;
import logbo.assy.automa.dao.PaiementAttributionDAO;
import logbo.assy.automa.models.AttributionVehicule;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFontFactory;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

public class ControllerAffectation {
    @FXML
    private TableView<AttributionVehicule> tabAffectation;
    @FXML
    private TableColumn<AttributionVehicule, Boolean> colCase;
    @FXML
    private TableColumn<AttributionVehicule, String> colPersonnel;
    @FXML
    private TableColumn<AttributionVehicule, String> colVehicule;
    @FXML
    private TableColumn<AttributionVehicule, String> colDate;
    @FXML
    private TableColumn<AttributionVehicule, String> colMontantTotal;
    @FXML
    private TableColumn<AttributionVehicule, String> colTotalVerse;
    @FXML
    private TableColumn<AttributionVehicule, String> colPourcentage;
    @FXML
    private Label lblPage;
    @FXML
    private Label lblTotal;
    @FXML
    private Button btnSuivant;
    @FXML
    private Button btnPrecedent;
    @FXML
    private TextField txtRecherche;

    private final int PAGE_SIZE = 10;
    private int currentPage = 1;
    private int totalPages = 1;

    private AttributionVehiculeDAO dao;
    private final ObservableList<AttributionVehicule> affectationsAffichees = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        try {
            dao = new AttributionVehiculeDAO();
            // Rendre le tableau éditable
            tabAffectation.setEditable(true);

            // Configuration de la colonne des cases à cocher
            colCase.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());
            colCase.setCellFactory(_ -> new CheckBoxTableCell<>());
            colCase.setEditable(true);

            // Rendre les autres colonnes non éditables si nécessaire
            colPersonnel.setEditable(false);
            colVehicule.setEditable(false);
            colDate.setEditable(false);
            colMontantTotal.setEditable(false);
            colTotalVerse.setEditable(false);
            colPourcentage.setEditable(false);
            colPersonnel.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPersonnel().getNom()));
            colVehicule.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getVehicule().getImmatriculation()));
            colDate.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDateAttribution()));
            colMontantTotal.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMontantTotal()));

            colTotalVerse.setCellValueFactory(data -> {
                try {
                    PaiementAttributionDAO dao = new PaiementAttributionDAO();
                    double total = dao.getTotalVerse(
                            data.getValue().getVehicule().getIdVehicule(),
                            data.getValue().getPersonnel().getIdPersonnel()
                    );
                    return new SimpleStringProperty(String.format("%.0f", total));
                } catch (SQLException e) {
                    return new SimpleStringProperty("Erreur");
                }
            });

            colPourcentage.setCellValueFactory(data -> {
                try {
                    PaiementAttributionDAO dao = new PaiementAttributionDAO();
                    double total = dao.getTotalVerse(
                            data.getValue().getVehicule().getIdVehicule(),
                            data.getValue().getPersonnel().getIdPersonnel()
                    );
                    double montant = Double.parseDouble(data.getValue().getMontantTotal().replaceAll("[^\\d.]", ""));
                    int mois = dao.getMoisPayes(
                            data.getValue().getVehicule().getIdVehicule(),
                            data.getValue().getPersonnel().getIdPersonnel()
                    );
                    int pourcent = (int) ((total / montant) * 100);
                    return new SimpleStringProperty(pourcent + "% (" + mois + "/60)");
                } catch (Exception e) {
                    return new SimpleStringProperty("Erreur");
                }
            });

            tabAffectation.setItems(affectationsAffichees);

            txtRecherche.textProperty().addListener((_, _, newVal) -> rechercher(newVal));
            chargerPage(currentPage);
        } catch (SQLException e) {
            afficherErreur("Erreur d'initialisation", e.getMessage());
        }
    }

    private void chargerPage(int page) {
        try {
            List<AttributionVehicule> all = dao.getAllAttributions();
            totalPages = (int) Math.ceil((double) all.size() / PAGE_SIZE);
            if (totalPages == 0) totalPages = 1;

            int fromIndex = Math.min((page - 1) * PAGE_SIZE, all.size());
            int toIndex = Math.min(fromIndex + PAGE_SIZE, all.size());

            affectationsAffichees.setAll(all.subList(fromIndex, toIndex));
            lblPage.setText("Page " + page + " / " + totalPages);
            lblTotal.setText("Total : " + all.size());
            btnPrecedent.setDisable(page <= 1);
            btnSuivant.setDisable(page >= totalPages);
        } catch (SQLException e) {
            afficherErreur("Erreur chargement", e.getMessage());
        }
    }

    @FXML
    private void precedentPage() {
        if (currentPage > 1) {
            currentPage--;
            chargerPage(currentPage);
        }
    }

    @FXML
    private void suivantPage() {
        if (currentPage < totalPages) {
            currentPage++;
            chargerPage(currentPage);
        }
    }

    @FXML
    private void ouvrirModalAjout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/layouts/modals/modalAddAffectation.fxml"));
            Parent root = loader.load();

            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initOwner(tabAffectation.getScene().getWindow());
            modalStage.setScene(new Scene(root));
            modalStage.setTitle("Nouvelle Affectation");
            modalStage.showAndWait();

            chargerPage(currentPage);
        } catch (IOException e) {
            afficherErreur("Erreur modal", e.getMessage());
        }
    }

    @FXML
    private void ouvrirModalPaiement() {
        List<AttributionVehicule> selectionnees = affectationsAffichees.filtered(AttributionVehicule::isSelected);

        if (selectionnees.isEmpty()) {
            afficherErreur("Aucune sélection", "Veuillez cocher une affectation pour enregistrer un paiement.");
            return;
        }

        if (selectionnees.size() > 1) {
            afficherErreur("Trop de sélections", "Veuillez ne cocher qu'une seule affectation à la fois.");
            return;
        }

        AttributionVehicule selected = selectionnees.get(0);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/layouts/modals/modalAddPaiement.fxml"));
            Parent root = loader.load();
            ControllerAjouterPaiement controller = loader.getController();
            controller.setAttribution(selected);

            Stage modal = new Stage();
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.initOwner(tabAffectation.getScene().getWindow());
            modal.setTitle("Ajouter un paiement");
            modal.setScene(new Scene(root));
            modal.showAndWait();

            chargerPage(currentPage);
        } catch (IOException e) {
            afficherErreur("Erreur modal", e.getMessage());
        }
    }


    @FXML
    private void supprimerAffectation() {
        List<AttributionVehicule> selectionnees = affectationsAffichees.filtered(AttributionVehicule::isSelected);
        if (selectionnees.isEmpty()) {
            afficherErreur("Aucune sélection", "Veuillez cocher au moins une affectation à supprimer.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Suppression");
        confirm.setHeaderText("Supprimer " + selectionnees.size() + " affectation(s) ?");
        confirm.setContentText("Cette action est irréversible.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                for (AttributionVehicule a : selectionnees) {
                    dao.deleteAttribution(a.getVehicule().getIdVehicule(), a.getPersonnel().getIdPersonnel());
                }
                chargerPage(currentPage);
                afficherInfo("Suppression", "Affectation(s) supprimée(s) avec succès.");
            } catch (SQLException e) {
                afficherErreur("Erreur suppression", e.getMessage());
            }
        }
    }

    @FXML
    private void imprimerListe() {
        File fichier = new File("liste_affectations.pdf");

        try (PdfWriter writer = new PdfWriter(fichier)) {
            PdfDocument pdf = new PdfDocument(writer);
            pdf.setDefaultPageSize(PageSize.A4.rotate());
            Document doc = new Document(pdf);

            doc.add(new Paragraph("Liste des affectations").setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)).setFontSize(16));
            Table table = new Table(3);
            table.addCell("Personnel");
            table.addCell("Véhicule");
            table.addCell("Date");

            for (AttributionVehicule a : affectationsAffichees) {
                table.addCell(a.getPersonnel().getNom());
                table.addCell(a.getVehicule().getImmatriculation());
                table.addCell(a.getDateAttribution());
            }

            doc.add(table);
            doc.close();
            afficherInfo("Export PDF", "Fichier généré avec succès :\n" + fichier.getAbsolutePath());

        } catch (Exception e) {
            afficherErreur("Erreur PDF", e.getMessage());
        }
    }

    private void rechercher(String motCle) {
        if (motCle == null || motCle.trim().isEmpty()) {
            chargerPage(currentPage);
            return;
        }
        try {
            List<AttributionVehicule> resultats = dao.getAllAttributions().stream().filter(a ->
                    a.getPersonnel().getNom().toLowerCase().contains(motCle.toLowerCase()) ||
                            a.getVehicule().getImmatriculation().toLowerCase().contains(motCle.toLowerCase())
            ).toList();

            affectationsAffichees.setAll(resultats);
            lblTotal.setText("Résultats : " + resultats.size());
            lblPage.setText("Recherche");
        } catch (SQLException e) {
            afficherErreur("Erreur recherche", e.getMessage());
        }
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