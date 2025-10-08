package org.openjfx.sio2E4.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.scene.Node;

import org.openjfx.sio2E4.model.LocalUser;

import java.io.IOException;

import org.openjfx.sio2E4.service.AuthService;

public class MainLayoutController {

	@FXML
	private Label usernameLabel;

	@FXML 
	private StackPane contentArea;

	@FXML
	public void initialize() {
		LocalUser user = AuthService.getCurrentUser();
		if (user != null) {
			String role = user.getRole().toUpperCase();
			String nom = user.getNom().toUpperCase();
			String prenom = capitalize(user.getPrenom());

			usernameLabel.setText(role + " - " + nom + " " + prenom);
		} else {
			usernameLabel.setText("Bienvenue invité");
		}
		javafx.application.Platform.runLater(() -> {
			Scene scene = usernameLabel.getScene();
			if (scene != null) {
				scene.getStylesheets()
						.add(getClass().getResource("/org/openjfx/sio2E4/css/AppLayout.css").toExternalForm());
			}
		});

		// Charge la vue par défaut (dashboard)
		showDashboard();
	}

	@FXML
	private void handleLogout() {
		// Réinitialise les infos d'auth (si tu utilises un service d'authentification)
		AuthService.logout();

		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/openjfx/sio2E4/loginPage.fxml"));
			Parent loginRoot = loader.load();

			Stage stage = (Stage) contentArea.getScene().getWindow();

			Scene loginScene = new Scene(loginRoot);

			stage.setScene(loginScene);
			stage.setTitle("Gnotes");

			stage.setResizable(false);


			stage.setWidth(600);
			stage.setHeight(400);
			stage.setMinWidth(600);
			stage.setMinHeight(400);

			stage.centerOnScreen();

			stage.setMaximized(false);

			// Gestionnaire d'événements pour empêcher la réduction de la taille
			stage.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_DRAGGED, event -> {
				// Réajuster les dimensions si elles sont trop petites
				if (stage.getWidth() < 800 || stage.getHeight() < 600) {
					stage.setWidth(600);
					stage.setHeight(400);
				}
			});

			stage.show();
		} catch (Exception e) {
			e.printStackTrace(); // Gérer les exceptions
		}
	}

	public void showDashboard() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/openjfx/sio2E4/view/HomeView.fxml"));

			Node homeView = loader.load();
			contentArea.getChildren().setAll(homeView); // Remplace tout le contenu du StackPane
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	private void showForms() {
		loadView("/org/openjfx/sio2E4/views/formsView.fxml");
	}

	@FXML
	private void showEtudiants() {
		loadView("/org/openjfx/sio2E4/view/EtudiantsView.fxml");
	}
	@FXML
	private void showMatieres() {
		loadView("/org/openjfx/sio2E4/view/MatieresView.fxml");
	}
	
	@FXML
	private void showNotes() {
		loadView("/org/openjfx/sio2E4/view/NoteView.fxml");
	}
	
//----------------------- User Card -----------------------

	
	@FXML
	private void showUsers() {
	    try {
	        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/openjfx/sio2E4/view/UserView.fxml"));
	        Parent usersRoot = loader.load();

	        UsersController usersController = loader.getController();
	        usersController.setMainLayoutController(this);

	        contentArea.getChildren().setAll(usersRoot);
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}


    
  //----------------------- User Card -----------------------

	public void showUserCard(int userId) {
	    Platform.runLater(() -> {
	        try {
	            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/openjfx/sio2E4/view/UserCardView.fxml"));
	            Parent userCardRoot = loader.load();

	            UserCardController controller = loader.getController();
	            controller.loadUser(userId); // charge les infos de l'utilisateur

	            contentArea.getChildren().setAll(userCardRoot); // MAJ de l'UI
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    });
	}



	
	

	private void loadView(String fxmlPath) {
		try {
			Parent view = FXMLLoader.load(getClass().getResource(fxmlPath));
			contentArea.getChildren().setAll(view);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Fonction pour faire du FULL MAJ
	private String capitalize(String str) {
		if (str == null || str.isEmpty())
			return "";
		return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
	}

	// Syle CSS réactif 
	@FXML
	private Button logoutButton;

	@FXML
	private void onLogoutHover() {
		if (logoutButton != null) {
			logoutButton.setStyle("-fx-background-color: #aa2e4a; -fx-text-fill: white;");
		}
	}

	@FXML
	private void onLogoutExit() {
		logoutButton.setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: #333;");
	}
	


}
