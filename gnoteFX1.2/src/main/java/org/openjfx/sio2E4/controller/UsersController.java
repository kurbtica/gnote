package org.openjfx.sio2E4.controller;

import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.shape.SVGPath;
import org.openjfx.sio2E4.constants.StyleConstants;
import org.openjfx.sio2E4.model.Role;
import org.openjfx.sio2E4.model.User;
import org.openjfx.sio2E4.repository.UserRepository;
import org.openjfx.sio2E4.util.AlertHelper;
import org.openjfx.sio2E4.util.UserValidator;

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

	// Injection du service
	private final UserRepository userRepository = new UserRepository();
    
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
				viewIcon.setStyle(StyleConstants.ButtonActionsColumn.VIEW_BUTTON_ICON);
				viewButton.setGraphic(viewIcon);
				viewButton.setStyle(StyleConstants.ButtonActionsColumn.VIEW_BUTTON);
				viewButton.setTooltip(new Tooltip("Voir la fiche"));

				// Icône SVG Pencil (Modifier)
				SVGPath editIcon = new SVGPath();
				editIcon.setContent("M15.728 9.686l-1.414-1.414L5 17.586V19h1.414l9.314-9.314zm1.414-1.414l1.414-1.414-1.414-1.414-1.414 1.414 1.414 1.414zM7.242 21H3v-4.243L16.435 3.322a1 1 0 0 1 1.414 0l2.829 2.829a1 1 0 0 1 0 1.414L7.243 21z");
				editIcon.setScaleX(0.8);
				editIcon.setScaleY(0.8);
				editIcon.setStyle(StyleConstants.ButtonActionsColumn.EDIT_BUTTON_ICON);
				editButton.setGraphic(editIcon);
				editButton.setStyle(StyleConstants.ButtonActionsColumn.EDIT_BUTTON);
				editButton.setTooltip(new Tooltip("Modifier"));

				// Icône SVG Trash (Supprimer)
				SVGPath deleteIcon = new SVGPath();
				deleteIcon.setContent("M5.5 5.5A.5.5 0 0 1 6 6v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5zm2.5 0a.5.5 0 0 1 .5.5v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5zm3 .5a.5.5 0 0 0-1 0v6a.5.5 0 0 0 1 0V6z M14.5 3a1 1 0 0 1-1 1H13v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V4h-.5a1 1 0 0 1-1-1V2a1 1 0 0 1 1-1H6a1 1 0 0 1 1-1h2a1 1 0 0 1 1 1h3.5a1 1 0 0 1 1 1v1zM4.118 4 4 4.059V13a1 1 0 0 0 1 1h6a1 1 0 0 0 1-1V4.059L11.882 4H4.118zM2.5 3V2h11v1h-11z");
				deleteIcon.setScaleX(0.8);
				deleteIcon.setScaleY(0.8);
				deleteIcon.setStyle(StyleConstants.ButtonActionsColumn.DELETE_BUTTON_ICON);
				deleteButton.setGraphic(deleteIcon);
				deleteButton.setStyle(StyleConstants.ButtonActionsColumn.DELETE_BUTTON);
				deleteButton.setTooltip(new Tooltip("Supprimer"));

				// Effets hover pour le bouton Voir fiche utilisateur
				viewButton.setOnMouseEntered(e -> {
					viewButton.setStyle(StyleConstants.ButtonActionsColumn.VIEW_BUTTON_HOVER);
					viewIcon.setStyle(StyleConstants.ButtonActionsColumn.VIEW_BUTTON_ICON_HOVER);
				});
				viewButton.setOnMouseExited(e -> {
					viewButton.setStyle(StyleConstants.ButtonActionsColumn.VIEW_BUTTON);
					viewIcon.setStyle(StyleConstants.ButtonActionsColumn.VIEW_BUTTON_ICON);
				});

				// Effets hover pour le bouton Modifier
				editButton.setOnMouseEntered(e -> {
					editButton.setStyle(StyleConstants.ButtonActionsColumn.EDIT_BUTTON_HOVER);
				});
				editButton.setOnMouseExited(e -> {
					editButton.setStyle(StyleConstants.ButtonActionsColumn.EDIT_BUTTON);
				});

				// Effets hover pour le bouton Supprimer
				deleteButton.setOnMouseEntered(e -> {
					deleteButton.setStyle(StyleConstants.ButtonActionsColumn.DELETE_BUTTON_HOVER);
					deleteIcon.setStyle(StyleConstants.ButtonActionsColumn.DELETE_BUTTON_ICON_HOVER);
				});
				deleteButton.setOnMouseExited(e -> {
					deleteButton.setStyle(StyleConstants.ButtonActionsColumn.DELETE_BUTTON);
					deleteIcon.setStyle(StyleConstants.ButtonActionsColumn.DELETE_BUTTON_ICON);
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

		loadUsersList();
	}

	public void loadUsersList() {
		// Appel au service pour l'utilisateur
		userRepository.getUsersList()
				.thenAccept(user -> {
					// Mise à jour UI Utilisateur
					Platform.runLater(() -> {
						usersTable.getItems().setAll(user);
						resultCountLabel.setText(user.size() + " résultat");
					});

				})
				.exceptionally(e -> {
					e.printStackTrace(); // Gérez l'erreur (ex: afficher une alerte)
					return null;
				});
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
		// Validations de base (champs vides, regex)
		if (user.getNom().isEmpty() || user.getPrenom().isEmpty() || user.getEmail().isEmpty()) {
			AlertHelper.showWarning("Veuillez remplir tous les champs obligatoires.");
			openUserDialog(user);
			return;
		}
		if (!UserValidator.validateEmail(user.getEmail())) {
			AlertHelper.showWarning("Email non conforme.");
			openUserDialog(user);
			return;
		}
		if (!UserValidator.validatePhone(user.getTelephone())) {
			AlertHelper.showWarning("Numéro de téléphone non conforme.");
			openUserDialog(user);
			return;
		}

		// Appel au repository
		userRepository.createUser(user)
				.thenAccept(success -> {
					Platform.runLater(() -> {
						if (success) {
							AlertHelper.showInformation("Utilisateur ajouté avec succès !");
							loadUsersList(); // Rafraîchir le tableau
						} else {
							AlertHelper.showError("Erreur lors de l'ajout de l'utilisateur.");
						}
					});
				})
				.exceptionally(e -> {
					Platform.runLater(() -> AlertHelper.showError("Erreur technique : " + e.getMessage()));
					return null;
				});
	}

	private int getRoleId(String roleName) { // TODO a supprimer
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
			AlertHelper.showWarning("Veuillez sélectionner un utilisateur.");
			return;
		}

		userRepository.deleteUser(user.getId())
				.thenAccept(success -> {
					Platform.runLater(() -> {
						if (success) {
							AlertHelper.showInformation("Utilisateur supprimé avec succès.");
							loadUsersList();
						} else {
							AlertHelper.showError("Impossible de supprimer cet utilisateur.");
						}
					});
				})
				.exceptionally(e -> {
					Platform.runLater(() -> AlertHelper.showError("Erreur technique : " + e.getMessage()));
					return null;
				});
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
		// Validations de base (champs vides, regex)
		if (user.getNom().isEmpty() || user.getPrenom().isEmpty() || user.getEmail().isEmpty()) {
			AlertHelper.showWarning("Veuillez remplir tous les champs obligatoires.");
			openUserDialog(user);
			return;
		}
		if (!UserValidator.validateEmail(user.getEmail())) {
			AlertHelper.showWarning("Email non conforme.");
			openUserDialog(user);
			return;
		}
		if (!UserValidator.validatePhone(user.getTelephone())) {
			AlertHelper.showWarning("Numéro de téléphone non conforme.");
			openUserDialog(user);
			return;
		}

		userRepository.updateUser(user)
				.thenAccept(success -> {
					Platform.runLater(() -> {
						if (success) {
							AlertHelper.showInformation("Utilisateur modifié avec succès !");
							loadUsersList();
						} else {
							AlertHelper.showError("Erreur lors de la modification.");
						}
					});
				})
				.exceptionally(e -> {
					Platform.runLater(() -> AlertHelper.showError("Erreur technique : " + e.getMessage()));
					return null;
				});
	}

	// Point d'entrée pour CRÉER
	@FXML
	private void showCreateDialog() {
		openUserDialog(null);
	}

	// Point d'entrée pour ÉDITER
	@FXML
	private void showEditDialog(User user) { // ou showEditDialog
		if (user != null) openUserDialog(user);
	}

	private void openUserDialog(User inputUser) {
		// On est en mode "Édition" seulement si l'utilisateur existe ET qu'il a un ID (donc il vient de la BDD)
		// Si l'ID est 0, c'est que c'est un "brouillon" qu'on réaffiche pour correction.
		boolean isEditMode = (inputUser != null && inputUser.getId() > 0);

		// Si on a un inputUser (même brouillon), on le reprend, sinon on en crée un vierge
		User user = (inputUser != null) ? inputUser : new User();

		Dialog<User> dialog = new Dialog<>();
		dialog.setTitle(isEditMode ? "Modifier l'utilisateur" : "Ajouter un utilisateur");
		dialog.setHeaderText(isEditMode ? "Modifier les informations :" : "Remplir les informations :");

		ButtonType validButtonType = new ButtonType(isEditMode ? "Enregistrer" : "Créer", ButtonBar.ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(validButtonType, ButtonType.CANCEL);

		// --- Création des champs ---
		// On pré-remplit avec les données de 'user' (qui peut être le brouillon refusé précédemment)
		TextField nomField = new TextField(user.getNom());
		TextField prenomField = new TextField(user.getPrenom());
		TextField emailField = new TextField(user.getEmail());
		TextField adresseField = new TextField(user.getAdresse());
		TextField phoneField = new TextField(user.getTelephone());
		PasswordField passField = new PasswordField();

		ComboBox<String> roleBox = new ComboBox<>();
		roleBox.getItems().addAll("ADMIN", "ENSEIGNANT", "ETUDIANT");
		// Gestion de sécurité pour éviter le NullPointerException sur le rôle
		if (user.getRole() != null) {
			roleBox.setValue(user.getRole().getLibelle());
		}

		// --- Mise en page (GridPane) ---
		GridPane grid = new GridPane();
		grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20, 150, 10, 10));

		addRows(grid,
				"Nom:", nomField,
				"Prénom:", prenomField,
				"Email:", emailField,
				"Adresse:", adresseField,
				"Téléphone:", phoneField
		);

		// Le champ mot de passe apparaît si ce n'est PAS une édition (donc création ou correction brouillon)
		if (!isEditMode) {
			grid.add(new Label("Mot de passe:"), 0, 5);
			grid.add(passField, 1, 5);
		}

		grid.add(new Label("Rôle:"), 0, 6);
		grid.add(roleBox, 1, 6);

		dialog.getDialogPane().setContent(grid);
		Platform.runLater(nomField::requestFocus);

		// --- Convertisseur de résultat ---
		dialog.setResultConverter(btn -> {
			if (btn == validButtonType) {
				user.setNom(nomField.getText().trim());
				user.setPrenom(prenomField.getText().trim());
				user.setEmail(emailField.getText().trim());
				user.setAdresse(adresseField.getText().trim());
				user.setTelephone(phoneField.getText().trim());

				String roleName = roleBox.getValue();
				if (roleName != null) {
					user.setRole(new Role(getRoleId(roleName), roleName));
				}

				// En création/correction, on capture le mot de passe
				if (!isEditMode && !passField.getText().isEmpty()) {
					// user.setHashedPassword(passField.getText()); // Décommenter si besoin
				}
				return user;
			}
			return null;
		});

		dialog.showAndWait().ifPresent(resultUser -> {
			if (isEditMode) {
				updateUser(resultUser);
			} else {
				ajouterUtilisateur(resultUser);
			}
		});
	}

	// Helper utilitaire (si tu ne l'as pas déjà mis)
	private void addRows(GridPane grid, Object... components) {
		for (int i = 0; i < components.length; i += 2) {
			grid.add(new Label((String) components[i]), 0, i / 2);
			grid.add((Node) components[i + 1], 1, i / 2);
		}
	}

}
