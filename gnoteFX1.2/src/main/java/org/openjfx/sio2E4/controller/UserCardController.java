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
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TextArea;
import javafx.scene.control.Button;
import java.util.Map;
import org.openjfx.sio2E4.service.LocalStorageService;
import org.openjfx.sio2E4.service.NetworkService;
import org.openjfx.sio2E4.service.NoteService;
import org.openjfx.sio2E4.model.User;
import org.openjfx.sio2E4.util.AlertHelper;

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

    // Éléments de l'éditeur d'appréciation (nouveau design)
    @FXML private Label selectedMatiereLabel;
    @FXML private TextArea appreciationTextArea;
    @FXML private Button saveAppButton;
    @FXML private Button cancelAppButton;

    private final String BEARER_TOKEN = "Bearer "+ AuthService.getToken();
    private int currentUserId = -1;

    private boolean isEditorAllowed() {
        try {
            User current = AuthService.getCurrentUser();
            if (current == null || current.getRole() == null) return false;
            String lib = current.getRole().getLibelle();
            return lib != null && (lib.equalsIgnoreCase("ADMIN") || lib.equalsIgnoreCase("ENSEIGNANT"));
        } catch (Exception e) {
            return false;
        }
    }
    
    public void loadUser(int userId) {
        this.currentUserId = userId;
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

            Map<String, List<Note>> notesParMatiere = NoteService.groupNotesByMatiere(notes);
            ObservableList<MatiereRow> data = NoteService.buildMatiereRows(notesParMatiere);

            // Charger les appréciations stockées localement pour cet utilisateur
            User user = LocalStorageService.findUserById(userId);
            Map<String, String> appMap = user != null ? user.getAppreciations() : null;

            // Remplacer les chaînes "Not implemented" par les appréciations réelles si disponibles
            ObservableList<MatiereRow> dataWithApp = FXCollections.observableArrayList();
            for (MatiereRow row : data) {
                String matiere = row.getMatiere();
                String appr = (appMap != null && appMap.containsKey(matiere)) ? appMap.get(matiere) : "";
                dataWithApp.add(new MatiereRow(matiere, row.getMoyenne(), row.getNotesHBox(), appr));
            }

            Platform.runLater(() -> {
                notesTable.setItems(dataWithApp);

                // Si l'utilisateur est un étudiant, calculer la moyenne
                if ("ETUDIANT".equals(role)) {
                    double moyenne = NoteService.calculateMoyenne(notes);
                    moyenneLabel.setText("Moyenne: " + moyenne);
                }

                // Initialisation du panneau d'édition en fonction du rôle de l'utilisateur connecté
                boolean allowed = isEditorAllowed();
                appreciationTextArea.setDisable(!allowed);
                saveAppButton.setDisable(!allowed);
                cancelAppButton.setDisable(!allowed);
                if (!allowed) {
                    selectedMatiereLabel.setText("(Édition réservée aux enseignants/admin)");
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
                    double moyenne = NoteService.calculateMoyenne(notes);
                    moyenneLabel.setText("Moyenne: " + moyenne);
                }
            });

        } catch (IOException e) {
            e.printStackTrace(); // Gérer l'erreur de parsing
        }
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
        // Permettre l'édition de l'appréciation par double-clic : ouvre une boîte de dialogue (si autorisé)
        appreciationsColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                    setOnMouseClicked(null);
                } else {
                    setText(item);
                    setOnMouseClicked(event -> {
                        if (event.getClickCount() == 2) {
                            if (!isEditorAllowed()) {
                                AlertHelper.showWarning("Vous n'avez pas la permission d'éditer les appréciations.");
                                return;
                            }
                            int index = getIndex();
                            if (index < 0 || index >= getTableView().getItems().size()) return;
                            String matiere = getTableView().getItems().get(index).getMatiere();
                            TextInputDialog dialog = new TextInputDialog(item == null ? "" : item);
                            dialog.setTitle("Éditer appréciation");
                            dialog.setHeaderText("Matière: " + matiere);
                            dialog.setContentText("Appréciation:");
                            dialog.showAndWait().ifPresent(value -> {
                                // Mettre à jour localement
                                User u = LocalStorageService.findUserById(currentUserId);
                                if (u != null) {
                                    Map<String, String> map = u.getAppreciations();
                                    if (map == null) map = new java.util.HashMap<>();
                                    map.put(matiere, value);
                                    u.setAppreciations(map);
                                    LocalStorageService.update(u);
                                }

                                // Mettre à jour l'affichage
                                getTableView().getItems().set(index,
                                        new MatiereRow(matiere,
                                                getTableView().getItems().get(index).getMoyenne(),
                                                getTableView().getItems().get(index).getNotesHBox(),
                                                value));

                                AlertHelper.showInformation("Appréciation enregistrée localement.");
                            });
                        }
                    });
                }
            }
        });

        // Écoute de sélection + actions Save/Cancel
        notesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            boolean allowed = isEditorAllowed();
            if (newSel == null) {
                selectedMatiereLabel.setText("(Sélectionne une matière)");
                appreciationTextArea.setText("");
                appreciationTextArea.setDisable(true);
                saveAppButton.setDisable(true);
                cancelAppButton.setDisable(true);
            } else {
                selectedMatiereLabel.setText(newSel.getMatiere());
                appreciationTextArea.setDisable(!allowed);
                saveAppButton.setDisable(!allowed);
                cancelAppButton.setDisable(!allowed);
                appreciationTextArea.setText(newSel.getAppreciations() == null ? "" : newSel.getAppreciations());
            }
        });

        saveAppButton.setOnAction(ev -> {
            MatiereRow sel = notesTable.getSelectionModel().getSelectedItem();
            if (sel == null) return;
            String matiere = sel.getMatiere();
            String value = appreciationTextArea.getText();

            User u = LocalStorageService.findUserById(currentUserId);
            if (u != null) {
                Map<String, String> map = u.getAppreciations();
                if (map == null) map = new java.util.HashMap<>();
                map.put(matiere, value);
                u.setAppreciations(map);
                LocalStorageService.update(u);
            }

            int idx = notesTable.getSelectionModel().getSelectedIndex();
            notesTable.getItems().set(idx, new MatiereRow(matiere, sel.getMoyenne(), sel.getNotesHBox(), value));
            AlertHelper.showInformation("Appréciation enregistrée localement.");
        });

        cancelAppButton.setOnAction(ev -> {
            MatiereRow sel = notesTable.getSelectionModel().getSelectedItem();
            if (sel == null) return;
            appreciationTextArea.setText(sel.getAppreciations() == null ? "" : sel.getAppreciations());
        });

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
