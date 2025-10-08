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
import org.openjfx.sio2E4.service.AuthService;
import org.openjfx.sio2E4.model.LocalUser;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

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

		// Vérifie si l'utilisateur est authentifié
		if (AuthService.login(email, password)) {
			// Récupère l'utilisateur connecté
			LocalUser currentUser = AuthService.getCurrentUser();

			// Affiche les informations de l'utilisateur (facultatif pour débogage)
			System.out.println("Token: " + currentUser.getToken());
			System.out.println("User: " + currentUser.getNom() + " " + currentUser.getPrenom());
			System.out.println("Role: " + currentUser.getRole());

			// Redirige en fonction du rôle
			try {
				FXMLLoader loader;
				Stage stage = (Stage) loginButton.getScene().getWindow();
				Scene scene;

				switch (currentUser.getRole()) {
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
						errorLabel.setStyle("-fx-text-fill: red;");
						return;
				}

				stage.setScene(scene);
				stage.setMaximized(true);
				stage.setResizable(true);
				stage.show();

			} catch (Exception e) {
				e.printStackTrace();
				errorLabel.setText("Erreur lors du chargement de la page.");
				errorLabel.setStyle("-fx-text-fill: red;");
			}
		} else {
			// Affiche un message d'erreur si l'authentification échoue
			errorLabel.setText("Email ou mot de passe incorrect !");
			errorLabel.setStyle("-fx-text-fill: red;");
		}
	}
}
