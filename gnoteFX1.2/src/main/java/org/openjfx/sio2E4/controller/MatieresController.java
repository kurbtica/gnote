package org.openjfx.sio2E4.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.application.Platform;
import javafx.scene.control.cell.TextFieldTableCell;

import org.openjfx.sio2E4.model.Matiere;
import org.openjfx.sio2E4.repository.MatiereRepository;
import org.openjfx.sio2E4.util.AlertHelper;

public class MatieresController {

    @FXML
    private TableView<Matiere> matieresTable;

    @FXML
    private TableColumn<Matiere, String> libelleColumn;

    @FXML
    private TextField libelleField;

    // Injection du service
    private final MatiereRepository matiereRepository = new MatiereRepository();

    @FXML
    private void initialize() {
        // Active l'édition des cellules dans la colonne
        libelleColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        libelleColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLibelle()));

        libelleColumn.setOnEditCommit(event -> {
            Matiere matiere = event.getRowValue();
            String newLibelle = event.getNewValue();
            System.out.println("Modification détectée: " + newLibelle);
            matiere.setLibelle(newLibelle);
            updateMatiere(matiere);
        });

        libelleColumn.setResizable(false);
        libelleColumn.setPrefWidth(9999);

        matieresTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        matieresTable.setEditable(true);

        loadMatieresList();
    }

    public void loadMatieresList() {
        // Appel au service pour l'utilisateur
        matiereRepository.getMatieresList()
                .thenAccept(matieres -> {
                    // Mise à jour UI Utilisateur
                    Platform.runLater(() -> {
                        matieresTable.getItems().setAll(matieres);
                    });

                })
                .exceptionally(e -> {
                    e.printStackTrace(); // Gérez l'erreur (ex: afficher une alerte)
                    return null;
                });
    }

    @FXML
    private void handleAddMatiere() {
        Matiere newMatiere = new Matiere();
        newMatiere.setLibelle(libelleField.getText());
        matiereRepository.createMatiere(newMatiere)
                .thenAccept(success -> {
                    Platform.runLater(() -> {
                        if (success) {
                            AlertHelper.showInformation("Matière ajouté avec succès !");
                            loadMatieresList(); // Rafraîchir le tableau
                        } else {
                            AlertHelper.showError("Erreur lors de l'ajout de la matière.");
                        }
                    });
                })
                .exceptionally(e -> {
                    Platform.runLater(() -> AlertHelper.showError("Erreur technique : " + e.getMessage()));
                    return null;
                });
    }

    // Méthode pour supprimer une matière
    @FXML
    private void handleDeleteMatiere() {
        Matiere selectedMatiere = matieresTable.getSelectionModel().getSelectedItem();
        matiereRepository.deleteMatiere(selectedMatiere.getId())
                .thenAccept(success -> {
                    Platform.runLater(() -> {
                        if (success) {
                            AlertHelper.showInformation("Matière supprimé avec succès.");
                            loadMatieresList(); // Rafraîchir le tableau
                        } else {
                            AlertHelper.showError("Impossible de supprimer cette matière.");
                        }
                    });
                })
                .exceptionally(e -> {
                    Platform.runLater(() -> AlertHelper.showError("Erreur technique : " + e.getMessage()));
                    return null;
                });
    }

    @FXML
    private void handleEditMatiere(TableColumn.CellEditEvent<Matiere, String> event) {
        updateMatiere(event.getRowValue());
    }

    private void updateMatiere(Matiere matiere) {
        matiereRepository.updateMatiere(matiere)
                .thenAccept(success -> {
                    Platform.runLater(() -> {
                        if (success) {
                            AlertHelper.showInformation("Matière modifié avec succès !");
                            loadMatieresList(); // Rafraîchir le tableau
                        } else {
                            AlertHelper.showError("Erreur lors de la modification.");
                        }
                    });
                })
                .exceptionally(e -> {
                    Platform.runLater(() -> AlertHelper.showError("Erreur technique : " + e.getMessage()));
                    return null;
                });
    }
}
