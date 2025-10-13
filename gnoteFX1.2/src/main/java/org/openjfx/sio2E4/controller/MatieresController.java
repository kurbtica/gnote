package org.openjfx.sio2E4.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.application.Platform;
import javafx.scene.control.cell.TextFieldTableCell;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.openjfx.sio2E4.model.Matiere;
import org.openjfx.sio2E4.model.Role;
import org.openjfx.sio2E4.model.User;
import org.openjfx.sio2E4.service.AuthService;
import org.openjfx.sio2E4.service.LocalStorageService;
import org.openjfx.sio2E4.service.NetworkService;

public class MatieresController {

    @FXML
    private TableView<Matiere> matieresTable;

    @FXML
    private TableColumn<Matiere, String> libelleColumn;

    @FXML
    private TextField libelleField;

    @FXML
    private Button addButton;

    private final String API_URL = "http://localhost:8080/api/matieres";
    private final String BEARER_TOKEN = "Bearer " + AuthService.getToken();

    // TODO déplacé toutes les méthodes showAlert, clearForm, ... dans un autre fichier

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }



    @FXML
    private void initialize() {
        // On suppose que libelleColumn est bien défini dans le FXML avec fx:id ET text="Matières"

        // Active l'édition des cellules dans la colonne
        libelleColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        libelleColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLibelle()));

        libelleColumn.setOnEditCommit(event -> {
            Matiere matiere = event.getRowValue();
            String newLibelle = event.getNewValue();
            System.out.println("Modification détectée: " + newLibelle);
            matiere.setLibelle(newLibelle);
            updateMatiere(matiere);
        });

        // Ne surtout pas réinitialiser les colonnes ici
        // matieresTable.getColumns().setAll(libelleColumn); // À supprimer !

        libelleColumn.setResizable(false);
        libelleColumn.setPrefWidth(9999);

        matieresTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        matieresTable.setEditable(true);

        fetchMatieres();
    }






    private void fetchMatieres() {
        if (NetworkService.isOnline()) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Authorization", BEARER_TOKEN)
                .GET()
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(this::parseMatieres)
                .exceptionally(e -> {
                    e.printStackTrace();
                    return null;
                });
        } else {
            System.out.println("Mode hors ligne activé — chargement local");
            ArrayList<Matiere> localMatieres = LocalStorageService.loadMatieres();
            Platform.runLater(() -> matieresTable.getItems().setAll(localMatieres));
        }
    }

    private void parseMatieres(String responseBody) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            List<Matiere> matieres = Arrays.asList(mapper.readValue(responseBody, Matiere[].class));
            Platform.runLater(() -> matieresTable.getItems().setAll(matieres));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddMatiere() {
        String libelle = libelleField.getText();

        if (!libelle.isEmpty()) {
            if (NetworkService.isOnline()) {
                // Envoi de la requête POST pour ajouter une nouvelle matière
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(API_URL))
                        .header("Authorization", BEARER_TOKEN)
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString("{\"libelle\": \"" + libelle + "\"}"))
                        .build();

                client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                        .thenAccept(response -> {
                            if (response.statusCode() == 201) {
                                fetchMatieres();  // Rafraîchit la liste des matières après l'ajout
                            }
                        })
                        .exceptionally(e -> {
                            e.printStackTrace();
                            return null;
                        });
            } else {
                // === Mode hors ligne ===

                Matiere newMatiere = new Matiere();
                newMatiere.setLibelle(libelle);

                LocalStorageService.save(newMatiere);

                Platform.runLater(() -> {
                    matieresTable.getItems().add(newMatiere);
                    showAlert(Alert.AlertType.INFORMATION, "Matière ajouté en local (mode hors ligne).");
                });
            }

            libelleField.clear();  // Vide le champ de texte après l'ajout
        }
    }

    @FXML
    private void handleEditMatiere(TableColumn.CellEditEvent<Matiere, String> event) {
        Matiere matiere = event.getRowValue();
        String newLibelle = event.getNewValue();
        matiere.setLibelle(newLibelle);

        // Envoi de la requête PUT pour mettre à jour la matière dans la base de données
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + "/" + matiere.getId()))
                .header("Authorization", BEARER_TOKEN)
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString("{\"id\": " + matiere.getId() + ", \"libelle\": \"" + newLibelle + "\"}"))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        fetchMatieres();  // Rafraîchit la liste après modification
                    }
                })
                .exceptionally(e -> {
                    e.printStackTrace();
                    return null;
                });
    }

    // Méthode pour supprimer une matière
    @FXML
    private void handleDeleteMatiere() {
        Matiere selectedMatiere = matieresTable.getSelectionModel().getSelectedItem();
        if (selectedMatiere != null) {
            int matiereId = selectedMatiere.getId(); // Assure-toi que la classe Matiere a un champ "id"
            if (NetworkService.isOnline()) {

                // Envoyer la requête DELETE pour supprimer la matière
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(API_URL + "/" + matiereId))
                        .header("Authorization", BEARER_TOKEN)
                        .DELETE()
                        .build();

                client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                        .thenAccept(response -> {
                            if (response.statusCode() == 204) {
                                fetchMatieres();  // Rafraîchit la liste après suppression
                            }
                        })
                        .exceptionally(e -> {
                            e.printStackTrace();
                            return null;
                        });
            } else {
                ArrayList<Matiere> matieres = LocalStorageService.loadMatieres();
                Optional<Matiere> matiere = matieres.stream()
                        .filter(u -> u.getId()==matiereId)
                        .findFirst();
                if (matiere.isPresent()) {
                    LocalStorageService.remove(matiere.get());

                    Platform.runLater(() -> {
                        showAlert(Alert.AlertType.INFORMATION, "Matière supprimé en local (mode hors ligne).");
                        fetchMatieres(); // Rafraîchit la liste des utilisateurs
                    });
                }
            }
        }
    }

    @FXML
    private void onTableClick() {
        // Code pour gérer l'événement de clic sur une ligne du tableau
        Matiere selectedMatiere = matieresTable.getSelectionModel().getSelectedItem();
        if (selectedMatiere != null) {
            System.out.println("Matière sélectionnée : " + selectedMatiere.getLibelle());
        }
    }
    
    private void updateMatiere(Matiere matiere) {
        if (NetworkService.isOnline()) {
            System.out.println("Mise à jour de la matière: " + matiere.getLibelle()); // Vérifie que la méthode est appelée

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL + "/" + matiere.getId()))
                    .header("Authorization", BEARER_TOKEN)
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString("{\"libelle\": \"" + matiere.getLibelle() + "\"}"))
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        if (response.statusCode() == 200) {
                            System.out.println("Matière mise à jour avec succès !");
                        } else {
                            System.out.println("Erreur lors de la mise à jour de la matière. Code: " + response.statusCode());
                        }
                    })
                    .exceptionally(e -> {
                        e.printStackTrace();
                        return null;
                    });
        } else {
            ArrayList<Matiere> matieres = LocalStorageService.loadMatieres();
            Optional<Matiere> localMatiere = matieres.stream()
                    .filter(u -> u.getId()==matiere.getId())
                    .findFirst();
            if (localMatiere.isPresent()) {
                LocalStorageService.update(matiere);

                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.INFORMATION, "Matière mis a jour en local (mode hors ligne).");
                    fetchMatieres(); // Rafraîchit la liste des utilisateurs
                });
            }
        }
    }



}
