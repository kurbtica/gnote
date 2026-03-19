package org.openjfx.sio2E4.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour l'enum Role.
 *
 * Ces tests vérifient que le système de rôles fonctionne correctement,
 * notamment la récupération par ID (utilisé lors de la désérialisation JSON depuis l'API).
 */
@DisplayName("Tests du modèle — Role")
class RoleTest {

    // -------------------------------------------------------
    //  Tests des propriétés des rôles
    // -------------------------------------------------------

    @Test
    @DisplayName("ADMIN doit avoir l'ID 1, le nom ADMIN, et le libellé Administrateur")
    void testAdmin_proprietes() {
        assertEquals(1,               Role.ADMIN.getId());
        assertEquals("ADMIN",         Role.ADMIN.getName());
        assertEquals("Administrateur",Role.ADMIN.getLibelle());
    }

    @Test
    @DisplayName("ENSEIGNANT doit avoir l'ID 2, le nom ENSEIGNANT, et le libellé Enseignant")
    void testEnseignant_proprietes() {
        assertEquals(2,           Role.ENSEIGNANT.getId());
        assertEquals("ENSEIGNANT",Role.ENSEIGNANT.getName());
        assertEquals("Enseignant",Role.ENSEIGNANT.getLibelle());
    }

    @Test
    @DisplayName("ETUDIANT doit avoir l'ID 3, le nom ETUDIANT, et le libellé Étudiant")
    void testEtudiant_proprietes() {
        assertEquals(3,         Role.ETUDIANT.getId());
        assertEquals("ETUDIANT",Role.ETUDIANT.getName());
        assertEquals("Étudiant",Role.ETUDIANT.getLibelle());
    }

    // -------------------------------------------------------
    //  Tests de la méthode getById()
    //  (utilisée lors de la désérialisation de la réponse API)
    // -------------------------------------------------------

    @Test
    @DisplayName("getById(1) doit retourner ADMIN")
    void testGetById_admin() {
        assertEquals(Role.ADMIN, Role.getById(1),
            "L'ID 1 doit correspondre au rôle ADMIN");
    }

    @Test
    @DisplayName("getById(2) doit retourner ENSEIGNANT")
    void testGetById_enseignant() {
        assertEquals(Role.ENSEIGNANT, Role.getById(2),
            "L'ID 2 doit correspondre au rôle ENSEIGNANT");
    }

    @Test
    @DisplayName("getById(3) doit retourner ETUDIANT")
    void testGetById_etudiant() {
        assertEquals(Role.ETUDIANT, Role.getById(3),
            "L'ID 3 doit correspondre au rôle ETUDIANT");
    }

    @Test
    @DisplayName("getById() avec un ID inexistant doit lever une IllegalArgumentException")
    void testGetById_idInconnu_leveException() {
        // Si l'API renvoie un ID de rôle inconnu, l'application doit lever une
        // exception explicite plutôt que de retourner null et provoquer un NullPointerException
        // ailleurs dans le code (plus difficile à déboguer).
        assertThrows(IllegalArgumentException.class,
            () -> Role.getById(999),
            "Un ID de rôle inconnu doit lever une IllegalArgumentException");
    }

    @Test
    @DisplayName("valueOf() avec une chaîne valide doit retourner le bon rôle")
    void testValueOf_chainValide() {
        // valueOf() est la méthode standard Java des enums.
        // Elle est utilisée dans AuthService pour convertir le rôle en String depuis la réponse API.
        assertEquals(Role.ADMIN,      Role.valueOf("ADMIN"));
        assertEquals(Role.ENSEIGNANT, Role.valueOf("ENSEIGNANT"));
        assertEquals(Role.ETUDIANT,   Role.valueOf("ETUDIANT"));
    }

    @Test
    @DisplayName("valueOf() avec une chaîne inconnue doit lever une exception")
    void testValueOf_chainInconnue_leveException() {
        assertThrows(IllegalArgumentException.class,
            () -> Role.valueOf("DIRECTEUR"),
            "Un nom de rôle inconnu doit lever une IllegalArgumentException");
    }
}
