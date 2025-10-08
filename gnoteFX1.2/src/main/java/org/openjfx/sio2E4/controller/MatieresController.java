package org.openjfx.sio2E4.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.beans.property.SimpleStringProperty;
import javafx.application.Platform;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.cell.TextFieldTableCell;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.openjfx.sio2E4.model.Matiere;
import org.openjfx.sio2E4.service.AuthService;

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
    }



}
