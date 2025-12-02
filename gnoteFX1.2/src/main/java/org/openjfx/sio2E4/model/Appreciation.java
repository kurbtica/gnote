package org.openjfx.sio2E4.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Appreciation {
    private Integer id;
    private User eleve;
    private Matiere matiere;
    private String appreciation;

    public Appreciation() {
    }

    public Appreciation(Integer id, User eleve, Matiere matiere, String appreciation) {
        this.id = id;
        this.eleve = eleve;
        this.matiere = matiere;
        this.appreciation = appreciation;
    }

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

    public Matiere getMatiere() {
        return matiere;
    }

    public void setMatiere(Matiere matiere) {
        this.matiere = matiere;
    }

    public String getAppreciation() {
        return appreciation;
    }

    public void setAppreciation(String appreciation) {
        this.appreciation = appreciation;
    }
}
