package org.openjfx.sio2E4.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openjfx.sio2E4.constants.APIConstants;
import org.openjfx.sio2E4.model.Role;
import org.openjfx.sio2E4.model.User;
import org.openjfx.sio2E4.service.LocalStorageService;
import org.openjfx.sio2E4.service.SyncService;
import org.openjfx.sio2E4.util.SecurityUtils;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Optional;

/**
 * ============================================================
 *  SERVICE D'AUTHENTIFICATION - Gestion Online ET Hors-Ligne
 * ============================================================
 *
 * Ce service gère deux cas de connexion :
 *
 *   CAS 1 - ONLINE : L'API est joignable.
 *     → On envoie l'email + mot de passe au serveur via HTTP.
 *     → Le serveur vérifie et répond avec un token + les infos utilisateur.
 *     → ON EN PROFITE pour préparer la connexion hors-ligne (voir plus bas).
 *
 *   CAS 2 - OFFLINE : L'API est injoignable (pas de réseau, serveur coupé...).
 *     → On ne peut PAS demander au serveur de vérifier le mot de passe.
 *     → On utilise à la place un cache local sécurisé (auth_cache.json).
 *     → Ce cache a été rempli lors de la DERNIÈRE connexion Online réussie.
 */
public class AuthService {

	private static User currentUser;
	private static String sessionToken;

	public static boolean login(String email, String password) {
		if (NetworkService.isOnline()) {
			try {
				// Construire la requête HTTP
				HttpClient client = HttpClient.newHttpClient();
				HttpRequest request = HttpRequest.newBuilder().uri(URI.create(APIConstants.AUTH_LOGIN))
						.header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers
								.ofString("{\"username\":\"" + email + "\", \"password\":\"" + password + "\"}"))
						.build();

				// Envoyer la requête et obtenir la réponse
				HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

				if (response.statusCode() == 200) {
					// Si la réponse est correcte, traiter le JSON
					String userJson = response.body();
					ObjectMapper objectMapper = new ObjectMapper();
					JsonNode rootNode = objectMapper.readTree(userJson);

					// Extraire les informations
					String token = rootNode.path("token").asText();

					int id = rootNode.path("id").asInt();
					String nom = rootNode.path("nom").asText();
					String prenom = rootNode.path("prenom").asText();
					String emailResponse = rootNode.path("email").asText();
					//String role = rootNode.path("role").asText();
                    //Role role = rootNode.path("role");
					String roleString = rootNode.path("role").asText("USER");
					Role role;
					try {
						role = Role.valueOf(roleString);
					} catch (IllegalArgumentException e) {
						role = Role.ENSEIGNANT; // Valeur par défaut si erreur
					}
					String adresse = rootNode.path("adresse").asText();
					String telephone = rootNode.path("telephone").asText();

					currentUser = new User(nom, prenom, emailResponse, role, adresse, telephone);
					sessionToken = token; // On garde le token JWT pour les prochains appels API

					/* -------------------------------------------------------
					 *  PRÉPARATION DU CACHE HORS-LIGNE
					 * -------------------------------------------------------
					 * Maintenant que le serveur a confirmé que le mot de passe est bon,
					 * on prépare une "empreinte" sécurisée pour les futures sessions hors-ligne.
					 *
					 * ÉTAPE 1 : Générer un sel aléatoire.
					 *   Le sel est une chaîne aléatoire (ex: "xK9p2mZ...").
					 *   Il est DIFFÉRENT à chaque connexion, ce qui rend les attaques pré-calculées
					 *   (Rainbow Tables) inutilisables.
					 *
					 * ÉTAPE 2 : Hacher le mot de passe + le sel avec SHA-256.
					 *   On ne stocke JAMAIS le mot de passe en clair.
					 *   On stocke seulement son "empreinte" (hash), qui est irréversible.
					 *   Exemple : SHA-256("MonMotDePasse" + "xK9p2mZ...") → "7f4ab2c..."
					 *
					 * ÉTAPE 3 : Sauvegarder dans auth_cache.json : { email, sel, hash }.
					 *   Le sel est stocké en clair (ce n'est pas un secret).
					 *   Même si un pirate lit ce fichier, il ne peut PAS retrouver le mot de passe
					 *   à partir du hash seul (c'est une fonction à sens unique).
					 * ------------------------------------------------------- */
					String salt = SecurityUtils.generateSalt(); // ÉTAPE 1 : sel aléatoire
					String hash = SecurityUtils.hashPassword(password, salt); // ÉTAPE 2 : hash irréversible
					LocalStorageService.saveOfflineCredentials(emailResponse, salt, hash); // ÉTAPE 3 : sauvegarde

					try {
						// Initialisation du fichier json pour le mode hors ligne
						LocalStorageService.setup();
						SyncService syncService = new SyncService();
						syncService.init();
					} catch (IOException e) {
						throw new RuntimeException(e);
					}return true; // Authentification réussie

				} else {
					System.out.println("Erreur Login: " + response.statusCode());
					return false;
				}
			} catch (Exception e) {
				e.printStackTrace();
				return false; // En cas d'erreur
			}
		} else {
			/* -------------------------------------------------------
			 *  VÉRIFICATION HORS-LIGNE (aucun réseau disponible)
			 * -------------------------------------------------------
			 * On ne peut pas joindre le serveur, donc on s'appuie sur le
			 * cache local (auth_cache.json) créé lors de la dernière connexion Online.
			 *
			 * RAPPEL de ce que contient auth_cache.json :
			 *   { "email": "prof@lycee.fr", "salt": "xK9p2mZ...", "hash": "7f4ab2c..." }
			 *
			 * PROCÉDURE DE VÉRIFICATION :
			 *   1. Lire le cache → récupérer l'email, le sel et le hash stockés.
			 *   2. Vérifier que l'email saisi correspond à celui du cache.
			 *      (Sécurité : seul le DERNIER utilisateur connecté online peut se connecter offline)
			 *   3. Recalculer : SHA-256(mot_de_passe_saisi + sel_stocké).
			 *      On utilise LE MÊME sel que lors de la sauvegarde, ce qui est indispensable.
			 *   4. Comparer le hash recalculé avec le hash stocké.
			 *      - S'ils sont IDENTIQUES → c'est nécessairement le bon mot de passe → accès autorisé.
			 *      - S'ils sont DIFFÉRENTS  → mauvais mot de passe → accès refusé.
			 * ------------------------------------------------------- */

			// ÉTAPE 1 : Lecture du cache local
			JsonNode creds = LocalStorageService.getOfflineCredentials();

			// ÉTAPE 2 : Vérification de l'email
			// Note : equalsIgnoreCase pour éviter les problèmes de casse (MAJ/minuscules)
			if (creds != null && creds.has("email") && creds.get("email").asText().equalsIgnoreCase(email)) {
				String salt       = creds.get("salt").asText(); // Le sel sauvegardé lors de la connexion Online
				String storedHash = creds.get("hash").asText(); // Le hash sauvegardé lors de la connexion Online

				// ÉTAPE 3 : On recalcule le hash avec le mot de passe SAISI et le SEL STOCKÉ
				// Si l'utilisateur a tapé le bon mot de passe, le résultat sera identique au hash stocké.
				String calculatedHash = SecurityUtils.hashPassword(password, salt);

				// ÉTAPE 4 : Comparaison des deux empreintes
				if (calculatedHash.equals(storedHash)) {
					// Les hashs correspondent : le mot de passe est correct.
					// On charge maintenant les données complètes de l'utilisateur depuis user_data.json
					ArrayList<User> localUsers = LocalStorageService.loadUsers();
					Optional<User> localUser = localUsers.stream()
							.filter(user -> user.getEmail().equalsIgnoreCase(email))
							.findFirst();

					if (localUser.isPresent()) {
						System.out.println("✅ Login hors-ligne réussi pour : " + email);
						currentUser = localUser.get();
						sessionToken = "OFFLINE_TOKEN"; // Token symbolique, inutilisable sur l'API réelle
						return true;
					}
				}
			}
			System.out.println("❌ Échec login hors-ligne : email ou mot de passe invalide.");
			return false;
		}
	}

	public static void logout() {
		try {
			if (sessionToken == null || sessionToken.isEmpty()) {
				System.out.println("Aucun token en session, pas de déconnexion nécessaire.");
				return;
			}
			if (!NetworkService.isOnline()) return;

			HttpClient client = HttpClient.newHttpClient();
			HttpRequest request = HttpRequest.newBuilder().uri(URI.create(APIConstants.AUTH_LOGOUT))
					.header("Authorization", "Bearer " + sessionToken).POST(HttpRequest.BodyPublishers.noBody())
					.build();

			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

			if (response.statusCode() == 200 || response.statusCode() == 204) {
				System.out.println("Déconnexion réussie !");
			} else {
				System.out.println("Erreur lors de la déconnexion. Code HTTP : " + response.statusCode());
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// Nettoyer les infos utilisateur même si l'appel échoue
			sessionToken = null;
			currentUser = null;
		}
	}

	// Méthode pour récupérer les informations de l'utilisateur courant
	public static User getCurrentUser() {
		return currentUser;
	}

	public static void setCurrentUser(User currentUser) {
		AuthService.currentUser = currentUser;
	}

	// Méthode pour récupérer le token
	public static String getToken() {
		return sessionToken;
	}

	public static void setSessionToken(String sessionToken) {
		AuthService.sessionToken = sessionToken;
	}
}
