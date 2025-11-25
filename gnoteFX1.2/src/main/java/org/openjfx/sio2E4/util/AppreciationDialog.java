package org.openjfx.sio2E4.util;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.Optional;

/**
 * Small helper to create a nicer-looking appreciation editor dialog.
 */
public class AppreciationDialog {

    public static Optional<String> show(String matiere, String initialValue) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Éditer appréciation");

        ButtonType okButton = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, cancelButton);

        // style class so CSS can target it
        dialog.getDialogPane().getStyleClass().add("appreciation-dialog");

        // Header with a small icon + label
        Label title = new Label("Matière: ");
        title.getStyleClass().add("appreciation-header-title");
        Label subject = new Label(matiere);
        subject.getStyleClass().add("appreciation-header-subject");

        HBox header = new HBox(10, title, subject);
        header.setAlignment(Pos.CENTER_LEFT);

        TextArea textArea = new TextArea(initialValue == null ? "" : initialValue);
        textArea.setWrapText(true);
        textArea.getStyleClass().add("appreciation-textarea");
        textArea.setPrefRowCount(5);

        Label hint = new Label("Saisir une appréciation (max 500 caractères)");
        hint.getStyleClass().add("appreciation-hint");

        Label counter = new Label();
        counter.getStyleClass().add("appreciation-counter");
        counter.setText((initialValue == null ? 0 : initialValue.length()) + "/500");

        textArea.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() > 500) {
                textArea.setText(newVal.substring(0, 500));
            }
            counter.setText(textArea.getText().length() + "/500");
        });

        HBox foot = new HBox(8, hint, counter);
        HBox.setHgrow(hint, Priority.ALWAYS);
        foot.setAlignment(Pos.CENTER_LEFT);

        VBox content = new VBox(12, header, textArea, foot);
        content.setPadding(new Insets(12));

        // Put content on dialog pane
        dialog.getDialogPane().setContent(content);

        // Focus text area when dialog shown
        dialog.setOnShown(ev -> textArea.requestFocus());

        Node okNode = dialog.getDialogPane().lookupButton(okButton);
        if (okNode != null) okNode.getStyleClass().add("ok-button");

        dialog.setResultConverter(btn -> {
            if (btn == okButton) {
                return textArea.getText();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        return result;
    }
}
