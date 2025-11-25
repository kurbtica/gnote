package org.openjfx.sio2E4.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
    private String tocken;
    private int id;
    private String nom;
    private String prenom;
    private String email;
    private String adresse;
    private String telephone;
    private Role role;
    private Map<String, String> appreciations;

    public User( String tocken ,int id, String nom, String prenom, String emailResponse, Role role, String adresse, String telephone) {
        this.tocken=tocken;
        this.id=id;
        this.nom=nom;
        this.prenom=  prenom;
        this.adresse=adresse;
        this.email=emailResponse;
        this.telephone=telephone;
        this.role=role;
        this.appreciations = new HashMap<>();
        
    }

  

    @Override
    public String toString() {
        return this.prenom + " " + this.nom;
    }

    public User() {
        // Constructeur par défaut nécessaire pour Jackson
        this.appreciations = new HashMap<>();
    }

    // Getters et setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public Role getRole() {
        return role;
    }

    @JsonProperty("role")
    public void unpackRoleFromId(int roleId) {

        Role newRole = new Role();
        newRole.setId(roleId);
        switch (roleId) {
            case 1:
                newRole.setLibelle("ADMIN");
                break;
            case 2:
                newRole.setLibelle("ENSEIGNANT");
                break;
            default:
                newRole.setLibelle("ETUDIANT");
                break;
        }

        this.role = newRole;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    @JsonProperty("appreciations")
    public Map<String, String> getAppreciations() {
        return appreciations;
    }

    @JsonProperty("appreciations")
    public void setAppreciations(Map<String, String> appreciations) {
        this.appreciations = appreciations;
        if (this.appreciations == null) this.appreciations = new HashMap<>();
    }

    public String getToken() {
        return tocken;
    }
}
