package org.openjfx.sio2E4.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.beans.property.SimpleStringProperty;
import org.openjfx.sio2E4.model.LocalUser;
import org.openjfx.sio2E4.service.AuthService;

public class LocalUserController {

    @FXML
    private TableView<LocalUser> localUserTable;
    @FXML
    private TableColumn<LocalUser, String> nomColumn;
    @FXML
    private TableColumn<LocalUser, String> prenomColumn;
    @FXML
    private TableColumn<LocalUser, String> emailColumn;
    @FXML
    private TableColumn<LocalUser, String> telephoneColumn;
    @FXML
    private TableColumn<LocalUser, String> adresseColumn;
    @FXML
    private TableColumn<LocalUser, String> roleColumn;
    @FXML
    private Label errorLabel;

    @FXML
    public void initialize() {
        nomColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNom()));
        prenomColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPrenom()));
        emailColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
        telephoneColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTelephone()));
        adresseColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAdresse()));
        roleColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRole()));

        // Charger l'utilisateur local
        loadLocalUser();
    }

    private void loadLocalUser() {
        LocalUser currentUser = AuthService.getCurrentUser();
        if (currentUser != null) {
            // Mettre à jour la table avec les données de l'utilisateur local
            localUserTable.getItems().add(currentUser);
        } else {
            errorLabel.setText("Aucun utilisateur local trouvé.");
            errorLabel.setStyle("-fx-text-fill: red;");
        }
    }
}
