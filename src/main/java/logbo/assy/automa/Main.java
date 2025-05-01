package logbo.assy.automa;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main extends Application {

    public static final String ICON_PATH = "/images/icon.png";
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    /**
     * Applique l'icône de l'application à une fenêtre JavaFX.
     */
    public static void appliquerIcon(Stage stage) {
        try {
            Image icon = new Image(Objects.requireNonNull(Main.class.getResourceAsStream(ICON_PATH)));
            stage.getIcons().add(icon);
            LOGGER.fine("Icône appliquée au stage : " + ICON_PATH);
        } catch (Exception e) {
            LOGGER.warning("Impossible de charger l'icône : " + e.getMessage());
        }
    }

    /**
     * Point d'entrée principal de l'application JavaFX.
     */
    @Override
    public void start(Stage stage) {
        LOGGER.info("Initialisation de l'application AutoMA");

        try {
            LOGGER.fine("Chargement du splash screen");
            FXMLLoader splashLoader = new FXMLLoader(getClass().getResource("/splash.fxml"));
            Scene splashScene = new Scene(splashLoader.load(), 400, 300);
            Stage splashStage = new Stage();
            splashStage.setScene(splashScene);
            splashStage.setTitle("Chargement de l'application...");
            splashStage.setResizable(false);
            appliquerIcon(splashStage);
            splashStage.show();

            // Chargement de l'interface principale dans un virtual thread
            Thread.startVirtualThread(() -> {
                try {
                    Thread.sleep(2000); // simulation du chargement

                    Platform.runLater(() -> {
                        try {
                            LOGGER.info("Chargement de l'interface principale");
                            FXMLLoader mainLoader = new FXMLLoader(getClass().getResource("/responsableLogistique.fxml"));
                            Scene mainScene = new Scene(mainLoader.load());

                            stage.setTitle("AutoMA - Gestion Responsable Logistique");
                            appliquerIcon(stage);
                            stage.setScene(mainScene);
                            stage.show();
                            splashStage.close();
                            LOGGER.info("Interface principale affichée avec succès");
                        } catch (IOException e) {
                            LOGGER.log(Level.SEVERE, "Échec du chargement de l'interface principale", e);
                        }
                    });

                } catch (InterruptedException e) {
                    LOGGER.log(Level.SEVERE, "Virtual thread interrompu", e);
                    Thread.currentThread().interrupt();
                }
            });

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Échec du chargement du splash screen", e);
        }
    }

    /**
     * Lancement de l'application JavaFX.
     */
    public static void main(String[] args) {
        LOGGER.info("Lancement du moteur JavaFX");
        launch();
    }
}