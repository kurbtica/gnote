package org.openjfx.sio2E4.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Etudiant {
    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String adresse;
    private String telephone;
    private Role role;

    // Inner class pour le rôle
    public static class Role {
        @JsonIgnore // Ignore le champ id lors de la désérialisation
        private Long id;
        
        private String libelle;

        @JsonProperty("libelle") // Désérialiser uniquement libelle
        public String getLibelle() {
            return libelle;
        }

        public void setLibelle(String libelle) {
            this.libelle = libelle;
        }

        // Les getters et setters pour l'id sont optionnels si tu ne veux vraiment pas qu'il soit mappé
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;  // Cette méthode peut être laissée vide si tu ne veux pas que l'id soit modifié
        }
    }

    // Getters & Setters pour l'Etudiant
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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
}
