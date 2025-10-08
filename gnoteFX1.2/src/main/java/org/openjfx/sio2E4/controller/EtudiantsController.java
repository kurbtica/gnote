package org.openjfx.sio2E4.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import java.util.Arrays;
import java.util.List;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.io.IOException;
import java.net.URI;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openjfx.sio2E4.model.Etudiant;
import org.openjfx.sio2E4.service.AuthService;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;




public class EtudiantsController {

    @FXML private TableView<Etudiant> etudiantsTable;
    @FXML private TableColumn<Etudiant, String> nomColumn;
    @FXML private TableColumn<Etudiant, String> prenomColumn;
    @FXML private TableColumn<Etudiant, String> emailColumn;
    @FXML private TableColumn<Etudiant, String> telephoneColumn;
    @FXML private TableColumn<Etudiant, String> adresseColumn;
    @FXML private TableColumn<Etudiant, String> roleColumn;

    private final String API_URL = "http://localhost:8080/api/etudiants";
    private final String BEARER_TOKEN = "Bearer "+ AuthService.getToken(); // à remplacer dynamiquement

    @FXML
    public void initialize() {
        nomColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNom()));
        prenomColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPrenom()));
        emailColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
        telephoneColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTelephone()));
        adresseColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAdresse()));
        roleColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRole().getLibelle()));

        fetchEtudiants();
    }

    private void fetchEtudiants() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(API_URL))
            .header("Authorization", BEARER_TOKEN)
            .GET()
            .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(HttpResponse::body)
            .thenAccept(this::parseEtudiants)
            .exceptionally(e -> {
                e.printStackTrace();
                return null;
            });
    }

    private void parseEtudiants(String responseBody) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            List<Etudiant> etudiants = Arrays.asList(mapper.readValue(responseBody, Etudiant[].class));
            Platform.runLater(() -> etudiantsTable.getItems().setAll(etudiants));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
