package org.openjfx.sio2E4.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.shape.SVGPath;
import org.openjfx.sio2E4.model.Role;
import org.openjfx.sio2E4.model.User;
import org.openjfx.sio2E4.service.AuthService;
import org.openjfx.sio2E4.service.LocalStorageService;
import org.openjfx.sio2E4.service.NetworkService;
import org.openjfx.sio2E4.util.AlertHelper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
	@FXML private TableColumn actionsColumn;

    @FXML private Label resultCountLabel;


	private final String API_URL = "http://localhost:8080/api/users";
	private final String BEARER_TOKEN = "Bearer " + AuthService.getToken();
    
	@FXML
	public void initialize() {
		nomColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNom()));
		prenomColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPrenom()));
		emailColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
		telephoneColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTelephone()));
		adresseColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAdresse()));
		roleColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRole().getLibelle()));
		actionsColumn.setCellFactory(column -> new TableCell<User, Void>() {
			private final Button viewButton = new Button();
			private final Button editButton = new Button();
			private final Button deleteButton = new Button();
			private final HBox buttonsBox = new HBox(8);

			{
				// Icône SVG Eye (Voir)
				SVGPath viewIcon = new SVGPath();
				viewIcon.setContent("M16 8s-3-5.5-8-5.5S0 8 0 8s3 5.5 8 5.5S16 8 16 8zM1.173 8a13.133 13.133 0 0 1 1.66-2.043C4.12 4.668 5.88 3.5 8 3.5c2.12 0 3.879 1.168 5.168 2.457A13.133 13.133 0 0 1 14.828 8c-.058.087-.122.183-.195.288-.335.48-.83 1.12-1.465 1.755C11.879 11.332 10.119 12.5 8 12.5c-2.12 0-3.879-1.168-5.168-2.457A13.134 13.134 0 0 1 1.172 8z M8 5.5a2.5 2.5 0 1 0 0 5 2.5 2.5 0 0 0 0-5zM4.5 8a3.5 3.5 0 1 1 7 0 3.5 3.5 0 0 1-7 0z");
				viewIcon.setScaleX(0.8);
				viewIcon.setScaleY(0.8);
				viewIcon.setStyle("-fx-fill: #059669;");
				viewButton.setGraphic(viewIcon);
				viewButton.setStyle(
						"-fx-background-color: #d1fae5; " +
								"-fx-border-color: #a7f3d0; " +
								"-fx-border-radius: 6; " +
								"-fx-background-radius: 6; " +
								"-fx-padding: 8 12; " +
								"-fx-cursor: hand; " +
								"-fx-min-width: 40; " +
								"-fx-min-height: 36;"
				);
				viewButton.setTooltip(new Tooltip("Voir la fiche"));

				// Icône SVG Pencil (Modifier)
				SVGPath editIcon = new SVGPath();
				editIcon.setContent("M15.728 9.686l-1.414-1.414L5 17.586V19h1.414l9.314-9.314zm1.414-1.414l1.414-1.414-1.414-1.414-1.414 1.414 1.414 1.414zM7.242 21H3v-4.243L16.435 3.322a1 1 0 0 1 1.414 0l2.829 2.829a1 1 0 0 1 0 1.414L7.243 21z");
				editIcon.setScaleX(0.8);
				editIcon.setScaleY(0.8);
				editIcon.setStyle("-fx-fill: #3b82f6;");
				editButton.setGraphic(editIcon);
				editButton.setStyle(
						"-fx-background-color: white; " +
								"-fx-border-color: #e5e7eb; " +
								"-fx-border-radius: 6; " +
								"-fx-background-radius: 6; " +
								"-fx-padding: 8 12; " +
								"-fx-cursor: hand; " +
								"-fx-min-width: 40; " +
								"-fx-min-height: 36;"
				);
				editButton.setTooltip(new Tooltip("Modifier"));

				// Icône SVG Trash (Supprimer)
				SVGPath deleteIcon = new SVGPath();
				deleteIcon.setContent("M5.5 5.5A.5.5 0 0 1 6 6v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5zm2.5 0a.5.5 0 0 1 .5.5v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5zm3 .5a.5.5 0 0 0-1 0v6a.5.5 0 0 0 1 0V6z M14.5 3a1 1 0 0 1-1 1H13v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V4h-.5a1 1 0 0 1-1-1V2a1 1 0 0 1 1-1H6a1 1 0 0 1 1-1h2a1 1 0 0 1 1 1h3.5a1 1 0 0 1 1 1v1zM4.118 4 4 4.059V13a1 1 0 0 0 1 1h6a1 1 0 0 0 1-1V4.059L11.882 4H4.118zM2.5 3V2h11v1h-11z");
				deleteIcon.setScaleX(0.8);
				deleteIcon.setScaleY(0.8);
				deleteIcon.setStyle("-fx-fill: #dc2626;");
				deleteButton.setGraphic(deleteIcon);
				deleteButton.setStyle(
						"-fx-background-color: #fee2e2; " +
								"-fx-border-color: #fecaca; " +
								"-fx-border-radius: 6; " +
								"-fx-background-radius: 6; " +
								"-fx-padding: 8 12; " +
								"-fx-cursor: hand; " +
								"-fx-min-width: 40; " +
								"-fx-min-height: 36;"
				);
				deleteButton.setTooltip(new Tooltip("Supprimer"));

				// Effets hover pour le bouton Voir fiche utilisateur
				viewButton.setOnMouseEntered(e -> {
					viewButton.setStyle(
							"-fx-background-color: #059669; " +
									"-fx-border-color: #059669; " +
									"-fx-border-radius: 6; " +
									"-fx-background-radius: 6; " +
									"-fx-padding: 8 12; " +
									"-fx-cursor: hand; " +
									"-fx-min-width: 40; " +
									"-fx-min-height: 36;"
					);
					viewIcon.setStyle("-fx-fill: white;");
				});
				viewButton.setOnMouseExited(e -> {
					viewButton.setStyle(
							"-fx-background-color: #d1fae5; " +
									"-fx-border-color: #a7f3d0; " +
									"-fx-border-radius: 6; " +
									"-fx-background-radius: 6; " +
									"-fx-padding: 8 12; " +
									"-fx-cursor: hand; " +
									"-fx-min-width: 40; " +
									"-fx-min-height: 36;"
					);
					viewIcon.setStyle("-fx-fill: #059669;");
				});

				// Effets hover pour le bouton Modifier
				editButton.setOnMouseEntered(e -> {
					editButton.setStyle(
							"-fx-background-color: #eff6ff; " +
									"-fx-border-color: #3b82f6; " +
									"-fx-border-radius: 6; " +
									"-fx-background-radius: 6; " +
									"-fx-padding: 8 12; " +
									"-fx-cursor: hand; " +
									"-fx-min-width: 40; " +
									"-fx-min-height: 36;"
					);
				});
				editButton.setOnMouseExited(e -> {
					editButton.setStyle(
							"-fx-background-color: white; " +
									"-fx-border-color: #e5e7eb; " +
									"-fx-border-radius: 6; " +
									"-fx-background-radius: 6; " +
									"-fx-padding: 8 12; " +
									"-fx-cursor: hand; " +
									"-fx-min-width: 40; " +
									"-fx-min-height: 36;"
					);
				});

				// Effets hover pour le bouton Supprimer
				deleteButton.setOnMouseEntered(e -> {
					deleteButton.setStyle(
							"-fx-background-color: #dc2626; " +
									"-fx-border-color: #dc2626; " +
									"-fx-border-radius: 6; " +
									"-fx-background-radius: 6; " +
									"-fx-padding: 8 12; " +
									"-fx-cursor: hand; " +
									"-fx-min-width: 40; " +
									"-fx-min-height: 36;"
					);
					deleteIcon.setStyle("-fx-fill: white;");
				});
				deleteButton.setOnMouseExited(e -> {
					deleteButton.setStyle(
							"-fx-background-color: #fee2e2; " +
									"-fx-border-color: #fecaca; " +
									"-fx-border-radius: 6; " +
									"-fx-background-radius: 6; " +
									"-fx-padding: 8 12; " +
									"-fx-cursor: hand; " +
									"-fx-min-width: 40; " +
									"-fx-min-height: 36;"
					);
					deleteIcon.setStyle("-fx-fill: #dc2626;");
				});

				buttonsBox.setAlignment(Pos.CENTER_RIGHT);
				buttonsBox.getChildren().addAll(viewButton, editButton, deleteButton);
				buttonsBox.setPadding(new Insets(0, 10, 0, 0));
			}

			@Override
			protected void updateItem(Void item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || getTableRow() == null) {
					setGraphic(null);
				} else {
					User user = getTableView().getItems().get(getIndex());

					// Actions des boutons
					viewButton.setOnAction(event -> handleShowUserCard(user));
					editButton.setOnAction(event -> handleEditUser(user));
					deleteButton.setOnAction(event -> handleDeleteUser(user));

					setGraphic(buttonsBox);
				}
			}
		});

		// Définir les largeurs en pourcentage de la largeur totale du tableau
		nomColumn.prefWidthProperty().bind(
				usersTable.widthProperty().multiply(0.14)
		);
		prenomColumn.prefWidthProperty().bind(
				usersTable.widthProperty().multiply(0.14) // 30
		);
		emailColumn.prefWidthProperty().bind(
				usersTable.widthProperty().multiply(0.15)
		);
		telephoneColumn.prefWidthProperty().bind(
				usersTable.widthProperty().multiply(0.14) // 60
		);
		adresseColumn.prefWidthProperty().bind(
				usersTable.widthProperty().multiply(0.15) //
		);
		roleColumn.prefWidthProperty().bind(
				usersTable.widthProperty().multiply(0.15) // 90
		);
		actionsColumn.prefWidthProperty().bind(
				usersTable.widthProperty().multiply(0.13)
		);
		usersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        fetchUsers();
	}

	private void fetchUsers() {
		if (NetworkService.isOnline()) {
			HttpClient client = HttpClient.newHttpClient();
			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create(API_URL))
					.header("Authorization", BEARER_TOKEN)
					.GET()
					.build();

			client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
					.thenApply(HttpResponse::body)
					.thenAccept(this::parseUsers)
					.exceptionally(e -> {
						e.printStackTrace();
						return null;
					});
		} else {
			System.out.println("Mode hors ligne activé — chargement local");
			ArrayList<User> localUsers = LocalStorageService.loadUsers();
			Platform.runLater(() -> {
                usersTable.getItems().setAll(localUsers);
                resultCountLabel.setText(" " + localUsers.size() + " résultat");
            });
		}
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
	private void handleShowUserCard(User user) {
		if (user != null && mainLayoutController != null) {
			mainLayoutController.showUserCard(user.getId());
		}
	}


	@FXML
	private void ajouterUtilisateur(User user) {

		int roleId = user.getRole().getId();

		if (user.getNom().isEmpty() || user.getPrenom().isEmpty() || user.getEmail().isEmpty()) { // || user.getHashedPassword().isEmpty()
			AlertHelper.showWarning("Veuillez remplir tous les champs obligatoires.");
			return;
		}
        if (!user.getEmail().matches("^[\\w\\d._%+-]+@[\\w\\d.-]+\\.[A-Za-z]{2,}$")) {
            AlertHelper.showWarning("Email non conforme.");
            return;
        }

        if (!user.getTelephone().matches("^(0|\\+33)[1-9](\\d{2}){4}$")) {
            AlertHelper.showWarning("Téléphone non conforme.");
            return;
        }
        if (NetworkService.isOnline()) {
			try {
				// Construction du JSON
				String json = String.format(
						"{\"nom\":\"%s\",\"prenom\":\"%s\",\"email\":\"%s\",\"adresse\":\"%s\",\"telephone\":\"%s\",\"passwordHash\":\"%s\",\"role\":{\"id\":%d,\"libelle\":\"%s\"}}",
						user.getNom(), user.getPrenom(), user.getEmail(), user.getAdresse(), user.getTelephone(), "user.getHashedPassword()", roleId, user.getRole().getId());

				HttpClient client = HttpClient.newHttpClient();
				HttpRequest request = HttpRequest.newBuilder().uri(URI.create(API_URL))
						.header("Authorization", BEARER_TOKEN).header("Content-Type", "application/json")
						.POST(BodyPublishers.ofString(json)).build();

				client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenAccept(response -> {
					if (response.statusCode() == 201 || response.statusCode() == 200) {
						Platform.runLater(() -> AlertHelper.showInformation("Utilisateur ajouté avec succès !"));
						Platform.runLater(this::fetchUsers);
						// Optionnel : refreshTable(); si tu veux actualiser la liste
					} else {
						Platform.runLater(() -> AlertHelper.showError("Erreur lors de l'ajout : " + response.body()));
					}
				}).exceptionally(e -> {
					e.printStackTrace();
					Platform.runLater(() -> AlertHelper.showError("Erreur réseau : " + e.getMessage()));
					return null;
				});

			} catch (Exception e) {
				e.printStackTrace();
				AlertHelper.showError("Erreur interne : " + e.getMessage());
			}
		} else {
			// === Mode hors ligne ===
			LocalStorageService.save(user);

			Platform.runLater(() -> {
				usersTable.getItems().add(user);
				AlertHelper.showInformation("Utilisateur ajouté en local (mode hors ligne).");
			});
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
	private void handleDeleteUser(User user) {
		if (user == null) {
			AlertHelper.showWarning("Veuillez sélectionner un utilisateur à supprimer.");
			return;
		}

		// Supprimer l'utilisateur immédiatement
		deleteUser(user.getId());
	}

	private void deleteUser(int userId) {
		if (NetworkService.isOnline()) {
			HttpClient client = HttpClient.newHttpClient();
			HttpRequest request = HttpRequest.newBuilder().uri(URI.create(API_URL + "/" + userId))
					.header("Authorization", BEARER_TOKEN).DELETE().build();

			client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenAccept(response -> {
				if (response.statusCode() == 204) { // 204 No Content signifie que la suppression a réussi
					Platform.runLater(() -> {
						AlertHelper.showInformation("Utilisateur supprimé avec succès.");
						fetchUsers(); // Rafraîchit la liste des utilisateurs
					});
				} else {
					Platform.runLater(() -> AlertHelper.showError("Erreur lors de la suppression de l'utilisateur."));
				}
			}).exceptionally(e -> {
				e.printStackTrace();
				Platform.runLater(() -> AlertHelper.showError("Erreur réseau : " + e.getMessage()));
				return null;
			});
		} else {
			ArrayList<User> users = LocalStorageService.loadUsers();
			Optional<User> offlineUser = users.stream()
					.filter(u -> u.getId()==userId)
					.findFirst();
			if (offlineUser.isPresent()) {
				LocalStorageService.remove(offlineUser.get());

				Platform.runLater(() -> {
					AlertHelper.showInformation("Utilisateur supprimé en local (mode hors ligne).");
					fetchUsers(); // Rafraîchit la liste des utilisateurs
				});
			}
		}
	}

	// --------------------------Modification de l'utilisateur par boite de dialogue

	@FXML
	private void handleEditUser(User user) {
		if (user == null) {
			AlertHelper.showWarning("Veuillez sélectionner un utilisateur à modifier.");
			return;
		}
		showEditDialog(user);
	}

	private void updateUser(User user) {
		if (NetworkService.isOnline()) {
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
							AlertHelper.showInformation("Utilisateur modifié avec succès !");
							fetchUsers();
						});
					} else {
						Platform.runLater(() -> AlertHelper.showError("Erreur de modification : " + response.body()));
					}
				}).exceptionally(e -> {
					e.printStackTrace();
					Platform.runLater(() -> AlertHelper.showError("Erreur réseau : " + e.getMessage()));
					return null;
				});
			} catch (Exception e) {
				e.printStackTrace();
				AlertHelper.showError("Erreur lors de la mise à jour.");
			}
		} else {
			ArrayList<User> users = LocalStorageService.loadUsers();
			Optional<User> offlineUser = users.stream()
					.filter(u -> u.getId()==user.getId())
					.findFirst();
			if (offlineUser.isPresent()) {
				LocalStorageService.update(user);

				Platform.runLater(() -> {
					AlertHelper.showInformation("Utilisateur mis a jour en local (mode hors ligne).");
					fetchUsers(); // Rafraîchit la liste des utilisateurs
				});
			}
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

	@FXML
	private void showCreateDialog() {
		Dialog<User> dialog = new Dialog<>();
		dialog.setTitle("Ajouter un utilisateur");
		dialog.setHeaderText("Créez un nouvel utilisateur :");

		// Boutons OK / Annuler
		ButtonType createButtonType = new ButtonType("Créer", ButtonBar.ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

		// Création du formulaire
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 150, 10, 10));

		TextField nomField = new TextField();
		nomField.setPromptText("Nom");
		TextField prenomField = new TextField();
		prenomField.setPromptText("Prénom");
		TextField emailField = new TextField();
		emailField.setPromptText("email@exemple.fr");
		TextField adresseField = new TextField();
		adresseField.setPromptText("Adresse");
		TextField telephoneField = new TextField();
		telephoneField.setPromptText("Téléphone");
		PasswordField passwordField = new PasswordField();
		passwordField.setPromptText("Mot de passe");
		ComboBox<String> roleBox = new ComboBox<>();
		roleBox.getItems().addAll("ADMIN", "ENSEIGNANT", "ETUDIANT");
		roleBox.setPromptText("Sélectionner un rôle");

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
		grid.add(new Label("Mot de passe:"), 0, 5);
		grid.add(passwordField, 1, 5);
		grid.add(new Label("Rôle:"), 0, 6);
		grid.add(roleBox, 1, 6);

		dialog.getDialogPane().setContent(grid);

		// Désactiver le bouton Créer si les champs obligatoires sont vides
		Node createButton = dialog.getDialogPane().lookupButton(createButtonType);
		createButton.setDisable(true);

		// Validation en temps réel
		nomField.textProperty().addListener((observable, oldValue, newValue) -> {
			createButton.setDisable(newValue.trim().isEmpty() ||
					prenomField.getText().trim().isEmpty() ||
					emailField.getText().trim().isEmpty() ||
					passwordField.getText().trim().isEmpty() ||
					roleBox.getValue() == null);
		});
		prenomField.textProperty().addListener((observable, oldValue, newValue) -> {
			createButton.setDisable(newValue.trim().isEmpty() ||
					nomField.getText().trim().isEmpty() ||
					emailField.getText().trim().isEmpty() ||
					passwordField.getText().trim().isEmpty() ||
					roleBox.getValue() == null);
		});
		emailField.textProperty().addListener((observable, oldValue, newValue) -> {
			createButton.setDisable(newValue.trim().isEmpty() ||
					nomField.getText().trim().isEmpty() ||
					prenomField.getText().trim().isEmpty() ||
					passwordField.getText().trim().isEmpty() ||
					roleBox.getValue() == null);
		});
		passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
			createButton.setDisable(newValue.trim().isEmpty() ||
					nomField.getText().trim().isEmpty() ||
					prenomField.getText().trim().isEmpty() ||
					emailField.getText().trim().isEmpty() ||
					roleBox.getValue() == null);
		});
		roleBox.valueProperty().addListener((observable, oldValue, newValue) -> {
			createButton.setDisable(newValue == null ||
					nomField.getText().trim().isEmpty() ||
					prenomField.getText().trim().isEmpty() ||
					emailField.getText().trim().isEmpty() ||
					passwordField.getText().trim().isEmpty());
		});

		// Focus sur le premier champ
		Platform.runLater(() -> nomField.requestFocus());

		// Convertir le résultat quand l'utilisateur clique sur Créer
		dialog.setResultConverter(dialogButton -> {
			if (dialogButton == createButtonType) {
				User newUser = new User();
				newUser.setNom(nomField.getText().trim());
				newUser.setPrenom(prenomField.getText().trim());
				newUser.setEmail(emailField.getText().trim());
				newUser.setAdresse(adresseField.getText().trim());
				newUser.setTelephone(telephoneField.getText().trim());
				//newUser.setHashedPassword(passwordField.getText());

				String selectedRole = roleBox.getValue();
				int roleId = getRoleId(selectedRole);
				newUser.setRole(new Role(roleId, selectedRole));

				return newUser;
			}
			return null;
		});

		dialog.showAndWait().ifPresent(newUser -> ajouterUtilisateur(newUser));
	}


}
