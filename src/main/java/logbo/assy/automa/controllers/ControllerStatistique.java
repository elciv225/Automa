package logbo.assy.automa.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import logbo.assy.automa.AuditLogger;
import logbo.assy.automa.Main;
import logbo.assy.automa.SessionManager;
import logbo.assy.automa.dao.AssuranceDAO;
import logbo.assy.automa.dao.AttributionVehiculeDAO;
import logbo.assy.automa.dao.CategorieVehiculeDAO;
import logbo.assy.automa.dao.EntretienDAO;
import logbo.assy.automa.dao.MissionDAO;
import logbo.assy.automa.dao.StatistiqueDAO;
import logbo.assy.automa.dao.VehiculeDAO;
import logbo.assy.automa.models.CategorieVehicule;
import logbo.assy.automa.models.Entretien;
import logbo.assy.automa.models.Mission;
import logbo.assy.automa.models.StatistiqueGraphique;
import logbo.assy.automa.models.Vehicule;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ControllerStatistique implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(ControllerStatistique.class.getName());

    @FXML private DatePicker dateDebut;
    @FXML private DatePicker dateFin;
    @FXML private Button btnAppliquer;

    @FXML private PieChart pieChartVehicules;
    @FXML private LineChart<String, Number> lineChartCouts;
    @FXML private BarChart<String, Number> barChartUtilisation;
    @FXML private ComboBox<String> comboFiltre;

    @FXML private Label labelTauxOccupation;
    @FXML private Label labelCoutEntretien;
    @FXML private Label labelMissions;
    @FXML private Label labelDepenses;

    // Les DAO pour accéder aux données
    private VehiculeDAO vehiculeDAO;
    private MissionDAO missionDAO;
    private EntretienDAO entretienDAO;
    private AssuranceDAO assuranceDAO;
    private AttributionVehiculeDAO attributionDAO;
    private CategorieVehiculeDAO categorieDAO;
    private StatistiqueDAO statistiqueDAO;

    // Période de temps par défaut
    private LocalDate periodeDebut;
    private LocalDate periodeFin;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        LOGGER.info("Initialisation de la page de statistiques");

        try {
            // Initialisation des DAOs
            vehiculeDAO = new VehiculeDAO();
            missionDAO = new MissionDAO();
            entretienDAO = new EntretienDAO();
            assuranceDAO = new AssuranceDAO();
            attributionDAO = new AttributionVehiculeDAO();
            categorieDAO = new CategorieVehiculeDAO();
            statistiqueDAO = new StatistiqueDAO();

            // Configuration de la période par défaut (6 derniers mois)
            periodeFin = LocalDate.now();
            periodeDebut = periodeFin.minusMonths(6);

            dateDebut.setValue(periodeDebut);
            dateFin.setValue(periodeFin);

            // Initialisation du ComboBox pour le filtre des véhicules
            comboFiltre.setItems(FXCollections.observableArrayList(
                    "Par marque",
                    "Par catégorie",
                    "Par état",
                    "Par taux d'utilisation"
            ));
            comboFiltre.getSelectionModel().selectFirst();

            // Chargement des graphiques et statistiques
            chargerDonnees();

            // Journaliser l'accès aux statistiques
            AuditLogger.log(
                    SessionManager.getLogin(),
                    "CONSULTATION",
                    "STATISTIQUES",
                    "FLOTTE",
                    "Accès à la page des statistiques de la flotte"
            );

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'initialisation de la page des statistiques", e);
            afficherErreur("Erreur d'initialisation", "Impossible de se connecter à la base de données");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur inattendue", e);
            afficherErreur("Erreur", "Une erreur inattendue s'est produite");
        }
    }

    /**
     * Charge toutes les données pour les graphiques et indicateurs
     */
    private void chargerDonnees() {
        chargerGraphiqueRepartition();
        chargerGraphiqueCouts();
        chargerGraphiqueUtilisation();
        chargerIndicateursPerformance();
    }

    /**
     * Action du bouton "Appliquer" pour filtrer les données selon la période sélectionnée
     */
    @FXML
    private void appliquerFiltre() {
        try {
            // Vérification des dates
            if (dateDebut.getValue() == null || dateFin.getValue() == null) {
                afficherErreur("Dates manquantes", "Veuillez sélectionner une période complète.");
                return;
            }

            if (dateDebut.getValue().isAfter(dateFin.getValue())) {
                afficherErreur("Dates invalides", "La date de début doit être antérieure à la date de fin.");
                return;
            }

            // Mise à jour de la période
            periodeDebut = dateDebut.getValue();
            periodeFin = dateFin.getValue();

            // Vérifier que la période n'est pas trop longue
            long mois = ChronoUnit.MONTHS.between(periodeDebut, periodeFin);
            if (mois > 24) {
                afficherErreur("Période trop longue", "Veuillez sélectionner une période de 24 mois maximum.");
                return;
            }

            // Rechargement des données avec la nouvelle période
            chargerDonnees();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'application du filtre de période", e);
            afficherErreur("Erreur", "Impossible d'appliquer le filtre de période");
        }
    }

    /**
     * Action du combobox pour filtrer le graphique d'utilisation
     */
    @FXML
    private void filtrerUtilisation() {
        try {
            String filtre = comboFiltre.getValue();
            if (filtre == null) {
                return;
            }

            // Recharger le graphique d'utilisation avec le filtre sélectionné
            chargerGraphiqueUtilisation();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'application du filtre d'utilisation", e);
        }
    }

    /**
     * Charge le graphique en camembert de répartition des véhicules
     */
    private void chargerGraphiqueRepartition() {
        try {
            // Récupérer les données des véhicules et catégories
            List<Vehicule> vehicules = vehiculeDAO.getAllVehicules();
            List<CategorieVehicule> categories = categorieDAO.getAllCategories();

            // Map pour stocker les compteurs par catégorie
            Map<String, Integer> countByCategory = new HashMap<>();

            // Initialiser avec toutes les catégories
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
            pieChartVehicules.setData(pieChartData);
            pieChartVehicules.setTitle("Total: " + vehicules.size() + " véhicules");

            LOGGER.info("Graphique de répartition des véhicules chargé avec succès");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du chargement du graphique de répartition", e);
        }
    }

    /**
     * Charge le graphique en ligne d'évolution des coûts
     */
    private void chargerGraphiqueCouts() {
        try {
            // Calculer le nombre de mois entre les deux dates
            long mois = ChronoUnit.MONTHS.between(
                    YearMonth.from(periodeDebut),
                    YearMonth.from(periodeFin)
            ) + 1; // +1 pour inclure le mois courant

            int nombreMois = (int) Math.min(mois, 12); // Limiter à 12 mois max pour le graphique

            // Récupérer les données des coûts
            List<StatistiqueGraphique> donneesCouts = statistiqueDAO.getDonneesGraphiqueCouts(nombreMois);

            // Créer les séries de données
            XYChart.Series<String, Number> seriesEntretien = new XYChart.Series<>();
            seriesEntretien.setName("Entretien");

            XYChart.Series<String, Number> seriesCarburant = new XYChart.Series<>();
            seriesCarburant.setName("Carburant");

            XYChart.Series<String, Number> seriesTotal = new XYChart.Series<>();
            seriesTotal.setName("Total");

            // Remplir les séries avec les données
            for (StatistiqueGraphique stat : donneesCouts) {
                String moisFormate = stat.getMoisFormate();
                seriesEntretien.getData().add(new XYChart.Data<>(moisFormate, stat.getCoutEntretien()));
                seriesCarburant.getData().add(new XYChart.Data<>(moisFormate, stat.getCoutCarburant()));
                seriesTotal.getData().add(new XYChart.Data<>(moisFormate, stat.getCoutTotal()));
            }

            // Appliquer les séries au graphique
            lineChartCouts.getData().clear();
            lineChartCouts.getData().addAll(seriesEntretien, seriesCarburant, seriesTotal);

            LOGGER.info("Graphique des coûts chargé avec succès");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du chargement du graphique des coûts", e);
        }
    }

    /**
     * Charge le graphique à barres d'utilisation des véhicules
     */
    private void chargerGraphiqueUtilisation() {
        try {
            String filtre = comboFiltre.getValue();
            if (filtre == null) {
                filtre = "Par marque"; // Valeur par défaut
            }

            // Récupérer les véhicules
            List<Vehicule> vehicules = vehiculeDAO.getAllVehicules();

            // Récupérer les missions pour la période sélectionnée
            List<Mission> missions = missionDAO.getAllMissions().stream()
                    .filter(m -> {
                        LocalDate dateMission = LocalDate.parse(m.getDateDebut());
                        return !dateMission.isBefore(periodeDebut) && !dateMission.isAfter(periodeFin);
                    })
                    .toList();

            // Map pour compter l'utilisation selon le filtre
            Map<String, Integer> utilisationMap = new HashMap<>();

            switch (filtre) {
                case "Par marque":
                    // Compter les missions par marque de véhicule
                    for (Mission mission : missions) {
                        String idVehicule = mission.getIdVehicule();
                        Vehicule vehicule = vehicules.stream()
                                .filter(v -> v.getIdVehicule().equals(idVehicule))
                                .findFirst()
                                .orElse(null);

                        if (vehicule != null) {
                            String marque = vehicule.getMarque();
                            utilisationMap.put(marque, utilisationMap.getOrDefault(marque, 0) + 1);
                        }
                    }
                    break;

                case "Par catégorie":
                    // Compter les missions par catégorie de véhicule
                    Map<String, String> mapVehiculeToCat = new HashMap<>();
                    for (Vehicule v : vehicules) {
                        mapVehiculeToCat.put(v.getIdVehicule(), v.getIdCategorie());
                    }

                    // Récupérer les catégories pour leurs libellés
                    Map<String, String> mapCatToLib = new HashMap<>();
                    for (CategorieVehicule cat : categorieDAO.getAllCategories()) {
                        mapCatToLib.put(cat.getIdCategorie(), cat.getLibelle());
                    }

                    for (Mission mission : missions) {
                        String idVehicule = mission.getIdVehicule();
                        String idCategorie = mapVehiculeToCat.get(idVehicule);
                        if (idCategorie != null) {
                            String libelle = mapCatToLib.getOrDefault(idCategorie, idCategorie);
                            utilisationMap.put(libelle, utilisationMap.getOrDefault(libelle, 0) + 1);
                        }
                    }
                    break;

                case "Par état":
                    // Compter les véhicules par état (en mission, en entretien, disponible)
                    int enMission = 0;
                    int enEntretien = 0;
                    int disponible = vehicules.size();

                    // Véhicules en mission actuellement
                    LocalDate today = LocalDate.now();
                    for (Mission mission : missionDAO.getAllMissions()) {
                        LocalDate debut = LocalDate.parse(mission.getDateDebut());
                        LocalDate fin = mission.getDateFin() != null ?
                                LocalDate.parse(mission.getDateFin()) : today.plusDays(1);

                        if (!debut.isAfter(today) && !fin.isBefore(today)) {
                            enMission++;
                            disponible--;
                        }
                    }

                    // Véhicules en entretien actuellement
                    for (Entretien entretien : entretienDAO.getAllEntretiens()) {
                        if (entretien.getDateSortie() == null ||
                                LocalDate.parse(entretien.getDateSortie()).isAfter(today)) {
                            enEntretien++;
                            // Pour éviter de compter deux fois un véhicule indisponible
                            if (disponible > 0) disponible--;
                        }
                    }

                    utilisationMap.put("En mission", enMission);
                    utilisationMap.put("En entretien", enEntretien);
                    utilisationMap.put("Disponible", disponible);
                    break;

                case "Par taux d'utilisation":
                    // Calculer le taux d'utilisation de chaque véhicule sur la période
                    Map<String, Integer> joursMission = new HashMap<>();
                    long totalJoursPeriode = ChronoUnit.DAYS.between(periodeDebut, periodeFin) + 1;

                    // Compter les jours de mission pour chaque véhicule
                    for (Mission mission : missions) {
                        String idVehicule = mission.getIdVehicule();
                        LocalDate debut = LocalDate.parse(mission.getDateDebut());
                        LocalDate fin = mission.getDateFin() != null ?
                                LocalDate.parse(mission.getDateFin()) : LocalDate.now();

                        // Ajuster les dates aux limites de la période
                        if (debut.isBefore(periodeDebut)) debut = periodeDebut;
                        if (fin.isAfter(periodeFin)) fin = periodeFin;

                        long jours = ChronoUnit.DAYS.between(debut, fin) + 1;
                        joursMission.put(idVehicule, joursMission.getOrDefault(idVehicule, 0) + (int)jours);
                    }

                    // Calculer les taux d'utilisation par catégories
                    utilisationMap.put("0-25%", 0);
                    utilisationMap.put("26-50%", 0);
                    utilisationMap.put("51-75%", 0);
                    utilisationMap.put("76-100%", 0);

                    for (Vehicule vehicule : vehicules) {
                        int jours = joursMission.getOrDefault(vehicule.getIdVehicule(), 0);
                        double tauxUtilisation = (double) jours / totalJoursPeriode * 100;

                        if (tauxUtilisation <= 25) {
                            utilisationMap.put("0-25%", utilisationMap.get("0-25%") + 1);
                        } else if (tauxUtilisation <= 50) {
                            utilisationMap.put("26-50%", utilisationMap.get("26-50%") + 1);
                        } else if (tauxUtilisation <= 75) {
                            utilisationMap.put("51-75%", utilisationMap.get("51-75%") + 1);
                        } else {
                            utilisationMap.put("76-100%", utilisationMap.get("76-100%") + 1);
                        }
                    }
                    break;
            }

            // Créer la série de données pour le graphique
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Nombre de véhicules");

            // Trier les entrées pour un affichage plus cohérent
            String finalFiltre = filtre;
            List<Map.Entry<String, Integer>> sortedEntries = utilisationMap.entrySet().stream()
                    .sorted((e1, e2) -> {
                        // Tri spécial pour les catégories de taux d'utilisation
                        if (finalFiltre.equals("Par taux d'utilisation")) {
                            return Integer.compare(
                                    getTauxOrdre(e1.getKey()),
                                    getTauxOrdre(e2.getKey())
                            );
                        }
                        // Tri par valeur décroissante pour les autres filtres
                        return -Integer.compare(e1.getValue(), e2.getValue());
                    })
                    .toList();

            // Ajouter les données triées au graphique
            for (Map.Entry<String, Integer> entry : sortedEntries) {
                series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            }

            // Appliquer la série au graphique
            barChartUtilisation.getData().clear();
            barChartUtilisation.getData().add(series);
            barChartUtilisation.setTitle("Utilisation " + filtre.toLowerCase());

            LOGGER.info("Graphique d'utilisation chargé avec succès");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du chargement du graphique d'utilisation", e);
        }
    }

    /**
     * Auxiliaire pour trier les catégories de taux d'utilisation
     */
    private int getTauxOrdre(String categorie) {
        return switch (categorie) {
            case "0-25%" -> 1;
            case "26-50%" -> 2;
            case "51-75%" -> 3;
            case "76-100%" -> 4;
            default -> 5;
        };
    }

    /**
     * Charge les indicateurs clés de performance
     */
    private void chargerIndicateursPerformance() {
        try {
            // Récupérer les données des coûts
            List<StatistiqueGraphique> donneesCouts = statistiqueDAO.getDonneesGraphiqueCouts(12);

            // Récupérer les véhicules et missions
            List<Vehicule> vehicules = vehiculeDAO.getAllVehicules();
            List<Mission> missions = missionDAO.getAllMissions().stream()
                    .filter(m -> {
                        LocalDate dateMission = LocalDate.parse(m.getDateDebut());
                        return !dateMission.isBefore(periodeDebut) && !dateMission.isAfter(periodeFin);
                    })
                    .toList();

            // 1. Taux d'occupation moyen
            // Calculer le nombre de jours d'utilisation pour chaque véhicule
            Map<String, Integer> joursMission = new HashMap<>();
            long totalJoursPeriode = ChronoUnit.DAYS.between(periodeDebut, periodeFin) + 1;

            for (Mission mission : missions) {
                String idVehicule = mission.getIdVehicule();
                LocalDate debut = LocalDate.parse(mission.getDateDebut());
                LocalDate fin = mission.getDateFin() != null ?
                        LocalDate.parse(mission.getDateFin()) : LocalDate.now();

                // Ajuster les dates aux limites de la période
                if (debut.isBefore(periodeDebut)) debut = periodeDebut;
                if (fin.isAfter(periodeFin)) fin = periodeFin;

                long jours = ChronoUnit.DAYS.between(debut, fin) + 1;
                joursMission.put(idVehicule, joursMission.getOrDefault(idVehicule, 0) + (int)jours);
            }

            // Calcul du taux d'occupation moyen
            double sommeOccupation = 0;
            for (Vehicule vehicule : vehicules) {
                int jours = joursMission.getOrDefault(vehicule.getIdVehicule(), 0);
                double tauxOccupation = (double) jours / totalJoursPeriode;
                sommeOccupation += tauxOccupation;
            }

            double tauxOccupationMoyen = vehicules.isEmpty() ?
                    0 : (sommeOccupation / vehicules.size()) * 100;

            labelTauxOccupation.setText(String.format("%.1f%%", tauxOccupationMoyen));

            // 2. Coût moyen d'entretien par mois
            double totalEntretien = 0;
            int nbMois = (int) Math.min(12, ChronoUnit.MONTHS.between(periodeDebut, periodeFin) + 1);

            for (StatistiqueGraphique donnee : donneesCouts) {
                totalEntretien += donnee.getCoutEntretien();
            }

            double coutMoyenEntretien = nbMois > 0 ? totalEntretien / nbMois : 0;

            // Formater le coût
            if (coutMoyenEntretien >= 1000000) {
                double millions = coutMoyenEntretien / 1000000.0;
                labelCoutEntretien.setText(String.format("%.1fM FCFA/mois", millions));
            } else {
                labelCoutEntretien.setText(String.format("%,.0f FCFA/mois", coutMoyenEntretien));
            }

            // 3. Nombre de missions effectuées
            labelMissions.setText(String.valueOf(missions.size()));

            // 4. Dépenses totales
            double depensesTotales = 0;
            for (StatistiqueGraphique donnee : donneesCouts) {
                depensesTotales += donnee.getCoutTotal();
            }

            // Formater les dépenses
            if (depensesTotales >= 1000000) {
                double millions = depensesTotales / 1000000.0;
                labelDepenses.setText(String.format("%.1fM FCFA", millions));
            } else {
                labelDepenses.setText(String.format("%,.0f FCFA", depensesTotales));
            }

            LOGGER.info("Indicateurs de performance chargés avec succès");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du chargement des indicateurs de performance", e);
        }
    }

    /**
     * Affiche une boîte de dialogue d'erreur
     */
    private void afficherErreur(String titre, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        Main.appliquerIconAlert(alert);
        alert.showAndWait();
    }
}