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
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.converter.DefaultStringConverter;
import logbo.assy.automa.dao.VehiculeDAO;
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

public class ControllerVehicule {
    private static final Logger LOGGER = Logger.getLogger(ControllerVehicule.class.getName());

    @FXML
    public TableView<Vehicule> tabVehicule;
    @FXML
    public TableColumn<Vehicule, Boolean> celCase;
    @FXML
    public TableColumn<Vehicule, String> celNumChassis;
    @FXML
    public TableColumn<Vehicule, String> celImmatriculation;
    @FXML
    public TableColumn<Vehicule, String> celMarque;
    @FXML
    public TableColumn<Vehicule, String> celModele;
    @FXML
    public TableColumn<Vehicule, String> celEnergie;
    @FXML
    public TableColumn<Vehicule, String> celPuissance;
    @FXML
    public TableColumn<Vehicule, String> celCategorie;
    @FXML
    public TableColumn<Vehicule, String> celCouleur;
    @FXML
    public TableColumn<Vehicule, String> celPrixAchat;
    @FXML
    public TableColumn<Vehicule, String> celDateAqui;
    @FXML
    public TableColumn<Vehicule, String> celDateMiseServ;
    @FXML
    public TableColumn<Vehicule, String> celDateAmmor;
    @FXML
    public Button btnSuivant;
    @FXML
    public Button btnPrecedent;
    @FXML
    public Button btnAjouter;
    @FXML
    public Button btnSupp;
    @FXML
    public Button btnImprim;
    @FXML
    public Label lblPage;
    @FXML
    public Label lblTotal;
    @FXML
    public ComboBox<String> comboFilter;
    @FXML
    public TextField txtRecherche;

    private int currentPage = 1;
    private static final int PAGE_SIZE = 10;
    private int totalItems = 0;
    private int totalPages = 1;

    private final ObservableList<Vehicule> vehiculesAffiches = FXCollections.observableArrayList();

    private VehiculeDAO dao;

    private static final String FIELD_NUM_CHASSIS = "numeroChassis";
    private static final String FIELD_IMMATRICULATION = "immatriculation";
    private static final String FIELD_MARQUE = "marque";
    private static final String FIELD_MODELE = "modele";
    private static final String FIELD_ENERGIE = "energie";
    private static final String FIELD_PUISSANCE = "puissance";
    private static final String FIELD_CATEGORIE = "idCategorie";
    private static final String FIELD_COULEUR = "couleur";
    private static final String FIELD_PRIX_ACHAT = "prixAchat";
    private static final String FIELD_DATE_ACHAT = "dateAchat";
    private static final String FIELD_DATE_MISE_EN_SERVICE = "dateMiseEnService";
    private static final String FIELD_DATE_AMMORTISSEMENT = "dateAmmortissement";

    @FXML
    public void initialize() {
        LOGGER.info("Initialisation de la page des véhicules");
        setupColonnes();

        try {
            dao = new VehiculeDAO();
            totalItems = dao.getTotalVehicules();
            totalPages = (int) Math.ceil((double) totalItems / PAGE_SIZE);
            if (totalPages == 0) totalPages = 1;

            chargerPage(currentPage);
            comboFilter.setItems(FXCollections.observableArrayList("Thermique", "Électrique", "Hybride"));

            txtRecherche.textProperty().addListener((_, _, newVal) -> rechercherVehicule(newVal));
        } catch (SQLException e) {
            LOGGER.severe("Erreur DAO : " + e.getMessage());
        }
    }

    private void setupColonnes() {
        celCase.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());
        celCase.setCellFactory(_ -> new CheckBoxTableCell<>());
        celNumChassis.setCellValueFactory(new PropertyValueFactory<>(FIELD_NUM_CHASSIS));
        celImmatriculation.setCellValueFactory(new PropertyValueFactory<>(FIELD_IMMATRICULATION));
        celMarque.setCellValueFactory(new PropertyValueFactory<>(FIELD_MARQUE));
        celModele.setCellValueFactory(new PropertyValueFactory<>(FIELD_MODELE));
        celEnergie.setCellValueFactory(new PropertyValueFactory<>(FIELD_ENERGIE));
        celPuissance.setCellValueFactory(new PropertyValueFactory<>(FIELD_PUISSANCE));
        celCategorie.setCellValueFactory(new PropertyValueFactory<>(FIELD_CATEGORIE));
        celCouleur.setCellValueFactory(new PropertyValueFactory<>(FIELD_COULEUR ));
        celPrixAchat.setCellValueFactory(new PropertyValueFactory<>(FIELD_PRIX_ACHAT));
        celDateAqui.setCellValueFactory(new PropertyValueFactory<>(FIELD_DATE_ACHAT));
        celDateMiseServ.setCellValueFactory(new PropertyValueFactory<>(FIELD_DATE_MISE_EN_SERVICE));
        celDateAmmor.setCellValueFactory(new PropertyValueFactory<>(FIELD_DATE_AMMORTISSEMENT));

        setupEditableColumn(celNumChassis, FIELD_NUM_CHASSIS);
        setupEditableColumn(celImmatriculation, FIELD_IMMATRICULATION);
        setupEditableColumn(celMarque, FIELD_MARQUE);
        setupEditableColumn(celModele, FIELD_MODELE);
        setupEditableColumn(celEnergie, FIELD_ENERGIE);
        setupEditableColumn(celPuissance, FIELD_PUISSANCE);
        setupEditableColumn(celCategorie, FIELD_CATEGORIE);
        setupEditableColumn(celCouleur, FIELD_COULEUR );
        setupEditableColumn(celPrixAchat, FIELD_PRIX_ACHAT);
        setupEditableColumn(celDateAqui, FIELD_DATE_ACHAT);
        setupEditableColumn(celDateMiseServ, FIELD_DATE_MISE_EN_SERVICE);
        setupEditableColumn(celDateAmmor, FIELD_DATE_AMMORTISSEMENT);

        tabVehicule.setEditable(true);
        tabVehicule.setItems(vehiculesAffiches);
    }

    private void setupEditableColumn(TableColumn<Vehicule, String> column, String field) {
        column.setCellFactory(TextFieldTableCell.forTableColumn(new DefaultStringConverter()));
        column.setOnEditCommit(event -> {
            Vehicule v = event.getRowValue();
            String oldValue = getFieldValue(v, field);
            String newValue = event.getNewValue();

            if (!Objects.equals(oldValue, newValue)) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Confirmer la modification");
                confirm.setHeaderText("Voulez-vous appliquer cette modification ?");
                confirm.setContentText("Ancienne valeur : " + oldValue + "\nNouvelle valeur : " + newValue);
                Optional<ButtonType> result = confirm.showAndWait();

                if (result.isPresent() && result.get() == ButtonType.OK) {
                    setFieldValue(v, field, newValue);
                    try {
                        dao.updateVehicule(v);
                        chargerPage(currentPage);
                    } catch (SQLException e) {
                        LOGGER.severe("Erreur lors de la mise à jour : " + e.getMessage());
                    }
                } else {
                    chargerPage(currentPage);
                }
            }
        });
    }

    private String getFieldValue(Vehicule v, String field) {
        return switch (field) {
            case FIELD_NUM_CHASSIS -> v.getNumeroChassis();
            case FIELD_IMMATRICULATION -> v.getImmatriculation();
            case FIELD_MARQUE -> v.getMarque();
            case FIELD_MODELE -> v.getModele();
            case FIELD_ENERGIE -> v.getEnergie();
            case FIELD_PUISSANCE -> v.getPuissance();
            case FIELD_CATEGORIE -> v.getIdCategorie();
            case FIELD_COULEUR  -> v.getCouleur();
            case FIELD_PRIX_ACHAT -> v.getPrixAchat();
            case FIELD_DATE_ACHAT -> v.getDateAchat();
            case FIELD_DATE_MISE_EN_SERVICE -> v.getDateMiseEnService();
            case FIELD_DATE_AMMORTISSEMENT -> v.getDateAmmortissement();
            default -> "😭";
        };
    }

    private void setFieldValue(Vehicule v, String field, String value) {
        switch (field) {
            case FIELD_NUM_CHASSIS -> v.setNumeroChassis(value);
            case FIELD_IMMATRICULATION -> v.setImmatriculation(value);
            case FIELD_MARQUE -> v.setMarque(value);
            case FIELD_MODELE -> v.setModele(value);
            case FIELD_ENERGIE -> v.setEnergie(value);
            case FIELD_PUISSANCE -> v.setPuissance(value);
            case FIELD_CATEGORIE -> v.setIdCategorie(value);
            case FIELD_COULEUR  -> v.setCouleur(value);
            case FIELD_PRIX_ACHAT -> v.setPrixAchat(value);
            case FIELD_DATE_ACHAT -> v.setDateAchat(value);
            case FIELD_DATE_MISE_EN_SERVICE -> v.setDateMiseEnService(value);
            case FIELD_DATE_AMMORTISSEMENT -> v.setDateAmmortissement(value);
        }
    }

    private void chargerPage(int page) {
        try {
            vehiculesAffiches.setAll(dao.getVehiculesPagines(page, PAGE_SIZE));
            lblPage.setText("Page " + currentPage + " / " + totalPages);
            lblTotal.setText("Total : " + totalItems + " véhicules");
            btnPrecedent.setDisable(currentPage <= 1);
            btnSuivant.setDisable(currentPage >= totalPages);
        } catch (SQLException e) {
            LOGGER.severe("Erreur chargement page : " + e.getMessage());
        }
    }

    @FXML
    private void ouvrirModalAjout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/layouts/modals/modalAddVehicule.fxml"));
            Parent root = loader.load();
            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initOwner(btnAjouter.getScene().getWindow());
            modalStage.setTitle("Ajouter un véhicule");
            modalStage.setScene(new Scene(root));
            modalStage.showAndWait();

            totalItems = dao.getTotalVehicules();
            totalPages = (int) Math.ceil((double) totalItems / PAGE_SIZE);
            chargerPage(currentPage);
        } catch (IOException | SQLException e) {
            LOGGER.severe("Erreur modal ajout : " + e.getMessage());
        }
    }

    @FXML
    private void supprimerVehicules() {
        List<Vehicule> selectionnes = vehiculesAffiches.filtered(Vehicule::isSelected);
        if (selectionnes.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Veuillez cocher au moins un véhicule à supprimer.", ButtonType.OK).showAndWait();
            return;
        }
        try {
            for (Vehicule v : selectionnes) dao.supprimerVehicule(v.getIdVehicule());
            totalItems = dao.getTotalVehicules();
            totalPages = (int) Math.ceil((double) totalItems / PAGE_SIZE);
            chargerPage(currentPage);
        } catch (SQLException e) {
            LOGGER.severe("Erreur suppression : " + e.getMessage());
        }
    }

    @FXML
    private void imprimerListe() {
        File fichier = new File("liste_vehicules.pdf");

        try {
            PdfWriter writer = new PdfWriter(fichier);
            PdfDocument pdf = new PdfDocument(writer);
            // Orientation paysage
            pdf.setDefaultPageSize(PageSize.A4.rotate());

            Document document = new Document(pdf);
            document.setMargins(20, 20, 20, 20);

            // Titre
            Paragraph titre = new Paragraph("Liste des véhicules");
            titre.setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD));
            titre.setFontSize(16);
            titre.setMarginBottom(20);
            document.add(titre);

            float[] columnWidths = {80f, 80f, 80f, 80f, 80f, 60f, 80f, 80f};
            Table table = new Table(columnWidths);
            table.setWidth(pdf.getDefaultPageSize().getWidth() - 40); // largeur max moins marges

            // En-têtes
            table.addCell("Châssis");
            table.addCell("Immat.");
            table.addCell(FIELD_MARQUE);
            table.addCell("Modèle");
            table.addCell("Énergie");
            table.addCell(FIELD_PUISSANCE);
            table.addCell("Catégorie");
            table.addCell("Prix");

            // Contenu
            for (Vehicule v : vehiculesAffiches) {
                table.addCell(v.getNumeroChassis());
                table.addCell(v.getImmatriculation());
                table.addCell(v.getMarque());
                table.addCell(v.getModele());
                table.addCell(v.getEnergie());
                table.addCell(v.getPuissance());
                table.addCell(v.getIdCategorie());
                table.addCell(v.getPrixAchat());
            }

            document.add(table);
            document.close();

            new Alert(Alert.AlertType.INFORMATION,
                    "Fichier PDF généré avec succès à l'emplacement :\n" + fichier.getAbsolutePath(),
                    ButtonType.OK).showAndWait();

        } catch (Exception e) {
            LOGGER.severe("Erreur export PDF : " + e.getMessage());
            new Alert(Alert.AlertType.ERROR, "Erreur lors de l'export PDF.", ButtonType.OK).showAndWait();
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
    private void rechercherVehicule(String motCle) {
        if (motCle == null || motCle.trim().isEmpty()) {
            chargerPage(currentPage);
            return;
        }
        try {
            List<Vehicule> resultats = dao.rechercherVehicules(motCle);
            vehiculesAffiches.setAll(resultats);
            lblTotal.setText(resultats.size() + " résultat(s)");
            lblPage.setText("Recherche");
        } catch (SQLException e) {
            LOGGER.severe("Erreur recherche : " + e.getMessage());
        }
    }

    @FXML
    private void filtrerVehicules() {
        String valeur = comboFilter.getValue();
        if (valeur == null || valeur.isEmpty()) {
            chargerPage(currentPage);
            return;
        }
        try {
            List<Vehicule> filtres = dao.filtrerVehicules(FIELD_ENERGIE, valeur);
            vehiculesAffiches.setAll(filtres);
            lblTotal.setText(filtres.size() + " résultat(s)");
            lblPage.setText("Filtrage");
        } catch (SQLException e) {
            LOGGER.severe("Erreur filtrage : " + e.getMessage());
        }
    }
}