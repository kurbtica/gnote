package org.openjfx.sio2E4.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour le modèle User.
 *
 * Ces tests vérifient que le modèle User se comporte correctement :
 * construction, getters/setters, et cas limites (null, valeurs vides).
 *
 * Note : On teste ici uniquement la LOGIQUE INTERNE du modèle (pas de réseau, pas de JavaFX).
 */
@DisplayName("Tests du modèle — User")
class UserTest {

    // Un utilisateur valide réutilisé dans plusieurs tests
    private User user;

    /**
     * @BeforeEach : Cette méthode est exécutée AVANT CHAQUE test.
     * Elle garantit que chaque test repart d'un état propre et connu.
     */
    @BeforeEach
    void setUp() {
        user = new User(
            "Dupont",
            "Marie",
            "marie.dupont@lycee.fr",
            Role.ENSEIGNANT,
            "10 Rue de la Paix, 75001 Paris",
            "0612345678"
        );
    }

    // -------------------------------------------------------
    //  Tests de construction du modèle
    // -------------------------------------------------------

    @Test
    @DisplayName("Les données passées au constructeur doivent être correctement stockées")
    void testConstructeur_donneesCorrectes() {
        assertEquals("Dupont",               user.getNom());
        assertEquals("Marie",                user.getPrenom());
        assertEquals("marie.dupont@lycee.fr",user.getEmail());
        assertEquals(Role.ENSEIGNANT,         user.getRole());
        assertEquals("0612345678",           user.getTelephone());
    }

    @Test
    @DisplayName("Un User créé avec le constructeur vide ne doit pas lever d'exception")
    void testConstructeurVide_sansException() {
        // Le constructeur vide est indispensable pour Jackson (désérialisation JSON).
        // S'il lève une exception, la lecture des fichiers locaux plantera.
        assertDoesNotThrow(() -> {
            User u = new User();
            assertNull(u.getNom(), "Un User vide doit avoir un nom null par défaut");
        });
    }

    // -------------------------------------------------------
    //  Tests de la méthode toString()
    // -------------------------------------------------------

    @Test
    @DisplayName("toString() doit retourner 'Prénom Nom'")
    void testToString_formatCorrect() {
        // toString() est utilisé dans les listes déroulantes et les logs.
        // Il doit afficher "Prénom Nom" et non l'inverse.
        String affichage = user.toString();
        assertEquals("Marie Dupont", affichage,
            "toString() doit retourner le prénom suivi du nom");
    }

    // -------------------------------------------------------
    //  Tests des setters
    // -------------------------------------------------------

    @Test
    @DisplayName("Les setters doivent modifier correctement les données")
    void testSetters_modificationCorrects() {
        user.setNom("Martin");
        user.setPrenom("Jean");
        user.setEmail("jean.martin@ecole.fr");
        user.setRole(Role.ADMIN);
        user.setTelephone("0723456789");

        assertEquals("Martin",           user.getNom());
        assertEquals("Jean",             user.getPrenom());
        assertEquals("jean.martin@ecole.fr", user.getEmail());
        assertEquals(Role.ADMIN,          user.getRole());
        assertEquals("0723456789",        user.getTelephone());
    }

    @Test
    @DisplayName("Assigner un ID doit fonctionner")
    void testSetId() {
        assertNull(user.getId(), "L'ID doit être null avant d'être assigné");
        user.setId(42);
        assertEquals(42, user.getId(), "L'ID doit valoir 42 après setId(42)");
    }

    // -------------------------------------------------------
    //  Tests de la gestion du token (sécurité)
    // -------------------------------------------------------

    @Test
    @DisplayName("Le token ne doit pas être null après avoir été défini")
    void testToken_assignation() {
        assertNull(user.getToken(), "Le token doit être null tant qu'il n'est pas défini");
        user.setToken("Bearer abc123xyz");
        assertEquals("Bearer abc123xyz", user.getToken());
    }
}
