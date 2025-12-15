package org.openjfx.sio2E4;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.openjfx.sio2E4.constants.APIConstants;
import org.openjfx.sio2E4.service.ConfIpApi;
import org.openjfx.sio2E4.service.ConfigLoader;
import org.openjfx.sio2E4.service.LocalStorageService;
import org.openjfx.sio2E4.util.AlertHelper;

import java.io.IOException;
import java.lang.module.Configuration;
import java.net.ServerSocket;
/**
 * JavaFX App
 */
public class App extends Application {
    private static ServerSocket lockSocket;
    @Override
    public void start(Stage stage) throws Exception {

        Thread.currentThread().setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                System.err.println("Exception non gérée sur le thread " + t.getName());
                e.printStackTrace();

                // Afficher une alerte utilisateur (doit se faire sur le Thread JavaFX)
                Platform.runLater(() -> {
                    AlertHelper.showError(
                            "Exception non gérée sur le thread " + t.getName(),
                            e.getMessage()); // Méthode à créer pour afficher une fenêtre d'erreur
                });

                // L'application ne s'arrête pas, mais l'erreur est notifiée.
            }
        });


        Parent root = FXMLLoader.load(getClass().getResource("/org/openjfx/sio2E4/loginPage.fxml"));

        Scene scene = new Scene(root);

        stage.setTitle("Gnotes");
        stage.setScene(scene);
        stage.getIcons().add(new Image(App.class.getResourceAsStream("logo.png")));
        stage.setResizable(false);
        stage.show();
    }
    private static void initializeApiConstants() {
        try {
            ConfIpApi config = ConfigLoader.loadYamlConfig();
            String baseUrlFromYaml = config.getApi().getBase_url();

            // Met à jour la constante de base de l'API
            APIConstants.setBaseUrl(baseUrlFromYaml);

            System.out.println("✅ Configuration API chargée : BASE_URL = " + APIConstants.getBaseUrl());

        } catch (IOException e) {
            System.err.println("FATAL: Impossible de charger le fichier de configuration API. " + e.getMessage());
            System.exit(1);
        } catch (NullPointerException e) {
            System.err.println("FATAL: Fichier YAML mal structuré. 'api' ou 'base_url' manquant.");
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        initializeApiConstants();
        try {
            lockSocket = new ServerSocket(9999); // port arbitraire
        } catch (IOException e) {
            System.out.println("⚠️ L'application Gnotes est déjà en cours d'exécution !");
            System.exit(0);
            return; // on quitte sans lancer JavaFX
        }
        try {
            // Initialisation du fichier json pour le mode hors ligne
            LocalStorageService.setup();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        launch(args);
    }
}