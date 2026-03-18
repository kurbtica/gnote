package org.openjfx.sio2E4.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * ============================================================
 *  CLASSE UTILITAIRE - SÉCURITÉ HORS-LIGNE (Cyber BTS SIO)
 * ============================================================
 *
 * PROBLÈME :
 *   En mode hors-ligne, on ne peut pas contacter le serveur pour vérifier un mot de passe.
 *   On doit donc trouver un moyen de vérifier le mot de passe LOCALEMENT,
 *   sans stocker ce mot de passe en clair dans un fichier (ce qui serait une faille de sécurité majeure).
 *
 * SOLUTION : Le HACHAGE + le SEL (Hash + Salt)
 *   - Un "hash" est une empreinte numérique d'un mot de passe. C'est une transformation à SENS UNIQUE :
 *       - On PEUT calculer l'empreinte d'un mot de passe : "azerty" → "a3f9c2..."
 *       - On ne PEUT PAS retrouver le mot de passe à partir de l'empreinte (mathématiquement impossible).
 *   - On stocke donc l'EMPREINTE et non le mot de passe lui-même.
 *
 * PROBLÈME DU HASH SEUL : Les "Rainbow Tables"
 *   - Un pirate peut pré-calculer les empreintes de millions de mots de passe courants à l'avance.
 *   - Si il trouve notre fichier, il cherche notre empreinte dans son tableau et retrouve le mot de passe.
 *   - Exemple : il sait déjà que SHA-256("azerty") = "a3f9c2..." → mot de passe retrouvé.
 *
 * SOLUTION FINALE : Le SEL (Salt)
 *   - Avant de hacher, on ajoute une chaîne de caractères ALÉATOIRE au mot de passe (le "sel").
 *   - Le hash devient : SHA-256("azerty" + "selAléatoire$xK9p") = "7b4f1a..." (résultat totalement différent)
 *   - Le sel est SAUVEGARDÉ en clair à côté du hash dans auth_cache.json. Le sel n'est pas secret.
 *   - Mais SANS le sel, les Rainbow Tables pré-calculées sont inutiles car elles n'incluent pas ce sel.
 *   - Pour vérifier : on relit le sel → on recalcule SHA-256(mot_de_passe_saisi + sel) → on compare.
 */
public class SecurityUtils {

    /**
     * Génère un "sel" (salt) aléatoire de 16 octets.
     *
     * Le sel est une chaîne aléatoire générée UNE SEULE FOIS par connexion réussie.
     * Il n'est pas secret (on le stocke en clair), mais il rend inutilisables les
     * Rainbow Tables, car le pirate devrait recalculer des milliers de hashs avec CE sel spécifique.
     *
     * On utilise SecureRandom (et non Random) car il est conçu pour la cryptographie :
     * ses valeurs sont réellement imprévisibles, contrairement à Random qui est prévisible.
     */
    public static String generateSalt() {
        SecureRandom random = new SecureRandom(); // Générateur de nombres vraiment aléatoires (cryptographique)
        byte[] salt = new byte[16];              // On veut 16 octets aléatoires (= 128 bits)
        random.nextBytes(salt);                  // On remplit le tableau avec des octets aléatoires
        return Base64.getEncoder().encodeToString(salt); // On convertit en texte pour le stocker dans le JSON
    }

    /**
     * Hash un mot de passe avec un sel en utilisant l'algorithme SHA-256.
     *
     * SHA-256 est un algorithme de hachage standard et reconnu.
     * "256" signifie que le résultat fait toujours 256 bits (64 caractères hexadécimaux).
     *
     * FONCTIONNEMENT :
     *   1. On initialise le moteur SHA-256.
     *   2. On lui donne d'abord le SEL (pour mélanger avant de hacher).
     *   3. On lui donne ensuite le MOT DE PASSE.
     *   4. On récupère le HASH résultant (un tableau d'octets) qu'on convertit en texte.
     *
     * POURQUOI LE SEL EN PREMIER ?
     *   L'ordre sel + mot_de_passe garantit que même un mot de passe vide avec un sel
     *   différent produira un hash différent. Cela maximise l'impact du sel.
     *
     * @param password Le mot de passe saisi par l'utilisateur
     * @param salt     Le sel récupéré depuis auth_cache.json (généré lors de la dernière connexion Online)
     * @return         Le hash en Base64, prêt à être comparé ou stocké
     */
    public static String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256"); // On demande l'algo SHA-256

            // ÉTAPE 1 : On "mélange" le sel dans le moteur de hachage AVANT le mot de passe
            md.update(salt.getBytes(StandardCharsets.UTF_8));

            // ÉTAPE 2 : On ajoute le mot de passe et on déclenche le calcul du hash
            byte[] hashedPassword = md.digest(password.getBytes(StandardCharsets.UTF_8));

            // ÉTAPE 3 : Le résultat brut est un tableau d'octets binaires.
            //           On le convertit en texte Base64 pour pouvoir le stocker dans un fichier JSON.
            return Base64.getEncoder().encodeToString(hashedPassword);

        } catch (NoSuchAlgorithmException e) {
            // SHA-256 est un standard Java et sera toujours disponible. Cette erreur ne devrait jamais arriver.
            throw new RuntimeException("Erreur lors du hashage du mot de passe", e);
        }
    }
}
