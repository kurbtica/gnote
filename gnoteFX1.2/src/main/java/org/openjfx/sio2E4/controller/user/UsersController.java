package org.openjfx.sio2E4.controller.user;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.openjfx.sio2E4.controller.MainLayoutController;
import org.openjfx.sio2E4.model.User;
import org.openjfx.sio2E4.repository.UserRepository;
import org.openjfx.sio2E4.util.AlertHelper;
import org.openjfx.sio2E4.util.UserValidator;

/**
 * Contrôleur de la vue Utilisateurs.
 * <p>
 * Responsabilités :
 * - Afficher la liste des utilisateurs dans un TableView
 * - Déléguer la logique des boutons à {@link UserActionButtonsCell}
 * - Déléguer la construction des dialogues à {@link UserDialogFactory}
 * - Appeler le {@link UserRepository} pour les opérations CRUD
 */
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
    @FXML
    private TableColumn actionsColumn;

    @FXML
    private Label resultCountLabel;

    private final UserRepository userRepository = new UserRepository();

    // -------------------------------------------------------
    //  Initialisation
    // -------------------------------------------------------

    @FXML
    public void initialize() {
        // Mapping des colonnes sur les propriétés du modèle User
        nomColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNom()));
        prenomColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPrenom()));
        emailColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
        telephoneColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTelephone()));
        adresseColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAdresse()));
        roleColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRole().getLibelle()));

        // Délégation des boutons d'action à UserActionButtonsCell
        actionsColumn.setCellFactory(column -> new UserActionButtonsCell(this));

        // Largeurs proportionnelles des colonnes
        nomColumn.prefWidthProperty().bind(usersTable.widthProperty().multiply(0.14));
        prenomColumn.prefWidthProperty().bind(usersTable.widthProperty().multiply(0.14));
        emailColumn.prefWidthProperty().bind(usersTable.widthProperty().multiply(0.15));
        telephoneColumn.prefWidthProperty().bind(usersTable.widthProperty().multiply(0.14));
        adresseColumn.prefWidthProperty().bind(usersTable.widthProperty().multiply(0.15));
        roleColumn.prefWidthProperty().bind(usersTable.widthProperty().multiply(0.15));
        actionsColumn.prefWidthProperty().bind(usersTable.widthProperty().multiply(0.13));
        usersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        loadUsersList();
    }

    // -------------------------------------------------------
    //  Chargement des données
    // -------------------------------------------------------

    public void loadUsersList() {
        userRepository.getUsersList()
                .thenAccept(users -> Platform.runLater(() -> {
                    usersTable.getItems().setAll(users);
                    resultCountLabel.setText(users.size() + " résultat");
                }))
                .exceptionally(e -> {
                    e.printStackTrace();
                    return null;
                });
    }

    // -------------------------------------------------------
    //  Navigation vers la fiche utilisateur
    // -------------------------------------------------------

    private MainLayoutController mainLayoutController;

    public void setMainLayoutController(MainLayoutController controller) {
        this.mainLayoutController = controller;
    }

    // Appelé depuis UserActionButtonsCell
    public void handleShowUserCard(User user) {
        if (user != null && mainLayoutController != null) {
            mainLayoutController.showUserCard(user.getId());
        }
    }

    // -------------------------------------------------------
    //  CRUD Utilisateur
    // -------------------------------------------------------

    // Point d'entrée pour CRÉER (bouton "Ajouter" dans la vue FXML)
    @FXML
    private void showCreateDialog() {
        openUserDialog(null);
    }

    // Appelé depuis UserActionButtonsCell
    public void handleEditUser(User user) {
        if (user != null) openUserDialog(user);
    }

    // Appelé depuis UserActionButtonsCell
    public void handleDeleteUser(User user) {
        if (user == null) {
            AlertHelper.showWarning("Veuillez sélectionner un utilisateur.");
            return;
        }

        userRepository.deleteUser(user.getId())
                .thenAccept(success -> Platform.runLater(() -> {
                    if (success) {
                        AlertHelper.showInformation("Utilisateur supprimé avec succès.");
                        loadUsersList();
                    } else {
                        AlertHelper.showError("Impossible de supprimer cet utilisateur.");
                    }
                }))
                .exceptionally(e -> {
                    Platform.runLater(() -> AlertHelper.showError("Erreur technique : " + e.getMessage()));
                    return null;
                });
    }

    // -------------------------------------------------------
    //  Dialogue de création / édition (délégué à UserDialogFactory)
    // -------------------------------------------------------

    private void openUserDialog(User inputUser) {
        boolean isEditMode = (inputUser != null
                && inputUser.getId() != null
                && inputUser.getId() > 0);

        UserDialogFactory.createUserDialog(inputUser)
                .showAndWait()
                .ifPresent(resultUser -> {
                    if (isEditMode) {
                        updateUser(resultUser);
                    } else {
                        ajouterUtilisateur(resultUser);
                    }
                });
    }

    private void ajouterUtilisateur(User user) {
        if (!validateUser(user)) return;

        userRepository.createUser(user)
                .thenAccept(success -> Platform.runLater(() -> {
                    if (success) {
                        AlertHelper.showInformation("Utilisateur ajouté avec succès !");
                        loadUsersList();
                    } else {
                        AlertHelper.showError("Erreur lors de l'ajout de l'utilisateur.");
                    }
                }))
                .exceptionally(e -> {
                    Platform.runLater(() -> AlertHelper.showError("Erreur technique : " + e.getMessage()));
                    return null;
                });
    }

    private void updateUser(User user) {
        if (!validateUser(user)) return;

        userRepository.updateUser(user)
                .thenAccept(success -> Platform.runLater(() -> {
                    if (success) {
                        AlertHelper.showInformation("Utilisateur modifié avec succès !");
                        loadUsersList();
                    } else {
                        AlertHelper.showError("Erreur lors de la modification.");
                    }
                }))
                .exceptionally(e -> {
                    Platform.runLater(() -> AlertHelper.showError("Erreur technique : " + e.getMessage()));
                    return null;
                });
    }

    // -------------------------------------------------------
    //  Validation
    // -------------------------------------------------------

    /**
     * Valide les champs obligatoires du formulaire et affiche un warning si invalide.
     *
     * @return true si valide, false sinon
     */
    private boolean validateUser(User user) {
        if (user.getNom().isEmpty() || user.getPrenom().isEmpty() || user.getEmail().isEmpty()) {
            AlertHelper.showWarning("Veuillez remplir tous les champs obligatoires.");
            openUserDialog(user); // Réouvrir avec les données saisies
            return false;
        }
        if (!UserValidator.validateEmail(user.getEmail())) {
            AlertHelper.showWarning("Email non conforme.");
            openUserDialog(user);
            return false;
        }
        if (!UserValidator.validatePhone(user.getTelephone())) {
            AlertHelper.showWarning("Numéro de téléphone non conforme.");
            openUserDialog(user);
            return false;
        }
        return true;
    }
}
