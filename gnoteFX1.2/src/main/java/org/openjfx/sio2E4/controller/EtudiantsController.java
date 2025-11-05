package org.openjfx.sio2E4.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.openjfx.sio2E4.model.Etudiant;
import org.openjfx.sio2E4.model.User;
import org.openjfx.sio2E4.service.AuthService;
import org.openjfx.sio2E4.service.LocalStorageService;
import org.openjfx.sio2E4.service.NetworkService;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EtudiantsController {

    @FXML
    private TableView<Etudiant> etudiantsTable;
    @FXML
    private TableColumn<Etudiant, String> nomColumn;
    @FXML
    private TableColumn<Etudiant, String> prenomColumn;
    @FXML
    private TableColumn<Etudiant, String> emailColumn;
    @FXML
    private TableColumn<Etudiant, String> telephoneColumn;
    @FXML
    private TableColumn<Etudiant, String> adresseColumn;
    @FXML
    private TableColumn<Etudiant, String> roleColumn;

    private final String API_URL = "http://localhost:8080/api/etudiants";
    private final String BEARER_TOKEN = "Bearer " + AuthService.getToken(); // à remplacer dynamiquement

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
        if (NetworkService.isOnline()) {
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
        } else {
            System.out.println("Mode hors ligne activé — chargement local");
            ArrayList<User> localUsers = LocalStorageService.loadUsers();

// Conversion User → Etudiant pour les rôles "ETUDIANT"
            List<Etudiant> localEtudiants = localUsers.stream()
                    .filter(u -> u.getRole() != null &&
                            "ETUDIANT".equalsIgnoreCase(u.getRole().getLibelle()))
                    .map(u -> {
                        Etudiant e = new Etudiant();
                        e.setId((long) u.getId());
                        e.setNom(u.getNom());
                        e.setPrenom(u.getPrenom());
                        e.setEmail(u.getEmail());
                        e.setAdresse(u.getAdresse());
                        e.setTelephone(u.getTelephone());

                        Etudiant.Role role = new Etudiant.Role();
                        role.setLibelle(u.getRole().getLibelle());
                        e.setRole(role);

                        return e;
                    })
                    .collect(Collectors.toList());

// Mise à jour de la table sur le thread JavaFX
            Platform.runLater(() -> etudiantsTable.getItems().setAll(localEtudiants));


        }
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
    // -------------------- user Card --------------------
    private MainLayoutController mainLayoutController;

    public void setMainLayoutController(MainLayoutController controller) {
        this.mainLayoutController = controller;
    }
    @FXML
    private void handleShowEtudiantCard() {
        Etudiant selectedEtudiant = etudiantsTable.getSelectionModel().getSelectedItem();
        System.out.println(mainLayoutController != null);
        if (selectedEtudiant != null && mainLayoutController != null) {
            mainLayoutController.showEtudiantCard(Math.toIntExact(selectedEtudiant.getId()));
        }
    }
}
