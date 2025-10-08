package org.openjfx.sio2E4.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openjfx.sio2E4.model.LocalUser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class AuthService {

	private static LocalUser currentUser;
	private static String sessionToken;

	// Méthode de login qui récupère toutes les informations de l'utilisateur
	public static boolean login(String email, String password) {
		try {
			// Construire la requête HTTP
			HttpClient client = HttpClient.newHttpClient();
			HttpRequest request = HttpRequest.newBuilder().uri(URI.create("http://localhost:8080/api/auth/login"))
					.header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers
							.ofString("{\"email\":\"" + email + "\", \"password\":\"" + password + "\"}"))
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
				String role = rootNode.path("role").path("libelle").asText();
				String adresse = rootNode.path("adresse").asText();
				String telephone = rootNode.path("telephone").asText();

				// Crée un objet User avec toutes les données reçues
				currentUser = new LocalUser(token, id, nom, prenom, emailResponse, role, adresse, telephone);
				sessionToken = token; // Sauvegarde le token pour utilisation future
				return true; // Authentification réussie
			} else {
				return false; // Erreur dans la réponse
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false; // En cas d'erreur
		}
	}

	public static void logout() {
		try {
			if (sessionToken == null || sessionToken.isEmpty()) {
				System.out.println("Aucun token en session, pas de déconnexion nécessaire.");
				return;
			}

			HttpClient client = HttpClient.newHttpClient();
			HttpRequest request = HttpRequest.newBuilder().uri(URI.create("http://localhost:8080/api/auth/logout"))
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
	public static LocalUser getCurrentUser() {
		return currentUser;
	}

	// Méthode pour récupérer le token
	public static String getToken() {
		return sessionToken;
	}
}
