package org.openjfx.sio2E4.controller;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;

import org.openjfx.sio2E4.model.Role;
import org.openjfx.sio2E4.model.User;
import org.openjfx.sio2E4.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;

public class UsersController {

	@FXML
	private TableView<User> usersTable;
	@FXML
	private TableColumn<User, String> nomColumn;
	@FXML
	private TableColumn<User, String> prenomColumn;
	@FXML
	private TableColumn<User, String> emailColumn;
	@FXML
	private TableColumn<User, String> telephoneColumn;
	@FXML
	private TableColumn<User, String> adresseColumn;
	@FXML
	private TableColumn<User, String> roleColumn;


	private final String API_URL = "http://localhost:8080/api/users";
	private final String BEARER_TOKEN = "Bearer " + AuthService.getToken();

	@FXML
	private ComboBox<String> roleComboBox;

	
	private void showAlert(AlertType type, String message) {
		Alert alert = new Alert(type);
		alert.setTitle("Information");
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}

	private void clearForm() {
		nomField.clear();
		prenomField.clear();
		emailField.clear();
		adresseField.clear();
		telephoneField.clear();
		passwordField.clear();
		roleComboBox.getSelectionModel().selectFirst();
	}
	
	
	@FXML
	public void initialize() {
		roleComboBox.getItems().addAll("ADMIN", "ENSEIGNANT", "ETUDIANT");

		nomColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNom()));
		prenomColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPrenom()));
		emailColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
		telephoneColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTelephone()));
		adresseColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAdresse()));
		roleColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRole().getLibelle()));

		
		fetchUsers();
	}

	private void fetchUsers() {
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(API_URL)).header("Authorization", BEARER_TOKEN)
				.GET().build();

		client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(HttpResponse::body)
				.thenAccept(this::parseUsers).exceptionally(e -> {
					e.printStackTrace();
					return null;
				});
	}

	private void parseUsers(String responseBody) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			List<User> users = Arrays.asList(mapper.readValue(responseBody, User[].class));

			// Assurer que la mise à jour du tableau se fait sur le thread principal JavaFX
			Platform.runLater(() -> usersTable.getItems().setAll(users));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

// -------------------- user Card --------------------
	private MainLayoutController mainLayoutController;

	public void setMainLayoutController(MainLayoutController controller) {
	    this.mainLayoutController = controller;
	}
	@FXML
	private void handleShowUserCard() {
	    User selectedUser = usersTable.getSelectionModel().getSelectedItem();
	    if (selectedUser != null && mainLayoutController != null) {
	        mainLayoutController.showUserCard(selectedUser.getId());
	    }
	}

	
	
	
	
	// Info de formulaire
	@FXML
	private TextField nomField;
	@FXML
	private TextField prenomField;
	@FXML
	private TextField emailField;
	@FXML
	private TextField telephoneField;
	@FXML
	private TextField adresseField;
	@FXML
	private PasswordField passwordField;
	@FXML
	private TextField roleField;
	@FXML
	private Button ajouterButton;

	@FXML
	private void ajouterUtilisateur() {
		try {
			String nom = nomField.getText().trim();
			String prenom = prenomField.getText().trim();
			String email = emailField.getText().trim();
			String adresse = adresseField.getText().trim();
			String telephone = telephoneField.getText().trim();
			String password = passwordField.getText();
			String selectedRole = roleComboBox.getValue();
			int roleId = getRoleId(selectedRole);

			if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || password.isEmpty()) {
				showAlert(AlertType.WARNING, "Veuillez remplir tous les champs obligatoires.");
				return;
			}

			// Construction du JSON
			String json = String.format(
					"{\"nom\":\"%s\",\"prenom\":\"%s\",\"email\":\"%s\",\"adresse\":\"%s\",\"telephone\":\"%s\",\"passwordHash\":\"%s\",\"role\":{\"id\":%d,\"libelle\":\"%s\"}}",
					nom, prenom, email, adresse, telephone, password, roleId, selectedRole);

			HttpClient client = HttpClient.newHttpClient();
			HttpRequest request = HttpRequest.newBuilder().uri(URI.create(API_URL))
					.header("Authorization", BEARER_TOKEN).header("Content-Type", "application/json")
					.POST(BodyPublishers.ofString(json)).build();

			client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenAccept(response -> {
				if (response.statusCode() == 201 || response.statusCode() == 200) {
					Platform.runLater(() -> showAlert(AlertType.INFORMATION, "Utilisateur ajouté avec succès !"));
					clearForm();
					Platform.runLater(this::fetchUsers);
					// Optionnel : refreshTable(); si tu veux actualiser la liste
				} else {
					Platform.runLater(() -> showAlert(AlertType.ERROR, "Erreur lors de l'ajout : " + response.body()));
				}
			}).exceptionally(e -> {
				e.printStackTrace();
				Platform.runLater(() -> showAlert(AlertType.ERROR, "Erreur réseau : " + e.getMessage()));
				return null;
			});

		} catch (Exception e) {
			e.printStackTrace();
			showAlert(AlertType.ERROR, "Erreur interne : " + e.getMessage());
		}
	}

	private int getRoleId(String roleName) {
		switch (roleName) {
		case "ADMIN":
			return 1;
		case "ENSEIGNANT":
			return 2;
		case "ETUDIANT":
			return 3;
		default:
			return 3;
		}
	}



	@FXML
	private void handleDeleteUser() {
		// Récupérer l'utilisateur sélectionné dans le tableau
		User selectedUser = usersTable.getSelectionModel().getSelectedItem();

		if (selectedUser == null) {
			showAlert(AlertType.WARNING, "Veuillez sélectionner un utilisateur à supprimer.");
			return;
		}

		// Supprimer l'utilisateur immédiatement
		deleteUser(selectedUser.getId());
	}

	private void deleteUser(int userId) {
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(API_URL + "/" + userId))
				.header("Authorization", BEARER_TOKEN).DELETE().build();

		client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenAccept(response -> {
			if (response.statusCode() == 204) { // 204 No Content signifie que la suppression a réussi
				Platform.runLater(() -> {
					showAlert(AlertType.INFORMATION, "Utilisateur supprimé avec succès.");
					fetchUsers(); // Rafraîchit la liste des utilisateurs
				});
			} else {
				Platform.runLater(() -> showAlert(AlertType.ERROR, "Erreur lors de la suppression de l'utilisateur."));
			}
		}).exceptionally(e -> {
			e.printStackTrace();
			Platform.runLater(() -> showAlert(AlertType.ERROR, "Erreur réseau : " + e.getMessage()));
			return null;
		});
	}

	// --------------------------Modification de l'utilisateur par boite de dialogue

	@FXML
	private void handleEditUser() {
		User selectedUser = usersTable.getSelectionModel().getSelectedItem();
		if (selectedUser == null) {
			showAlert(AlertType.WARNING, "Veuillez sélectionner un utilisateur à modifier.");
			return;
		}
		showEditDialog(selectedUser);
	}

	private void updateUser(User user) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			String json = mapper.writeValueAsString(user);

			HttpRequest request = HttpRequest.newBuilder().uri(URI.create(API_URL + "/" + user.getId()))
					.header("Authorization", BEARER_TOKEN).header("Content-Type", "application/json")
					.PUT(HttpRequest.BodyPublishers.ofString(json)).build();

			HttpClient client = HttpClient.newHttpClient();
			client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenAccept(response -> {
				if (response.statusCode() == 200) {
					Platform.runLater(() -> {
						showAlert(AlertType.INFORMATION, "Utilisateur modifié avec succès !");
						fetchUsers();
					});
				} else {
					Platform.runLater(() -> showAlert(AlertType.ERROR, "Erreur de modification : " + response.body()));
				}
			}).exceptionally(e -> {
				e.printStackTrace();
				Platform.runLater(() -> showAlert(AlertType.ERROR, "Erreur réseau : " + e.getMessage()));
				return null;
			});
		} catch (Exception e) {
			e.printStackTrace();
			showAlert(AlertType.ERROR, "Erreur lors de la mise à jour.");
		}
	}

	private void showEditDialog(User user) {
		Dialog<User> dialog = new Dialog<>();
		dialog.setTitle("Modifier l'utilisateur");
		dialog.setHeaderText("Modifiez les informations de l'utilisateur :");

		// Boutons OK / Annuler
		ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

		// Création du formulaire
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 150, 10, 10));

		TextField nomField = new TextField(user.getNom());
		TextField prenomField = new TextField(user.getPrenom());
		TextField emailField = new TextField(user.getEmail());
		TextField adresseField = new TextField(user.getAdresse());
		TextField telephoneField = new TextField(user.getTelephone());
		ComboBox<String> roleBox = new ComboBox<>();
		roleBox.getItems().addAll("ADMIN", "ENSEIGNANT", "ETUDIANT");
		roleBox.setValue(user.getRole().getLibelle());

		grid.add(new Label("Nom:"), 0, 0);
		grid.add(nomField, 1, 0);
		grid.add(new Label("Prénom:"), 0, 1);
		grid.add(prenomField, 1, 1);
		grid.add(new Label("Email:"), 0, 2);
		grid.add(emailField, 1, 2);
		grid.add(new Label("Adresse:"), 0, 3);
		grid.add(adresseField, 1, 3);
		grid.add(new Label("Téléphone:"), 0, 4);
		grid.add(telephoneField, 1, 4);
		grid.add(new Label("Rôle:"), 0, 5);
		grid.add(roleBox, 1, 5);

		dialog.getDialogPane().setContent(grid);

		// Convertir le résultat quand l'utilisateur clique sur Enregistrer
		dialog.setResultConverter(dialogButton -> {
			if (dialogButton == saveButtonType) {
				user.setNom(nomField.getText());
				user.setPrenom(prenomField.getText());
				user.setEmail(emailField.getText());
				user.setAdresse(adresseField.getText());
				user.setTelephone(telephoneField.getText());
				String selectedRole = roleBox.getValue();
				int roleId = getRoleId(selectedRole);

				// On recrée un rôle pour éviter les nulls
				user.setRole(new Role(roleId, selectedRole));

				return user;
			}

			return null;
		});

		dialog.showAndWait().ifPresent(updatedUser -> updateUser(updatedUser));
	}

}
