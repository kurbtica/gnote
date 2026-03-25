package org.openjfx.sio2E4.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.util.Optional;

public class AlertHelper {
    private static Optional<ButtonType> showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait();
    }

    private static Optional<ButtonType> showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait();
    }

    public static Optional<ButtonType> showInformation(String message) {
        return showAlert(Alert.AlertType.INFORMATION, message);
    }

    public static Optional<ButtonType> showInformation(String title, String message) {
        return showAlert(Alert.AlertType.INFORMATION, title, message);
    }

    public static Optional<ButtonType> showWarning(String message) {
        return showAlert(Alert.AlertType.WARNING, message);
    }

    public static Optional<ButtonType> showWarning(String title, String message) {
        return showAlert(Alert.AlertType.WARNING, title, message);
    }

    public static Optional<ButtonType> showError(String message) {
        return showAlert(Alert.AlertType.ERROR, message);
    }

    public static Optional<ButtonType> showError(String title, String message) {
        return showAlert(Alert.AlertType.ERROR, title, message);
    }

    public static Optional<ButtonType> showConfirmation(String message) {
        return showAlert(Alert.AlertType.CONFIRMATION, message);
    }

    public static Optional<ButtonType> showConfirmation(String title, String message) {
        return showAlert(Alert.AlertType.CONFIRMATION, title, message);
    }
}
