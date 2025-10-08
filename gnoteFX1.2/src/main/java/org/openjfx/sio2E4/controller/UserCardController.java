package org.openjfx.sio2E4.controller;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import org.openjfx.sio2E4.model.Note;
import org.openjfx.sio2E4.model.User;
import org.openjfx.sio2E4.service.AuthService;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import javafx.beans.property.SimpleStringProperty;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class UserCardController {

    @FXML private Label nomLabel;
    @FXML private Label prenomLabel;
    @FXML private Label emailLabel;
    @FXML private Label telephoneLabel;
    @FXML private Label adresseLabel;
    @FXML private Label roleLabel;
    @FXML private Label moyenneLabel; // Label pour afficher la moyenne de l'étudiant

    // Tableau des notes
    @FXML private TableView<Note> notesTable;
    @FXML private TableColumn<Note, String> matiereColumn;
    @FXML private TableColumn<Note, Double> valeurColumn;
    @FXML private TableColumn<Note, String> dateColumn;
    @FXML private TableColumn<Note, Double> coefficientColumn;
    @FXML private TableColumn<Note, String> noteTypeColumn;

    private final String BEARER_TOKEN = "Bearer "+ AuthService.getToken();
    
    public void loadUser(int userId) {
        HttpClient client = HttpClient.newHttpClient();
        String urlString = "http://localhost:8080/api/users/" + userId;

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(urlString))
            .header("Authorization", BEARER_TOKEN)
            .GET()
            .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(HttpResponse::body)
            .thenAccept(this::parseUserResponse)
            .exceptionally(e -> {
                e.printStackTrace();
                return null;
            });
    }

    private void parseUserResponse(String response) {
        try {
            ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            User user = mapper.readValue(response, User.class);

            // Utilisation de Platform.runLater pour manipuler l'UI sur le FX thread
            Platform.runLater(() -> {
                // Affichage des informations utilisateur dans les labels
                nomLabel.setText(user.getNom());
                prenomLabel.setText(user.getPrenom());
                emailLabel.setText(user.getEmail());
                telephoneLabel.setText(user.getTelephone());
                adresseLabel.setText(user.getAdresse());
                roleLabel.setText(user.getRole().getLibelle());
            });

            // Charger et afficher les notes de l'utilisateur
            loadUserNotes(user.getId(), user.getRole().getLibelle());

        } catch (IOException e) {
            e.printStackTrace(); // Gérer l'erreur de parsing
        }
    }

    private void loadUserNotes(int userId, String role) {
        HttpClient client = HttpClient.newHttpClient();
        String urlString = "http://localhost:8080/api/users/" + userId + "/notes";

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(urlString))
            .header("Authorization", BEARER_TOKEN)
            .GET()
            .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(HttpResponse::body)
            .thenAccept(response -> parseNotesResponse(response, role))
            .exceptionally(e -> {
                e.printStackTrace();
                return null;
            });
    }

    private void parseNotesResponse(String response, String role) {
        try {
            ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            List<Note> notes = mapper.readValue(response, mapper.getTypeFactory().constructCollectionType(List.class, Note.class));

            // Utilisation de Platform.runLater pour manipuler l'UI sur le FX thread
            Platform.runLater(() -> {
                // Mettre à jour la TableView avec les données
                notesTable.getItems().setAll(notes);

                // Si l'utilisateur est un étudiant, calculer la moyenne
                if ("ETUDIANT".equals(role)) {
                    double moyenne = calculateMoyenne(notes);
                    moyenneLabel.setText("Moyenne: " + moyenne);
                }
            });

        } catch (IOException e) {
            e.printStackTrace(); // Gérer l'erreur de parsing
        }
    }

    private double calculateMoyenne(List<Note> notes) {
        double totalCoef = 0;
        double totalNotes = 0;

        for (Note note : notes) {
            totalCoef += note.getCoefficient();
            totalNotes += note.getValeur() * note.getCoefficient();
        }

        if (totalCoef == 0) {
            return 0; // Eviter une division par zéro si aucun coefficient n'est trouvé
        }

        return totalNotes / totalCoef;
    }

    // Initialiser les colonnes de la TableView
    @FXML
    private void initialize() {
        matiereColumn.setCellValueFactory(cellData -> cellData.getValue().getMatiere() != null ? 
                new SimpleStringProperty(cellData.getValue().getMatiere().getLibelle()) : null);

        valeurColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getValeur()).asObject());

        dateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDate()));

        coefficientColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getCoefficient()).asObject());

        noteTypeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNoteType()));
    }
}
