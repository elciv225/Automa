package logbo.assy.automa.controllers;

import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
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
import logbo.assy.automa.dao.AssuranceDAO;
import logbo.assy.automa.models.Assurance;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

public class ControllerAssurance {

    private static final Logger LOGGER = Logger.getLogger(ControllerAssurance.class.getName());

    @FXML
    private TableView<Assurance> tabAssurance;
    @FXML
    private TableColumn<Assurance, Boolean> colCase;
    @FXML
    private TableColumn<Assurance, String> colCompagnie, colType, colVehicule, colDateDebut, colDateFin, colMontant;
    @FXML
    private Label lblPage, lblTotal;
    @FXML
    private Button btnSuivant, btnPrecedent, btnAjouter, btnSupp, btnImprim;
    @FXML
    private TextField txtRecherche;
    @FXML
    private ComboBox<String> comboFilter;

    private final ObservableList<Assurance> assurancesAffichees = FXCollections.observableArrayList();
    private AssuranceDAO dao;
    private int currentPage = 1;
    private static final int PAGE_SIZE = 10;
    private int totalItems = 0;
    private int totalPages = 1;

    @FXML
    public void initialize() {
        try {
            dao = new AssuranceDAO();
            setupColonnes();
            tabAssurance.setEditable(true);

            comboFilter.setItems(FXCollections.observableArrayList("NSIA", "SUNU", "ALLIANZ"));
            comboFilter.setOnAction(e -> filtrerAssurances());

            txtRecherche.textProperty().addListener((obs, oldVal, newVal) -> rechercherAssurances(newVal));

            rechargerTotal();
        } catch (SQLException e) {
            LOGGER.severe("Erreur DAO : " + e.getMessage());
        }
    }

    private void setupColonnes() {
        colCase.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());
        colCase.setCellFactory(_ -> new CheckBoxTableCell<>());

        colCompagnie.setCellValueFactory(new PropertyValueFactory<>("agence"));
        colType.setCellValueFactory(new PropertyValueFactory<>("contrat"));
        colVehicule.setCellValueFactory(new PropertyValueFactory<>("idVehicule"));
        colDateDebut.setCellValueFactory(new PropertyValueFactory<>("dateDebut"));
        colDateFin.setCellValueFactory(new PropertyValueFactory<>("dateFin"));
        colMontant.setCellValueFactory(new PropertyValueFactory<>("prix"));

        setupEditableColumn(colCompagnie, "agence");
        setupEditableColumn(colType, "contrat");
        setupEditableColumn(colVehicule, "idVehicule");
        setupEditableColumn(colDateDebut, "dateDebut");
        setupEditableColumn(colDateFin, "dateFin");
        setupEditableColumn(colMontant, "prix");

        tabAssurance.setItems(assurancesAffichees);
    }

    private void setupEditableColumn(TableColumn<Assurance, String> column, String field) {
        column.setCellFactory(TextFieldTableCell.forTableColumn(new DefaultStringConverter()));
        column.setOnEditCommit(event -> {
            Assurance a = event.getRowValue();
            String oldValue = getFieldValue(a, field);
            String newValue = event.getNewValue();

            if (!Objects.equals(oldValue, newValue)) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Confirmer la modification");
                confirm.setHeaderText("Voulez-vous appliquer cette modification ?");
                confirm.setContentText("Ancienne valeur : " + oldValue + "\nNouvelle valeur : " + newValue);
                Optional<ButtonType> result = confirm.showAndWait();

                if (result.isPresent() && result.get() == ButtonType.OK) {
                    setFieldValue(a, field, newValue);
                    try {
                        dao.update(a);
                        chargerPage(currentPage);
                    } catch (SQLException e) {
                        LOGGER.severe("Erreur mise à jour : " + e.getMessage());
                    }
                } else {
                    try {
                        chargerPage(currentPage);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }

    private String getFieldValue(Assurance a, String field) {
        return switch (field) {
            case "agence" -> a.getAgence();
            case "contrat" -> a.getContrat();
            case "idVehicule" -> a.getIdVehicule();
            case "dateDebut" -> a.getDateDebut();
            case "dateFin" -> a.getDateFin();
            case "prix" -> a.getPrix();
            default -> "";
        };
    }

    private void setFieldValue(Assurance a, String field, String value) {
        switch (field) {
            case "agence" -> a.setAgence(value);
            case "contrat" -> a.setContrat(value);
            case "idVehicule" -> a.setIdVehicule(value);
            case "dateDebut" -> a.setDateDebut(value);
            case "dateFin" -> a.setDateFin(value);
            case "prix" -> a.setPrix(value);
        }
    }

    private void rechargerTotal() throws SQLException {
        totalItems = dao.getAll().size();
        totalPages = (int) Math.ceil((double) totalItems / PAGE_SIZE);
        if (totalPages == 0) totalPages = 1;
        chargerPage(currentPage);
    }

    private void chargerPage(int page) throws SQLException {
        List<Assurance> toutes = dao.getAll();
        int from = (page - 1) * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, toutes.size());
        assurancesAffichees.setAll(toutes.subList(from, to));
        lblPage.setText("Page " + page + " / " + totalPages);
        lblTotal.setText("Total : " + toutes.size() + " assurances");
        btnPrecedent.setDisable(currentPage <= 1);
        btnSuivant.setDisable(currentPage >= totalPages);
    }

    @FXML
    private void precedentPage() throws SQLException {
        if (currentPage > 1) {
            currentPage--;
            chargerPage(currentPage);
        }
    }

    @FXML
    private void suivantPage() throws SQLException {
        if (currentPage < totalPages) {
            currentPage++;
            chargerPage(currentPage);
        }
    }

    @FXML
    private void ouvrirModalAjout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/layouts/modals/modalAddAssurance.fxml"));
            Parent root = loader.load();
            Stage modal = new Stage();
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.setScene(new Scene(root));
            modal.setTitle("Ajouter une assurance");
            modal.showAndWait();
            rechargerTotal();
        } catch (IOException | SQLException e) {
            LOGGER.severe("Erreur ajout : " + e.getMessage());
        }
    }

    @FXML
    private void supprimerAssurance() {
        List<Assurance> selectionnees = assurancesAffichees.filtered(Assurance::isSelected);
        if (selectionnees.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Veuillez cocher au moins une assurance à supprimer.", ButtonType.OK).showAndWait();
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText("Êtes-vous sûr de vouloir supprimer ces assurances ?");
        confirmation.setContentText("Cette action est irréversible.");

        Optional<ButtonType> result = confirmation.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                for (Assurance a : selectionnees) {
                    dao.delete(a.getIdAssurance());
                }
                rechargerTotal();
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Les assurances sélectionnées ont été supprimées avec succès.", ButtonType.OK);
                Main.appliquerIconAlert(alert);
                alert.showAndWait();
            } catch (SQLException e) {
                LOGGER.severe("Erreur suppression : " + e.getMessage());
                Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors de la suppression des assurances.", ButtonType.OK);
                Main.appliquerIconAlert(alert);
                alert.showAndWait();
            }
        }
    }

    @FXML
    private void filtrerAssurances() {
        String filtre = comboFilter.getValue();
        if (filtre == null || filtre.isEmpty()) return;
        try {
            List<Assurance> toutes = dao.getAll();
            List<Assurance> filtrees = toutes.stream().filter(a -> a.getAgence().equalsIgnoreCase(filtre)).toList();
            assurancesAffichees.setAll(filtrees);
            lblPage.setText("Filtrage : " + filtre);
            lblTotal.setText("Total : " + filtrees.size() + " assurances");
        } catch (SQLException e) {
            LOGGER.severe("Erreur filtrage : " + e.getMessage());
        }
    }

    private void rechercherAssurances(String query) {
        try {
            if (query == null || query.isEmpty()) {
                chargerPage(currentPage);
                return;
            }
            List<Assurance> resultats = dao.getAll().stream().filter(a ->
                    a.getAgence().toLowerCase().contains(query.toLowerCase()) ||
                            a.getIdVehicule().toLowerCase().contains(query.toLowerCase()) ||
                            a.getIdAssurance().toLowerCase().contains(query.toLowerCase())
            ).toList();
            assurancesAffichees.setAll(resultats);
            lblPage.setText("Recherche : " + query);
            lblTotal.setText("Résultats : " + resultats.size());
        } catch (SQLException e) {
            LOGGER.severe("Erreur recherche : " + e.getMessage());
        }
    }

    @FXML
    private void imprimerListe() {
        File fichier = new File("liste_assurances.pdf");
        try (PdfWriter writer = new PdfWriter(fichier)) {
            PdfDocument pdf = new PdfDocument(writer);
            pdf.setDefaultPageSize(PageSize.A4.rotate());
            Document doc = new Document(pdf);
            doc.add(new Paragraph("Liste des assurances").setFont(PdfFontFactory.createFont()).setFontSize(16));
            Table table = new Table(6);
            table.addCell("Compagnie");
            table.addCell("Type");
            table.addCell("Véhicule");
            table.addCell("Début");
            table.addCell("Fin");
            table.addCell("Montant");
            for (Assurance a : assurancesAffichees) {
                table.addCell(a.getAgence());
                table.addCell(a.getContrat());
                table.addCell(a.getIdVehicule());
                table.addCell(a.getDateDebut());
                table.addCell(a.getDateFin());
                table.addCell(a.getPrix());
            }
            doc.add(table);
            doc.close();
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "PDF généré : " + fichier.getAbsolutePath());
            Main.appliquerIconAlert(alert);
            alert.showAndWait();
        } catch (Exception e) {
            LOGGER.severe("Erreur export PDF : " + e.getMessage());
        }
    }
}
