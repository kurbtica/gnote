package org.openjfx.sio2E4.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

// Cette annotation force Jackson à serialiser l'enum comme un Objet JSON et non juste une String
//@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum Role {

    // Définition des constantes
    ADMIN(1, "ADMIN","Administrateur"),
    ENSEIGNANT(2, "ENSEIGNANT", "Enseignant"),
    ETUDIANT(3, "ETUDIANT","Étudiant");

    private final int id;
    private final String name;
    private final String libelle;

    // Constructeur privé (obligatoire pour les enums)
    Role(int id, String name, String libelle) {
        this.id = id;
        this.name = name;
        this.libelle = libelle;
    }

    public int getId() {
        return id;
    }

    @JsonValue
    public String getName() {
        return name;
    }

    public String getLibelle() {
        return libelle;
    }

    // Méthode utilitaire pour retrouver un Role via son ID (utile pour la DB ou le front)
    public static Role getById(int id) {
        return Arrays.stream(values())
                .filter(r -> r.id == id)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Id de rôle inconnu : " + id));
    }

    // Optionnel : Pour permettre à Jackson de créer l'enum depuis un JSON entrant
    // Exemple d'entrée acceptée : { "id": 1, "libelle": "Administrateur" }
    @JsonCreator
    public static Role forValues(@JsonProperty("id") int id, @JsonProperty("libelle") String libelle) {
        // On se base généralement sur l'ID pour retrouver le rôle
        return getById(id);
    }
}
