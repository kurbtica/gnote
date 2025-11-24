package org.openjfx.sio2E4.controller;

import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import org.openjfx.sio2E4.model.table.MatiereRow;
import org.openjfx.sio2E4.model.Note;
import org.openjfx.sio2E4.model.User;
import org.openjfx.sio2E4.repository.UserRepository;
import org.openjfx.sio2E4.service.LocalStorageService;

import java.util.List;
import java.util.Map;
import javafx.scene.control.TextInputDialog;
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

        // Calcul moyenne
        if ("ETUDIANT".equals(role)) {
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
                            TextInputDialog dialog = new TextInputDialog(item == null ? "" : item);
                            dialog.setTitle("Éditer appréciation");
                            dialog.setHeaderText("Matière: " + matiere);
                            dialog.setContentText("Appréciation:");
                            dialog.showAndWait().ifPresent(value -> {
                                User u = LocalStorageService.findUserById(currentUserId);
                                if (u != null) {
                                    Map<String, String> map = u.getAppreciations();
                                    if (map == null) map = new java.util.HashMap<>();
                                    map.put(matiere, value);
                                    u.setAppreciations(map);
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

        // Quand l'utilisateur sélectionne une ligne, afficher l'appréciation dans le panneau droit
        notesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel == null) {
                selectedMatiereLabel.setText("(Sélectionne une matière)");
                appreciationTextArea.setText("");
                appreciationTextArea.setDisable(true);
                saveAppButton.setDisable(true);
                cancelAppButton.setDisable(true);
            } else {
                selectedMatiereLabel.setText(newSel.getMatiere());
                appreciationTextArea.setDisable(false);
                saveAppButton.setDisable(false);
                cancelAppButton.setDisable(false);
                appreciationTextArea.setText(newSel.getAppreciations() == null ? "" : newSel.getAppreciations());
            }
        });

        // Handler Enregistrer
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

        // Handler Annuler (restaurer la valeur actuelle sans sauvegarder)
        cancelAppButton.setOnAction(ev -> {
            MatiereRow sel = notesTable.getSelectionModel().getSelectedItem();
            if (sel == null) return;
            appreciationTextArea.setText(sel.getAppreciations() == null ? "" : sel.getAppreciations());
        });
    }
}
