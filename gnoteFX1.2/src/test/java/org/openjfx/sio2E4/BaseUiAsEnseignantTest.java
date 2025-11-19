package org.openjfx.sio2E4;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.openjfx.sio2E4.model.LocalUser;
import org.openjfx.sio2E4.service.AuthService;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationTest;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Classe abstraite de base pour les tests d'interface graphique en mode enseignant.
 *
 * Lors du démarrage du test :
 *  - Un utilisateur "test.prof@lycee.local" (rôle ENSEIGNANT) est automatiquement injecté
 *    dans le service d'authentification via {@link AuthService#setCurrentUser(LocalUser)}.
 *  - La vue "Enseignant.fxml" est directement chargée dans la scène principale.
 *
 * Les classes de test qui étendent {@code BaseUiAsEnseignantTest}
 * peuvent donc interagir immédiatement avec l'interface d'enseignant,
 * sans devoir passer par la page de connexion.
 */
public abstract class BaseUiAsEnseignantTest extends ApplicationTest {

    public void start(Stage stage) throws IOException, TimeoutException {
        FxToolkit.registerPrimaryStage(); // assure isolation de chaque test

        // Simule un enseignant connecté
        LocalUser testUser = new LocalUser(
                "abc",
                0,
                "Test",
                "Enseignant",
                "test.prof@lycee.local",
                "ENSEIGNANT",
                "",
                ""
        );
        AuthService.setCurrentUser(testUser);
        AuthService.setSessionToken(testUser.getToken());

        Parent root = FXMLLoader.load(getClass().getResource("/org/openjfx/sio2E4/layout/Admin.fxml"));

        Scene scene = new Scene(root);

        stage.setTitle("Gnotes");
        stage.setScene(scene);
        stage.getIcons().add(new Image(App.class.getResourceAsStream("logo.png")));
        stage.setMaximized(true);
        stage.setResizable(true);
        stage.show();
    }
}
