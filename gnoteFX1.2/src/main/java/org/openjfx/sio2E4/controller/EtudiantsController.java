package org.openjfx.sio2E4.controller;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.openjfx.sio2E4.model.Etudiant;
import org.openjfx.sio2E4.repository.UserRepository;

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

    // Injection du service
    private final UserRepository etudiantRepository = new UserRepository();

    @FXML
    public void initialize() {
        nomColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNom()));
        prenomColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPrenom()));
        emailColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
        telephoneColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTelephone()));
        adresseColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAdresse()));
        roleColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRole().getLibelle()));

        loadEtudiantsList();
    }

    public void loadEtudiantsList() {
        // Appel au service pour l'utilisateur
        etudiantRepository.getUsersList()
                .thenAccept(users -> {
                    List<Etudiant> etudiants = users.stream()
                            .filter(u -> u.getRole() != null &&
                                    "ETUDIANT".equalsIgnoreCase(u.getRole().getLibelle()))
                            .map(u -> {
                                Etudiant e = new Etudiant();
                                e.setId(u.getId());
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
                    // Mise à jour UI Utilisateur
                    Platform.runLater(() -> {
                        etudiantsTable.getItems().setAll(etudiants);
                    });

                })
                .exceptionally(e -> {
                    e.printStackTrace(); // Gérez l'erreur (ex: afficher une alerte)
                    return null;
                });
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
