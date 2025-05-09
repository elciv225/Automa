package logbo.assy.automa.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import logbo.assy.automa.AuditLogger;
import logbo.assy.automa.SessionManager;
import logbo.assy.automa.dao.AuditLogDAO;
import logbo.assy.automa.dao.CategorieVehiculeDAO;
import logbo.assy.automa.dao.PersonnelDAO;
import logbo.assy.automa.dao.ServiceDAO;
import logbo.assy.automa.dao.VehiculeDAO;
import logbo.assy.automa.models.AuditLog;
import logbo.assy.automa.models.CategorieVehicule;
import logbo.assy.automa.models.Vehicule;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ControllerTableauBordAdmin implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(ControllerTableauBordAdmin.class.getName());

    @FXML
    private Label lblTotalVehicules;

    @FXML
    private Label lblTotalPersonnels;

    @FXML
    private Label lblTotalServices;

    @FXML
    private Label lblTotalUsers;

    @FXML
    private PieChart pieChart;

    @FXML
    private BarChart<String, Number> barChart;

    private VehiculeDAO vehiculeDAO;
    private PersonnelDAO personnelDAO;
    private ServiceDAO serviceDAO;
    private CategorieVehiculeDAO categorieDAO;
    private AuditLogDAO auditLogDAO;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            // Initialiser les DAO
            vehiculeDAO = new VehiculeDAO();
            personnelDAO = new PersonnelDAO();
            serviceDAO = new ServiceDAO();
            categorieDAO = new CategorieVehiculeDAO();
            auditLogDAO = new AuditLogDAO();

            // Charger les statistiques
            chargerStatistiques();

            // Charger les graphiques
            chargerGraphiqueCategories();
            chargerGraphiqueActivite();

            // Journaliser l'accès au tableau de bord
            AuditLogger.log(
                    SessionManager.getLogin(),
                    "CONSULTATION",
                    "TABLEAU_BORD",
                    "ADMIN",
                    "Accès au tableau de bord administrateur"
            );

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'initialisation du tableau de bord", e);
        }
    }

    /**
     * Charge les statistiques principales
     */
    private void chargerStatistiques() {
        try {
            // Nombre total de véhicules
            int totalVehicules = vehiculeDAO.getTotalVehicules();
            lblTotalVehicules.setText(String.valueOf(totalVehicules));

            // Nombre total de personnel
            int totalPersonnels = personnelDAO.getAllPersonnel().size();
            lblTotalPersonnels.setText(String.valueOf(totalPersonnels));

            // Nombre total de services
            int totalServices = serviceDAO.getAllServices().size();
            lblTotalServices.setText(String.valueOf(totalServices));

            // Nombre d'utilisateurs (personnel avec login)
            int totalUsers = personnelDAO.getAllPersonnel().stream()
                    .filter(p -> p.getLogin() != null && !p.getLogin().isEmpty())
                    .toList().size();
            lblTotalUsers.setText(String.valueOf(totalUsers));

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du chargement des statistiques", e);
        }
    }

    /**
     * Charge le graphique de répartition par catégorie
     */
    private void chargerGraphiqueCategories() {
        try {
            // Récupérer les catégories
            List<CategorieVehicule> categories = categorieDAO.getAllCategories();
            // Récupérer tous les véhicules
            List<Vehicule> vehicules = vehiculeDAO.getAllVehicules();

            // Compter le nombre de véhicules par catégorie
            Map<String, Integer> countByCategory = new HashMap<>();

            // Initialiser le compteur pour chaque catégorie
            for (CategorieVehicule cat : categories) {
                countByCategory.put(cat.getLibelle(), 0);
            }

            // Compter les véhicules par catégorie
            for (Vehicule vehicule : vehicules) {
                String idCategorie = vehicule.getIdCategorie();
                for (CategorieVehicule cat : categories) {
                    if (cat.getIdCategorie().equals(idCategorie)) {
                        String libelle = cat.getLibelle();
                        countByCategory.put(libelle, countByCategory.getOrDefault(libelle, 0) + 1);
                        break;
                    }
                }
            }

            // Créer les données pour le PieChart
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
            countByCategory.forEach((categorie, count) -> {
                pieChartData.add(new PieChart.Data(categorie + " (" + count + ")", count));
            });

            // Appliquer les données au graphique
            pieChart.setData(pieChartData);
            pieChart.setTitle("Répartition des véhicules par catégorie");

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du chargement du graphique de catégories", e);
        }
    }

    /**
     * Charge le graphique d'activité système mensuelle
     */
    private void chargerGraphiqueActivite() {
        try {
            // Créer une série pour les données
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Nombre d'actions");

            // Définir la période pour les 6 derniers mois
            LocalDate today = LocalDate.now();
            LocalDate sixMonthsAgo = today.minusMonths(6);

            // Récupérer les logs d'audit pour la période
            List<AuditLog> logs = auditLogDAO.getLogs(sixMonthsAgo, today, null);

            // Compter le nombre d'actions par mois
            Map<String, Integer> actionsPerMonth = new HashMap<>();
            DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM yyyy");

            // Initialiser tous les mois dans la période
            for (int i = 0; i < 6; i++) {
                LocalDate month = today.minusMonths(i);
                actionsPerMonth.put(month.format(monthFormatter), 0);
            }

            // Compter les actions par mois
            for (AuditLog log : logs) {
                if (log.getDateAction() != null) {
                    String monthYear = log.getDateAction().toLocalDate().format(monthFormatter);
                    actionsPerMonth.put(monthYear, actionsPerMonth.getOrDefault(monthYear, 0) + 1);
                }
            }

            // Ajouter les données à la série en ordre chronologique
            for (int i = 5; i >= 0; i--) {
                LocalDate month = today.minusMonths(i);
                String monthKey = month.format(monthFormatter);
                series.getData().add(new XYChart.Data<>(monthKey, actionsPerMonth.getOrDefault(monthKey, 0)));
            }

            // Configurer le graphique
            barChart.getData().add(series);
            barChart.setTitle("Activité système des 6 derniers mois");

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du chargement du graphique d'activité", e);
        }
    }
}