package org.openjfx.sio2E4.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.layout.HBox;
import javafx.scene.shape.SVGPath;
import javafx.scene.control.Tooltip;
import org.openjfx.sio2E4.constants.StyleConstants;

import org.openjfx.sio2E4.model.Matiere;
import org.openjfx.sio2E4.repository.MatiereRepository;
import org.openjfx.sio2E4.repository.EvaluationRepository;
import org.openjfx.sio2E4.util.AlertHelper;
import java.util.Optional;
import javafx.scene.control.ButtonType;
import javafx.scene.control.cell.TextFieldTableCell;

public class MatieresController {

    @FXML
    private TableView<Matiere> matieresTable;

    @FXML
    private TableColumn<Matiere, String> libelleColumn;

    @FXML
    private TableColumn<Matiere, String> moyenneColumn;

    @FXML
    private TableColumn<Matiere, String> minColumn;

    @FXML
    private TableColumn<Matiere, String> maxColumn;

    @FXML
    private TableColumn<Matiere, Void> actionsColumn;

    @FXML
    private TextField libelleField;

    @FXML
    private Label statusLabel;

    // Injection des repositories
    private final MatiereRepository matiereRepository = new MatiereRepository();
    private final EvaluationRepository evaluationRepository = new EvaluationRepository();

    // Cache des évaluations pour calcul stats
    private java.util.List<org.openjfx.sio2E4.model.Evaluation> allEvaluations = new java.util.ArrayList<>();

    @FXML
    private void initialize() {
        // Configuration des colonnes
        libelleColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getLibelle()));
        libelleColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        libelleColumn.setOnEditCommit(event -> {
            Matiere matiere = event.getRowValue();
            matiere.setLibelle(event.getNewValue());
            updateMatiere(matiere);
        });

        // Colonnes de statistiques
        moyenneColumn.setCellValueFactory(data -> {
            double avg = calculateAverageForMatiere(data.getValue());
            return new javafx.beans.property.SimpleStringProperty(String.format("%.2f", avg));
        });

        minColumn.setCellValueFactory(data -> {
            double min = calculateMinForMatiere(data.getValue());
            return new javafx.beans.property.SimpleStringProperty(Double.isNaN(min) ? "-" : String.format("%.2f", min));
        });

        maxColumn.setCellValueFactory(data -> {
            double max = calculateMaxForMatiere(data.getValue());
            return new javafx.beans.property.SimpleStringProperty(Double.isNaN(max) ? "-" : String.format("%.2f", max));
        });

        // Colonne Actions
        setupActionsColumn();

        matieresTable.setEditable(true);
        loadData();
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(column -> new TableCell<Matiere, Void>() {
            private final Button editButton = new Button();
            private final Button deleteButton = new Button();
            private final HBox buttonsBox = new HBox(8);

            {
                // Icône SVG Pencil (Modifier)
                SVGPath editIcon = new SVGPath();
                editIcon.setContent("M15.728 9.686l-1.414-1.414L5 17.586V19h1.414l9.314-9.314zm1.414-1.414l1.414-1.414-1.414-1.414-1.414 1.414 1.414 1.414zM7.242 21H3v-4.243L16.435 3.322a1 1 0 0 1 1.414 0l2.829 2.829a1 1 0 0 1 0 1.414L7.243 21z");
                editIcon.setScaleX(0.8);
                editIcon.setScaleY(0.8);
                editIcon.setStyle(StyleConstants.ButtonActionsColumn.EDIT_BUTTON_ICON);
                editButton.setGraphic(editIcon);
                editButton.setStyle(StyleConstants.ButtonActionsColumn.EDIT_BUTTON);
                editButton.setTooltip(new Tooltip("Modifier le libellé"));

                // Icône SVG Trash (Supprimer)
                SVGPath deleteIcon = new SVGPath();
                deleteIcon.setContent("M5.5 5.5A.5.5 0 0 1 6 6v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5zm2.5 0a.5.5 0 0 1 .5.5v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5zm3 .5a.5.5 0 0 0-1 0v6a.5.5 0 0 0 1 0V6z M14.5 3a1 1 0 0 1-1 1H13v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V4h-.5a1 1 0 0 1-1-1V2a1 1 0 0 1 1-1H6a1 1 0 0 1 1-1h2a1 1 0 0 1 1 1h3.5a1 1 0 0 1 1 1v1zM4.118 4 4 4.059V13a1 1 0 0 0 1 1h6a1 1 0 0 0 1-1V4.059L11.882 4H4.118zM2.5 3V2h11v1h-11z");
                deleteIcon.setScaleX(0.8);
                deleteIcon.setScaleY(0.8);
                deleteIcon.setStyle(StyleConstants.ButtonActionsColumn.DELETE_BUTTON_ICON);
                deleteButton.setGraphic(deleteIcon);
                deleteButton.setStyle(StyleConstants.ButtonActionsColumn.DELETE_BUTTON);
                deleteButton.setTooltip(new Tooltip("Supprimer cette matière"));

                // Effets hover pour le bouton Modifier
                editButton.setOnMouseEntered(e -> editButton.setStyle(StyleConstants.ButtonActionsColumn.EDIT_BUTTON_HOVER));
                editButton.setOnMouseExited(e -> editButton.setStyle(StyleConstants.ButtonActionsColumn.EDIT_BUTTON));

                // Effets hover pour le bouton Supprimer
                deleteButton.setOnMouseEntered(e -> {
                    deleteButton.setStyle(StyleConstants.ButtonActionsColumn.DELETE_BUTTON_HOVER);
                    deleteIcon.setStyle(StyleConstants.ButtonActionsColumn.DELETE_BUTTON_ICON_HOVER);
                });
                deleteButton.setOnMouseExited(e -> {
                    deleteButton.setStyle(StyleConstants.ButtonActionsColumn.DELETE_BUTTON);
                    deleteIcon.setStyle(StyleConstants.ButtonActionsColumn.DELETE_BUTTON_ICON);
                });

                buttonsBox.setAlignment(Pos.CENTER);
                buttonsBox.getChildren().addAll(editButton, deleteButton);
                buttonsBox.setPadding(new Insets(0, 5, 0, 5));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null) {
                    setGraphic(null);
                } else {
                    Matiere matiere = getTableView().getItems().get(getIndex());
                    editButton.setOnAction(e -> matieresTable.edit(getIndex(), libelleColumn));
                    deleteButton.setOnAction(e -> handleDeleteMatiere(matiere));
                    setGraphic(buttonsBox);
                }
            }
        });
    }

    private void loadData() {
        // Charger les matières ET les évaluations pour les stats
        evaluationRepository.getEvaluationsList().thenAccept(evals -> {
            allEvaluations = evals;
            loadMatieresList();
        });
    }

    public void loadMatieresList() {
        matiereRepository.getMatieresList()
                .thenAccept(matieres -> {
                    Platform.runLater(() -> {
                        matieresTable.getItems().setAll(matieres);
                    });
                })
                .exceptionally(e -> {
                    e.printStackTrace();
                    return null;
                });
    }

    private double calculateAverageForMatiere(Matiere matiere) {
        return allEvaluations.stream()
                .filter(e -> e.getMatiere().getId().equals(matiere.getId()))
                .flatMap(e -> e.getNotes().stream())
                .mapToDouble(org.openjfx.sio2E4.model.Note::getValeur)
                .average()
                .orElse(0.0);
    }

    private double calculateMinForMatiere(Matiere matiere) {
        return allEvaluations.stream()
                .filter(e -> e.getMatiere().getId().equals(matiere.getId()))
                .flatMap(e -> e.getNotes().stream())
                .mapToDouble(org.openjfx.sio2E4.model.Note::getValeur)
                .min()
                .orElse(Double.NaN);
    }

    private double calculateMaxForMatiere(Matiere matiere) {
        return allEvaluations.stream()
                .filter(e -> e.getMatiere().getId().equals(matiere.getId()))
                .flatMap(e -> e.getNotes().stream())
                .mapToDouble(org.openjfx.sio2E4.model.Note::getValeur)
                .max()
                .orElse(Double.NaN);
    }

    @FXML
    private void handleAddMatiere() {
        String libelle = libelleField.getText();
        if (libelle == null || libelle.trim().isEmpty()) {
            AlertHelper.showError("Le nom de la matière ne peut pas être vide.");
            return;
        }

        Matiere newMatiere = new Matiere();
        newMatiere.setLibelle(libelle);
        matiereRepository.createMatiere(newMatiere)
                .thenAccept(success -> {
                    Platform.runLater(() -> {
                        if (success) {
                            libelleField.clear();
                            loadData();
                        } else {
                            AlertHelper.showError("Erreur lors de l'ajout.");
                        }
                    });
                });
    }

    private void handleDeleteMatiere(Matiere matiere) {
        if (matiere == null) return;
        
        Optional<ButtonType> result = AlertHelper.showConfirmation("Confirmation", 
                "Êtes-vous sûr de vouloir supprimer la matière '" + matiere.getLibelle() + "' ?");
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            matiereRepository.deleteMatiere(matiere.getId())
                    .thenAccept(success -> {
                        Platform.runLater(() -> {
                            if (success) {
                                loadData();
                            } else {
                                AlertHelper.showError("Impossible de supprimer cette matière.");
                            }
                        });
                    });
        }
    }

    private void updateMatiere(Matiere matiere) {
        matiereRepository.updateMatiere(matiere)
                .thenAccept(success -> {
                    Platform.runLater(() -> {
                        if (success) {
                            loadData();
                        } else {
                            AlertHelper.showError("Erreur lors de la modification.");
                        }
                    });
                });
    }
}
