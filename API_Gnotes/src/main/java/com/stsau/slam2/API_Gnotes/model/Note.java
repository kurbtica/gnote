package com.stsau.slam2.API_Gnotes.model;

import jakarta.persistence.*;

import java.sql.Timestamp;
import java.util.Objects;

@Entity
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mon_seq_gen")
    @SequenceGenerator(name = "mon_seq_gen", sequenceName = "note_seq", allocationSize=10)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "eleve_id")
    private User eleve;
    private Double valeur;
    private Timestamp modification;
    @ManyToOne
    @JoinColumn(name = "evaluation_id")
    private Evaluation evaluation;

    Note() {
    }

    public Note(Long id, User eleve, Double valeur, Timestamp modification, Evaluation evaluation) {
        this.id = id;
        this.eleve = eleve;
        this.valeur = valeur;
        this.modification = modification;
        this.evaluation = evaluation;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getEleve() {
        return eleve;
    }

    public void setEleve(User eleve) {
        this.eleve = eleve;
    }

    public Double getValeur() {
        return valeur;
    }

    public void setValeur(Double valeur) {
        this.valeur = valeur;
    }

    public Timestamp getModification() {
        return modification;
    }

    public void setModification(Timestamp modification) {
        this.modification = modification;
    }

    public Evaluation getEvaluation() {
        return evaluation;
    }

    public void setEvaluation(Evaluation evaluation) {
        this.evaluation = evaluation;
    }

    @Override
    public String toString() {
        return "Note{" +
                "id=" + id +
                ", eleve=" + eleve +
                ", valeur=" + valeur +
                ", modification=" + modification +
                ", evaluation=" + evaluation +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Note note = (Note) o;
        return Objects.equals(id, note.id) &&
                Objects.equals(eleve, note.eleve) &&
                Objects.equals(valeur, note.valeur) &&
                Objects.equals(modification, note.modification) &&
                Objects.equals(evaluation, note.evaluation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, eleve, valeur, modification, evaluation);
    }
}