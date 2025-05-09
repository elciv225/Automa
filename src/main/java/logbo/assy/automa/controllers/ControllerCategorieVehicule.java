package logbo.assy.automa.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import logbo.assy.automa.AuditLogger;
import logbo.assy.automa.Main;
import logbo.assy.automa.dao.CategorieVehiculeDAO;
import logbo.assy.automa.models.CategorieVehicule;

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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ControllerCategorieVehicule {
    private static final Logger LOGGER = Logger.getLogger(ControllerCategorieVehicule.class.getName());

    @FXML
    private TableView<CategorieVehicule> tabCategorie;
    @FXML
    private TableColumn<CategorieVehicule, String> celId;
    @FXML
    private TableColumn<CategorieVehicule, String> celLibelle;
    @FXML
    private TableColumn<CategorieVehicule, String> celNombrePlace;


    @FXML
    private TextField txtRecherche;
    @FXML
    private ComboBox<String> comboFilter;

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

    private CategorieVehiculeDAO categorieDAO;
    private final ObservableList<CategorieVehicule> categoriesAffichees = FXCollections.observableArrayList();

    private List<CategorieVehicule> allCategories = new ArrayList<>();
    private int currentPage = 1;
    private int totalPages = 1;
    private static final int PAGE_SIZE = 10;

    @FXML
    public void initialize() {
        try {
            // Initialisation du DAO
            categorieDAO = new CategorieVehiculeDAO();

            // Configuration du tableau
            setupTable();

            // Configuration de la recherche
            txtRecherche.textProperty().addListener((_, _, newVal) -> rechercherCategorie(newVal));

            // Configuration du filtre
            comboFilter.getItems().addAll("Toutes les catégories", "Petites (1-5 places)", "Moyennes (6-15 places)", "Grandes (16+ places)");
            comboFilter.setValue("Toutes les catégories");
            comboFilter.setOnAction(e -> filtrerCategories());

            // Chargement initial des données
            chargerToutesLesCategories();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur d'initialisation", e);
            afficherErreur("Erreur d'initialisation", "Impossible de se connecter à la base de données: " + e.getMessage());
        }
    }

    private void setupTable() {
        // Configuration des colonnes de données
        celId.setCellValueFactory(new PropertyValueFactory<>("idCategorie"));
        celLibelle.setCellValueFactory(new PropertyValueFactory<>("libelle"));
        celNombrePlace.setCellValueFactory(new PropertyValueFactory<>("nombrePlace"));

        tabCategorie.setItems(categoriesAffichees);
    }

    private void chargerToutesLesCategories() {
        try {
            allCategories = categorieDAO.getAllCategories();
            totalPages = (int) Math.ceil((double) allCategories.size() / PAGE_SIZE);
            if (totalPages == 0) totalPages = 1;

            chargerPage(currentPage);

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur de chargement des catégories", e);
            afficherErreur("Erreur de chargement", "Impossible de charger la liste des catégories: " + e.getMessage());
        }
    }

    private void chargerPage(int page) {
        int fromIndex = Math.min((page - 1) * PAGE_SIZE, allCategories.size());
        int toIndex = Math.min(fromIndex + PAGE_SIZE, allCategories.size());

        categoriesAffichees.setAll(allCategories.subList(fromIndex, toIndex));

        lblPage.setText("Page " + page + " / " + totalPages);
        lblTotal.setText("Total de catégories : " + allCategories.size());

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
    private void ajouterCategorie() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/layouts/modals/modalAddCategorieVehicule.fxml"));
            Parent root = loader.load();

            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initOwner(tabCategorie.getScene().getWindow());
            modalStage.setScene(new Scene(root));
            modalStage.setTitle("Ajouter une catégorie de véhicule");
            modalStage.showAndWait();

            // Rafraîchir l'affichage après l'ajout
            chargerToutesLesCategories();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erreur d'ouverture du modal", e);
            afficherErreur("Erreur", "Impossible d'ouvrir le formulaire d'ajout: " + e.getMessage());
        }
    }

    @FXML
    private void supprimerCategorie() {
        List<CategorieVehicule> selectionnees = new ArrayList<>();
        for (CategorieVehicule cat : categoriesAffichees) {
            if (cat.isSelected()) {
                selectionnees.add(cat);
            }
        }

        if (selectionnees.isEmpty()) {
            afficherErreur("Aucune sélection", "Veuillez cocher au moins une catégorie à supprimer.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText("Êtes-vous sûr de vouloir supprimer " + selectionnees.size() + " catégorie(s) ?");
        confirmation.setContentText("Cette action est irréversible et pourrait affecter les véhicules associés à ces catégories.");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            for (CategorieVehicule cat : selectionnees) {
                try {
                    categorieDAO.deleteCategory(cat.getIdCategorie());

                    // Journalisation de l'action
                    AuditLogger.log(
                            "Administrateur", // Remplacer par l'utilisateur connecté
                            "Suppression d'une catégorie de véhicule",
                            "categorie_vehicule",
                            cat.getIdCategorie(),
                            "Suppression de la catégorie " + cat.getLibelle()
                    );
                } catch (SQLException e) {
                    // Si une catégorie ne peut pas être supprimée (contraintes de clés étrangères), on affiche un message
                    afficherErreur("Erreur de suppression",
                            "Impossible de supprimer la catégorie " + cat.getLibelle() + " car elle est utilisée par un ou plusieurs véhicules.");
                    LOGGER.log(Level.WARNING, "Erreur lors de la suppression de la catégorie " + cat.getIdCategorie(), e);
                }
            }

            chargerToutesLesCategories();
            afficherInfo("Suppression réussie", "Les catégories supprimables ont été supprimées avec succès.");


        }
    }

    private void rechercherCategorie(String motCle) {
        if (motCle == null || motCle.trim().isEmpty()) {
            chargerToutesLesCategories();
            return;
        }

        // Filtrage local par mot-clé
        String recherche = motCle.toLowerCase().trim();

        List<CategorieVehicule> resultats = allCategories.stream()
                .filter(c -> c.getIdCategorie().toLowerCase().contains(recherche) ||
                        c.getLibelle().toLowerCase().contains(recherche) ||
                        c.getNombrePlace().toLowerCase().contains(recherche))
                .collect(Collectors.toList());

        // Mise à jour de l'affichage
        categoriesAffichees.setAll(resultats);
        lblTotal.setText("Résultats : " + resultats.size());
        lblPage.setText("Recherche");
    }

    private void filtrerCategories() {
        String filtre = comboFilter.getValue();
        if (filtre == null || filtre.equals("Toutes les catégories")) {
            chargerToutesLesCategories();
            return;
        }

        // Filtrage par nombre de places
        List<CategorieVehicule> filtrees = allCategories;
        if (filtre.equals("Petites (1-5 places)")) {
            filtrees = allCategories.stream()
                    .filter(c -> {
                        try {
                            int places = Integer.parseInt(c.getNombrePlace());
                            return places >= 1 && places <= 5;
                        } catch (NumberFormatException e) {
                            return false;
                        }
                    })
                    .collect(Collectors.toList());
        } else if (filtre.equals("Moyennes (6-15 places)")) {
            filtrees = allCategories.stream()
                    .filter(c -> {
                        try {
                            int places = Integer.parseInt(c.getNombrePlace());
                            return places >= 6 && places <= 15;
                        } catch (NumberFormatException e) {
                            return false;
                        }
                    })
                    .collect(Collectors.toList());
        } else if (filtre.equals("Grandes (16+ places)")) {
            filtrees = allCategories.stream()
                    .filter(c -> {
                        try {
                            int places = Integer.parseInt(c.getNombrePlace());
                            return places >= 16;
                        } catch (NumberFormatException e) {
                            return false;
                        }
                    })
                    .collect(Collectors.toList());
        }

        // Mise à jour de l'affichage
        categoriesAffichees.setAll(filtrees);
        lblTotal.setText("Résultats : " + filtrees.size());
        lblPage.setText("Filtre: " + filtre);
    }

    @FXML
    private void imprimerListe() {
        File fichier = new File("liste_categories_vehicules.pdf");

        try (PdfWriter writer = new PdfWriter(fichier)) {
            PdfDocument pdf = new PdfDocument(writer);
            pdf.setDefaultPageSize(PageSize.A4);
            Document doc = new Document(pdf);

            doc.add(new Paragraph("Liste des catégories de véhicules")
                    .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD))
                    .setFontSize(16));

            Table table = new Table(3);
            table.addCell("ID");
            table.addCell("Libellé");
            table.addCell("Nombre de places");

            for (CategorieVehicule c : categoriesAffichees) {
                table.addCell(c.getIdCategorie());
                table.addCell(c.getLibelle());
                table.addCell(c.getNombrePlace());
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