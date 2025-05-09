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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import logbo.assy.automa.AuditLogger;
import logbo.assy.automa.Main;
import logbo.assy.automa.dao.ServiceDAO;
import logbo.assy.automa.models.Service;

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

public class ControllerService {
    private static final Logger LOGGER = Logger.getLogger(ControllerService.class.getName());

    @FXML
    private TableView<Service> tabService;
    @FXML
    private TableColumn<Service, Boolean> colCase;
    @FXML
    private TableColumn<Service, String> colId;
    @FXML
    private TableColumn<Service, String> colLibelle;
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

    private ServiceDAO serviceDAO;
    private final ObservableList<Service> servicesAffiches = FXCollections.observableArrayList();

    private List<Service> allServices = new ArrayList<>();
    private int currentPage = 1;
    private int totalPages = 1;
    private static final int PAGE_SIZE = 10;

    @FXML
    public void initialize() {
        try {
            // Initialisation du DAO
            serviceDAO = new ServiceDAO();

            // Configuration du tableau
            setupTable();

            // Configuration de la recherche
            txtRecherche.textProperty().addListener((_, _, newVal) -> rechercherService(newVal));

            // Chargement initial des données
            chargerTousLesServices();

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
        colId.setCellValueFactory(new PropertyValueFactory<>("idService"));
        colLibelle.setCellValueFactory(new PropertyValueFactory<>("libelle"));

        // Rendre le tableau éditable pour les cases à cocher
        tabService.setEditable(true);
        tabService.setItems(servicesAffiches);
    }

    private void chargerTousLesServices() {
        try {
            allServices = serviceDAO.getAllServices();
            totalPages = (int) Math.ceil((double) allServices.size() / PAGE_SIZE);
            if (totalPages == 0) totalPages = 1;

            chargerPage(currentPage);

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur de chargement des services", e);
            afficherErreur("Erreur de chargement", "Impossible de charger la liste des services: " + e.getMessage());
        }
    }

    private void chargerPage(int page) {
        int fromIndex = Math.min((page - 1) * PAGE_SIZE, allServices.size());
        int toIndex = Math.min(fromIndex + PAGE_SIZE, allServices.size());

        servicesAffiches.setAll(allServices.subList(fromIndex, toIndex));

        lblPage.setText("Page " + page + " / " + totalPages);
        lblTotal.setText("Total : " + allServices.size() + " services");

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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/layouts/modals/modalAddService.fxml"));
            Parent root = loader.load();

            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initOwner(tabService.getScene().getWindow());
            modalStage.setScene(new Scene(root));
            modalStage.setTitle("Ajouter un service");
            modalStage.showAndWait();

            // Rafraîchir l'affichage après l'ajout
            chargerTousLesServices();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erreur d'ouverture du modal", e);
            afficherErreur("Erreur", "Impossible d'ouvrir le formulaire d'ajout: " + e.getMessage());
        }
    }

    @FXML
    private void supprimerService() {
        List<Service> selectionnes = servicesAffiches.filtered(Service::isSelected);

        if (selectionnes.isEmpty()) {
            afficherErreur("Aucune sélection", "Veuillez cocher au moins un service à supprimer.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText("Êtes-vous sûr de vouloir supprimer " + selectionnes.size() + " service(s) ?");
        confirmation.setContentText("Cette action est irréversible et pourrait affecter les enregistrements liés à ces services.");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {

            for (Service s : selectionnes) {
                try {
                    serviceDAO.deleteService(s.getIdService());

                    // Journalisation de l'action
                    AuditLogger.log(
                            "Administrateur", // Remplacer par l'utilisateur connecté
                            "Suppression d'un service",
                            "service",
                            s.getIdService(),
                            "Suppression du service " + s.getLibelle()
                    );
                } catch (SQLException e) {
                    // Si un service ne peut pas être supprimé (contraintes de clés étrangères), on affiche un message
                    afficherErreur("Erreur de suppression",
                            "Impossible de supprimer le service " + s.getLibelle() + " car il est utilisé par un ou plusieurs enregistrements.");
                    LOGGER.log(Level.WARNING, "Erreur lors de la suppression du service " + s.getIdService(), e);
                }
            }

            chargerTousLesServices();
            afficherInfo("Suppression réussie", "Les services supprimables ont été supprimés avec succès.");


        }
    }

    @FXML
    private void rechercherService(String motCle) {
        if (motCle == null || motCle.trim().isEmpty()) {
            chargerTousLesServices();
            return;
        }

        // Filtrage local par mot-clé
        String recherche = motCle.toLowerCase().trim();

        List<Service> resultats = allServices.stream()
                .filter(s -> s.getIdService().toLowerCase().contains(recherche) ||
                        s.getLibelle().toLowerCase().contains(recherche) ||
                        (s.getlocalisation() != null && s.getlocalisation().toLowerCase().contains(recherche)))
                .collect(Collectors.toList());

        // Mise à jour de l'affichage
        servicesAffiches.setAll(resultats);
        lblTotal.setText("Résultats : " + resultats.size());
        lblPage.setText("Recherche");
    }

    @FXML
    private void imprimerListe() {
        File fichier = new File("liste_services.pdf");

        try (PdfWriter writer = new PdfWriter(fichier)) {
            PdfDocument pdf = new PdfDocument(writer);
            pdf.setDefaultPageSize(PageSize.A4);
            Document doc = new Document(pdf);

            doc.add(new Paragraph("Liste des services")
                    .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD))
                    .setFontSize(16));

            Table table = new Table(3);
            table.addCell("ID");
            table.addCell("Nom du service");
            table.addCell("Localisation");

            for (Service s : servicesAffiches) {
                table.addCell(s.getIdService());
                table.addCell(s.getLibelle());
                table.addCell(s.getlocalisation() != null ? s.getlocalisation() : "");
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