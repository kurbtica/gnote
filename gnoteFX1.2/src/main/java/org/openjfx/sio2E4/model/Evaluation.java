package org.openjfx.sio2E4.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Evaluation {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer id; // avec Integer a la place de int la valeur peut etre null
    private User enseignant;
    private Matiere matiere;
    private double coefficient;
    private String titre;
    private String date;
    private String modification; // 🔄 nouveau champ
    private NoteType noteType;
    private ArrayList<Note> notes;

    // GETTERS / SETTERS

    public Integer getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public User getEnseignant() {
        return enseignant;
    }
    public void setEnseignant(User enseignant) {
        this.enseignant = enseignant;
    }

    public Matiere getMatiere() {
        return matiere;
    }
    public void setMatiere(Matiere matiere) {
        this.matiere = matiere;
    }

    public double getCoefficient() {
        return coefficient;
    }
    public void setCoefficient(double coefficient) {
        this.coefficient = coefficient;
    }

    public String getTitre() {
        return titre;
    }
    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }

    public String getModification() {
        return modification;
    }
    public void setModification(String modification) {
        this.modification = modification;
    }

    public NoteType getNoteType() {
        return noteType;
    }
    public void setNoteType(NoteType noteType) {
        this.noteType = noteType;
    }

    public ArrayList<Note> getNotes() {
        return notes;
    }

    public void setNotes(ArrayList<Note> notes) {
        this.notes = notes;
    }

}
