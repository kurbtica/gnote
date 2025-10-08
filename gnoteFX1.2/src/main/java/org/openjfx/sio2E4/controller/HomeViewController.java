package org.openjfx.sio2E4.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class HomeViewController {

    @FXML
    private Label welcomeLabel;

    public void initialize() {
        if (welcomeLabel != null) {
            welcomeLabel.setText("Bienvenue dans l'application!");
        } else {
            System.out.println("Le label 'welcomeLabel' est nul.");
        }
    }
}
