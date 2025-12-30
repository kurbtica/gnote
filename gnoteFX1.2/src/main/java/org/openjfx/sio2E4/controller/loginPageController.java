package org.openjfx.sio2E4.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.openjfx.sio2E4.constants.StyleConstants;

import org.openjfx.sio2E4.model.User;
import org.openjfx.sio2E4.service.AuthService;
import org.openjfx.sio2E4.service.LocalStorageService;
import org.openjfx.sio2E4.service.NetworkService;
import org.openjfx.sio2E4.util.AlertHelper;

import java.util.ArrayList;

public class loginPageController {

	@FXML
	private TextField usernameField;

	@FXML
	private PasswordField passwordField;

	@FXML
	private Label errorLabel;

	@FXML
	private Button loginButton;

	@FXML
	private void GoToHome() {
		String email = usernameField.getText();
		String password = passwordField.getText();

        ArrayList<User> localUsers = LocalStorageService.loadUsers();
        if (localUsers.isEmpty() && !NetworkService.isOnline()) {
            AlertHelper.showWarning("Connection API", "Connection impossible, l'API ne répond pas et aucune donnée en local n'est enregistrée.");
            return;
        }

		// Vérifie si l'utilisateur est authentifié
		if (AuthService.login(email, password)) {
			// Récupère l'utilisateur connecté
			User currentUser = AuthService.getCurrentUser();

			// Affiche les informations de l'utilisateur (facultatif pour débogage)
			System.out.println("Token: " + AuthService.getToken());
			System.out.println("User: " + currentUser.getNom() + " " + currentUser.getPrenom());
			System.out.println("Role: " + currentUser.getRole().getName());

			// Redirige en fonction du rôle
			try {
				FXMLLoader loader;
				Stage stage = (Stage) loginButton.getScene().getWindow();
				Scene scene;

				switch (currentUser.getRole().getName()) {
					case "ADMIN":
						loader = new FXMLLoader(getClass().getResource("/org/openjfx/sio2E4/layout/Admin.fxml"));
						Parent adminRoot = loader.load();
						scene = new Scene(adminRoot);
						break;

					case "ENSEIGNANT":
						loader = new FXMLLoader(getClass().getResource("/org/openjfx/sio2E4/layout/Enseignant.fxml"));
						Parent enseignantRoot = loader.load();
						scene = new Scene(enseignantRoot);
						break;

					default:
						errorLabel.setText("Accès refusé. Vous n'avez pas les droits nécessaires.");
						errorLabel.setStyle(StyleConstants.ERROR_LABEL_STYLE);
						return;
				}

				stage.setScene(scene);
				stage.setMaximized(true);
				stage.setResizable(true);
				stage.show();

			} catch (Exception e) {
				e.printStackTrace();
				errorLabel.setText("Erreur lors du chargement de la page.");
				errorLabel.setStyle(StyleConstants.ERROR_LABEL_STYLE);
			}
		} else {
			// Affiche un message d'erreur si l'authentification échoue
			errorLabel.setText("Email ou mot de passe incorrect !");
			errorLabel.setStyle(StyleConstants.ERROR_LABEL_STYLE);
		}
	}
}
