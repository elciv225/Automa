package logbo.assy.automa.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import logbo.assy.automa.Main;
import logbo.assy.automa.SessionManager;

import java.io.IOException;
import java.util.HashMap;
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
    public ScrollPane contenantPages;
    @FXML
    public Label nomPrenUser;


    private HBox currentSelected;
    // Mise en cache des vues pour de bonnes performances
    private final Map<String, Parent> vueCachee = new HashMap<>();


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

        routes.values().forEach(path -> {
            try {
                Parent page = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(path)));
                vueCachee.put(path, page);
            } catch (IOException e) {
                LOGGER.warning("Préchargement échoué pour : " + path);
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

        // Ajout du nom et de la photo de l'utilisateur
        nomPrenUser.setText(SessionManager.getPrenom()+" "+SessionManager.getNom().toUpperCase());

        // Chargement par défaut du tableau de bord
        loadLayout(routes.get(linkTableauBord));
        updateSelected(linkTableauBord);

        // Appliquer l'icone des fenêtres
        Platform.runLater(() -> {
            Stage stage = (Stage) linkDeconnexion.getScene().getWindow();
            stage.setTitle("AutoMA - Espace Responsable Logistique");
            Main.appliquerIcon(stage);
        });

    }

    /**
     * Charge et injecte la page dans le VBox sans param événementiel
     */
    private void loadLayout(String layoutPath) {
    new Thread(() -> {
        try {
            Parent page = vueCachee.get(layoutPath);
            if (page == null) {
                page = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(layoutPath)));
                vueCachee.put(layoutPath, page);
            }

            Parent finalPage = page;
            javafx.application.Platform.runLater(() -> contenantPages.setContent(finalPage));

        } catch (IOException ex) {
            LOGGER.severe("Erreur de chargement de " + layoutPath + " : " + ex.getMessage());
        }
    }).start();
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
