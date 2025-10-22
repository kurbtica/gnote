package org.openjfx.sio2E4;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.openjfx.sio2E4.service.LocalStorageService;

import java.io.IOException;

/**
 * JavaFX App
 */
public class App extends Application {
	
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/org/openjfx/sio2E4/loginPage.fxml"));

        Scene scene = new Scene(root);

        stage.setTitle("Gnotes");
        stage.setScene(scene);
        stage.getIcons().add(new Image(App.class.getResourceAsStream("logo.png")));
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        try {
            // Initialisation du fichier json pour le mode hors ligne
            LocalStorageService.setup();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        launch(args);
    }
}