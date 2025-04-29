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

import java.net.URL;
import java.util.ResourceBundle;
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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        LOGGER.info("Initialisation du tableau de bord Responsable Logistique");

        // Chargement des statistiques
        loadStatistics();

        // Chargement des graphiques
        loadPieChart();
        loadBarChart();

        // Chargement des alertes
        // Note: Les alertes sont déjà définies statiquement dans le FXML
        // Cette méthode pourrait être utilisée pour charger des alertes dynamiques depuis la base de données
        // loadAlertes();
    }

    private void loadStatistics() {
        try {
            // TODO: Charger les vraies statistiques depuis la base de données
            // Ces valeurs sont des exemples statiques qui doivent être remplacées par des données réelles
            lblTotalVehicules.setText("32");
            lblTotalMissions.setText("14");
            lblTotalEntretiens.setText("8");
            lblAssuranceExpire.setText("3");
            lblCoutMensuel.setText("7.2M");
            lblAffectations.setText("28");

            LOGGER.info("Statistiques chargées avec succès");
        } catch (Exception e) {
            LOGGER.severe("Erreur lors du chargement des statistiques: " + e.getMessage());
        }
    }

    private void loadPieChart() {
        try {
            // Données d'exemple pour le graphique en camembert des catégories de véhicules
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Berlines", 12),
                new PieChart.Data("SUV", 8),
                new PieChart.Data("4x4", 5),
                new PieChart.Data("Utilitaires", 4),
                new PieChart.Data("Camions", 3)
            );

            pieChartCategories.setData(pieChartData);
            pieChartCategories.setTitle("Répartition par catégorie");

            // Ajout de légendes visibles
            pieChartCategories.setLabelsVisible(true);

            LOGGER.info("Graphique en camembert chargé avec succès");
        } catch (Exception e) {
            LOGGER.severe("Erreur lors du chargement du graphique en camembert: " + e.getMessage());
        }
    }

    private void loadBarChart() {
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
            barChartCouts.getData().addAll(seriesCarburant, seriesEntretien, seriesAssurance);
            barChartCouts.setTitle("Évolution des coûts (FCFA)");

            LOGGER.info("Graphique à barres chargé avec succès");
        } catch (Exception e) {
            LOGGER.severe("Erreur lors du chargement du graphique à barres: " + e.getMessage());
        }
    }

    // Cette méthode pourrait être utilisée pour charger des alertes depuis la base de données
    private void loadAlertes() {
        try {
            // TODO: Implémenter le chargement des alertes depuis la base de données
            // Le code ci-dessous est un exemple qui pourrait être utilisé pour ajouter dynamiquement des alertes
            /*
            Label alerte1 = new Label("⚠️ 2 assurances expirent cette semaine");
            alerte1.getStyleClass().add("alert-item");

            Label alerte2 = new Label("🔧 Entretien préventif requis pour 3 véhicules");
            alerte2.getStyleClass().add("alert-item");

            Label alerte3 = new Label("📊 Budget carburant dépassé de 15%");
            alerte3.getStyleClass().add("alert-item");

            vboxAlertes.getChildren().addAll(alerte1, alerte2, alerte3);
            */

            LOGGER.info("Alertes chargées avec succès");
        } catch (Exception e) {
            LOGGER.severe("Erreur lors du chargement des alertes: " + e.getMessage());
        }
    }
}