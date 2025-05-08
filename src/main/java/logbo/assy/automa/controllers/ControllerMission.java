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
import logbo.assy.automa.dao.MissionDAO;
import logbo.assy.automa.dao.VehiculeDAO;
import logbo.assy.automa.models.Mission;
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
import java.time.LocalDate;
import java.util.*;
import java.util.logging.Logger;

public class ControllerMission {
    private static final Logger LOGGER = Logger.getLogger(ControllerMission.class.getName());

    @FXML private TableView<Mission> tabMission;
    @FXML private TableColumn<Mission, Boolean> colCase;
    @FXML private TableColumn<Mission, String> colTitre;
    @FXML private TableColumn<Mission, String> colVehicule;
    @FXML private TableColumn<Mission, String> colDestination;
    @FXML private TableColumn<Mission, String> colDateDepart;
    @FXML private TableColumn<Mission, String> colDateRetour;
    @FXML private TableColumn<Mission, String> colCout;

    @FXML private ComboBox<String> comboFilter;
    @FXML private TextField txtRecherche;

    @FXML private Button btnPrecedent;
    @FXML private Button btnSuivant;
    @FXML private Button btnAjouter;
    @FXML private Button btnSupp;
    @FXML private Button btnImprim;

    @FXML private Label lblPage;
    @FXML private Label lblTotal;

    private MissionDAO dao;
    private VehiculeDAO vehiculeDAO;
    private final ObservableList<Mission> missionsAffichees = FXCollections.observableArrayList();

    private int currentPage = 1;
    private int totalPages = 1;
    private static final int PAGE_SIZE = 10;
    private Map<String, Vehicule> vehiculesCache = new HashMap<>();

    // Définition des champs pour l'édition
    private static final String FIELD_CIRCUIT = "circuit";
    private static final String FIELD_DATE_DEBUT = "dateDebut";
    private static final String FIELD_DATE_FIN = "dateFin";
    private static final String FIELD_COUT = "cout";
    private static final String FIELD_COUT_CARBURANT = "coutCarburant";
    private static final String FIELD_OBSERVATION = "observation";

    @FXML
    public void initialize() {
        try {
            dao = new MissionDAO();
            vehiculeDAO = new VehiculeDAO();

            // Configuration du TableView
            tabMission.setEditable(true);

            // Configuration des colonnes
            setupColonnes();

            tabMission.setItems(missionsAffichees);

            // Configuration du ComboBox de filtrage
            comboFilter.setItems(FXCollections.observableArrayList(
                    "Toutes les missions",
                    "Missions en cours",
                    "Missions terminées",
                    "Missions prévues"
            ));
            comboFilter.setValue("Toutes les missions");

            // Configuration de la recherche
            txtRecherche.textProperty().addListener((_, _, newVal) -> rechercherMission(newVal));

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

        // Configuration des colonnes de données avec PropertyValueFactory
        colTitre.setCellValueFactory(new PropertyValueFactory<>(FIELD_CIRCUIT));
        colDestination.setCellValueFactory(new PropertyValueFactory<>(FIELD_CIRCUIT));
        colDateDepart.setCellValueFactory(new PropertyValueFactory<>(FIELD_DATE_DEBUT));
        colDateRetour.setCellValueFactory(new PropertyValueFactory<>(FIELD_DATE_FIN));
        colCout.setCellValueFactory(new PropertyValueFactory<>(FIELD_COUT));

        // Configuration de la colonne véhicule (nécessite un traitement spécial)
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
        setupEditableColumn(colTitre, FIELD_CIRCUIT);
        setupEditableColumn(colDestination, FIELD_CIRCUIT);
        setupEditableColumn(colDateDepart, FIELD_DATE_DEBUT);
        setupEditableColumn(colDateRetour, FIELD_DATE_FIN);
        setupEditableColumn(colCout, FIELD_COUT);
    }

    private void setupEditableColumn(TableColumn<Mission, String> column, String field) {
        column.setCellFactory(TextFieldTableCell.forTableColumn(new DefaultStringConverter()));
        column.setOnEditCommit(event -> {
            Mission mission = event.getRowValue();
            String oldValue = getFieldValue(mission, field);
            String newValue = event.getNewValue();

            if (!Objects.equals(oldValue, newValue)) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Confirmer la modification");
                confirm.setHeaderText("Voulez-vous appliquer cette modification ?");
                confirm.setContentText("Ancienne valeur : " + oldValue + "\nNouvelle valeur : " + newValue);
                Optional<ButtonType> result = confirm.showAndWait();

                if (result.isPresent() && result.get() == ButtonType.OK) {
                    setFieldValue(mission, field, newValue);
                    try {
                        dao.updateMission(mission);
                        chargerPage(currentPage);
                    } catch (SQLException e) {
                        LOGGER.severe("Erreur lors de la mise à jour : " + e.getMessage());
                        afficherErreur("Erreur de mise à jour", "Impossible de mettre à jour la mission: " + e.getMessage());
                    }
                } else {
                    chargerPage(currentPage);
                }
            }
        });
    }

    private String getFieldValue(Mission m, String field) {
        return switch (field) {
            case FIELD_CIRCUIT -> m.getCircuit();
            case FIELD_DATE_DEBUT -> m.getDateDebut();
            case FIELD_DATE_FIN -> m.getDateFin();
            case FIELD_COUT -> m.getCout();
            case FIELD_COUT_CARBURANT -> m.getCoutCarburant();
            case FIELD_OBSERVATION -> m.getObservation();
            default -> "";
        };
    }

    private void setFieldValue(Mission m, String field, String value) {
        switch (field) {
            case FIELD_CIRCUIT -> m.setCircuit(value);
            case FIELD_DATE_DEBUT -> m.setDateDebut(value);
            case FIELD_DATE_FIN -> m.setDateFin(value);
            case FIELD_COUT -> m.setCout(value);
            case FIELD_COUT_CARBURANT -> m.setCoutCarburant(value);
            case FIELD_OBSERVATION -> m.setObservation(value);
        }
    }

    private void chargerPage(int page) {
        try {
            List<Mission> missions = dao.getAllMissions();

            totalPages = (int) Math.ceil((double) missions.size() / PAGE_SIZE);
            if (totalPages == 0) totalPages = 1;

            int fromIndex = Math.min((page - 1) * PAGE_SIZE, missions.size());
            int toIndex = Math.min(fromIndex + PAGE_SIZE, missions.size());

            missionsAffichees.setAll(missions.subList(fromIndex, toIndex));

            lblPage.setText("Page " + page + " / " + totalPages);
            lblTotal.setText("Total des missions : " + missions.size());

            btnPrecedent.setDisable(page <= 1);
            btnSuivant.setDisable(page >= totalPages);

        } catch (SQLException e) {
            afficherErreur("Erreur de chargement", "Impossible de charger les missions: " + e.getMessage());
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/layouts/modals/modalAddMission.fxml"));
            Parent root = loader.load();

            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initOwner(tabMission.getScene().getWindow());
            modalStage.setScene(new Scene(root));
            modalStage.setTitle("Ajouter une mission");
            modalStage.showAndWait();

            // Rafraîchir l'affichage après l'ajout
            chargerPage(currentPage);

        } catch (IOException e) {
            afficherErreur("Erreur", "Impossible d'ouvrir le formulaire d'ajout: " + e.getMessage());
        }
    }

    @FXML
    private void supprimerMissions() {
        List<Mission> selectionnees = missionsAffichees.filtered(Mission::isSelected);

        if (selectionnees.isEmpty()) {
            afficherErreur("Aucune sélection", "Veuillez cocher au moins une mission à supprimer.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText("Êtes-vous sûr de vouloir supprimer " + selectionnees.size() + " mission(s) ?");
        confirmation.setContentText("Cette action est irréversible.");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                for (Mission m : selectionnees) {
                    dao.deleteMission(m.getIdMission());
                }

                chargerPage(currentPage);
                afficherInfo("Suppression réussie", "Les missions sélectionnées ont été supprimées avec succès.");

            } catch (SQLException e) {
                afficherErreur("Erreur de suppression", "Impossible de supprimer les missions: " + e.getMessage());
            }
        }
    }

    @FXML
    private void rechercherMission(String motCle) {
        if (motCle == null || motCle.trim().isEmpty()) {
            chargerPage(currentPage);
            return;
        }

        try {
            List<Mission> missions = dao.getAllMissions();

            List<Mission> resultats = missions.stream()
                    .filter(m -> m.getCircuit().toLowerCase().contains(motCle.toLowerCase()) ||
                            m.getIdMission().toLowerCase().contains(motCle.toLowerCase()) ||
                            m.getDateDebut().contains(motCle) ||
                            m.getDateFin().contains(motCle))
                    .toList();

            missionsAffichees.setAll(resultats);
            lblTotal.setText("Résultats : " + resultats.size());
            lblPage.setText("Recherche");

        } catch (SQLException e) {
            afficherErreur("Erreur de recherche", "Impossible d'effectuer la recherche: " + e.getMessage());
        }
    }

    @FXML
    private void filtrerMissions() {
        String filtre = comboFilter.getValue();
        if (filtre == null || filtre.equals("Toutes les missions")) {
            chargerPage(currentPage);
            return;
        }

        try {
            List<Mission> missions = dao.getAllMissions();
            LocalDate today = LocalDate.now();

            List<Mission> filtrees = switch (filtre) {
                case "Missions en cours" -> missions.stream()
                        .filter(m -> {
                            LocalDate debut = LocalDate.parse(m.getDateDebut());
                            LocalDate fin = m.getDateFin() != null ? LocalDate.parse(m.getDateFin()) : today.plusDays(1);
                            return !debut.isAfter(today) && !fin.isBefore(today);
                        })
                        .toList();

                case "Missions terminées" -> missions.stream()
                        .filter(m -> {
                            if (m.getDateFin() == null) return false;
                            return LocalDate.parse(m.getDateFin()).isBefore(today);
                        })
                        .toList();

                case "Missions prévues" -> missions.stream()
                        .filter(m -> LocalDate.parse(m.getDateDebut()).isAfter(today))
                        .toList();

                default -> missions;
            };

            missionsAffichees.setAll(filtrees);
            lblTotal.setText("Résultats : " + filtrees.size());
            lblPage.setText("Filtre: " + filtre);

        } catch (SQLException e) {
            afficherErreur("Erreur de filtrage", "Impossible d'appliquer le filtre: " + e.getMessage());
        }
    }

    @FXML
    private void imprimerListe() {
        File fichier = new File("liste_missions.pdf");

        try (PdfWriter writer = new PdfWriter(fichier)) {
            PdfDocument pdf = new PdfDocument(writer);
            pdf.setDefaultPageSize(PageSize.A4.rotate());
            Document doc = new Document(pdf);

            doc.add(new Paragraph("Liste des missions").setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)).setFontSize(16));

            Table table = new Table(5);
            table.addCell("Titre");
            table.addCell("Véhicule");
            table.addCell("Destination");
            table.addCell("Date départ");
            table.addCell("Date retour");

            for (Mission m : missionsAffichees) {
                table.addCell(m.getCircuit());

                String immatriculation = "[Non trouvé]";
                try {
                    if (!vehiculesCache.containsKey(m.getIdVehicule())) {
                        Vehicule v = vehiculeDAO.getVehiculeById(m.getIdVehicule());
                        vehiculesCache.put(m.getIdVehicule(), v);
                    }
                    immatriculation = vehiculesCache.get(m.getIdVehicule()).getImmatriculation();
                } catch (SQLException ignored) {}

                table.addCell(immatriculation);
                table.addCell(m.getCircuit());
                table.addCell(m.getDateDebut());
                table.addCell(m.getDateFin() != null ? m.getDateFin() : "");
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
        alert.showAndWait();
    }

    private void afficherErreur(String titre, String contenu) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(contenu);
        alert.showAndWait();
    }
}
