package org.openjfx.sio2E4.service;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import org.openjfx.sio2E4.constants.StyleConstants;
import org.openjfx.sio2E4.model.table.MatiereRow;
import org.openjfx.sio2E4.model.Note;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NoteService {

    // --- Utilitaires logiques ---

    /**
     * Regroupe une liste de notes par matière.
     * Cette méthode filtre automatiquement les notes incomplètes (sans évaluation ou matière).
     *
     * @param notes La liste brute de toutes les notes d'un élève.
     * @return Une Map où la clé est le nom de la matière et la valeur la liste des notes associées.
     */
    public static Map<String, List<Note>> groupNotesByMatiere(List<Note> notes) {
        if (notes == null) return Map.of();
        return notes.stream()
                .filter(n -> n.getEvaluation() != null && n.getEvaluation().getMatiere() != null)
                .collect(Collectors.groupingBy(n -> n.getEvaluation().getMatiere().getLibelle()));
    }

    public static ObservableList<MatiereRow> buildMatiereRows(Map<String, List<Note>> notesParMatiere) {
        ObservableList<MatiereRow> data = FXCollections.observableArrayList();

        notesParMatiere.forEach((matiere, notesMatiere) -> {
            HBox notesBox = buildNotesHBox(notesMatiere);
            double moyenne = NoteService.calculateMoyenne(notesMatiere);
            data.add(new MatiereRow(matiere, moyenne, notesBox, "Aucune appreciation renseigné"));
        });

        return data;
    }

    // --- UI Building ---

    private static HBox buildNotesHBox(List<Note> notesMatiere) {
        HBox notesBox = new HBox(5);
        notesBox.setAlignment(Pos.CENTER_LEFT);
        notesBox.setFillHeight(true);

        for (Note note : notesMatiere) {
            notesBox.getChildren().add(buildNoteContainer(note));
        }

        return notesBox;
    }

    private static HBox buildNoteContainer(Note note) {
        Text valeur = new Text(String.valueOf(note.getValeur()));
        Text coef = new Text("(" + note.getEvaluation().getCoefficient() + ")");
        coef.setStyle(StyleConstants.COEFFICIENT_STYLE);

        HBox container = new HBox(2);
        container.setAlignment(Pos.CENTER_LEFT);
        container.setFillHeight(true);
        container.getChildren().addAll(valeur, coef);
        container.setStyle(StyleConstants.NOTE_CONTAINER_STYLE);

        Tooltip tooltip = new Tooltip(
                note.getEvaluation().getTitre() +
                        "\nType: " + note.getEvaluation().getNoteType().getLibelle() +
                        "\nDate: " + note.getEvaluation().getDate() +
                        "\nEnseignant: " + note.getEvaluation().getEnseignant().getNom().toUpperCase() + " " + note.getEvaluation().getEnseignant().getPrenom()
        );
        Tooltip.install(container, tooltip);

        return container;
    }

    public static double calculateMoyenne(List<Note> notes) {
        double totalCoef = 0;
        double totalNotes = 0;

        for (Note note : notes) {
            totalCoef += note.getEvaluation().getCoefficient();
            totalNotes += note.getValeur() * note.getEvaluation().getCoefficient();
        }

        if (totalCoef == 0) {
            return 0; // Eviter une division par zéro si aucun coefficient n'est trouvé
        }

        // Arrondi a 2 chiffres après la virgule
        return Math.round((totalNotes / totalCoef) * 100) / 100.0;
    }

    /**
     * Calcule la moyenne générale d'un élève.
     * <p>
     * L'algorithme procède en deux étapes pour respecter la logique scolaire :
     * 1. Calcul de la moyenne de chaque matière (pondérée par les coefficients des devoirs).
     * 2. Moyenne arithmétique des moyennes des matières.
     * </p>
     *
     * @param notes La liste complète des notes de l'élève.
     * @return La moyenne générale arrondie à 2 décimales, ou 0.0 si aucune note valide n'est trouvée.
     */
    public static double calculateMoyenneGenerale(List<Note> notes) {
        if (notes == null || notes.isEmpty()) return 0.0;

        Map<String, List<Note>> notesParMatiere = groupNotesByMatiere(notes);

        double sommeDesMoyennes = 0;
        int nombreDeMatieres = 0;

        for (List<Note> notesDuneMatiere : notesParMatiere.values()) {
            double moyenneMatiere = calculateMoyenneUneMatiere(notesDuneMatiere);

            // On ne compte la matière que si une moyenne a pu être calculée
            if (moyenneMatiere >= 0) {
                sommeDesMoyennes += moyenneMatiere;
                nombreDeMatieres++;
            }
        }

        if (nombreDeMatieres == 0) return 0.0;

        return round(sommeDesMoyennes / nombreDeMatieres);
    }

    /**
     * Calcule la moyenne pondérée pour une liste de notes spécifique (généralement une seule matière).
     *
     * @param notes La liste des notes à traiter.
     * @return La moyenne précise (non arrondie), ou -1 si la somme des coefficients est nulle.
     */
    private static double calculateMoyenneUneMatiere(List<Note> notes) {
        double totalCoef = 0;
        double totalPoints = 0;

        for (Note note : notes) {
            double coef = note.getEvaluation().getCoefficient();
            double noteVal = note.getValeur();

            totalPoints += noteVal * coef;
            totalCoef += coef;
        }

        if (totalCoef == 0) return -1;

        return totalPoints / totalCoef;
    }

    /**
     * Arrondit un nombre décimal à deux chiffres après la virgule.
     *
     * @param value La valeur brute.
     * @return La valeur arrondie (ex: 15.6666 -> 15.67).
     */
    private static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
