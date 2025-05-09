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
import logbo.assy.automa.dao.FonctionDAO;
import logbo.assy.automa.dao.PersonnelDAO;
import logbo.assy.automa.dao.ServiceDAO;
import logbo.assy.automa.models.Fonction;
import logbo.assy.automa.models.Personnel;
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

public class ControllerPersonnel {
    private static final Logger LOGGER = Logger.getLogger(ControllerPersonnel.class.getName());


    @FXML private TableView<Personnel> tabPersonnel;
    @FXML private TableColumn<Personnel, Boolean> colCase;
    @FXML private TableColumn<Personnel, String> colNom;
    @FXML private TableColumn<Personnel, String> colPrenom;
    @FXML public TableColumn<Personnel, String> colLogin;
    @FXML private TableColumn<Personnel, String> colEmail;
    @FXML private TableColumn<Personnel, String> colTelephone;
    @FXML private TableColumn<Personnel, String> colFonction;
    @FXML private TableColumn<Personnel, String> colService;

    @FXML private ComboBox<Service> comboFilter;
    @FXML private TextField txtRecherche;

    @FXML private Button btnPrecedent;
    @FXML private Button btnSuivant;
    @FXML private Button btnAjouter;
    @FXML private Button btnSupp;
    @FXML private Button btnImprim;

    @FXML private Label lblPage;
    @FXML private Label lblTotal;

    private PersonnelDAO personnelDAO;
    private FonctionDAO fonctionDAO;
    private ServiceDAO serviceDAO;

    private final ObservableList<Personnel> personnelsAffiches = FXCollections.observableArrayList();
    private Map<String, Fonction> fonctionsCache = new HashMap<>();
    private Map<String, Service> servicesCache = new HashMap<>();

    private int currentPage = 1;
    private int totalPages = 1;
    private static final int PAGE_SIZE = 10;
    private List<Personnel> allPersonnel = new ArrayList<>();

    @FXML
    public void initialize() {
        try {
            // Initialisation des DAOs
            personnelDAO = new PersonnelDAO();
            fonctionDAO = new FonctionDAO();
            serviceDAO = new ServiceDAO();

            // Chargement des caches pour les fonctions et services
            chargerCaches();

            // Configuration des colonnes du tableau
            setupTable();

            // Chargement des services dans le ComboBox de filtrage
            comboFilter.getItems().add(null); // Option "Tous les services"
            comboFilter.getItems().addAll(servicesCache.values());
            comboFilter.setConverter(new javafx.util.StringConverter<Service>() {
                @Override
                public String toString(Service service) {
                    return service == null ? "Tous les services" : service.getLibelle();
                }

                @Override
                public Service fromString(String string) {
                    return null;
                }
            });
            comboFilter.setValue(null);

            // Configuration de la recherche
            txtRecherche.textProperty().addListener((_, _, newVal) -> rechercherPersonnel(newVal));

            // Chargement initial des données
            chargerToutLePersonnel();

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
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colLogin.setCellValueFactory(new PropertyValueFactory<>("login"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone"));

        // Configuration des colonnes pour fonction et service (qui sont des IDs dans le modèle)
        colFonction.setCellValueFactory(data -> {
            String idFonction = data.getValue().getIdFonction();
            Fonction fonction = fonctionsCache.get(idFonction);
            return new SimpleStringProperty(fonction != null ? fonction.getLibelle() : "");
        });

        colService.setCellValueFactory(data -> {
            String idService = data.getValue().getIdService();
            Service service = servicesCache.get(idService);
            return new SimpleStringProperty(service != null ? service.getLibelle() : "");
        });

        // Rendre le tableau éditable pour les cases à cocher
        tabPersonnel.setEditable(true);
        tabPersonnel.setItems(personnelsAffiches);
    }

    private void chargerCaches() throws SQLException {
        // Chargement des fonctions
        List<Fonction> fonctions = fonctionDAO.getAllFonctions();
        fonctionsCache = fonctions.stream()
                .collect(Collectors.toMap(Fonction::getIdFonction, fonction -> fonction));

        // Chargement des services
        List<Service> services = serviceDAO.getAllServices();
        servicesCache = services.stream()
                .collect(Collectors.toMap(Service::getIdService, service -> service));
    }

    private void chargerToutLePersonnel() {
        try {
            allPersonnel = personnelDAO.getAllPersonnel();
            totalPages = (int) Math.ceil((double) allPersonnel.size() / PAGE_SIZE);
            if (totalPages == 0) totalPages = 1;

            chargerPage(currentPage);

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur de chargement du personnel", e);
            afficherErreur("Erreur de chargement", "Impossible de charger la liste du personnel: " + e.getMessage());
        }
    }

    private void chargerPage(int page) {
        int fromIndex = Math.min((page - 1) * PAGE_SIZE, allPersonnel.size());
        int toIndex = Math.min(fromIndex + PAGE_SIZE, allPersonnel.size());

        personnelsAffiches.setAll(allPersonnel.subList(fromIndex, toIndex));

        lblPage.setText("Page " + page + " / " + totalPages);
        lblTotal.setText("Total du personnel : " + allPersonnel.size());

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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/layouts/modals/modalAddPersonnel.fxml"));
            Parent root = loader.load();

            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initOwner(tabPersonnel.getScene().getWindow());
            modalStage.setScene(new Scene(root));
            modalStage.setTitle("Ajouter un personnel");
            modalStage.showAndWait();

            // Rafraîchir l'affichage après l'ajout
            chargerToutLePersonnel();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erreur d'ouverture du modal", e);
            afficherErreur("Erreur", "Impossible d'ouvrir le formulaire d'ajout: " + e.getMessage());
        }
    }

    @FXML
    private void supprimerPersonnel() {
        List<Personnel> selectionnes = personnelsAffiches.filtered(Personnel::isSelected);

        if (selectionnes.isEmpty()) {
            afficherErreur("Aucune sélection", "Veuillez cocher au moins un personnel à supprimer.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText("Êtes-vous sûr de vouloir supprimer " + selectionnes.size() + " personnel(s) ?");
        confirmation.setContentText("Cette action est irréversible.");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                for (Personnel p : selectionnes) {
                    personnelDAO.supprimerPersonnel(p.getIdPersonnel());

                    // Journalisation de l'action
                    AuditLogger.log(
                            "Administrateur", // Remplacer par l'utilisateur connecté
                            "Suppression d'un personnel",
                            "personnel",
                            p.getIdPersonnel(),
                            "Suppression de " + p.getNom() + " " + p.getPrenom()
                    );
                }

                chargerToutLePersonnel();
                afficherInfo("Suppression réussie", "Les personnels sélectionnés ont été supprimés avec succès.");

            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Erreur de suppression", e);
                afficherErreur("Erreur de suppression", "Impossible de supprimer les personnels: " + e.getMessage());
            }
        }
    }

    @FXML
    private void rechercherPersonnel(String motCle) {
        if (motCle == null || motCle.trim().isEmpty()) {
            if (comboFilter.getValue() == null) {
                // Si pas de filtre par service et pas de recherche, on charge tout
                chargerToutLePersonnel();
            } else {
                // Si filtre par service actif, on le réapplique
                filtrerParService();
            }
            return;
        }

        // Filtrage local par mot-clé
        String recherche = motCle.toLowerCase().trim();

        List<Personnel> resultats = allPersonnel.stream()
                .filter(p -> p.getNom().toLowerCase().contains(recherche) ||
                        p.getPrenom().toLowerCase().contains(recherche) ||
                        fonctionsCache.get(p.getIdFonction()).getLibelle().toLowerCase().contains(recherche) ||
                        servicesCache.get(p.getIdService()).getLibelle().toLowerCase().contains(recherche))
                .collect(Collectors.toList());

        // Si un filtre par service est actif, on l'applique aussi
        if (comboFilter.getValue() != null) {
            String idService = comboFilter.getValue().getIdService();
            resultats = resultats.stream()
                    .filter(p -> p.getIdService().equals(idService))
                    .collect(Collectors.toList());
        }

        // Mise à jour de l'affichage
        personnelsAffiches.setAll(resultats);
        lblTotal.setText("Résultats : " + resultats.size());
        lblPage.setText("Recherche");
    }

    @FXML
    private void filtrerParService() {
        Service serviceSelectionne = comboFilter.getValue();

        if (serviceSelectionne == null) {
            // Si "Tous les services" est sélectionné, on recharge tout
            chargerToutLePersonnel();
            return;
        }

        String idService = serviceSelectionne.getIdService();

        // Filtrage local par service
        List<Personnel> filtres = allPersonnel.stream()
                .filter(p -> p.getIdService().equals(idService))
                .collect(Collectors.toList());

        // Si une recherche par mot-clé est active, on l'applique aussi
        String recherche = txtRecherche.getText().trim();
        if (!recherche.isEmpty()) {
            recherche = recherche.toLowerCase();
            final String motCle = recherche; // Variable finale pour le lambda

            filtres = filtres.stream()
                    .filter(p -> p.getNom().toLowerCase().contains(motCle) ||
                            p.getPrenom().toLowerCase().contains(motCle) ||
                            fonctionsCache.get(p.getIdFonction()).getLibelle().toLowerCase().contains(motCle))
                    .collect(Collectors.toList());
        }

        // Mise à jour de l'affichage
        personnelsAffiches.setAll(filtres);
        lblTotal.setText("Résultats : " + filtres.size());
        lblPage.setText("Filtre: " + serviceSelectionne.getLibelle());
    }

    @FXML
    private void imprimerListe() {
        File fichier = new File("liste_personnel.pdf");

        try (PdfWriter writer = new PdfWriter(fichier)) {
            PdfDocument pdf = new PdfDocument(writer);
            pdf.setDefaultPageSize(PageSize.A4.rotate());
            Document doc = new Document(pdf);

            doc.add(new Paragraph("Liste du personnel")
                    .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD))
                    .setFontSize(16));

            Table table = new Table(4);
            table.addCell("Nom");
            table.addCell("Prénom");
            table.addCell("Fonction");
            table.addCell("Service");

            for (Personnel p : personnelsAffiches) {
                table.addCell(p.getNom());
                table.addCell(p.getPrenom());

                Fonction fonction = fonctionsCache.get(p.getIdFonction());
                table.addCell(fonction != null ? fonction.getLibelle() : "");

                Service service = servicesCache.get(p.getIdService());
                table.addCell(service != null ? service.getLibelle() : "");
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