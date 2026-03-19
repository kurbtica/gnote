package org.openjfx.sio2E4.controller.user.cards;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import org.openjfx.sio2E4.constants.StyleConstants;
import org.openjfx.sio2E4.model.Evaluation;
import org.openjfx.sio2E4.model.Note;
import org.openjfx.sio2E4.repository.EvaluationRepository;
import org.openjfx.sio2E4.repository.UserRepository;
import org.openjfx.sio2E4.util.AlertHelper;

import java.util.List;
import java.util.stream.Collectors;

public class TeacherCardController {

    @FXML private Label nomLabel;
    @FXML private Label prenomLabel;
    @FXML private Label emailLabel;
    @FXML private Label telephoneLabel;
    @FXML private Label adresseLabel;
    @FXML private Label roleLabel;

    @FXML private TableView<Evaluation> evaluationTable;
    @FXML private TableColumn<Evaluation, String> titleColumn;
    @FXML private TableColumn<Evaluation, String> matiereColumn;
    @FXML private TableColumn<Evaluation, String> dateColumn;
    @FXML private TableColumn<Evaluation, String> moyenneColumn;
    @FXML private TableColumn<Evaluation, String> moyenneMinColumn;
    @FXML private TableColumn<Evaluation, String> moyenneMaxColumn;
    @FXML private TableColumn<Evaluation, Void> actionsColumn;

    private final EvaluationRepository evaluationRepository = new EvaluationRepository();
    private final UserRepository userRepository = new UserRepository();

    // we keep currentTeacherId for potential future use
    private int currentTeacherId = -1;

    @FXML
    private void initialize() {
        titleColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTitre()));
        matiereColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMatiere().getLibelle()));
        dateColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDate()));

        moyenneColumn.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(
            org.openjfx.sio2E4.service.NoteService.calculateMoyenne(d.getValue().getNotes())
        )));

        moyenneMinColumn.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(
                d.getValue().getNotes().stream().mapToDouble(Note::getValeur).min().orElse(Double.NaN)
        )));

        moyenneMaxColumn.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(
                d.getValue().getNotes().stream().mapToDouble(Note::getValeur).max().orElse(Double.NaN)
        )));

        actionsColumn.setCellFactory(col -> new TableCell<Evaluation, Void>() {
            private final Button viewButton = new Button("Voir");
            private final HBox box = new HBox(6);

            { box.setAlignment(Pos.CENTER_RIGHT); box.setPadding(new Insets(0,6,0,0)); viewButton.setStyle(StyleConstants.ButtonActionsColumn.VIEW_BUTTON); }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else {
                    Evaluation ev = getTableView().getItems().get(getIndex());
                    viewButton.setOnAction(a -> showEvaluation(ev));
                    box.getChildren().setAll(viewButton);
                    setGraphic(box);
                }
            }
        });

        evaluationTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    public void loadUser(int teacherId) {
        this.currentTeacherId = teacherId;

        // Load teacher info
        userRepository.getUser(teacherId).thenAccept(user -> Platform.runLater(() -> {
            if (user != null) {
                nomLabel.setText(user.getNom());
                prenomLabel.setText(user.getPrenom());
                emailLabel.setText(user.getEmail());
                telephoneLabel.setText(user.getTelephone());
                adresseLabel.setText(user.getAdresse());
                roleLabel.setText(user.getRole().getLibelle());
            }
        })).exceptionally(e -> { e.printStackTrace(); return null; });

        // Load evaluations where this user is the enseignant
        evaluationRepository.getEvaluationsList().thenAccept(list -> {
                List<Evaluation> teacherEvals = list.stream()
                    .filter(ev -> ev.getEnseignant() != null && ev.getEnseignant().getId() == teacherId)
                    .collect(Collectors.toList());

            ObservableList<Evaluation> data = FXCollections.observableArrayList(teacherEvals);
            Platform.runLater(() -> evaluationTable.setItems(data));
        }).exceptionally(e -> { e.printStackTrace(); return null; });
    }

    private void showEvaluation(Evaluation evaluation) {
        if (evaluation == null) return;
        // Reuse MainLayout to show evaluation view if available
        // Try to fetch a MainLayoutController from the current scene — simplified approach: open evaluation detail in a dialog
        AlertHelper.showInformation("Ouvrir la page de l'évaluation (id="+evaluation.getId()+")");
    }
}
