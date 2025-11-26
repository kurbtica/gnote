package org.openjfx.sio2E4.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.IOException;
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
    @JsonDeserialize(using = RoleDeserializer.class)
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

    // Custom deserializer pour gérer int ou objet Role
    public static class RoleDeserializer extends JsonDeserializer<Role> {
        @Override
        public Role deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
            JsonNode node = p.getCodec().readTree(p);
            Role newRole = new Role();

            if (node.isInt()) { // Cas API
                int roleId = node.asInt();
                newRole.setId(roleId);
                switch (roleId) {
                    case 1: newRole.setLibelle("ADMIN"); break;
                    case 2: newRole.setLibelle("ENSEIGNANT"); break;
                    default: newRole.setLibelle("ETUDIANT"); break;
                }
            } else if (node.isObject()) { // Cas JSON local
                if (node.has("id")) newRole.setId(node.get("id").asInt());
                if (node.has("libelle")) newRole.setLibelle(node.get("libelle").asText());
            }

            return newRole;
        }
    }
}
