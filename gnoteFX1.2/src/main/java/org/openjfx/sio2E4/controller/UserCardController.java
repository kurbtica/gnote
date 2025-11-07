package org.openjfx.sio2E4.controller;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import org.openjfx.sio2E4.constants.APIConstants;
import org.openjfx.sio2E4.constants.StyleConstants;
import org.openjfx.sio2E4.model.MatiereRow;
import org.openjfx.sio2E4.model.Note;
import org.openjfx.sio2E4.model.User;
import org.openjfx.sio2E4.service.AuthService;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import javafx.beans.property.SimpleStringProperty;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import org.openjfx.sio2E4.service.LocalStorageService;
import org.openjfx.sio2E4.service.NetworkService;

public class UserCardController {

    @FXML private Label nomLabel;
    @FXML private Label prenomLabel;
    @FXML private Label emailLabel;
    @FXML private Label telephoneLabel;
    @FXML private Label adresseLabel;
    @FXML private Label roleLabel;
    @FXML private Label moyenneLabel; // Label pour afficher la moyenne de l'étudiant

    // Tableau des notes
    @FXML private TableView<MatiereRow> notesTable;

    @FXML private TableColumn<MatiereRow, String> matiereColumn;
    @FXML public TableColumn<MatiereRow, Object> moyennes;
    @FXML private TableColumn<MatiereRow, Double> moyenneEleveColumn;
    @FXML private TableColumn<MatiereRow, HBox> notesColumn;
    @FXML private TableColumn<MatiereRow, String> appreciationsColumn;

    private final String BEARER_TOKEN = "Bearer "+ AuthService.getToken();
    
    public void loadUser(int userId) {
        if (NetworkService.isOnline()) {
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(APIConstants.formatUrl(APIConstants.USER_BY_ID, userId)))
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
        } else {
            User user = LocalStorageService.findUserById(userId);
            try {
                Platform.runLater(() -> {
                    // Affichage des informations utilisateur dans les labels
                    nomLabel.setText(user.getNom());
                    prenomLabel.setText(user.getPrenom());
                    emailLabel.setText(user.getEmail());
                    telephoneLabel.setText(user.getTelephone());
                    adresseLabel.setText(user.getAdresse());
                    roleLabel.setText(user.getRole().getLibelle());
                });

                loadUserNotes(user.getId(), user.getRole().getLibelle());
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
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
        if (NetworkService.isOnline()) {
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(APIConstants.formatUrl(APIConstants.USER_NOTES, userId)))
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
        } else {
            ArrayList<Note> notes = (ArrayList<Note>) LocalStorageService.loadNotes().stream()
                    .filter(note -> note.getEleve().getId() == userId)
                    .collect(Collectors.toList());

            // Regroupe les notes par matière pour n'avoir qu'une seule ligne par matière dans le TableView
            // Utilise un Map<String, List<Note>> où la clé est le nom de la matière et la valeur est la liste des notes

            Map<String, List<Note>> notesParMatiere = notes.stream()
                    .filter(note -> note.getEleve().getId() == userId)
                    .collect(Collectors.groupingBy(n -> n.getMatiere().getLibelle()));

            ObservableList<MatiereRow> data = FXCollections.observableArrayList();

            for (Map.Entry<String, List<Note>> entry : notesParMatiere.entrySet()) {
                String matiere = entry.getKey();
                List<Note> notesMatiere = entry.getValue();

                HBox notesBox = new HBox(5); // crée un conteneur horizontal avec 5 pixels d’écart entre chaque élément
                notesBox.setAlignment(Pos.CENTER_LEFT);
                notesBox.setFillHeight(true);

                for (Note note : notesMatiere) {
                    // Crée un Label pour chaque note et applique un style visuel (couleur, arrondi, padding)
                    // Installe un Tooltip sur chaque Label pour afficher le type et la date de la note lors du survol

                    Text valeur = new Text(String.valueOf(note.getValeur()));
                    Text coef = new Text("(" + note.getCoefficient() + ")");
                    coef.setStyle(StyleConstants.COEFFICIENT_STYLE); // affiché en "indice"

                    // Créer un conteneur pour chaque note
                    HBox noteContainer = new HBox(2);
                    noteContainer.setAlignment(Pos.CENTER_LEFT);
                    noteContainer.setFillHeight(true);
                    noteContainer.getChildren().addAll(valeur, coef);
                    noteContainer.setStyle(StyleConstants.NOTE_CONTAINER_STYLE); // espace entre les notes

                    // Tooltip pour la date et le type
                    Tooltip tooltip = new Tooltip(
                            note.getCommentaire() +
                            "\nType: " + note.getNoteType().getLibelle() +
                            "\nDate: " + note.getDate() +
                            "\nEnseignant: " + note.getEnseignant().getNom().toUpperCase() + " " + note.getEnseignant().getPrenom()
                    );
                    Tooltip.install(noteContainer, tooltip);

                    notesBox.getChildren().add(noteContainer);
                }

                // TODO cree un système appreciation par matière et le mettre a la place de "Not implemented" (emplacement déjà défini dans la vue)
                data.add(new MatiereRow(matiere, calculateMoyenne(notesMatiere), notesBox, "Not implemented"));
            }

            Platform.runLater(() -> {
                notesTable.setItems(data);

                // Si l'utilisateur est un étudiant, calculer la moyenne
                if ("ETUDIANT".equals(role)) {
                    double moyenne = calculateMoyenne(notes);
                    moyenneLabel.setText("Moyenne: " + moyenne);
                }
            });
        }
    }

    private void parseNotesResponse(String response, String role) {
        try {
            ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            List<Note> notes = mapper.readValue(response, mapper.getTypeFactory().constructCollectionType(List.class, Note.class));

            // Utilisation de Platform.runLater pour manipuler l'UI sur le FX thread
            Platform.runLater(() -> {
                // Mettre à jour la TableView avec les données
                // TODO fonction avec l'api temporairement désactivé (pas d'api pour le moment, donc aucun moyen de tester)
                //notesTable.getItems().setAll(notes);

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

        // Arrondi a 2 chiffres après la virgule
        return Math.round((totalNotes / totalCoef) * 100) / 100.0;
    }

    // Initialiser les colonnes de la TableView
    @FXML
    private void initialize() {
        matiereColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMatiere()));
        moyenneEleveColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getMoyenne()).asObject());

        // Utilisation d'un cellFactory pour que chaque cellule de la colonne notesColumn puisse afficher correctement l'HBox
        // Si la cellule est vide, setGraphic(null)
        notesColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getNotesHBox()));
        notesColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(HBox item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : item);
            }
        });

        appreciationsColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAppreciations()));

        // Définir les largeurs en pourcentage de la largeur totale du tableau
        matiereColumn.prefWidthProperty().bind(
                notesTable.widthProperty().multiply(0.15) // 15%
        );
        moyennes.prefWidthProperty().bind(
                notesTable.widthProperty().multiply(0.15) // 15%
        );
        notesColumn.prefWidthProperty().bind(
                notesTable.widthProperty().multiply(0.30) // 30%
        );
        appreciationsColumn.prefWidthProperty().bind(
                notesTable.widthProperty().multiply(0.40) // 40%
        );
        notesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }
}
