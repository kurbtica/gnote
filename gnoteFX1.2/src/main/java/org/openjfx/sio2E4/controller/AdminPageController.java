package org.openjfx.sio2E4.controller;

import javafx.fxml.FXML;
 
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class AdminPageController {

    @FXML
    private Button logoutButton;

    /**
     * Ouvre la fenêtre FXML pour créer un étudiant.
     */
    @FXML
    private void handleCreateEtudiant() {
        ouvrirFenetreFXML("/org/openjfx/sio2E4/Etudiant.fxml", "Créer un Étudiant");
    }

    /**
     * Ouvre la fenêtre FXML pour créer une matière.
     */
    @FXML
    private void handleCreateMatiere() {
        ouvrirFenetreFXML("/org/openjfx/sio2E4/Matiere.fxml", "Créer une Matière");
    }

    /**
     * Ouvre la fenêtre FXML pour créer un enseignant.
     */
    @FXML
    private void handleCreateEnseignant() {
        ouvrirFenetreFXML("/org/openjfx/sio2E4/Enseignant.fxml", "Créer un Enseignant");
    }

    /**
     * Ouvre la fenêtre FXML pour afficher la liste des étudiants.
     */
    @FXML
    private void handleListeEtudiants() {
        ouvrirFenetreFXML("/org/openjfx/sio2E4/ListeEtudiants.fxml", "Liste des Étudiants");
    }

    /**
     * Méthode générique pour ouvrir une fenêtre FXML.
     *
     * @param fxmlPath Le chemin du fichier FXML.
     * @param title    Le titre de la fenêtre.
     */
    private void ouvrirFenetreFXML(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root, 600, 400)); // Définit une taille correcte
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la fenêtre : " + title);
        }
    }


    /**
     * Affiche un message d'alerte à l'utilisateur.
     *
     * @param title   Le titre de l'alerte.
     * @param message Le message affiché.
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Gère la déconnexion et redirige vers la page de connexion.
     */
    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/openjfx/sio2E4/loginPage.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(new Scene(root, 600, 400));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de se déconnecter.");
        }
    }
}
