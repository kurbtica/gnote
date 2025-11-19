package org.openjfx.sio2E4.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.openjfx.sio2E4.constants.APIConstants;
import org.openjfx.sio2E4.model.*;
import org.openjfx.sio2E4.service.AuthService;
import org.openjfx.sio2E4.service.LocalStorageService;
import org.openjfx.sio2E4.service.NetworkService;
import org.openjfx.sio2E4.util.AlertHelper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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

    private final String API_URL = "http://localhost:8080/api/notes";
    private final String BEARER_TOKEN = "Bearer " + AuthService.getToken();

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
        if (NetworkService.isOnline()) {
            // TODO Méthode via API a faire
        } else {
            Evaluation evaluation = LocalStorageService.findEvaluationById(evaluationId);
            this.currentEvaluation = evaluation;
            System.out.println("Chargement de l'évaluation");
            try {
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

            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
        etudiantTable.refresh();
    }

    public void loadEditEvaluation(int evaluationId) {
        if (NetworkService.isOnline()) {
            // TODO Méthode via API a faire
        } else {
            Evaluation evaluation = LocalStorageService.findEvaluationById(evaluationId);
            this.currentEvaluation = evaluation;
            System.out.println("Chargement de l'évaluation");
            try {
                Platform.runLater(() -> {
                    pageTitle.setText("Évaluation : " + evaluation.getTitre() + " du " + evaluation.getDate());

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

            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
        etudiantTable.refresh();
    }

    @FXML
    public void initialize() {

        // Mapping des colonnes
        nomColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNom()));

        prenomColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPrenom()));

        /*----FORMATTAGE DES COMBOBOX----*/

        noteTypeComboBox.setCellFactory(lv -> new ListCell<NoteType>() {
            @Override
            protected void updateItem(NoteType item, boolean empty) {
                super.updateItem(item, empty);
                // Affiche le libellé ou "vide" si l'élément est null ou la cellule vide
                setText(empty || item == null ? null : item.getLibelle());
            }
        });
        noteTypeComboBox.setButtonCell(noteTypeComboBox.getCellFactory().call(null)); // Rendu du bouton du ComboBox

        enseignantComboBox.setCellFactory(lv -> new ListCell<User>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getPrenom() + " " + item.getNom());
            }
        });
        enseignantComboBox.setButtonCell(enseignantComboBox.getCellFactory().call(null)); // Rendu du bouton du ComboBox

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

                if (empty) {
                    setGraphic(null);
                } else {
                    String displayText = note == null ? "—" : String.valueOf(note);

                    // Vérifier si la table est éditable
                    if (getTableView() != null && getTableView().isEditable()) {
                        textField.setText(note == null ? "" : String.valueOf(note));
                        setGraphic(textField);
                    } else {
                        label.setText(displayText);
                        setGraphic(label);
                    }
                }
            }
        });

        // Chargement des données du formulaire
        fetchUsers();
        fetchMatieres();
        fetchNoteTypes();
    }

    /*@FXML
    private Label meilleurEleveLabel; // Le Label qui affichera l'élève avec la meilleure moyenne

    private void eleveAvecMeilleureMoyenne(List<Note> notes) {
        // Map pour stocker les totaux des valeurs pondérées et des coefficients par élève
        Map<Integer, Double> totalNotes = new HashMap<>();
        Map<Integer, Double> totalCoefficients = new HashMap<>();

        // Parcours des notes pour calculer les totaux
        for (Note note : notes) {
            int eleveId = note.getEleve().getId();
            double valeur = note.getValeur();
            double coefficient = note.getEvaluation().getCoefficient();

            // Ajoute la valeur pondérée à l'élève
            totalNotes.put(eleveId, totalNotes.getOrDefault(eleveId, 0.0) + (valeur * coefficient));
            totalCoefficients.put(eleveId, totalCoefficients.getOrDefault(eleveId, 0.0) + coefficient);
        }

        // Calculer la moyenne pondérée pour chaque élève
        Map<Integer, Double> moyennes = new HashMap<>();
        for (int eleveId : totalNotes.keySet()) {
            double moyenne = totalNotes.get(eleveId) / totalCoefficients.get(eleveId);
            moyennes.put(eleveId, moyenne);
        }

        // Trouver l'élève avec la meilleure moyenne
        int meilleurEleveId = -1;
        double meilleureMoyenne = -1;
        for (Map.Entry<Integer, Double> entry : moyennes.entrySet()) {
            if (entry.getValue() > meilleureMoyenne) {
                meilleureMoyenne = entry.getValue();
                meilleurEleveId = entry.getKey();
            }
        }

        // Trouver l'élève correspondant à l'ID avec la meilleure moyenne
        User meilleurEleve = null;
        for (Note note : notes) {
            if (note.getEleve().getId() == meilleurEleveId) {
                meilleurEleve = note.getEleve();
                break;
            }
        }

        // Trouver la date la plus récente parmi les notes
        Instant dateRecenteInstant = null;
        DateTimeFormatter formatterAffichage = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (Note note : notes) {
            String dateStr = note.getEvaluation().getDate();

            try {
                Instant instantNote;

                if (dateStr.contains("T")) {
                    // Cas avec date + heure + offset, ex: "2025-05-23T12:26:22.884+00:00"
                    instantNote = OffsetDateTime.parse(dateStr).toInstant();
                } else {
                    // Cas avec date seule, ex: "2024-03-17"
                    LocalDate ld = LocalDate.parse(dateStr);
                    instantNote = ld.atStartOfDay(ZoneId.systemDefault()).toInstant();
                }

                if (dateRecenteInstant == null || instantNote.isAfter(dateRecenteInstant)) {
                    dateRecenteInstant = instantNote;
                }
            } catch (DateTimeParseException e) {
                System.err.println("Erreur de parsing date : " + dateStr);
            }
        }

        // Mettre à jour le Label avec l'élève ayant la meilleure moyenne et la date la plus récente
        if (meilleurEleve != null) {
            String texte = "Major de promotion : " + meilleurEleve.getPrenom() + " " + meilleurEleve.getNom()
                    + " avec une moyenne de " + String.format("%.2f", meilleureMoyenne);

            if (dateRecenteInstant != null) {
                ZonedDateTime dateRecenteLocal = dateRecenteInstant.atZone(ZoneId.systemDefault());
                texte += " | Date la plus récente : " + dateRecenteLocal.format(formatterAffichage);
            }

            final String texteFinal = texte; // variable finale pour la lambda
            Platform.runLater(() -> meilleurEleveLabel.setText(texteFinal));
        }
    }*/


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
        // Récupérer les données du formulaire
        String titre = titreField.getText();
        User enseignant = enseignantComboBox.getValue();
        Matiere matiere = matiereComboBox.getValue();
        NoteType noteType = noteTypeComboBox.getValue();

        double coefficient = Double.parseDouble(coefField.getText());
        String date = datePicker.getValue().toString();
        String commentaire = commentaireField.getText();

        // Récupérer toutes les notes des étudiants
        Map<User, Double> notesMap = new HashMap<>();
        for (EvaluationRow etudiant : etudiantTable.getItems()) {
            if (etudiant.getNote() != null) {
                notesMap.put(etudiant.getEleve(), etudiant.getNoteValeur());
            }
        }


        if (NetworkService.isOnline()) {
            // TODO revoir le fonctionnement de l'api pour fusionner plusieurs ajout de note en meme temps
        } else {
            Evaluation evaluation = new Evaluation();
            evaluation.setId(LocalStorageService.getNextID(evaluation));
            evaluation.setEnseignant(enseignant);
            evaluation.setMatiere(matiere);
            evaluation.setNoteType(noteType);

            evaluation.setCoefficient(coefficient);
            evaluation.setDate(date);
            evaluation.setTitre(titre);

            ArrayList<Note> notes = new ArrayList<>();
            evaluation.setNotes(notes);
            LocalStorageService.save(evaluation);

            notesMap.forEach((user, note) -> {
                Note newNote = new Note();
                newNote.setEleve(user);
                newNote.setEvaluation(evaluation);

                newNote.setValeur(note);
                notes.add(newNote);

                LocalStorageService.save(newNote);
            });

            evaluation.setNotes(notes);
            LocalStorageService.update(evaluation);


            Platform.runLater(() -> {
                AlertHelper.showInformation("Evaluation ajouté en local (mode hors ligne).");
                mainLayoutController.showEvaluationList();
                clearForm();
            });
        }
    }


    @FXML
    private void handleUpdate() {
        // Récupérer les données du formulaire
        String titre = titreField.getText();
        User enseignant = enseignantComboBox.getValue();
        Matiere matiere = matiereComboBox.getValue();
        NoteType noteType = noteTypeComboBox.getValue();

        double coefficient = Double.parseDouble(coefField.getText());
        String date = datePicker.getValue().toString();
        String commentaire = commentaireField.getText();

        if (NetworkService.isOnline()) {
            // TODO revoir le fonctionnement de l'api pour fusionner plusieurs ajout de note en meme temps
        } else {
            currentEvaluation.setEnseignant(enseignant);
            currentEvaluation.setMatiere(matiere);
            currentEvaluation.setNoteType(noteType);

            currentEvaluation.setCoefficient(coefficient);
            currentEvaluation.setDate(date);
            currentEvaluation.setTitre(titre);

            LocalStorageService.update(currentEvaluation);

            // Récupérer toutes les notes des étudiants et mise a jour de la note
            for (EvaluationRow evaluationRow : etudiantTable.getItems()) {
                if (evaluationRow.getNote() != null) {
                    evaluationRow.getNote().setValeur(evaluationRow.getNoteValeur());
                }
            }

            LocalStorageService.update(currentEvaluation);


            Platform.runLater(() -> {
                AlertHelper.showInformation("Evaluation ajouté en local (mode hors ligne).");
                mainLayoutController.showEvaluationList();
                clearForm();
            });
        }
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

    private void fetchUsers() {
        if (NetworkService.isOnline()) {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(APIConstants.USERS))
                    .header("Authorization", BEARER_TOKEN).GET().build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(HttpResponse::body)
                    .thenAccept(this::parseUsers).exceptionally(e -> {
                        e.printStackTrace();
                        return null;
                    });
        } else {
            parseUsers("");
        }
    }

    private void parseUsers(String responseBody) {
        User user = AuthService.getCurrentUser();
        ObjectMapper mapper = new ObjectMapper();
        if (NetworkService.isOnline()) {
            try {
                List<User> users = Arrays.asList(mapper.readValue(responseBody, User[].class));

                // Filtrer les utilisateurs par rôle
                List<User> eleves = users.stream().filter(u -> "ETUDIANT".equalsIgnoreCase(u.getRole().getLibelle()))
                        .collect(Collectors.toList());

                List<User> enseignants = users.stream().filter(u -> "ENSEIGNANT".equalsIgnoreCase(u.getRole().getLibelle()))
                        .collect(Collectors.toList());

                // Mettre à jour les ComboBox dans le thread JavaFX
                Platform.runLater(() -> {
                    // Mettre les utilisateurs dans les ComboBox
                    enseignantComboBox.getItems().setAll(enseignants);

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
                    if ("ENSEIGNANT".equalsIgnoreCase(user.getRole().getLibelle())) {
                        // Trouver l'objet User correspondant à l'enseignant
                        User enseignant = enseignants.stream().filter(
                                        u -> (u.getPrenom() + " " + u.getNom()).equals(user.getPrenom() + " " + user.getNom()))
                                .findFirst().orElse(null);

                        if (enseignant != null) {
                            enseignantComboBox.setValue(enseignant);
                            enseignantComboBox.setDisable(true);
                        }
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Mode hors ligne activé — chargement des utilisateur en local");
            ArrayList<User> localUsers = LocalStorageService.loadUsers();

            // Filtrer les utilisateurs par rôle
            List<User> eleves = localUsers.stream().filter(u -> u.getRole().getLibelle().equalsIgnoreCase("ETUDIANT"))
                    .collect(Collectors.toList());

            List<User> enseignants = localUsers.stream().filter(u -> u.getRole().getLibelle().equalsIgnoreCase("ENSEIGNANT"))
                    .collect(Collectors.toList());

            List<EvaluationRow> elevesRow = new ArrayList<>();

            eleves.forEach(e -> {
                Note newNote = new Note();
                newNote.setEleve(e);
                EvaluationRow evalRow = new EvaluationRow(e, newNote);
                elevesRow.add(evalRow);
            });

            // Mettre à jour les ComboBox dans le thread JavaFX
            Platform.runLater(() -> {
                // Mettre les utilisateurs dans les ComboBox
                enseignantComboBox.getItems().setAll(enseignants);

                etudiantTable.getItems().setAll(elevesRow);

                // Rendre l'affichage correct pour le bouton du ComboBox (afficher le nom
                // complet)
                enseignantComboBox.setButtonCell(enseignantComboBox.getCellFactory().call(null));

                // Si l'utilisateur est un enseignant connecté, sélectionner son nom dans le
                // ComboBox
                if (user.getRole().getLibelle().equalsIgnoreCase("ENSEIGNANT")) {
                    // Trouver l'objet User correspondant à l'enseignant
                    User enseignant = enseignants.stream().filter(
                                    u -> (u.getPrenom() + " " + u.getNom()).equals(user.getPrenom() + " " + user.getNom()))
                            .findFirst().orElse(null);

                    if (enseignant != null) {
                        enseignantComboBox.setValue(enseignant);
                        enseignantComboBox.setDisable(true);
                    }
                }
            });
        }
    }

    private void fetchMatieres() {
        if (NetworkService.isOnline()) {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(APIConstants.MATIERES))
                    .header("Authorization", BEARER_TOKEN).GET().build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(HttpResponse::body)
                    .thenAccept(this::parseMatieres).exceptionally(e -> {
                        e.printStackTrace();
                        return null;
                    });
        } else {
            System.out.println("Mode hors ligne activé — chargement des matières local");
            ArrayList<Matiere> localMatieres = LocalStorageService.loadMatieres();
            Platform.runLater(() -> matiereComboBox.getItems().setAll(localMatieres));
        }
    }



    private void parseMatieres(String responseBody) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            List<Matiere> matieres = Arrays.asList(mapper.readValue(responseBody, Matiere[].class));

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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void fetchNoteTypes() {
        if (NetworkService.isOnline()) {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(APIConstants.NOTE_TYPES))
                    .header("Authorization", BEARER_TOKEN).GET().build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(HttpResponse::body)
                    .thenAccept(this::parseNoteTypes).exceptionally(e -> {
                        e.printStackTrace();
                        return null;
                    });
        } else {
            System.out.println("Mode hors ligne activé — chargement des types de notes en local");
            ArrayList<NoteType> localNoteType = LocalStorageService.loadNoteTypes();
            Platform.runLater(() -> noteTypeComboBox.getItems().setAll(localNoteType));
        }
    }

    private void parseNoteTypes(String responseBody) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            List<NoteType> types = Arrays.asList(mapper.readValue(responseBody, NoteType[].class));

            Platform.runLater(() -> {
                // Ajouter les objets NoteType directement au ComboBox
                noteTypeComboBox.getItems().setAll(types);

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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
