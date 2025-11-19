package org.openjfx.sio2E4.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class EvaluationRow {
    private User eleve;
    private Note note;
    private final ObjectProperty<Double> noteValeur;

    public EvaluationRow(User eleve, Note note) {
        this.eleve = eleve;
        this.note = note;
        this.noteValeur = new SimpleObjectProperty<>(note.getValeur());
    }

    // Getters
    public User getEleve() {
        return eleve;
    }

    public String getNom() {
        return eleve.getNom();
    }

    public String getPrenom() {
        return eleve.getPrenom();
    }

    public void setNote(Note note) {
        this.note = note;
    }

    public Note getNote() {
        return note;
    }


    public void setNoteValeur(Double noteValeur) {
        this.noteValeur.setValue(noteValeur);
    }

    public Double getNoteValeur() {
        return noteValeur.get();
    }

    public ObjectProperty<Double> noteValeurProperty() {
        return noteValeur;
    }


}
