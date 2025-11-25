package org.openjfx.sio2E4.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.openjfx.sio2E4.model.User;
import org.openjfx.sio2E4.service.AuthService;
import org.openjfx.sio2E4.service.LocalStorageService;
import org.openjfx.sio2E4.service.NoteService;

import java.util.List;
import java.util.stream.Collectors;

public class HomeViewController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label subtitleLabel;

    @FXML
    private Label studentsCountLabel;

    @FXML
    private Label teachersCountLabel;

    @FXML
    private Label evaluationsCountLabel;

    @FXML
    private Label homeMessageLabel;

    public void initialize() {
        User current = AuthService.getCurrentUser();

        // Friendly header
        if (current != null) {
            welcomeLabel.setText("Bienvenue, " + current.getPrenom() + " " + current.getNom());
            subtitleLabel.setText("Rôle : " + (current.getRole() != null ? current.getRole().getLibelle() : "--"));
        } else {
            welcomeLabel.setText("Bienvenue dans l'application!");
            subtitleLabel.setText("Tableau de bord");
        }

        // Fill counts
        int studentsCount = LocalStorageService.loadEtudiants().size();
        int evaluationsCount = LocalStorageService.loadEvaluations().size();

        // Count teachers from users with role ENSEIGNANT (case-insensitive)
        List<User> users = LocalStorageService.loadUsers();
        long teachersCount = users.stream()
                .filter(u -> u.getRole() != null)
                .filter(u -> "ENSEIGNANT".equalsIgnoreCase(u.getRole().getLibelle()))
                .count();

        studentsCountLabel.setText(String.valueOf(studentsCount));
        teachersCountLabel.setText(String.valueOf(teachersCount));
        evaluationsCountLabel.setText(String.valueOf(evaluationsCount));

        // Home message: role-aware summary
        if (homeMessageLabel != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("Gnotes vous aide à gérer les matières, créer et suivre les évaluations, et consulter les relevés d'étudiants.");

            if (current != null && current.getRole() != null) {
                String role = current.getRole().getLibelle();
                if ("ENSEIGNANT".equalsIgnoreCase(role)) {
                    sb.append(" En tant qu'enseignant, vous pouvez créer des évaluations, consulter la moyenne de votre classe et ajouter des appréciations.");
                } else if ("ADMIN".equalsIgnoreCase(role)) {
                    sb.append(" En tant qu'admin, vous pouvez gérer les utilisateurs, les matières et administrer les évaluations de l'établissement.");
                } else if ("ETUDIANT".equalsIgnoreCase(role)) {
                    sb.append(" Vous pouvez consulter vos notes, relevés et appréciations par matière dans votre profil.");
                }
            }

            homeMessageLabel.setText(sb.toString());
        }

        // If current user is a student, show a small summary of their average per subject in subtitle
        if (current != null && current.getRole() != null && "ETUDIANT".equalsIgnoreCase(current.getRole().getLibelle())) {
            // compute student averages using loaded notes
            List<org.openjfx.sio2E4.model.Note> allNotes = LocalStorageService.loadNotes();
            List<org.openjfx.sio2E4.model.Note> myNotes = allNotes.stream()
                    .filter(n -> n.getEleve() != null && current.getId() == n.getEleve().getId())
                    .collect(Collectors.toList());

            if (!myNotes.isEmpty()) {
                double moy = NoteService.calculateMoyenne(myNotes);
                subtitleLabel.setText(subtitleLabel.getText() + " — Moyenne générale: " + moy);
            } else {
                subtitleLabel.setText(subtitleLabel.getText() + " — Pas encore de notes");
            }
        }
    }
}
