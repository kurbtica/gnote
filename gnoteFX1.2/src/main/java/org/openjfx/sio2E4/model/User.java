package org.openjfx.sio2E4.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class User {
    private String token;
    private Integer id;
    private String nom;
    private String prenom;
    private String email;
    private String adresse;
    private String telephone;
    @JsonDeserialize(using = RoleDeserializer.class)
    private Role role;
    //private Map<String, String> appreciations;

    /*public User( String token ,int id, String nom, String prenom, String emailResponse, Role role, String adresse, String telephone) {
        this.token = token;
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.adresse = adresse;
        this.email = emailResponse;
        this.telephone = telephone;
        this.role = role;
        //this.appreciations = new HashMap<>();
    }*/

    public User(String nom, String prenom, String emailResponse, Role role, String adresse, String telephone) {
        this.nom = nom;
        this.prenom = prenom;
        this.adresse = adresse;
        this.email = emailResponse;
        this.telephone = telephone;
        this.role = role;
    }

  

    @Override
    public String toString() {
        return this.prenom + " " + this.nom;
    }

    public User() {
        // Constructeur par défaut nécessaire pour Jackson
        //this.appreciations = new HashMap<>();
    }

    // Getters et setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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

    /*@JsonProperty("appreciations")
    public Map<String, String> getAppreciations() {
        return appreciations;
    }

    @JsonProperty("appreciations")
    public void setAppreciations(Map<String, String> appreciations) {
        this.appreciations = appreciations;
        if (this.appreciations == null) this.appreciations = new HashMap<>();
    }*/

    public void setToken(String token) {
        this.token = token;
    }

    @JsonIgnore
    public String getToken() {
        return token;
    }

    // Custom deserializer pour gérer int ou objet Role
    public static class RoleDeserializer extends JsonDeserializer<Role> {
        @Override
        public Role deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
            JsonNode node = p.getCodec().readTree(p);

            if (node.isInt()) {
                // Cas où on reçoit l'ID (ex: 1)
                return Role.getById(node.asInt());
            } else if (node.isObject()) {
                // Cas où on reçoit un objet complet (ex: { "id": 1, "libelle": "..." })
                if (node.has("id")) {
                    return Role.getById(node.get("id").asInt());
                }
            } else if (node.isTextual()) {
                // Cas où on reçoit le nom (ex: "ADMIN")
                String roleName = node.asText();

                try {
                    return Role.valueOf(roleName);
                } catch (IllegalArgumentException e) {
                    // Si le texte ne correspond à aucun enum, on gère l'erreur ou on met une valeur par défaut
                    System.err.println("Role inconnu reçu : " + roleName);
                    return Role.ETUDIANT; // Valeur par défaut
                }
            }

            return Role.ETUDIANT; // Fallback final
        }
    }
}
