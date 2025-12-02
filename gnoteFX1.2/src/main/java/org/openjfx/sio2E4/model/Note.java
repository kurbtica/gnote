package org.openjfx.sio2E4.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.openjfx.sio2E4.service.LocalStorageService;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Note {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer id; // avec Integer a la place de int la valeur peut etre null
    private User eleve;
    private double valeur;
    private String modification; // 🔄 nouveau champ
    private int evaluationId; // on met pas l'objet pour l'instant car ca crée des boucles infini dans le json

    // GETTERS / SETTERS

    public Integer getId() {
        return id;
    }
    public void setId(int id) {
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

    @JsonIgnore
    public Evaluation getEvaluation() {
        return LocalStorageService.findEvaluationById(evaluationId);
    }
    @JsonIgnore
    public void setEvaluation(Evaluation evaluation) {
        this.evaluationId = evaluation.getId();
    }

    public Integer getEvaluationId() {
        return evaluationId;
    }
    public void setEvaluationId(int evaluationId) {
        this.evaluationId = evaluationId;
    }
}
