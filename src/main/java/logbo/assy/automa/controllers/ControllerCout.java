package logbo.assy.automa.controllers;

import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import logbo.assy.automa.dao.EntretienDAO;
import logbo.assy.automa.dao.MissionDAO;
import logbo.assy.automa.dao.VehiculeDAO;
import logbo.assy.automa.models.Entretien;
import logbo.assy.automa.models.Mission;
import logbo.assy.automa.models.Vehicule;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Logger;

public class ControllerCout {
    private static final Logger LOGGER = Logger.getLogger(ControllerCout.class.getName());

    @FXML private DatePicker dateDebut;
    @FXML private DatePicker dateFin;
    @FXML private PieChart pieChartDepenses;
    @FXML private LineChart<String, Number> lineChartAmortissement;
    @FXML private Label labelTotalDepenses;
    @FXML private Label labelAmortissementAnnuel;

    private final VehiculeDAO vehiculeDAO;
    private final MissionDAO missionDAO;
    private final EntretienDAO entretienDAO;

    public ControllerCout() throws SQLException {
        vehiculeDAO = new VehiculeDAO();
        missionDAO = new MissionDAO();
        entretienDAO = new EntretienDAO();
    }

    @FXML
    public void initialize() {
        LOGGER.info("Initialisation de la page des coûts");

        // Valeurs par défaut
        dateDebut.setValue(LocalDate.now().withDayOfYear(1));
        dateFin.setValue(LocalDate.now());

        dateDebut.valueProperty().addListener((obs, oldDate, newDate) -> updateStats());
        dateFin.valueProperty().addListener((obs, oldDate, newDate) -> updateStats());

        updateStats();
    }

    private void updateStats() {
        try {
            LocalDate debut = dateDebut.getValue();
            LocalDate fin = dateFin.getValue();
            if (debut == null || fin == null || debut.isAfter(fin)) return;

            String date1 = debut.toString();
            String date2 = fin.toString();

            // --- Dépenses ---
            List<Mission> missions = missionDAO.getMissionsEntre(date1, date2);
            List<Entretien> entretiens = entretienDAO.getEntretiensEntre(date1, date2);

            float totalCarburant = 0, totalMission = 0, totalEntretien = 0;
            for (Mission m : missions) {
                totalMission += parseCFA(m.getCout());
                totalCarburant += parseCFA(m.getCoutCarburant());
            }
            for (Entretien e : entretiens) {
                totalEntretien += parseCFA(e.getPrix());
            }

            float total = totalMission + totalCarburant + totalEntretien;

            // KPI total
            labelTotalDepenses.setText(String.format("%.0f FCFA", total));

            // PieChart
            pieChartDepenses.getData().clear();
            pieChartDepenses.getData().add(new PieChart.Data("Carburant", totalCarburant));
            pieChartDepenses.getData().add(new PieChart.Data("Missions", totalMission));
            pieChartDepenses.getData().add(new PieChart.Data("Entretiens", totalEntretien));

            // --- Amortissement ---
            Map<String, Float> amortissementsParAn = new TreeMap<>();
            for (Vehicule v : vehiculeDAO.getAllVehicules()) {
                float annuel = vehiculeDAO.getAmortissementAnnuel(v);
                String annee = LocalDate.parse(v.getDateAchat()).getYear() + "";
                amortissementsParAn.put(annee, amortissementsParAn.getOrDefault(annee, 0f) + annuel);
            }

            float totalAmort = amortissementsParAn.values().stream().reduce(0f, Float::sum);
            labelAmortissementAnnuel.setText(String.format("%.0f FCFA", totalAmort / amortissementsParAn.size()));

            // LineChart
            lineChartAmortissement.getData().clear();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Amortissement annuel");
            amortissementsParAn.forEach((annee, montant) -> {
                series.getData().add(new XYChart.Data<>(annee, montant));
            });
            lineChartAmortissement.getData().add(series);

        } catch (SQLException e) {
            LOGGER.severe("Erreur lors du chargement des données de coûts : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private float parseCFA(String montant) {
        try {
            return Float.parseFloat(montant.replaceAll("[^0-9.]", "").trim());
        } catch (Exception e) {
            return 0;
        }
    }
}
