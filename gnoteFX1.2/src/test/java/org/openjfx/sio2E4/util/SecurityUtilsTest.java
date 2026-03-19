package org.openjfx.sio2E4.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour SecurityUtils.
 *
 * Ces tests vérifient le bon fonctionnement du système de hashage + sel
 * utilisé pour sécuriser l'authentification hors-ligne.
 *
 * Un test unitaire vérifie UNE SEULE fonctionnalité à la fois,
 * de manière isolée, sans réseau ni base de données.
 */
@DisplayName("Tests de sécurité — SecurityUtils")
class SecurityUtilsTest {

    // -------------------------------------------------------
    //  Tests sur la génération du sel (generateSalt)
    // -------------------------------------------------------

    @Test
    @DisplayName("Le sel généré ne doit pas être null ni vide")
    void testGenerateSalt_notNullOrEmpty() {
        String salt = SecurityUtils.generateSalt();

        assertNotNull(salt, "Le sel ne doit pas être null");
        assertFalse(salt.isBlank(), "Le sel ne doit pas être vide");
    }

    @Test
    @DisplayName("Deux sels générés successivement doivent être différents (aléatoire)")
    void testGenerateSalt_isRandom() {
        // Si le sel était toujours le même, il ne protègerait pas contre les Rainbow Tables.
        String salt1 = SecurityUtils.generateSalt();
        String salt2 = SecurityUtils.generateSalt();

        assertNotEquals(salt1, salt2,
            "Deux sels doivent être différents — le sel doit être aléatoire");
    }

    // -------------------------------------------------------
    //  Tests sur le hashage du mot de passe (hashPassword)
    // -------------------------------------------------------

    @Test
    @DisplayName("Hacher le même mot de passe avec le même sel doit toujours donner le même résultat")
    void testHashPassword_deterministeAvecMemeSel() {
        // C'est la propriété fondamentale qui permet la vérification :
        // si l'utilisateur retape le bon mot de passe → même hash → accès autorisé.
        String password = "MonMotDePasse123";
        String salt     = "selFixePourLeTest";

        String hash1 = SecurityUtils.hashPassword(password, salt);
        String hash2 = SecurityUtils.hashPassword(password, salt);

        assertEquals(hash1, hash2,
            "Le même mot de passe + le même sel doivent toujours produire le même hash");
    }

    @Test
    @DisplayName("Un mauvais mot de passe ne doit PAS correspondre au hash enregistré")
    void testHashPassword_mauvaisMotDePasseNeMarchePas() {
        // C'est la vérification centrale du système hors-ligne :
        // si l'utilisateur tape le mauvais mot de passe → hash différent → accès refusé.
        String bonMotDePasse    = "MonMotDePasse123";
        String mauvaisMotDePasse = "MauvaisMotDePasse";
        String salt             = SecurityUtils.generateSalt();

        String hashCorrect  = SecurityUtils.hashPassword(bonMotDePasse, salt);
        String hashIncorrect = SecurityUtils.hashPassword(mauvaisMotDePasse, salt);

        assertNotEquals(hashCorrect, hashIncorrect,
            "Un mot de passe incorrect ne doit jamais produire le même hash que le bon");
    }

    @Test
    @DisplayName("Le même mot de passe avec deux sels différents doit donner deux hashs différents")
    void testHashPassword_selDifferentDonneHashDifferent() {
        // C'est ce qui rend les Rainbow Tables inutiles :
        // même mot de passe → hash DIFFÉRENT selon le sel → impossible de pré-calculer.
        String password = "MonMotDePasse123";
        String salt1    = SecurityUtils.generateSalt();
        String salt2    = SecurityUtils.generateSalt();

        String hash1 = SecurityUtils.hashPassword(password, salt1);
        String hash2 = SecurityUtils.hashPassword(password, salt2);

        assertNotEquals(hash1, hash2,
            "Le même mot de passe avec des sels différents doit produire des hashs différents");
    }

    @Test
    @DisplayName("Le hash résultant ne doit pas être null ni vide")
    void testHashPassword_notNullOrEmpty() {
        String hash = SecurityUtils.hashPassword("unMotDePasse", "unSel");

        assertNotNull(hash, "Le hash ne doit pas être null");
        assertFalse(hash.isBlank(), "Le hash ne doit pas être vide");
    }

    @Test
    @DisplayName("Le hash ne doit pas contenir le mot de passe original en clair")
    void testHashPassword_nePasContenirMotDePasseEnClair() {
        // Vérification basique que le mot de passe n'est pas simplement copié dans le hash.
        String password = "MonMotDePasse123";
        String hash     = SecurityUtils.hashPassword(password, "unSel");

        assertFalse(hash.contains(password),
            "Le hash ne doit jamais contenir le mot de passe en clair");
    }
}
