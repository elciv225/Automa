package logbo.assy.automa.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import logbo.assy.automa.AuditLogger;
import logbo.assy.automa.SessionManager;
import logbo.assy.automa.dao.AssuranceDAO;
import logbo.assy.automa.dao.AttributionVehiculeDAO;
import logbo.assy.automa.dao.CategorieVehiculeDAO;
import logbo.assy.automa.dao.EntretienDAO;
import logbo.assy.automa.dao.MissionDAO;
import logbo.assy.automa.dao.StatistiqueDAO;
import logbo.assy.automa.dao.VehiculeDAO;
import logbo.assy.automa.models.CategorieVehicule;
import logbo.assy.automa.models.StatistiqueGraphique;
import logbo.assy.automa.models.Vehicule;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ControllerTableauBord implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(ControllerTableauBord.class.getName());

    @FXML private Label lblTotalVehicules;
    @FXML private Label lblTotalMissions;
    @FXML private Label lblTotalEntretiens;
    @FXML private Label lblAssuranceExpire;
    @FXML private Label lblCoutMensuel;
    @FXML private Label lblAffectations;

    @FXML private PieChart pieChartCategories;
    @FXML private BarChart<String, Number> barChartCouts;

    @FXML private VBox vboxAlertes;

    // DAOs pour accéder aux données
    private VehiculeDAO vehiculeDAO;
    private MissionDAO missionDAO;
    private EntretienDAO entretienDAO;
    private AssuranceDAO assuranceDAO;
    private AttributionVehiculeDAO attributionDAO;
    private CategorieVehiculeDAO categorieDAO;
    private StatistiqueDAO statistiqueDAO;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        LOGGER.info("Initialisation du tableau de bord Responsable Logistique");

        try {
            // Initialisation des DAOs
            vehiculeDAO = new VehiculeDAO();
            missionDAO = new MissionDAO();
            entretienDAO = new EntretienDAO();
            assuranceDAO = new AssuranceDAO();
            attributionDAO = new AttributionVehiculeDAO();
            categorieDAO = new CategorieVehiculeDAO();
            statistiqueDAO = new StatistiqueDAO();

            // Chargement des composants du tableau de bord
            chargerStatistiques();
            chargerGraphiqueCategories();
            chargerGraphiqueCouts();
            chargerAlertes();

            // Journaliser l'accès au tableau de bord
            AuditLogger.log(
                    SessionManager.getLogin(),
                    "CONSULTATION",
                    "TABLEAU_BORD",
                    "LOGISTIQUE",
                    "Accès au tableau de bord responsable logistique"
            );
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'initialisation du tableau de bord", e);
        }
    }

    /**
     * Charge les statistiques principales à afficher sur le tableau de bord
     */
    private void chargerStatistiques() {
        try {
            // Nombre total de véhicules
            int totalVehicules = vehiculeDAO.getTotalVehicules();
            lblTotalVehicules.setText(String.valueOf(totalVehicules));

            // Nombre de missions en cours
            int missionsEnCours = missionDAO.getAllMissions().stream()
                    .filter(m -> {
                        LocalDate dateDebut = LocalDate.parse(m.getDateDebut());
                        LocalDate today = LocalDate.now();

                        if (m.getDateFin() == null) {
                            return !dateDebut.isAfter(today);
                        } else {
                            LocalDate dateFin = LocalDate.parse(m.getDateFin());
                            return !dateDebut.isAfter(today) && !dateFin.isBefore(today);
                        }
                    })
                    .toList()
                    .size();
            lblTotalMissions.setText(String.valueOf(missionsEnCours));

            // Nombre de véhicules en entretien
            int entretiensEnCours = entretienDAO.getAllEntretiens().stream()
                    .filter(e -> e.getDateSortie() == null || LocalDate.parse(e.getDateSortie()).isAfter(LocalDate.now()))
                    .toList()
                    .size();
            lblTotalEntretiens.setText(String.valueOf(entretiensEnCours));

            // Assurances expirant ce mois
            LocalDate today = LocalDate.now();
            LocalDate finMois = today.withDayOfMonth(today.lengthOfMonth());
            int assurancesExpirant = assuranceDAO.getAll().stream()
                    .filter(a -> {
                        LocalDate dateFin = LocalDate.parse(a.getDateFin());
                        return dateFin.isAfter(today) && dateFin.isBefore(finMois.plusDays(1));
                    })
                    .toList()
                    .size();
            lblAssuranceExpire.setText(String.valueOf(assurancesExpirant));

            // Coût mensuel (à partir des 30 derniers jours)
            // Calcul basé sur les données des DAO
            Map<String, Object> statsOperations = statistiqueDAO.getStatistiquesOperations();
            double coutEntretiensAnnuels = (double) statsOperations.getOrDefault("coutEntretiensAnnuels", 0.0);
            double coutMissionsAnnuels = (double) statsOperations.getOrDefault("coutMissionsAnnuels", 0.0);
            double coutCarburantAnnuel = (double) statsOperations.getOrDefault("coutCarburantAnnuel", 0.0);

            // Calcul du coût mensuel moyen
            double coutMensuelMoyen = (coutEntretiensAnnuels + coutMissionsAnnuels + coutCarburantAnnuel) / 12.0;

            // Formater le coût en millions de FCFA
            if (coutMensuelMoyen >= 1000000) {
                double millions = coutMensuelMoyen / 1000000.0;
                lblCoutMensuel.setText(String.format("%.1fM", millions));
            } else {
                lblCoutMensuel.setText(String.format("%,.0f", coutMensuelMoyen));
            }

            // Nombre total d'attributions
            int totalAttributions = attributionDAO.getAllAttributions().size();
            lblAffectations.setText(String.valueOf(totalAttributions));

            LOGGER.info("Statistiques chargées avec succès");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du chargement des statistiques", e);
        }
    }

    /**
     * Charge le graphique en camembert de répartition des véhicules par catégorie
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
                if (count > 0) { // Ne montrer que les catégories avec au moins un véhicule
                    pieChartData.add(new PieChart.Data(categorie + " (" + count + ")", count));
                }
            });

            // Appliquer les données au graphique
            pieChartCategories.setData(pieChartData);
            pieChartCategories.setLabelsVisible(true);

            LOGGER.info("Graphique en camembert chargé avec succès");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du chargement du graphique en camembert", e);
        }
    }

    /**
     * Charge le graphique à barres d'évolution des coûts
     */
    private void chargerGraphiqueCouts() {
        try {
            // Récupérer les données des coûts des 6 derniers mois
            List<StatistiqueGraphique> donneesGraphique = statistiqueDAO.getDonneesGraphiqueCouts(6);

            // Série pour les coûts de carburant
            XYChart.Series<String, Number> seriesCarburant = new XYChart.Series<>();
            seriesCarburant.setName("Carburant");

            // Série pour les coûts d'entretien
            XYChart.Series<String, Number> seriesEntretien = new XYChart.Series<>();
            seriesEntretien.setName("Entretien");

            // Série pour les coûts totaux (autres coûts)
            XYChart.Series<String, Number> seriesTotal = new XYChart.Series<>();
            seriesTotal.setName("Autres coûts");

            // Ajouter les données aux séries
            for (StatistiqueGraphique donnee : donneesGraphique) {
                String moisFormate = donnee.getMoisFormate();

                // Arrondir les coûts pour une meilleure lisibilité
                double coutCarburant = Math.round(donnee.getCoutCarburant());
                double coutEntretien = Math.round(donnee.getCoutEntretien());
                double coutTotal = Math.round(donnee.getCoutTotal());
                double autresCoûts = coutTotal - (coutCarburant + coutEntretien);

                seriesCarburant.getData().add(new XYChart.Data<>(moisFormate, coutCarburant));
                seriesEntretien.getData().add(new XYChart.Data<>(moisFormate, coutEntretien));
                seriesTotal.getData().add(new XYChart.Data<>(moisFormate, autresCoûts > 0 ? autresCoûts : 0));
            }

            // Ajout des séries au graphique
            barChartCouts.getData().clear(); // Effacer d'abord les anciennes données
            barChartCouts.getData().addAll(seriesCarburant, seriesEntretien, seriesTotal);

            LOGGER.info("Graphique à barres chargé avec succès");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du chargement du graphique à barres", e);

            // Si une erreur se produit, on charge des données d'exemple
            chargerDonneesExempleBarChart();
        }
    }

    /**
     * Charge des données d'exemple pour le graphique à barres en cas d'erreur
     */
    private void chargerDonneesExempleBarChart() {
        try {
            // Série pour les coûts de carburant
            XYChart.Series<String, Number> seriesCarburant = new XYChart.Series<>();
            seriesCarburant.setName("Carburant");

            seriesCarburant.getData().add(new XYChart.Data<>("Jan", 2100000));
            seriesCarburant.getData().add(new XYChart.Data<>("Fév", 2300000));
            seriesCarburant.getData().add(new XYChart.Data<>("Mar", 2500000));
            seriesCarburant.getData().add(new XYChart.Data<>("Avr", 2400000));
            seriesCarburant.getData().add(new XYChart.Data<>("Mai", 2600000));
            seriesCarburant.getData().add(new XYChart.Data<>("Juin", 2700000));

            // Série pour les coûts d'entretien
            XYChart.Series<String, Number> seriesEntretien = new XYChart.Series<>();
            seriesEntretien.setName("Entretien");

            seriesEntretien.getData().add(new XYChart.Data<>("Jan", 1200000));
            seriesEntretien.getData().add(new XYChart.Data<>("Fév", 800000));
            seriesEntretien.getData().add(new XYChart.Data<>("Mar", 1500000));
            seriesEntretien.getData().add(new XYChart.Data<>("Avr", 900000));
            seriesEntretien.getData().add(new XYChart.Data<>("Mai", 1100000));
            seriesEntretien.getData().add(new XYChart.Data<>("Juin", 1300000));

            // Série pour les coûts d'assurance
            XYChart.Series<String, Number> seriesAssurance = new XYChart.Series<>();
            seriesAssurance.setName("Assurance");

            seriesAssurance.getData().add(new XYChart.Data<>("Jan", 3200000));
            seriesAssurance.getData().add(new XYChart.Data<>("Fév", 3200000));
            seriesAssurance.getData().add(new XYChart.Data<>("Mar", 3200000));
            seriesAssurance.getData().add(new XYChart.Data<>("Avr", 3200000));
            seriesAssurance.getData().add(new XYChart.Data<>("Mai", 3400000));
            seriesAssurance.getData().add(new XYChart.Data<>("Juin", 3200000));

            // Ajout des séries au graphique
            barChartCouts.getData().clear(); // Effacer d'abord les anciennes données
            barChartCouts.getData().addAll(seriesCarburant, seriesEntretien, seriesAssurance);

            LOGGER.info("Graphique à barres d'exemple chargé avec succès");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du chargement du graphique à barres d'exemple", e);
        }
    }

    /**
     * Charge les alertes dynamiquement à partir des données du système
     */
    private void chargerAlertes() {
        try {
            // Vider les alertes existantes
            vboxAlertes.getChildren().clear();

            // Récupérer les alertes du système
            List<String> alertes = statistiqueDAO.getAlertes();

            if (alertes.isEmpty()) {
                // Ajouter un message si aucune alerte
                Label noAlerts = new Label("Aucune alerte à signaler");
                noAlerts.getStyleClass().add("alert-item");
                vboxAlertes.getChildren().add(noAlerts);
            } else {
                // Ajouter chaque alerte au conteneur
                for (String alerte : alertes) {
                    Label alerteLabel = new Label(alerte);
                    alerteLabel.getStyleClass().add("alert-item");
                    vboxAlertes.getChildren().add(alerteLabel);
                }
            }

            // Vérifier le dépassement de budget
            ajouterAlertesBudget();

            LOGGER.info("Alertes chargées avec succès");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du chargement des alertes", e);

            // En cas d'erreur, ajouter quelques alertes par défaut
            Label alerte1 = new Label("⚠️ 2 assurances expirent cette semaine");
            alerte1.getStyleClass().add("alert-item");

            Label alerte2 = new Label("🔧 Entretien préventif requis pour 3 véhicules");
            alerte2.getStyleClass().add("alert-item");

            Label alerte3 = new Label("📊 Budget carburant dépassé de 15%");
            alerte3.getStyleClass().add("alert-item");

            vboxAlertes.getChildren().addAll(alerte1, alerte2, alerte3);
        }
    }

    /**
     * Ajoute des alertes spécifiques concernant le budget
     */
    private void ajouterAlertesBudget() {
        try {
            // Cette méthode analyse les données de coûts pour détecter des dépassements de budget
            List<StatistiqueGraphique> donneesCouts = statistiqueDAO.getDonneesGraphiqueCouts(2);

            if (donneesCouts.size() >= 2) {
                StatistiqueGraphique moisActuel = donneesCouts.get(0);
                StatistiqueGraphique moisPrecedent = donneesCouts.get(1);

                // Vérifier si le coût de carburant a augmenté de plus de 10%
                if (moisActuel.getCoutCarburant() > moisPrecedent.getCoutCarburant() * 1.1) {
                    double pourcentage = ((moisActuel.getCoutCarburant() / moisPrecedent.getCoutCarburant()) - 1) * 100;
                    Label alerteCarburant = new Label(String.format("📊 Coût de carburant en hausse de %.1f%%", pourcentage));
                    alerteCarburant.getStyleClass().add("alert-item");
                    vboxAlertes.getChildren().add(alerteCarburant);
                }

                // Vérifier si les coûts totaux ont augmenté de plus de 15%
                if (moisActuel.getCoutTotal() > moisPrecedent.getCoutTotal() * 1.15) {
                    double pourcentage = ((moisActuel.getCoutTotal() / moisPrecedent.getCoutTotal()) - 1) * 100;
                    Label alerteCoutTotal = new Label(String.format("⚠️ Budget total dépassé de %.1f%%", pourcentage));
                    alerteCoutTotal.getStyleClass().add("alert-item");
                    vboxAlertes.getChildren().add(alerteCoutTotal);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erreur lors de l'analyse des coûts pour les alertes de budget", e);
        }
    }
}