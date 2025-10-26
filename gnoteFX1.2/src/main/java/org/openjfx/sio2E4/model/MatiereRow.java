package org.openjfx.sio2E4.model;

import javafx.scene.layout.HBox;

public class MatiereRow {
    private final String matiere;
    private final double moyenne;
    private final HBox notesHBox;
    private final String appreciations;

    public MatiereRow(String matiere, double moyenne, HBox notesHBox, String appreciations) {
        this.matiere = matiere;
        this.moyenne = moyenne;
        this.notesHBox = notesHBox;
        this.appreciations = appreciations;
    }

    public String getMatiere() {
        return matiere;
    }

    public double getMoyenne() {
        return moyenne;
    }

    public HBox getNotesHBox() {
        return notesHBox;
    }

    public String getAppreciations() {
        return appreciations;
    }
}

