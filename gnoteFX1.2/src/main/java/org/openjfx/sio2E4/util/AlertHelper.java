package org.openjfx.sio2E4.util;

import javafx.scene.control.Alert;

public class AlertHelper {
    private static void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void showInformation(String message) {
        showAlert(Alert.AlertType.INFORMATION, message);
    }

    public static void showWarning(String message) {
        showAlert(Alert.AlertType.WARNING, message);
    }

    public static void showError(String message) {
        showAlert(Alert.AlertType.ERROR, message);
    }

    public static void showConfirmation(String message) {
        showAlert(Alert.AlertType.CONFIRMATION, message);
    }
}
