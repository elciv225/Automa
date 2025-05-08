package logbo.assy.automa.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;

import logbo.assy.automa.dao.EntretienDAO;
import logbo.assy.automa.dao.MissionDAO;
import logbo.assy.automa.dao.VehiculeDAO;
import logbo.assy.automa.models.Entretien;
import logbo.assy.automa.models.Mission;
import logbo.assy.automa.models.Vehicule;

import java.io.File;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ControllerCout {
    private static final Logger LOGGER = Logger.getLogger(ControllerCout.class.getName());

    @FXML private DatePicker dateDebut;
    @FXML private DatePicker dateFin;
    @FXML private PieChart pieChartDepenses;
    @FXML private LineChart<String, Number> lineChartAmortissement;
    @FXML private Label labelTotalDepenses;
    @FXML private Label labelAmortissementAnnuel;

    private EntretienDAO entretienDAO;
    private MissionDAO missionDAO;
    private VehiculeDAO vehiculeDAO;

    @FXML
    public void initialize() {
        try {
            // Initialisation des DAOs
            entretienDAO = new EntretienDAO();
            missionDAO = new MissionDAO();
            vehiculeDAO = new VehiculeDAO();

            // Configuration des dates par défaut (derniers 12 mois)
            dateFin.setValue(LocalDate.now());
            dateDebut.setValue(LocalDate.now().minusMonths(12));

            // Chargement initial des données
            chargerDonnees();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur d'initialisation", e);
            afficherErreur("Erreur d'initialisation", "Impossible de se connecter à la base de données: " + e.getMessage());
        }
    }

    @FXML
    private void filtrerDonnees() {
        if (dateDebut.getValue() == null || dateFin.getValue() == null) {
            afficherErreur("Dates manquantes", "Veuillez sélectionner une période complète.");
            return;
        }

        if (dateDebut.getValue().isAfter(dateFin.getValue())) {
            afficherErreur("Dates invalides", "La date de début doit être antérieure à la date de fin.");
            return;
        }

        chargerDonnees();
    }

    private void chargerDonnees() {
        try {
            // Chargement des entretiens et missions
            List<Entretien> entretiens = filtrerEntretiens(entretienDAO.getAllEntretiens());
            List<Mission> missions = filtrerMissions(missionDAO.getAllMissions());
            List<Vehicule> vehicules = vehiculeDAO.getAllVehicules();

            // Mise à jour des graphiques
            mettreAJourPieChart(entretiens, missions);
            mettreAJourLineChart(entretiens, missions, vehicules);

            // Calcul et affichage des statistiques
            calculerEtAfficherStatistiques(entretiens, missions, vehicules);

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur de chargement des données", e);
            afficherErreur("Erreur de chargement", "Impossible de charger les données: " + e.getMessage());
        }
    }

    private List<Entretien> filtrerEntretiens(List<Entretien> entretiens) {
        LocalDate debut = dateDebut.getValue();
        LocalDate fin = dateFin.getValue();

        return entretiens.stream()
                .filter(e -> {
                    LocalDate dateEntree = LocalDate.parse(e.getDateEntree());
                    return !dateEntree.isBefore(debut) && !dateEntree.isAfter(fin);
                })
                .collect(Collectors.toList());
    }

    private List<Mission> filtrerMissions(List<Mission> missions) {
        LocalDate debut = dateDebut.getValue();
        LocalDate fin = dateFin.getValue();

        return missions.stream()
                .filter(m -> {
                    LocalDate dateDebut = LocalDate.parse(m.getDateDebut());
                    return !dateDebut.isBefore(debut) && !dateDebut.isAfter(fin);
                })
                .collect(Collectors.toList());
    }

    private void mettreAJourPieChart(List<Entretien> entretiens, List<Mission> missions) {
        // Calcul des coûts par catégorie
        double coutEntretiens = entretiens.stream()
                .mapToDouble(e -> e.getPrix() != null ? parseDouble(e.getPrix()) : 0)
                .sum();

        double coutCarburant = missions.stream()
                .mapToDouble(m -> m.getCoutCarburant() != null ? parseDouble(m.getCoutCarburant()) : 0)
                .sum();

        double coutMissions = missions.stream()
                .mapToDouble(m -> m.getCout() != null ? parseDouble(m.getCout()) : 0)
                .sum() - coutCarburant; // Soustraction pour éviter le double comptage

        // Création des données pour le PieChart
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Entretiens", coutEntretiens),
                new PieChart.Data("Carburant", coutCarburant),
                new PieChart.Data("Autres coûts mission", coutMissions)
        );

        pieChartDepenses.setData(pieChartData);

        // Ajout de labels avec les valeurs
        pieChartData.forEach(data -> {
            data.nameProperty().set(data.getName() + ": " + String.format("%,.0f FCFA", data.getPieValue()));
        });
    }

    private void mettreAJourLineChart(List<Entretien> entretiens, List<Mission> missions, List<Vehicule> vehicules) {
        // Effacer les données existantes
        lineChartAmortissement.getData().clear();

        // Période à afficher
        LocalDate debut = dateDebut.getValue();
        LocalDate fin = dateFin.getValue();

        // Créer une série pour l'amortissement mensuel
        XYChart.Series<String, Number> amortissementSeries = new XYChart.Series<>();
        amortissementSeries.setName("Amortissement mensuel");

        // Créer une série pour les dépenses mensuelles
        XYChart.Series<String, Number> depensesSeries = new XYChart.Series<>();
        depensesSeries.setName("Dépenses mensuelles");

        // Calculer l'amortissement mensuel pour chaque mois dans la période
        YearMonth current = YearMonth.from(debut);
        YearMonth end = YearMonth.from(fin);

        Map<YearMonth, Double> depensesMensuelles = new HashMap<>();

        // Calculer les dépenses mensuelles
        for (Entretien e : entretiens) {
            LocalDate date = LocalDate.parse(e.getDateEntree());
            YearMonth mois = YearMonth.from(date);

            double montant = e.getPrix() != null ? parseDouble(e.getPrix()) : 0;
            depensesMensuelles.put(mois, depensesMensuelles.getOrDefault(mois, 0.0) + montant);
        }

        for (Mission m : missions) {
            LocalDate date = LocalDate.parse(m.getDateDebut());
            YearMonth mois = YearMonth.from(date);

            double montant = m.getCout() != null ? parseDouble(m.getCout()) : 0;
            depensesMensuelles.put(mois, depensesMensuelles.getOrDefault(mois, 0.0) + montant);
        }

        // Calculer l'amortissement mensuel total de tous les véhicules
        double amortissementMensuelTotal = vehicules.stream()
                .mapToDouble(v -> {
                    double prixAchat = parseDouble(v.getPrixAchat());
                    // Amortissement sur 5 ans (60 mois)
                    return prixAchat / 60.0;
                })
                .sum();

        // Ajouter les données pour chaque mois
        while (!current.isAfter(end)) {
            String moisLabel = current.getMonth().getDisplayName(TextStyle.SHORT, Locale.getDefault()) + " " + current.getYear();

            // Ajouter l'amortissement mensuel constant
            amortissementSeries.getData().add(new XYChart.Data<>(moisLabel, amortissementMensuelTotal));

            // Ajouter les dépenses pour ce mois (s'il y en a)
            double depensesMois = depensesMensuelles.getOrDefault(current, 0.0);
            depensesSeries.getData().add(new XYChart.Data<>(moisLabel, depensesMois));

            current = current.plusMonths(1);
        }

        // Ajouter les séries au graphique
        lineChartAmortissement.getData().add(amortissementSeries);
        lineChartAmortissement.getData().add(depensesSeries);
    }

    private void calculerEtAfficherStatistiques(List<Entretien> entretiens, List<Mission> missions, List<Vehicule> vehicules) {
        // Calcul du total des dépenses
        double totalEntretiens = entretiens.stream()
                .mapToDouble(e -> e.getPrix() != null ? parseDouble(e.getPrix()) : 0)
                .sum();

        double totalMissions = missions.stream()
                .mapToDouble(m -> m.getCout() != null ? parseDouble(m.getCout()) : 0)
                .sum();

        double totalDepenses = totalEntretiens + totalMissions;

        // Calcul de l'amortissement annuel moyen
        double amortissementAnnuel = vehicules.stream()
                .mapToDouble(v -> {
                    double prixAchat = parseDouble(v.getPrixAchat());
                    // Amortissement sur 5 ans
                    return prixAchat / 5.0;
                })
                .sum();

        // Affichage des statistiques
        labelTotalDepenses.setText(String.format("%,.0f FCFA", totalDepenses));
        labelAmortissementAnnuel.setText(String.format("%,.0f FCFA", amortissementAnnuel));
    }

    @FXML
    private void exporterPDF() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer le rapport de coûts");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
            fileChooser.setInitialFileName("rapport_couts.pdf");

            File fichier = fileChooser.showSaveDialog(dateFin.getScene().getWindow());
            if (fichier == null) return;

            // Chargement des données pour le rapport
            List<Entretien> entretiens = filtrerEntretiens(entretienDAO.getAllEntretiens());
            List<Mission> missions = filtrerMissions(missionDAO.getAllMissions());
            List<Vehicule> vehicules = vehiculeDAO.getAllVehicules();

            // Création du document PDF
            try (PdfWriter writer = new PdfWriter(fichier)) {
                PdfDocument pdf = new PdfDocument(writer);
                pdf.setDefaultPageSize(PageSize.A4);
                Document doc = new Document(pdf);

                // Titre et période
                doc.add(new Paragraph("Rapport de coûts et amortissements")
                        .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD))
                        .setFontSize(16));

                doc.add(new Paragraph("Période: " + formatDate(dateDebut.getValue()) + " à " + formatDate(dateFin.getValue()))
                        .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA))
                        .setFontSize(12));

                doc.add(new Paragraph("\n"));

                // Résumé des coûts
                doc.add(new Paragraph("Résumé des coûts")
                        .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD))
                        .setFontSize(14));

                // Statistiques
                double totalEntretiens = entretiens.stream()
                        .mapToDouble(e -> e.getPrix() != null ? parseDouble(e.getPrix()) : 0)
                        .sum();

                double totalCarburant = missions.stream()
                        .mapToDouble(m -> m.getCoutCarburant() != null ? parseDouble(m.getCoutCarburant()) : 0)
                        .sum();

                double totalMissions = missions.stream()
                        .mapToDouble(m -> m.getCout() != null ? parseDouble(m.getCout()) : 0)
                        .sum() - totalCarburant;

                double totalDepenses = totalEntretiens + totalCarburant + totalMissions;

                // Tableau des coûts
                Table table = new Table(2);
                table.setWidth(400);

                table.addCell("Coûts d'entretien");
                table.addCell(String.format("%,.0f FCFA", totalEntretiens));

                table.addCell("Coûts de carburant");
                table.addCell(String.format("%,.0f FCFA", totalCarburant));

                table.addCell("Autres coûts de mission");
                table.addCell(String.format("%,.0f FCFA", totalMissions));

                table.addCell("Total des dépenses");
                table.addCell(String.format("%,.0f FCFA", totalDepenses));

                doc.add(table);

                doc.add(new Paragraph("\n"));

                // Amortissement
                doc.add(new Paragraph("Amortissement")
                        .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD))
                        .setFontSize(14));

                double amortissementAnnuel = vehicules.stream()
                        .mapToDouble(v -> parseDouble(v.getPrixAchat()) / 5.0)
                        .sum();

                double amortissementPeriode = amortissementAnnuel / 12.0 *
                        getPeriodInMonths(dateDebut.getValue(), dateFin.getValue());

                Table tableAmort = new Table(2);
                tableAmort.setWidth(400);

                tableAmort.addCell("Amortissement annuel");
                tableAmort.addCell(String.format("%,.0f FCFA", amortissementAnnuel));

                tableAmort.addCell("Amortissement sur la période");
                tableAmort.addCell(String.format("%,.0f FCFA", amortissementPeriode));

                doc.add(tableAmort);

                doc.add(new Paragraph("\n"));

                // Détail des véhicules
                doc.add(new Paragraph("Détail des véhicules")
                        .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD))
                        .setFontSize(14));

                Table tableVehicules = new Table(4);
                tableVehicules.setWidth(500);

                tableVehicules.addCell("Immatriculation");
                tableVehicules.addCell("Prix d'achat");
                tableVehicules.addCell("Date acquisition");
                tableVehicules.addCell("Amortissement mensuel");

                for (Vehicule v : vehicules) {
                    tableVehicules.addCell(v.getImmatriculation());
                    tableVehicules.addCell(v.getPrixAchat() + " FCFA");
                    tableVehicules.addCell(v.getDateAchat());

                    double amortMensuel = parseDouble(v.getPrixAchat()) / 60.0;
                    tableVehicules.addCell(String.format("%,.0f FCFA", amortMensuel));
                }

                doc.add(tableVehicules);

                doc.close();

                afficherInfo("Export réussi", "Le rapport a été exporté avec succès :\n" + fichier.getAbsolutePath());

            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Erreur lors de la création du PDF", e);
                afficherErreur("Erreur d'exportation", "Impossible de créer le fichier PDF: " + e.getMessage());
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur de chargement des données", e);
            afficherErreur("Erreur de chargement", "Impossible de charger les données pour l'export: " + e.getMessage());
        }
    }

    private double parseDouble(String value) {
        if (value == null || value.isEmpty()) return 0;

        // Nettoyage de la chaîne pour extraire uniquement les chiffres
        String cleaned = value.replaceAll("[^\\d.,]", "").replace(",", ".");

        try {
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            LOGGER.warning("Impossible de convertir en nombre: " + value);
            return 0;
        }
    }

    private String formatDate(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    private long getPeriodInMonths(LocalDate start, LocalDate end) {
        long years = end.getYear() - start.getYear();
        long months = end.getMonthValue() - start.getMonthValue();

        return years * 12 + months + 1; // +1 pour inclure le mois de fin
    }

    private void afficherInfo(String titre, String contenu) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(contenu);
        alert.showAndWait();
    }

    private void afficherErreur(String titre, String contenu) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(contenu);
        alert.showAndWait();
    }
}