package org.openjfx.sio2E4.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.openjfx.sio2E4.service.LocalStorageService;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Note {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer id; // avec Integer a la place de int la valeur peut etre null
    private User eleve;
    private double valeur;
    private String modification; // 🔄 nouveau champ

    //@JsonProperty("evaluation") // 1. Permet de LIRE le champ "evaluation" du JSON
    @JsonIgnoreProperties("notes") // 2. Sécurité : Si on renvoie cet objet en JSON, on coupe la boucle
    private Evaluation evaluation;

    // GETTERS / SETTERS

    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }

    public User getEleve() {
        return eleve;
    }
    public void setEleve(User eleve) {
        this.eleve = eleve;
    }

    public double getValeur() {
        return valeur;
    }
    public void setValeur(double valeur) {
        this.valeur = valeur;
    }

    public String getModification() {
        return modification;
    }
    public void setModification(String modification) {
        this.modification = modification;
    }

    public Evaluation getEvaluation() {
        return evaluation;
    }

    public void setEvaluation(Evaluation evaluation) {
        this.evaluation = evaluation;
    }
}
