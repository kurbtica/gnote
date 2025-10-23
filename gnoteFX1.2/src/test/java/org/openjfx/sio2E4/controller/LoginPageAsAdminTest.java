package org.openjfx.sio2E4.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openjfx.sio2E4.App;

import org.openjfx.sio2E4.model.LocalUser;
import org.openjfx.sio2E4.service.AuthService;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(ApplicationExtension.class)
class LoginPageAsAdminTest extends ApplicationTest {

    @Override
    public void start(Stage stage) throws IOException, TimeoutException {
        FxToolkit.registerPrimaryStage(); // assure isolation de chaque test

        // Simule un admin connecté
        LocalUser testUser = new LocalUser(
                "abc",
                0,
                "Test",
                "Admin",
                "test.admin@lycee.local",
                "ADMIN",
                "",
                ""
        );
        AuthService.setCurrentUser(testUser);

        Parent root = FXMLLoader.load(getClass().getResource("/org/openjfx/sio2E4/loginPage.fxml"));

        Scene scene = new Scene(root);

        stage.setTitle("Gnotes");
        stage.setScene(scene);
        stage.getIcons().add(new Image(App.class.getResourceAsStream("logo.png")));
        stage.setMaximized(false);
        stage.setResizable(false);
        stage.show();
    }

    @Test
    public void login(FxRobot robot) {
        // given:
        clickOn("#usernameField").write("test.admin@lycee.local");
        clickOn("#passwordField").write("1234");
        robot.clickOn("#loginButton");

        // when/then:
        // Attendre que la nouvelle vue se charge
        sleep(1000);
        WaitForAsyncUtils.waitForFxEvents();

        assertNotNull(AuthService.getCurrentUser(), "L'administrateur doit être connecté");
        assertEquals("test.admin@lycee.local", AuthService.getCurrentUser().getEmail());
        assertEquals("ADMIN", AuthService.getCurrentUser().getRole());

    }
  
}