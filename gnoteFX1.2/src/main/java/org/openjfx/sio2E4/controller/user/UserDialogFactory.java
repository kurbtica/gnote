package org.openjfx.sio2E4.controller.user;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import org.openjfx.sio2E4.model.Role;
import org.openjfx.sio2E4.model.User;

/**
 * Fabrique de boîtes de dialogue pour la gestion des utilisateurs.
 * <p>
 * Cette classe est extraite de UsersController pour séparer la construction
 * de l'interface de dialogue (champs, grille, validations) de la logique métier.
 * <p>
 * Elle gère deux modes :
 * - CRÉATION  : tous les champs vides + champ mot de passe affiché
 * - ÉDITION   : champs pré-remplis + champ mot de passe masqué
 */
public class UserDialogFactory {

    /**
     * Crée et retourne un Dialog<User> prêt à l'affichage.
     *
     * @param inputUser null pour une création, un User existant pour une édition
     * @return le Dialog configuré, à appeler avec .showAndWait()
     */
    public static Dialog<User> createUserDialog(User inputUser) {
        // Détermination du mode : édition si l'utilisateur existe et a un ID en base
        boolean isEditMode = (inputUser != null
                && inputUser.getId() != null
                && inputUser.getId() > 0);

        // Si on a un brouillon (validation échouée), on garde ses données, sinon on part d'un User vide
        User user = (inputUser != null) ? inputUser : new User();

        // --- Configuration du dialogue ---
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle(isEditMode ? "Modifier l'utilisateur" : "Ajouter un utilisateur");
        dialog.setHeaderText(isEditMode ? "Modifier les informations :" : "Remplir les informations :");

        ButtonType validButtonType = new ButtonType(
                isEditMode ? "Enregistrer" : "Créer",
                ButtonBar.ButtonData.OK_DONE
        );
        dialog.getDialogPane().getButtonTypes().addAll(validButtonType, ButtonType.CANCEL);

        // --- Création des champs (pré-remplis si édition ou brouillon) ---
        TextField nomField = new TextField(user.getNom() != null ? user.getNom() : "");
        TextField prenomField = new TextField(user.getPrenom() != null ? user.getPrenom() : "");
        TextField emailField = new TextField(user.getEmail() != null ? user.getEmail() : "");
        TextField adresseField = new TextField(user.getAdresse() != null ? user.getAdresse() : "");
        TextField phoneField = new TextField(user.getTelephone() != null ? user.getTelephone() : "");
        PasswordField passField = new PasswordField();

        ComboBox<String> roleBox = new ComboBox<>();
        roleBox.getItems().addAll("ADMIN", "ENSEIGNANT", "ETUDIANT");
        if (user.getRole() != null) {
            roleBox.setValue(user.getRole().getName());
        }

        // --- Mise en page (GridPane) ---
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        addRows(grid,
                "Nom:", nomField,
                "Prénom:", prenomField,
                "Email:", emailField,
                "Adresse:", adresseField,
                "Téléphone:", phoneField
        );

        // Champ mot de passe uniquement en mode création
        if (!isEditMode) {
            grid.add(new Label("Mot de passe:"), 0, 5);
            grid.add(passField, 1, 5);
        }

        grid.add(new Label("Rôle:"), 0, 6);
        grid.add(roleBox, 1, 6);

        dialog.getDialogPane().setContent(grid);
        Platform.runLater(nomField::requestFocus);

        // --- Convertisseur de résultat : remplir l'objet user quand on clique sur le bouton valider ---
        dialog.setResultConverter(btn -> {
            if (btn == validButtonType) {
                user.setNom(nomField.getText().trim());
                user.setPrenom(prenomField.getText().trim());
                user.setEmail(emailField.getText().trim());
                user.setAdresse(adresseField.getText().trim());
                user.setTelephone(phoneField.getText().trim());

                String roleName = roleBox.getValue();
                if (roleName != null) {
                    user.setRole(Role.valueOf(roleName));
                }
                return user;
            }
            return null; // Annulation → null
        });

        return dialog;
    }

    /**
     * Ajoute des paires (Label, Champ) sur des lignes successives du GridPane.
     * Les arguments doivent être passés par paires : "Libellé:", champ, "Libellé2:", champ2, ...
     */
    private static void addRows(GridPane grid, Object... components) {
        for (int i = 0; i < components.length; i += 2) {
            grid.add(new Label((String) components[i]), 0, i / 2);
            grid.add((Node) components[i + 1], 1, i / 2);
        }
    }
}
