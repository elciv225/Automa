package logbo.assy.automa.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

public class ControllerResponsableLogistique {
    private static final Logger LOGGER = Logger.getLogger(ControllerResponsableLogistique.class.getName());
    @FXML
    public HBox linkDeconnexion;
    @FXML
    public HBox linkCout;
    @FXML
    public HBox linkStatistique;
    @FXML
    public HBox linkEntretien;
    @FXML
    public HBox linkAssurance;
    @FXML
    public HBox linkMission;
    @FXML
    public HBox linkAffectation;
    @FXML
    public HBox linkVehicule;
    @FXML
    public HBox linkTableauBord;
    @FXML
    public HBox linkCompte;
    @FXML
    public VBox contenantPages;

    private HBox currentSelected;

    @FXML
    public void initialize() {
        LOGGER.info("Initialisation de la page responsable logistique");
        // Mappe chaque HBox sur son FXML
        Map<HBox, String> routes = Map.of(
                linkCout, "/layouts/cout.fxml",
                linkStatistique, "/layouts/statistique.fxml",
                linkEntretien, "/layouts/entretien.fxml",
                linkAssurance, "/layouts/assurance.fxml",
                linkMission, "/layouts/mission.fxml",
                linkAffectation, "/layouts/affectation.fxml",
                linkVehicule, "/layouts/vehicule.fxml",
                linkTableauBord, "/layouts/tableauBord.fxml",
                linkCompte, "/layouts/compte.fxml"
        );

        // Traitement du linkDeconnexion : redirection vers la vue d'authentification
        linkDeconnexion.setCursor(Cursor.HAND);
        linkDeconnexion.setOnMouseClicked(_ -> {
            try {
                Parent authRoot = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/authentification.fxml")));
                Stage stage = (Stage) linkDeconnexion.getScene().getWindow();
                stage.setScene(new Scene(authRoot));
            } catch (IOException ex) {
                LOGGER.severe("Erreur chargement page authentification : " + ex.getMessage());
            }
        });

        // Bind de chaque HBox : curseur, clic et style
        routes.forEach((hbox, fxmlPath) -> {
            hbox.setCursor(Cursor.HAND);
            hbox.setOnMouseClicked(_ -> {
                loadLayout(fxmlPath);
                updateSelected(hbox);
            });
        });

        // Chargement par défaut du tableau de bord
        loadLayout(routes.get(linkTableauBord));
        updateSelected(linkTableauBord);
    }

    /**
     * Charge et injecte la page dans le VBox sans param événementiel
     */
    private void loadLayout(String layoutPath) {
        try {
            Parent page = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(layoutPath)));
            contenantPages.getChildren().setAll(page);
        } catch (IOException ex) {
            LOGGER.severe("Erreur de chargement de " + layoutPath + " : " + ex.getMessage());
        }
    }

    /**
     * Met à jour le style pour le lien sélectionné
     */
    private void updateSelected(HBox selected) {
        if (currentSelected != null) {
            currentSelected.getStyleClass().remove("selected");
        }
        selected.getStyleClass().add("selected");
        currentSelected = selected;
    }
}
