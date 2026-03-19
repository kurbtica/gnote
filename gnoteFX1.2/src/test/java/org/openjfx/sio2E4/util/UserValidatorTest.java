package org.openjfx.sio2E4.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openjfx.sio2E4.model.Role;
import org.openjfx.sio2E4.model.User;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour UserValidator.
 *
 * Ces tests vérifient que les règles de validation des données utilisateur
 * (email, téléphone, champs obligatoires) fonctionnent correctement.
 */
@DisplayName("Tests de validation — UserValidator")
class UserValidatorTest {

    // -------------------------------------------------------
    //  Tests de validation d'email
    // -------------------------------------------------------

    @Test
    @DisplayName("Un email valide doit être accepté")
    void testValidateEmail_valide() {
        assertTrue(UserValidator.validateEmail("prof@lycee.fr"),
            "Un email classique (prénom@domaine.fr) doit être valide");
        assertTrue(UserValidator.validateEmail("jean.dupont@education.gouv.fr"),
            "Un email avec point dans le nom doit être valide");
        assertTrue(UserValidator.validateEmail("user.test+tag@sub.domain.com"),
            "Un email avec + et sous-domaine doit être valide");
    }

    @Test
    @DisplayName("Un email mal formé doit être refusé")
    void testValidateEmail_invalide() {
        assertFalse(UserValidator.validateEmail("pasunemail"),
            "Un texte sans @ doit être refusé");
        assertFalse(UserValidator.validateEmail("@domaine.fr"),
            "Un email sans nom local (avant @) doit être refusé");
        assertFalse(UserValidator.validateEmail("prof@"),
            "Un email sans domaine doit être refusé");
        assertFalse(UserValidator.validateEmail("prof@domaine"),
            "Un email sans extension (.fr, .com...) doit être refusé");
        assertFalse(UserValidator.validateEmail(""),
            "Une chaîne vide doit être refusée");
    }

    // -------------------------------------------------------
    //  Tests de validation du numéro de téléphone
    // -------------------------------------------------------

    @Test
    @DisplayName("Un numéro de téléphone français valide doit être accepté")
    void testValidatePhone_valide() {
        assertTrue(UserValidator.validatePhone("0612345678"),
            "Un numéro mobile français (06) doit être valide");
        assertTrue(UserValidator.validatePhone("0712345678"),
            "Un numéro mobile français (07) doit être valide");
        assertTrue(UserValidator.validatePhone("0123456789"),
            "Un numéro fixe français (01) doit être valide");
    }

    @Test
    @DisplayName("Un numéro de téléphone invalide doit être refusé")
    void testValidatePhone_invalide() {
        assertFalse(UserValidator.validatePhone("061234567"),
            "Un numéro trop court (9 chiffres) doit être refusé");
        assertFalse(UserValidator.validatePhone("06123456789"),
            "Un numéro trop long (11 chiffres) doit être refusé");
        assertFalse(UserValidator.validatePhone("0012345678"),
            "Un numéro commençant par 00 doit être refusé");
        assertFalse(UserValidator.validatePhone("abcdefghij"),
            "Un numéro composé de lettres doit être refusé");
        assertFalse(UserValidator.validatePhone(""),
            "Un numéro vide doit être refusé");
    }

    // -------------------------------------------------------
    //  Tests de validation d'un objet User complet
    // -------------------------------------------------------

    @Test
    @DisplayName("Un utilisateur avec toutes les données valides doit être accepté")
    void testValidateUser_complet() {
        User user = new User(
            "Dupont",
            "Jean",
            "jean.dupont@lycee.fr",
            Role.ENSEIGNANT,
            "12 Rue des Fleurs, 75001 Paris",
            "0612345678"
        );
        assertTrue(UserValidator.validateUser(user),
            "Un utilisateur avec des données valides et complètes doit passer la validation");
    }

    @Test
    @DisplayName("Un utilisateur avec un nom vide doit être refusé")
    void testValidateUser_nomVide() {
        User user = new User(
            "",           // nom vide ← invalide
            "Jean",
            "jean@lycee.fr",
            Role.ENSEIGNANT,
            "12 Rue des Fleurs",
            "0612345678"
        );
        assertFalse(UserValidator.validateUser(user),
            "Un utilisateur sans nom doit être refusé");
    }

    @Test
    @DisplayName("Un utilisateur avec email invalide doit être refusé")
    void testValidateUser_emailInvalide() {
        User user = new User(
            "Dupont",
            "Jean",
            "pasunemail",  // email invalide ← invalide
            Role.ENSEIGNANT,
            "12 Rue des Fleurs",
            "0612345678"
        );
        assertFalse(UserValidator.validateUser(user),
            "Un utilisateur avec un email mal formé doit être refusé");
    }

    @Test
    @DisplayName("Un utilisateur sans rôle doit être refusé")
    void testValidateUser_sansRole() {
        User user = new User(
            "Dupont",
            "Jean",
            "jean@lycee.fr",
            null,          // rôle null ← invalide
            "12 Rue des Fleurs",
            "0612345678"
        );
        assertFalse(UserValidator.validateUser(user),
            "Un utilisateur sans rôle doit être refusé");
    }
}
