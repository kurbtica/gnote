package org.openjfx.sio2E4.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class logoutPageController {
	
	@FXML
    private Button actiontarget;

    @FXML
    private void GoToLog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/openjfx/sio2E4/loginPage.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) actiontarget.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
