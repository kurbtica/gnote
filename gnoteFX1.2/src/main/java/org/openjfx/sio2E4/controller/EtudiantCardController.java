package org.openjfx.sio2E4.controller;

import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.openjfx.sio2E4.model.Appreciation;
import org.openjfx.sio2E4.model.Matiere;
import org.openjfx.sio2E4.model.table.MatiereRow;
import org.openjfx.sio2E4.model.Note;
import org.openjfx.sio2E4.model.User;
import org.openjfx.sio2E4.repository.UserRepository;
import org.openjfx.sio2E4.service.LocalStorageService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.openjfx.sio2E4.util.AppreciationDialog;
import org.openjfx.sio2E4.service.NoteService;
import org.openjfx.sio2E4.util.AlertHelper;

public class EtudiantCardController {

    @FXML private Label nomLabel;
    @FXML private Label prenomLabel;
    @FXML private Label emailLabel;
    @FXML private Label telephoneLabel;
    @FXML private Label adresseLabel;
    @FXML private Label roleLabel;
    @FXML private Label moyenneLabel; // Label pour afficher la moyenne de l'étudiant

    // Tableau des notes
    @FXML private TableView<MatiereRow> notesTable;
    // container wrapping the notes table and moyenne; used to hide the whole block for non-students
    @FXML private VBox notesCard;

    @FXML private TableColumn<MatiereRow, String> matiereColumn;
    @FXML public TableColumn<MatiereRow, Object> moyennes;
    @FXML private TableColumn<MatiereRow, Double> moyenneEleveColumn;
    @FXML private TableColumn<MatiereRow, HBox> notesColumn;
    @FXML private TableColumn<MatiereRow, String> appreciationsColumn;

    private int currentUserId = -1;

    // Injection du service
    private final UserRepository userRepository = new UserRepository();

    public void loadUser(int userId) {
        currentUserId = userId;
        // Appel au service pour l'utilisateur
        userRepository.getUser(userId)
                .thenAccept(user -> {
                    // Mise à jour UI Utilisateur
                    Platform.runLater(() -> updateUserInfoUI(user));

                    // Une fois l'user chargé, on charge ses notes
                    loadUserNotes(user.getId(), user.getRole().getLibelle());
                })
                .exceptionally(e -> {
                    e.printStackTrace(); // Gérez l'erreur (ex: afficher une alerte)
                    return null;
                });
    }

    private void loadUserNotes(int userId, String role) {
        // Appel au service pour les notes
        userRepository.getUserNotes(userId)
                .thenAccept(notes -> {
                    Platform.runLater(() -> updateNotesUI(notes, role));
                })
                .exceptionally(e -> {
                    e.printStackTrace();
                    return null;
                });
    }

    // --- Méthodes de mise à jour purement UI ---

    private void updateUserInfoUI(User user) {
        if (user == null) return;
        nomLabel.setText(user.getNom());
        prenomLabel.setText(user.getPrenom());
        emailLabel.setText(user.getEmail());
        telephoneLabel.setText(user.getTelephone());
        adresseLabel.setText(user.getAdresse());
        roleLabel.setText(user.getRole().getLibelle());
    }

    private void updateNotesUI(List<Note> notes, String role) {
        // Préparation des données pour le tableau (Logique de présentation)
        Map<String, List<Note>> notesParMatiere = NoteService.groupNotesByMatiere(notes);
        ObservableList<MatiereRow> data = NoteService.buildMatiereRows(notesParMatiere);

        notesTable.setItems(data);

        boolean isStudent = "ETUDIANT".equalsIgnoreCase(role);
        if (notesCard != null) {
            notesCard.setVisible(isStudent);
            notesCard.setManaged(isStudent);
        }

        if (isStudent) {
            double moyenne = NoteService.calculateMoyenne(notes);
            moyenneLabel.setText("Moyenne: " + String.format("%.2f", moyenne));
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
                            int index = getIndex();
                            if (index < 0 || index >= getTableView().getItems().size()) return;
                            String matiere = getTableView().getItems().get(index).getMatiere();
                                AppreciationDialog.show(matiere, item == null ? "" : item)
                                    .ifPresent(value -> {
                                User u = LocalStorageService.findUserById(currentUserId);
                                ArrayList<Appreciation> a = LocalStorageService.loadAppreciations();
                                List<Appreciation> userAppreciation = a.stream().filter(appreciation -> {
                                    return appreciation.getEleve().getPrenom().equals(u.getPrenom()) &&
                                            appreciation.getEleve().getNom().equals(u.getNom());
                                }).collect(Collectors.toList());
                                if (u != null) {
                                    Map<String, String> map = new HashMap<>();
                                    for (Appreciation appreciation : userAppreciation) {
                                        map.put(appreciation.getMatiere().getLibelle(), appreciation.getAppreciation());
                                    }
                                    if (map == null) map = new java.util.HashMap<>();
                                    map.put(matiere, value);
                                    List<Matiere> toutesLesMatieres = LocalStorageService.loadMatieres();

                                    Matiere matiereObj = toutesLesMatieres.stream()
                                            .filter(m -> m.getLibelle().equals(matiere)) // 'matiere' est le String récupéré plus haut
                                            .findFirst()
                                            .orElse(null);

                                    if (matiereObj != null) {
                                        Appreciation newAppreciation = new Appreciation(0, u, matiereObj, value);

                                        LocalStorageService.update(newAppreciation);
                                    } else {
                                        System.err.println("Erreur : Matière introuvable pour le libellé " + matiere);
                                    }
                                    LocalStorageService.update(u);
                                }

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

        // no right-hand appreciation editor — selection-based editor removed
    }
}
