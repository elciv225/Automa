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
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.converter.DefaultStringConverter;
import logbo.assy.automa.Main;
import logbo.assy.automa.dao.EntretienDAO;
import logbo.assy.automa.dao.VehiculeDAO;
import logbo.assy.automa.models.Entretien;
import logbo.assy.automa.models.Vehicule;

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
import java.util.logging.Logger;

public class ControllerEntretien {
    private static final Logger LOGGER = Logger.getLogger(ControllerEntretien.class.getName());

    @FXML private TableView<Entretien> tabEntretien;
    @FXML private TableColumn<Entretien, Boolean> colCase;
    @FXML private TableColumn<Entretien, String> colVehicule;
    @FXML private TableColumn<Entretien, String> colType;
    @FXML private TableColumn<Entretien, String> colDateDebut;
    @FXML private TableColumn<Entretien, String> colDateFin;
    @FXML private TableColumn<Entretien, String> colDescription;

    @FXML private ComboBox<String> comboFilter;
    @FXML private TextField txtRecherche;

    @FXML private Button btnPrecedent;
    @FXML private Button btnSuivant;
    @FXML private Button btnAjouter;
    @FXML private Button btnSupp;
    @FXML private Button btnImprim;

    @FXML private Label lblPage;
    @FXML private Label lblTotal;

    private EntretienDAO dao;
    private VehiculeDAO vehiculeDAO;
    private final ObservableList<Entretien> entretiensAffiches = FXCollections.observableArrayList();

    private int currentPage = 1;
    private int totalPages = 1;
    private static final int PAGE_SIZE = 10;
    private Map<String, Vehicule> vehiculesCache = new HashMap<>();

    // Définition des champs pour l'édition
    private static final String FIELD_MOTIF = "motif";
    private static final String FIELD_DATE_ENTREE = "dateEntree";
    private static final String FIELD_DATE_SORTIE = "dateSortie";
    private static final String FIELD_OBSERVATION = "observation";

    @FXML
    public void initialize() {
        try {
            dao = new EntretienDAO();
            vehiculeDAO = new VehiculeDAO();

            // Configuration du TableView
            tabEntretien.setEditable(true);

            // Configuration des colonnes
            setupColonnes();

            tabEntretien.setItems(entretiensAffiches);

            // Configuration du ComboBox de filtrage
            comboFilter.setItems(FXCollections.observableArrayList(
                    "Tous les entretiens",
                    "Préventif",
                    "Curatif",
                    "Révision périodique",
                    "Réparation",
                    "Vidange",
                    "Changement pneus",
                    "Autre",
                    "En cours",
                    "Terminés"
            ));
            comboFilter.setValue("Tous les entretiens");

            // Configuration de la recherche
            txtRecherche.textProperty().addListener((_, _, newVal) -> rechercherEntretien(newVal));

            // Chargement des données
            chargerPage(currentPage);

        } catch (SQLException e) {
            afficherErreur("Erreur d'initialisation", "Impossible de se connecter à la base de données: " + e.getMessage());
        }
    }

    private void setupColonnes() {
        // Configuration de la colonne des cases à cocher
        colCase.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());
        colCase.setCellFactory(_ -> new CheckBoxTableCell<>());

        // Configuration des colonnes de données
        colType.setCellValueFactory(new PropertyValueFactory<>(FIELD_MOTIF));
        colDateDebut.setCellValueFactory(new PropertyValueFactory<>(FIELD_DATE_ENTREE));
        colDateFin.setCellValueFactory(new PropertyValueFactory<>(FIELD_DATE_SORTIE));
        colDescription.setCellValueFactory(new PropertyValueFactory<>(FIELD_OBSERVATION));

        // Configuration de la colonne véhicule
        colVehicule.setCellValueFactory(data -> {
            String idVehicule = data.getValue().getIdVehicule();
            try {
                if (!vehiculesCache.containsKey(idVehicule)) {
                    Vehicule v = vehiculeDAO.getVehiculeById(idVehicule);
                    vehiculesCache.put(idVehicule, v);
                }
                return new SimpleStringProperty(vehiculesCache.get(idVehicule).getImmatriculation());
            } catch (SQLException e) {
                return new SimpleStringProperty("[Non trouvé]");
            }
        });

        // Rendre les colonnes éditables
        setupEditableColumn(colType, FIELD_MOTIF);
        setupEditableColumn(colDateDebut, FIELD_DATE_ENTREE);
        setupEditableColumn(colDateFin, FIELD_DATE_SORTIE);
        setupEditableColumn(colDescription, FIELD_OBSERVATION);
    }

    private void setupEditableColumn(TableColumn<Entretien, String> column, String field) {
        column.setCellFactory(TextFieldTableCell.forTableColumn(new DefaultStringConverter()));
        column.setOnEditCommit(event -> {
            Entretien entretien = event.getRowValue();
            String oldValue = getFieldValue(entretien, field);
            String newValue = event.getNewValue();

            if (!Objects.equals(oldValue, newValue)) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Confirmer la modification");
                confirm.setHeaderText("Voulez-vous appliquer cette modification ?");
                confirm.setContentText("Ancienne valeur : " + oldValue + "\nNouvelle valeur : " + newValue);
                Optional<ButtonType> result = confirm.showAndWait();

                if (result.isPresent() && result.get() == ButtonType.OK) {
                    setFieldValue(entretien, field, newValue);
                    try {
                        dao.updateEntretien(entretien);
                        chargerPage(currentPage);
                    } catch (SQLException e) {
                        LOGGER.severe("Erreur lors de la mise à jour : " + e.getMessage());
                        afficherErreur("Erreur de mise à jour", "Impossible de mettre à jour l'entretien: " + e.getMessage());
                    }
                } else {
                    chargerPage(currentPage);
                }
            }
        });
    }

    private String getFieldValue(Entretien e, String field) {
        return switch (field) {
            case FIELD_MOTIF -> e.getMotif();
            case FIELD_DATE_ENTREE -> e.getDateEntree();
            case FIELD_DATE_SORTIE -> e.getDateSortie();
            case FIELD_OBSERVATION -> e.getObservation();
            default -> "";
        };
    }

    private void setFieldValue(Entretien e, String field, String value) {
        switch (field) {
            case FIELD_MOTIF -> e.setMotif(value);
            case FIELD_DATE_ENTREE -> e.setDateEntree(value);
            case FIELD_DATE_SORTIE -> e.setDateSortie(value);
            case FIELD_OBSERVATION -> e.setObservation(value);
        }
    }

    private void chargerPage(int page) {
        try {
            List<Entretien> entretiens = dao.getAllEntretiens();

            totalPages = (int) Math.ceil((double) entretiens.size() / PAGE_SIZE);
            if (totalPages == 0) totalPages = 1;

            int fromIndex = Math.min((page - 1) * PAGE_SIZE, entretiens.size());
            int toIndex = Math.min(fromIndex + PAGE_SIZE, entretiens.size());

            entretiensAffiches.setAll(entretiens.subList(fromIndex, toIndex));

            lblPage.setText("Page " + page + " / " + totalPages);
            lblTotal.setText("Total des entretiens : " + entretiens.size());

            btnPrecedent.setDisable(page <= 1);
            btnSuivant.setDisable(page >= totalPages);

        } catch (SQLException e) {
            afficherErreur("Erreur de chargement", "Impossible de charger les entretiens: " + e.getMessage());
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/layouts/modals/modalAddEntretien.fxml"));
            Parent root = loader.load();

            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initOwner(tabEntretien.getScene().getWindow());
            modalStage.setScene(new Scene(root));
            modalStage.setTitle("Ajouter un entretien");
            modalStage.showAndWait();

            // Rafraîchir l'affichage après l'ajout
            chargerPage(currentPage);

        } catch (IOException e) {
            afficherErreur("Erreur", "Impossible d'ouvrir le formulaire d'ajout: " + e.getMessage());
        }
    }

    @FXML
    private void supprimerEntretiens() {
        List<Entretien> selectionnes = entretiensAffiches.filtered(Entretien::isSelected);

        if (selectionnes.isEmpty()) {
            afficherErreur("Aucune sélection", "Veuillez cocher au moins un entretien à supprimer.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText("Êtes-vous sûr de vouloir supprimer " + selectionnes.size() + " entretien(s) ?");
        confirmation.setContentText("Cette action est irréversible.");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                for (Entretien e : selectionnes) {
                    dao.deleteEntretien(e.getIdEntretien());
                }

                chargerPage(currentPage);
                afficherInfo("Suppression réussie", "Les entretiens sélectionnés ont été supprimés avec succès.");

            } catch (SQLException e) {
                afficherErreur("Erreur de suppression", "Impossible de supprimer les entretiens: " + e.getMessage());
            }
        }
    }

    @FXML
    private void rechercherEntretien(String motCle) {
        if (motCle == null || motCle.trim().isEmpty()) {
            chargerPage(currentPage);
            return;
        }

        try {
            List<Entretien> resultats = dao.rechercherEntretiens(motCle);

            entretiensAffiches.setAll(resultats);
            lblTotal.setText("Résultats : " + resultats.size());
            lblPage.setText("Recherche");

        } catch (SQLException e) {
            afficherErreur("Erreur de recherche", "Impossible d'effectuer la recherche: " + e.getMessage());
        }
    }

    @FXML
    private void filtrerEntretiens() {
        String filtre = comboFilter.getValue();
        if (filtre == null || filtre.equals("Tous les entretiens")) {
            chargerPage(currentPage);
            return;
        }

        try {
            List<Entretien> entretiens = dao.getAllEntretiens();
            List<Entretien> filtres;

            if (filtre.equals("En cours")) {
                filtres = entretiens.stream()
                        .filter(e -> e.getDateSortie() == null)
                        .toList();
            } else if (filtre.equals("Terminés")) {
                filtres = entretiens.stream()
                        .filter(e -> e.getDateSortie() != null)
                        .toList();
            } else {
                // Filtrer par type d'entretien
                filtres = entretiens.stream()
                        .filter(e -> filtre.equals(e.getMotif()))
                        .toList();
            }

            entretiensAffiches.setAll(filtres);
            lblTotal.setText("Résultats : " + filtres.size());
            lblPage.setText("Filtre: " + filtre);

        } catch (SQLException e) {
            afficherErreur("Erreur de filtrage", "Impossible d'appliquer le filtre: " + e.getMessage());
        }
    }

    @FXML
    private void imprimerListe() {
        File fichier = new File("liste_entretiens.pdf");

        try (PdfWriter writer = new PdfWriter(fichier)) {
            PdfDocument pdf = new PdfDocument(writer);
            pdf.setDefaultPageSize(PageSize.A4.rotate());
            Document doc = new Document(pdf);

            doc.add(new Paragraph("Liste des entretiens").setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)).setFontSize(16));

            Table table = new Table(5);
            table.addCell("Véhicule");
            table.addCell("Type");
            table.addCell("Date début");
            table.addCell("Date fin");
            table.addCell("Description");

            for (Entretien e : entretiensAffiches) {
                String immatriculation = "[Non trouvé]";
                try {
                    if (!vehiculesCache.containsKey(e.getIdVehicule())) {
                        Vehicule v = vehiculeDAO.getVehiculeById(e.getIdVehicule());
                        vehiculesCache.put(e.getIdVehicule(), v);
                    }
                    immatriculation = vehiculesCache.get(e.getIdVehicule()).getImmatriculation();
                } catch (SQLException ignored) {}

                table.addCell(immatriculation);
                table.addCell(e.getMotif());
                table.addCell(e.getDateEntree());
                table.addCell(e.getDateSortie() != null ? e.getDateSortie() : "");
                table.addCell(e.getObservation());
            }

            doc.add(table);
            doc.close();

            afficherInfo("Export PDF", "Fichier généré avec succès :\n" + fichier.getAbsolutePath());

        } catch (Exception e) {
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