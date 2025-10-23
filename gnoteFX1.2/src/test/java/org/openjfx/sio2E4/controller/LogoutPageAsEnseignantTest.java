package org.openjfx.sio2E4.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openjfx.sio2E4.BaseUiAsEnseignantTest;
import org.openjfx.sio2E4.service.AuthService;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.util.WaitForAsyncUtils;

import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(ApplicationExtension.class)
public class LogoutPageAsEnseignantTest extends BaseUiAsEnseignantTest {

    @Test
    public void logout(FxRobot robot){
        // given:
        robot.clickOn("#logoutButton");

        // when/then:
        // Attendre que la nouvelle vue se charge
        sleep(1000);
        WaitForAsyncUtils.waitForFxEvents();

        assertNull(AuthService.getCurrentUser(), "L'enseignant doit être déconnecté");
    }
}
