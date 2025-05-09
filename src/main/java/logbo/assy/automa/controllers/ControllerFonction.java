package logbo.assy.automa.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import logbo.assy.automa.AuditLogger;
import logbo.assy.automa.Main;
import logbo.assy.automa.dao.FonctionDAO;
import logbo.assy.automa.models.Fonction;

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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ControllerFonction {
    private static final Logger LOGGER = Logger.getLogger(ControllerFonction.class.getName());

    @FXML
    private TableView<Fonction> tabFonction;
    @FXML
    private TableColumn<Fonction, Boolean> colCase;
    @FXML
    private TableColumn<Fonction, String> colIdFonction;
    @FXML
    private TableColumn<Fonction, String> colLibelleFonction;
    @FXML
    private TextField txtRecherche;
    @FXML
    private Button btnPrecedent;
    @FXML
    private Button btnSuivant;
    @FXML
    private Button btnAjouter;
    @FXML
    private Button btnSupp;
    @FXML
    private Button btnImprim;
    @FXML
    private Label lblPage;
    @FXML
    private Label lblTotal;

    private FonctionDAO fonctionDAO;
    private final ObservableList<Fonction> fonctionsAffichees = FXCollections.observableArrayList();

    private List<Fonction> allFonctions = new ArrayList<>();
    private int currentPage = 1;
    private int totalPages = 1;
    private static final int PAGE_SIZE = 10;

    @FXML
    public void initialize() {
        try {
            // Initialisation du DAO
            fonctionDAO = new FonctionDAO();

            // Configuration du tableau
            setupTable();

            // Configuration de la recherche
            txtRecherche.textProperty().addListener((_, _, newVal) -> rechercherFonction(newVal));

            // Chargement initial des données
            chargerToutesLesFonctions();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur d'initialisation", e);
            afficherErreur("Erreur d'initialisation", "Impossible de se connecter à la base de données: " + e.getMessage());
        }
    }

    private void setupTable() {
        // Configuration de la colonne des cases à cocher
        colCase.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());
        colCase.setCellFactory(col -> new CheckBoxTableCell<>());

        // Configuration des colonnes de données
        colIdFonction.setCellValueFactory(new PropertyValueFactory<>("idFonction"));
        colLibelleFonction.setCellValueFactory(new PropertyValueFactory<>("libelle"));

        // Rendre le tableau éditable pour les cases à cocher
        tabFonction.setEditable(true);
        tabFonction.setItems(fonctionsAffichees);
    }

    private void chargerToutesLesFonctions() {
        try {
            allFonctions = fonctionDAO.getAllFonctions();
            totalPages = (int) Math.ceil((double) allFonctions.size() / PAGE_SIZE);
            if (totalPages == 0) totalPages = 1;

            chargerPage(currentPage);

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur de chargement des fonctions", e);
            afficherErreur("Erreur de chargement", "Impossible de charger la liste des fonctions: " + e.getMessage());
        }
    }

    private void chargerPage(int page) {
        int fromIndex = Math.min((page - 1) * PAGE_SIZE, allFonctions.size());
        int toIndex = Math.min(fromIndex + PAGE_SIZE, allFonctions.size());

        fonctionsAffichees.setAll(allFonctions.subList(fromIndex, toIndex));

        lblPage.setText("Page " + page + " / " + totalPages);
        lblTotal.setText("Total de fonctions : " + allFonctions.size());

        btnPrecedent.setDisable(page <= 1);
        btnSuivant.setDisable(page >= totalPages);
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/layouts/modals/modalAddFonction.fxml"));
            Parent root = loader.load();

            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initOwner(tabFonction.getScene().getWindow());
            modalStage.setScene(new Scene(root));
            modalStage.setTitle("Ajouter une fonction");
            modalStage.showAndWait();

            // Rafraîchir l'affichage après l'ajout
            chargerToutesLesFonctions();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erreur d'ouverture du modal", e);
            afficherErreur("Erreur", "Impossible d'ouvrir le formulaire d'ajout: " + e.getMessage());
        }
    }

    @FXML
    private void supprimerFonction() {
        List<Fonction> selectionnees = fonctionsAffichees.filtered(Fonction::isSelected);

        if (selectionnees.isEmpty()) {
            afficherErreur("Aucune sélection", "Veuillez cocher au moins une fonction à supprimer.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText("Êtes-vous sûr de vouloir supprimer " + selectionnees.size() + " fonction(s) ?");
        confirmation.setContentText("Cette action est irréversible et pourrait affecter les personnels associés à ces fonctions.");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {

            for (Fonction f : selectionnees) {
                try {
                    fonctionDAO.deleteFonction(f.getIdFonction());

                    // Journalisation de l'action
                    AuditLogger.log(
                            "Administrateur", // Remplacer par l'utilisateur connecté
                            "Suppression d'une fonction",
                            "fonction",
                            f.getIdFonction(),
                            "Suppression de la fonction " + f.getLibelle()
                    );
                } catch (SQLException e) {
                    // Si une fonction ne peut pas être supprimée (contraintes de clés étrangères), on affiche un message
                    afficherErreur("Erreur de suppression",
                            "Impossible de supprimer la fonction " + f.getLibelle() + " car elle est utilisée par un ou plusieurs personnels.");
                    LOGGER.log(Level.WARNING, "Erreur lors de la suppression de la fonction " + f.getIdFonction(), e);
                }
            }

            chargerToutesLesFonctions();
            afficherInfo("Suppression réussie", "Les fonctions supprimables ont été supprimées avec succès.");


        }
    }

    @FXML
    private void rechercherFonction(String motCle) {
        if (motCle == null || motCle.trim().isEmpty()) {
            chargerToutesLesFonctions();
            return;
        }

        // Filtrage local par mot-clé
        String recherche = motCle.toLowerCase().trim();

        List<Fonction> resultats = allFonctions.stream()
                .filter(f -> f.getIdFonction().toLowerCase().contains(recherche) ||
                        f.getLibelle().toLowerCase().contains(recherche))
                .collect(Collectors.toList());

        // Mise à jour de l'affichage
        fonctionsAffichees.setAll(resultats);
        lblTotal.setText("Résultats : " + resultats.size());
        lblPage.setText("Recherche");
    }

    @FXML
    private void imprimerListe() {
        File fichier = new File("liste_fonctions.pdf");

        try (PdfWriter writer = new PdfWriter(fichier)) {
            PdfDocument pdf = new PdfDocument(writer);
            pdf.setDefaultPageSize(PageSize.A4);
            Document doc = new Document(pdf);

            doc.add(new Paragraph("Liste des fonctions du personnel")
                    .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD))
                    .setFontSize(16));

            Table table = new Table(2);
            table.addCell("ID");
            table.addCell("Libellé de la fonction");

            for (Fonction f : fonctionsAffichees) {
                table.addCell(f.getIdFonction());
                table.addCell(f.getLibelle());
            }

            doc.add(table);
            doc.close();

            afficherInfo("Export PDF", "Fichier généré avec succès :\n" + fichier.getAbsolutePath());

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur d'export PDF", e);
            afficherErreur("Erreur PDF", "Impossible de générer le fichier PDF: " + e.getMessage());
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