package org.openjfx.sio2E4.controller.evaluation;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import org.openjfx.sio2E4.controller.MainLayoutController;
import org.openjfx.sio2E4.model.*;
import org.openjfx.sio2E4.model.table.EvaluationRow;
import org.openjfx.sio2E4.repository.EvaluationRepository;
import org.openjfx.sio2E4.repository.MatiereRepository;
import org.openjfx.sio2E4.repository.NoteTypeRepository;
import org.openjfx.sio2E4.repository.UserRepository;
import org.openjfx.sio2E4.service.AuthService;
import org.openjfx.sio2E4.util.AlertHelper;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class EvaluationFormController {
    User currentUser = AuthService.getCurrentUser();
    String role = currentUser.getRole().getLibelle();

    private Evaluation currentEvaluation;

    /* Tableau d'affichage de note */
    @FXML
    private Label pageTitle;

    @FXML
    private TableView<EvaluationRow> etudiantTable;
    @FXML
    private TableColumn<EvaluationRow, String> nomColumn;
    @FXML
    private TableColumn<EvaluationRow, String> prenomColumn;
    @FXML
    private TableColumn<EvaluationRow, String> noteColumn;

    // Injection du service
    private final EvaluationRepository evaluationRepository = new EvaluationRepository();
    private final UserRepository userRepository = new UserRepository();
    private final MatiereRepository matiereRepository = new MatiereRepository();
    private final NoteTypeRepository noteTypeRepository = new NoteTypeRepository();

    // TODO implémenter le nouveaux système de repository

    private void clearForm() {
        Platform.runLater(() -> {
            matiereComboBox.setValue(null);
            noteTypeComboBox.setValue(null);
            datePicker.setValue(null);
            commentaireField.clear();
            coefField.clear();

            // Si l'utilisateur connecté est un enseignant, on le sélectionne à nouveau
            if (currentUser.getRole().getLibelle().equalsIgnoreCase("ENSEIGNANT")) {
                // On cherche l'enseignant correspondant dans la liste (important si l'objet n'est pas le même en mémoire)
                User enseignant = enseignantComboBox.getItems().stream().filter(u -> u.getId() == currentUser.getId())
                        .findFirst().orElse(null);

                if (enseignant != null) {
                    enseignantComboBox.setValue(enseignant);
                    enseignantComboBox.setDisable(true); // on le rend non modifiable
                }
            } else {
                enseignantComboBox.setValue(null);
                enseignantComboBox.setDisable(false); // autoriser la sélection si ce n’est pas un enseignant
            }
        });
    }

    public void loadViewEvaluation(int evaluationId) {
        evaluationRepository.getEvaluation(evaluationId).
                thenAccept(evaluation -> {
                    // Mise à jour UI Utilisateur
                    Platform.runLater(() -> {
                        pageTitle.setText("Évaluation : " + evaluation.getTitre() + " du " + evaluation.getDate());

                        coefField.setText(String.valueOf(evaluation.getCoefficient()));
                        datePicker.setValue(LocalDate.parse(evaluation.getDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                        titreField.setText(evaluation.getTitre());
                        matiereComboBox.setValue(evaluation.getMatiere());
                        enseignantComboBox.setValue(evaluation.getEnseignant());
                        noteTypeComboBox.setValue(evaluation.getNoteType());

                        coefField.setDisable(true);
                        datePicker.setDisable(true);
                        titreField.setDisable(true);
                        matiereComboBox.setDisable(true);
                        enseignantComboBox.setDisable(true);
                        noteTypeComboBox.setDisable(true);
                        commentaireField.setDisable(true);

                        etudiantTable.setEditable(false);
                        noteColumn.setEditable(false);

                        saveButton.setDisable(true);
                        cancelButton.setDisable(true);

                        ObservableList<EvaluationRow> evaluationRow = FXCollections.observableArrayList();;
                        for (Note note : evaluation.getNotes()) {
                            evaluationRow.add(new EvaluationRow(note.getEleve(), note));
                        }
                        etudiantTable.setItems(evaluationRow);
                    });

                })
                .exceptionally(e -> {
                    e.printStackTrace(); // Gérez l'erreur (ex: afficher une alerte)
                    return null;
                });
        etudiantTable.refresh();
    }


    public void loadEditEvaluation(int evaluationId) {
        evaluationRepository.getEvaluation(evaluationId).
                thenAccept(evaluation -> {
                    // Mise à jour UI Utilisateur
                    Platform.runLater(() -> {
                        pageTitle.setText("Évaluation : " + evaluation.getTitre() + " du " + evaluation.getDate());
                        this.currentEvaluation = evaluation;

                        coefField.setText(String.valueOf(evaluation.getCoefficient()));
                        datePicker.setValue(LocalDate.parse(evaluation.getDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                        titreField.setText(evaluation.getTitre());
                        matiereComboBox.setValue(evaluation.getMatiere());
                        enseignantComboBox.setValue(evaluation.getEnseignant());
                        noteTypeComboBox.setValue(evaluation.getNoteType());

                        saveButton.setOnAction(event -> handleUpdate());
                        saveButton.setText("Mettre à jour");

                        ObservableList<EvaluationRow> evaluationRow = FXCollections.observableArrayList();;
                        for (Note note : evaluation.getNotes()) {
                            evaluationRow.add(new EvaluationRow(note.getEleve(), note));
                        }
                        etudiantTable.setItems(evaluationRow);
                    });

                })
                .exceptionally(e -> {
                    e.printStackTrace(); // Gérez l'erreur (ex: afficher une alerte)
                    return null;
                });
        etudiantTable.refresh();
    }

    @FXML
    public void initialize() {

        // Mapping des colonnes
        nomColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNom()));

        prenomColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPrenom()));

        /*----FORMATTAGE DES COMBOBOX----*/

        // Sécurisation du datePicker en empêchant toute saisie invalide
        datePicker.getEditor().addEventFilter(KeyEvent.KEY_RELEASED, e -> {
            String text = datePicker.getEditor().getText();

            if (!text.matches("[0-9/]*")) {
                datePicker.getEditor().setText(text.replaceAll("[^0-9/]", ""));
            }
        });

        noteTypeComboBox.setCellFactory(lv -> new ListCell<NoteType>() {
            @Override
            protected void updateItem(NoteType item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getLibelle());
            }
        });
        noteTypeComboBox.setButtonCell(noteTypeComboBox.getCellFactory().call(null));

        enseignantComboBox.setCellFactory(lv -> new ListCell<User>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getPrenom() + " " + item.getNom());
            }
        });
        enseignantComboBox.setButtonCell(enseignantComboBox.getCellFactory().call(null));

        // Rendre la colonne éditable
        etudiantTable.setEditable(true);

        noteColumn.setCellValueFactory(data -> {
            Double noteValue = data.getValue().getNoteValeur();
            return new SimpleStringProperty(noteValue == null ? null : String.valueOf(noteValue));
        });

        noteColumn.setCellFactory(column -> new TableCell<EvaluationRow, String>() {
            private final TextField textField = new TextField();
            private final Label label = new Label();

            {
                textField.setPromptText("—");
                textField.setPrefWidth(column.getPrefWidth());

                textField.textProperty().addListener((obs, oldValue, newValue) -> {
                    if (!newValue.matches("\\d*\\.?\\d*")) {
                        textField.setText(oldValue);
                    }
                });

                textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                    if (!isNowFocused) {
                        commitEdit();
                    }
                });

                textField.setOnAction(event -> commitEdit());
            }

            private void commitEdit() {
                String text = textField.getText();
                if (text.isEmpty()) {
                    getTableView().getItems().get(getIndex()).setNoteValeur(null);
                } else {
                    try {
                        double value = Double.parseDouble(text);
                        if (value >= 0 && value <= 20) {
                            getTableView().getItems().get(getIndex()).setNoteValeur(value);
                        } else {
                            textField.setText("");
                        }
                    } catch (NumberFormatException e) {
                        textField.setText("");
                    }
                }
            }

            @Override
            protected void updateItem(String note, boolean empty) {
                super.updateItem(note, empty);

                // Nettoyer les styles de couleur du label
                label.getStyleClass().removeAll("best-note-text", "worst-note-text");
                // Nettoyer les styles de couleur de la cellule
                getStyleClass().removeAll("best-note-cell", "worst-note-cell");

                if (empty) {
                    setGraphic(null);
                } else {
                    String displayText = note == null ? "—" : String.valueOf(note);

                    // Vérifier si la table est éditable
                    if (getTableView() != null && getTableView().isEditable()) {
                        // Mode édition: on affiche le TextField sans coloration
                        textField.setText(note == null ? "" : String.valueOf(note));
                        setGraphic(textField);
                        setStyle(null);
                    } else {
                        // Mode lecture (View/Edit après l'initialisation): appliquer la coloration
                        label.setText(displayText);
                        setGraphic(label);

                        // LOGIQUE DE COULEUR
                        if (note != null) {
                            try {
                                double noteValue = Double.parseDouble(note);

                                // On détermine la note max/min parmi toutes les notes visibles
                                double maxNote = etudiantTable.getItems().stream()
                                        .map(EvaluationRow::getNoteValeur)
                                        .filter(Objects::nonNull)
                                        .mapToDouble(Double::doubleValue)
                                        .max()
                                        .orElse(Double.NaN);

                                double minNote = etudiantTable.getItems().stream()
                                        .map(EvaluationRow::getNoteValeur)
                                        .filter(Objects::nonNull)
                                        .mapToDouble(Double::doubleValue)
                                        .min()
                                        .orElse(Double.NaN);

                                if (noteValue == maxNote && !Double.isNaN(maxNote)) {
                                    // Application de la couleur pour le texte de la meilleure note
                                    label.getStyleClass().add("best-note-text");
                                } else if (noteValue == minNote && !Double.isNaN(minNote)) {
                                    // Application de la couleur pour le texte de la pire note
                                    label.getStyleClass().add("worst-note-text");
                                }
                            } catch (NumberFormatException ignored) {
                                // La note n'est pas un nombre, ignorer la coloration
                            }
                        }
                    }
                }
            }
        });

        // Chargement des données du formulaire
        loadUsersList();
        loadMatieresList();
        loadNoteTypesList();
    }

    /**
     * Regroupe la configuration des 3 ComboBox (rendu personnalisé).
     * Extrait de initialize() pour alléger la méthode principale.
     */
    private void configureComboBoxes() {
        noteTypeComboBox.setCellFactory(lv -> new ListCell<NoteType>() {
            @Override
            protected void updateItem(NoteType item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getLibelle());
            }
        });
        noteTypeComboBox.setButtonCell(noteTypeComboBox.getCellFactory().call(null));

        enseignantComboBox.setCellFactory(lv -> new ListCell<User>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getPrenom() + " " + item.getNom());
            }
        });
        enseignantComboBox.setButtonCell(enseignantComboBox.getCellFactory().call(null));
    }

    /**
     * Construit un objet Evaluation à partir des valeurs saisies dans le formulaire.
     * Utilisé à la fois par handleSave() et handleUpdate() pour éviter la duplication.
     *
     * @param base Evaluation de base à remplir (new Evaluation() pour une création,
     *             currentEvaluation pour une mise à jour)
     * @return l'objet Evaluation rempli avec les données du formulaire
     */
    private Evaluation buildEvaluationFromForm(Evaluation base) {
        base.setTitre(titreField.getText());
        base.setEnseignant(enseignantComboBox.getValue());
        base.setMatiere(matiereComboBox.getValue());
        base.setNoteType(noteTypeComboBox.getValue());
        base.setCoefficient(Double.parseDouble(coefField.getText()));
        base.setDate(datePicker.getValue().toString());
        return base;
    }




    /* Formulaire de saisie de note */

    @FXML
    private TextField titreField;
    @FXML
    private TextField coefField;
    @FXML
    private DatePicker datePicker;
    @FXML
    private TextArea commentaireField;

    @FXML
    private ComboBox<Matiere> matiereComboBox;
    @FXML
    private ComboBox<User> enseignantComboBox;
    @FXML
    private ComboBox<NoteType> noteTypeComboBox;

    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;


    @FXML
    private void handleSave() {
        Evaluation evaluation = buildEvaluationFromForm(new Evaluation());

        // Récupérer les notes saisies dans le tableau
        ArrayList<Note> notes = new ArrayList<>();
        for (EvaluationRow row : etudiantTable.getItems()) {
            if (row.getNote() != null) {
                Note note = new Note();
                note.setEleve(row.getEleve());
                note.setEvaluation(evaluation);
                note.setValeur(row.getNoteValeur());
                notes.add(note);
            }
        }
        evaluation.setNotes(notes);

        evaluationRepository.createEvaluation(evaluation)
                .thenAccept(eval -> Platform.runLater(() -> {
                    AlertHelper.showInformation("Evaluation sauvegardé avec succès.");
                    mainLayoutController.showEvaluationList();
                    clearForm();
                }))
                .exceptionally(e -> {
                    e.printStackTrace();
                    Platform.runLater(() -> AlertHelper.showError("Erreur lors de la sauvegarde : " + e.getMessage()));
                    return null;
                });
    }


    @FXML
    private void handleUpdate() {
        buildEvaluationFromForm(currentEvaluation);

        // Mise à jour des notes existantes depuis le tableau
        for (EvaluationRow row : etudiantTable.getItems()) {
            if (row.getNote() != null) {
                row.getNote().setValeur(row.getNoteValeur());
            }
        }

        evaluationRepository.updateEvaluation(currentEvaluation)
                .thenAccept(eval -> Platform.runLater(() -> {
                    AlertHelper.showInformation("Evaluation mise à jour avec succès.");
                    mainLayoutController.showEvaluationList();
                    clearForm();
                }))
                .exceptionally(e -> {
                    e.printStackTrace();
                    Platform.runLater(() -> AlertHelper.showError("Erreur lors de la mise à jour : " + e.getMessage()));
                    return null;
                });
    }

    @FXML
    private void handleCancel() {
        // Confirmer l'annulation si des données ont été saisies
        if (hasUnsavedData()) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmer l'annulation");
            confirm.setHeaderText(null);
            confirm.setContentText("Des modifications non sauvegardées seront perdues. Continuer ?");

            confirm.showAndWait()
                    .filter(response -> response == ButtonType.OK)
                    .ifPresent(response -> mainLayoutController.showEvaluationList());
        } else {
            mainLayoutController.showEvaluationList();
        }
    }

    private boolean hasUnsavedData() {
        return (titreField.getText() != null && !titreField.getText().isEmpty()) ||
                (matiereComboBox.getValue() != null) ||
                (noteTypeComboBox.getValue() != null) ||
                etudiantTable.getItems().stream().anyMatch(etudiant -> etudiant.getNote() != null);
    }


    private MainLayoutController mainLayoutController;

    public void setMainLayoutController(MainLayoutController controller) {
        this.mainLayoutController = controller;
    }

    public void loadUsersList() {
        // Appel au service pour l'utilisateur
        userRepository.getUsersList()
                .thenAccept(users -> {
                    // Mise à jour UI Utilisateur
                    Platform.runLater(() -> {
                        // Filtrer les utilisateurs par rôle
                        List<User> eleves = users.stream().filter(u -> "ETUDIANT".equalsIgnoreCase(u.getRole().getName()))
                                .collect(Collectors.toList());

                        List<User> enseignants = users.stream().filter(u -> "ENSEIGNANT".equalsIgnoreCase(u.getRole().getName()))
                                .collect(Collectors.toList());

                        List<EvaluationRow> elevesRow = new ArrayList<>();

                        eleves.forEach(e -> {
                            Note newNote = new Note();
                            newNote.setEleve(e);
                            EvaluationRow evalRow = new EvaluationRow(e, newNote);
                            elevesRow.add(evalRow);
                        });

                        // Mettre les utilisateurs dans les ComboBox
                        enseignantComboBox.getItems().setAll(enseignants);

                        etudiantTable.getItems().setAll(elevesRow);

                        // Personnaliser l'affichage des ComboBox pour afficher le nom complet
                        enseignantComboBox.setCellFactory(lv -> new ListCell<User>() {
                            @Override
                            protected void updateItem(User item, boolean empty) {
                                super.updateItem(item, empty);
                                setText(empty || item == null ? null : item.getPrenom() + " " + item.getNom());
                            }
                        });

                        // Rendre l'affichage correct pour le bouton du ComboBox (afficher le nom complet)
                        enseignantComboBox.setButtonCell(enseignantComboBox.getCellFactory().call(null));

                        // Si l'utilisateur est un enseignant connecté, sélectionner son nom dans le ComboBox
                        if ("ENSEIGNANT".equalsIgnoreCase(AuthService.getCurrentUser().getRole().getLibelle())) {
                            // Trouver l'objet User correspondant à l'enseignant
                            User enseignant = enseignants.stream().filter(
                                            u -> (u.getPrenom() + " " + u.getNom()).equals(AuthService.getCurrentUser().getPrenom() + " " + AuthService.getCurrentUser().getNom()))
                                    .findFirst().orElse(null);

                            if (enseignant != null) {
                                enseignantComboBox.setValue(enseignant);
                                enseignantComboBox.setDisable(true);
                            }
                        }
                    });

                })
                .exceptionally(e -> {
                    e.printStackTrace(); // Gérez l'erreur (ex: afficher une alerte)
                    return null;
                });
    }

    public void loadMatieresList() {
        // Appel au service pour l'utilisateur
        matiereRepository.getMatieresList()
                .thenAccept(matieres -> {
                    // Mise à jour UI Utilisateur
                    Platform.runLater(() -> {
                        // Ajouter les objets Matiere directement au ComboBox
                        matiereComboBox.getItems().setAll(matieres);

                        // Afficher uniquement le libellé dans la liste déroulante
                        matiereComboBox.setCellFactory(lv -> new ListCell<Matiere>() {
                            @Override
                            protected void updateItem(Matiere item, boolean empty) {
                                super.updateItem(item, empty);
                                setText(empty || item == null ? null : item.getLibelle());
                            }
                        });

                        // Rendu du bouton du ComboBox
                        matiereComboBox.setButtonCell(matiereComboBox.getCellFactory().call(null));
                    });

                })
                .exceptionally(e -> {
                    e.printStackTrace(); // Gérez l'erreur (ex: afficher une alerte)
                    return null;
                });
    }


    public void loadNoteTypesList() {
        // Appel au service pour l'utilisateur
        noteTypeRepository.getNoteTypesList()
                .thenAccept(noteTypes -> {
                    // Mise à jour UI Utilisateur
                    Platform.runLater(() -> {
                        // Ajouter les objets NoteType directement au ComboBox
                        noteTypeComboBox.getItems().setAll(noteTypes);

                        // Afficher uniquement le libellé dans la liste déroulante
                        noteTypeComboBox.setCellFactory(lv -> new ListCell<NoteType>() {
                            @Override
                            protected void updateItem(NoteType item, boolean empty) {
                                super.updateItem(item, empty);
                                setText(empty || item == null ? null : item.getLibelle());
                            }
                        });

                        // Rendu du bouton du ComboBox
                        noteTypeComboBox.setButtonCell(noteTypeComboBox.getCellFactory().call(null));
                    });

                })
                .exceptionally(e -> {
                    e.printStackTrace(); // Gérez l'erreur (ex: afficher une alerte)
                    return null;
                });
    }
}