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
    private Integer id_enseignant;
    private Integer id_eleve;
    private Integer id_matiere;
    private Integer id_type;
    private Integer coefficient;
    private Integer valeur;
    private Integer id_appreciation;
    private Timestamp date;
    private Timestamp date_modif;

    Note() {
    }

    public Note(Integer id_enseignant, Integer id_eleve,Integer valeur, Integer id_matiere, Integer coefficient, Integer id_type, Timestamp date,Timestamp date_modif) {

        this.id_enseignant = id_enseignant;
        this.id_eleve = id_eleve;
        this.id_matiere=id_matiere;
        this.valeur=valeur;
        this.coefficient=coefficient;
        this.id_type =id_type;
        this.date = date;
        this.date_modif=date_modif;
    }


    public Integer getId_enseignant() {return id_enseignant;}

    public void setId_enseignant(Integer id_enseignant) {this.id_enseignant = id_enseignant;}

    public Integer getId_eleve() {return id_eleve;}

    public void setId_eleve(Integer id_eleve) {this.id_eleve = id_eleve;}

    public Integer getId_matiere() {return id_matiere;}

    public void setId_matiere(Integer id_matiere) {this.id_matiere = id_matiere;}

    public Integer getCoefficient() {return coefficient;}

    public void setCoefficient(Integer coefficient) {this.coefficient = coefficient;}

    public Integer getValeur() {return valeur;}

    public void setValeur(Integer valeur) {this.valeur = valeur;}

    public Integer getId_appreciation() {return id_appreciation;}

    public void setId_appreciation(Integer id_appreciation) {this.id_appreciation = id_appreciation;}

    public Timestamp getDate_modif() {return date_modif;}

    public void setDate_modif(Timestamp date_modif) {this.date_modif = date_modif;}

    public void setId(Long id) {this.id = id;}

    public Integer getId_type() {
        return id_type;
    }

    public void setId_type(Integer id_type) {
        this.id_type = id_type;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    public Timestamp getDate() {
        return date;
    }

    public Long getId() {
        return this.id;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", id_enseignant='" + id_enseignant + '\'' +
                ", id_eleve='" + id_eleve + '\'' +
                ", matiere='" + id_matiere + '\'' +
                ", type='" + id_type + '\'' +
                ", coefficient='" + coefficient + '\'' +
                ", appreciation='" + id_appreciation + '\'' +
                ", date='" + date + '\'' +
                ", date_modif='" + date_modif + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Note user = (Note) o;
        return Objects.equals(id, user.id)
                && Objects.equals(id_enseignant, user.id_enseignant)
                && Objects.equals(id_eleve, user.id_eleve)
                && Objects.equals(id_matiere, user.id_matiere)
                && Objects.equals(id_type, user.id_type)
                && Objects.equals(coefficient, user.coefficient)
                && Objects.equals(id_appreciation, user.id_appreciation)
                && Objects.equals(date, user.date)
                && Objects.equals(date_modif, user.date_modif);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, id_enseignant, id_eleve,id_matiere,id_type,coefficient,id_appreciation, date,date_modif);
    }


}