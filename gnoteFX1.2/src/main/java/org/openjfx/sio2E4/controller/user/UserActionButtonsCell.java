package org.openjfx.sio2E4.controller.user;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.shape.SVGPath;
import org.openjfx.sio2E4.constants.StyleConstants;
import org.openjfx.sio2E4.model.User;

/**
 * Cellule personnalisée pour la colonne "Actions" du tableau des utilisateurs.
 * <p>
 * Cette classe est extraite de UsersController pour séparer la logique d'affichage
 * des boutons (SVG, hover, actions) de la logique métier du contrôleur principal.
 * <p>
 * Elle contient 3 boutons :
 * - 👁 Voir la fiche de l'utilisateur
 * - ✏ Modifier l'utilisateur
 * - 🗑 Supprimer l'utilisateur
 */
public class UserActionButtonsCell extends TableCell<User, Void> {

    private final Button viewButton = new Button();
    private final Button editButton = new Button();
    private final Button deleteButton = new Button();
    private final HBox buttonsBox = new HBox(8);

    // Icônes SVG (gardées en champs pour les effets hover)
    private final SVGPath viewIcon = new SVGPath();
    private final SVGPath editIcon = new SVGPath();
    private final SVGPath deleteIcon = new SVGPath();

    // Référence au contrôleur parent pour déclencher les actions
    private final UsersController controller;

    public UserActionButtonsCell(UsersController controller) {
        this.controller = controller;

        createViewButton();
        createEditButton();
        createDeleteButton();

        buttonsBox.setAlignment(Pos.CENTER_RIGHT);
        buttonsBox.getChildren().addAll(viewButton, editButton, deleteButton);
        buttonsBox.setPadding(new Insets(0, 10, 0, 0));
    }

    /**
     * Crée le bouton "Voir" (icône œil, fond vert clair).
     */
    private void createViewButton() {
        viewIcon.setContent("M16 8s-3-5.5-8-5.5S0 8 0 8s3 5.5 8 5.5S16 8 16 8zM1.173 8a13.133 13.133 0 0 1 1.66-2.043C4.12 4.668 5.88 3.5 8 3.5c2.12 0 3.879 1.168 5.168 2.457A13.133 13.133 0 0 1 14.828 8c-.058.087-.122.183-.195.288-.335.48-.83 1.12-1.465 1.755C11.879 11.332 10.119 12.5 8 12.5c-2.12 0-3.879-1.168-5.168-2.457A13.134 13.134 0 0 1 1.172 8z M8 5.5a2.5 2.5 0 1 0 0 5 2.5 2.5 0 0 0 0-5zM4.5 8a3.5 3.5 0 1 1 7 0 3.5 3.5 0 0 1-7 0z");
        viewIcon.setScaleX(0.8);
        viewIcon.setScaleY(0.8);
        viewIcon.setStyle(StyleConstants.ButtonActionsColumn.VIEW_BUTTON_ICON);

        viewButton.setGraphic(viewIcon);
        viewButton.setStyle(StyleConstants.ButtonActionsColumn.VIEW_BUTTON);
        viewButton.setTooltip(new Tooltip("Voir la fiche"));

        viewButton.setOnMouseEntered(e -> {
            viewButton.setStyle(StyleConstants.ButtonActionsColumn.VIEW_BUTTON_HOVER);
            viewIcon.setStyle(StyleConstants.ButtonActionsColumn.VIEW_BUTTON_ICON_HOVER);
        });
        viewButton.setOnMouseExited(e -> {
            viewButton.setStyle(StyleConstants.ButtonActionsColumn.VIEW_BUTTON);
            viewIcon.setStyle(StyleConstants.ButtonActionsColumn.VIEW_BUTTON_ICON);
        });
    }

    /**
     * Crée le bouton "Modifier" (icône crayon, fond blanc).
     */
    private void createEditButton() {
        editIcon.setContent("M15.728 9.686l-1.414-1.414L5 17.586V19h1.414l9.314-9.314zm1.414-1.414l1.414-1.414-1.414-1.414-1.414 1.414 1.414 1.414zM7.242 21H3v-4.243L16.435 3.322a1 1 0 0 1 1.414 0l2.829 2.829a1 1 0 0 1 0 1.414L7.243 21z");
        editIcon.setScaleX(0.8);
        editIcon.setScaleY(0.8);
        editIcon.setStyle(StyleConstants.ButtonActionsColumn.EDIT_BUTTON_ICON);

        editButton.setGraphic(editIcon);
        editButton.setStyle(StyleConstants.ButtonActionsColumn.EDIT_BUTTON);
        editButton.setTooltip(new Tooltip("Modifier"));

        editButton.setOnMouseEntered(e -> editButton.setStyle(StyleConstants.ButtonActionsColumn.EDIT_BUTTON_HOVER));
        editButton.setOnMouseExited(e -> editButton.setStyle(StyleConstants.ButtonActionsColumn.EDIT_BUTTON));
    }

    /**
     * Crée le bouton "Supprimer" (icône poubelle, fond rouge clair).
     */
    private void createDeleteButton() {
        deleteIcon.setContent("M5.5 5.5A.5.5 0 0 1 6 6v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5zm2.5 0a.5.5 0 0 1 .5.5v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5zm3 .5a.5.5 0 0 0-1 0v6a.5.5 0 0 0 1 0V6z M14.5 3a1 1 0 0 1-1 1H13v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V4h-.5a1 1 0 0 1-1-1V2a1 1 0 0 1 1-1H6a1 1 0 0 1 1-1h2a1 1 0 0 1 1 1h3.5a1 1 0 0 1 1 1v1zM4.118 4 4 4.059V13a1 1 0 0 0 1 1h6a1 1 0 0 0 1-1V4.059L11.882 4H4.118zM2.5 3V2h11v1h-11z");
        deleteIcon.setScaleX(0.8);
        deleteIcon.setScaleY(0.8);
        deleteIcon.setStyle(StyleConstants.ButtonActionsColumn.DELETE_BUTTON_ICON);

        deleteButton.setGraphic(deleteIcon);
        deleteButton.setStyle(StyleConstants.ButtonActionsColumn.DELETE_BUTTON);
        deleteButton.setTooltip(new Tooltip("Supprimer"));

        deleteButton.setOnMouseEntered(e -> {
            deleteButton.setStyle(StyleConstants.ButtonActionsColumn.DELETE_BUTTON_HOVER);
            deleteIcon.setStyle(StyleConstants.ButtonActionsColumn.DELETE_BUTTON_ICON_HOVER);
        });
        deleteButton.setOnMouseExited(e -> {
            deleteButton.setStyle(StyleConstants.ButtonActionsColumn.DELETE_BUTTON);
            deleteIcon.setStyle(StyleConstants.ButtonActionsColumn.DELETE_BUTTON_ICON);
        });
    }

    /**
     * Méthode appelée par JavaFX à chaque rendu de cellule.
     * On y connecte les actions des boutons à l'utilisateur de la ligne.
     */
    @Override
    protected void updateItem(Void item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || getTableRow() == null) {
            setGraphic(null);
        } else {
            User user = getTableView().getItems().get(getIndex());

            // Connexion des actions au contrôleur parent
            viewButton.setOnAction(event -> controller.handleShowUserCard(user));
            editButton.setOnAction(event -> controller.handleEditUser(user));
            deleteButton.setOnAction(event -> controller.handleDeleteUser(user));

            setGraphic(buttonsBox);
        }
    }
}
