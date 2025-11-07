package org.openjfx.sio2E4.service;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import org.openjfx.sio2E4.constants.StyleConstants;
import org.openjfx.sio2E4.model.MatiereRow;
import org.openjfx.sio2E4.model.Note;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NoteService {

    // --- Utilitaires logiques ---

    public static Map<String, List<Note>> groupNotesByMatiere(List<Note> notes) {
        return notes.stream()
                .collect(Collectors.groupingBy(n -> n.getMatiere().getLibelle()));
    }

    public static ObservableList<MatiereRow> buildMatiereRows(Map<String, List<Note>> notesParMatiere) {
        ObservableList<MatiereRow> data = FXCollections.observableArrayList();

        notesParMatiere.forEach((matiere, notesMatiere) -> {
            HBox notesBox = buildNotesHBox(notesMatiere);
            double moyenne = NoteService.calculateMoyenne(notesMatiere);
            data.add(new MatiereRow(matiere, moyenne, notesBox, "Not implemented"));
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
        Text coef = new Text("(" + note.getCoefficient() + ")");
        coef.setStyle(StyleConstants.COEFFICIENT_STYLE);

        HBox container = new HBox(2);
        container.setAlignment(Pos.CENTER_LEFT);
        container.setFillHeight(true);
        container.getChildren().addAll(valeur, coef);
        container.setStyle(StyleConstants.NOTE_CONTAINER_STYLE);

        Tooltip tooltip = new Tooltip(
                note.getCommentaire() +
                        "\nType: " + note.getNoteType().getLibelle() +
                        "\nDate: " + note.getDate() +
                        "\nEnseignant: " + note.getEnseignant().getNom().toUpperCase() + " " + note.getEnseignant().getPrenom()
        );
        Tooltip.install(container, tooltip);

        return container;
    }

    public static double calculateMoyenne(List<Note> notes) {
        double totalCoef = 0;
        double totalNotes = 0;

        for (Note note : notes) {
            totalCoef += note.getCoefficient();
            totalNotes += note.getValeur() * note.getCoefficient();
        }

        if (totalCoef == 0) {
            return 0; // Eviter une division par zéro si aucun coefficient n'est trouvé
        }

        // Arrondi a 2 chiffres après la virgule
        return Math.round((totalNotes / totalCoef) * 100) / 100.0;
    }
}
