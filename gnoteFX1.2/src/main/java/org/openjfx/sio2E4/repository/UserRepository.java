package org.openjfx.sio2E4.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openjfx.sio2E4.constants.APIConstants;
import org.openjfx.sio2E4.model.Note;
import org.openjfx.sio2E4.model.User;
import org.openjfx.sio2E4.service.AuthService;
import org.openjfx.sio2E4.service.LocalStorageService;
import org.openjfx.sio2E4.service.NetworkService;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class UserRepository {

    private final String BEARER_TOKEN = "Bearer " + AuthService.getToken();
    private final ObjectMapper mapper;
    private final HttpClient client;

    public UserRepository(ObjectMapper mapper, HttpClient client) {
        this.mapper = mapper;
        this.client = client;
    }

    public UserRepository() {
        this.mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.client = HttpClient.newHttpClient();
    }

    // Récupérer un utilisateur (Online ou Offline)
    public CompletableFuture<User> getUser(int userId) {
        if (NetworkService.isOnline()) {
            return fetchUserFromApi(userId);
        } else {
            // On enveloppe l'appel local dans un Future pour garder la cohérence async
            return CompletableFuture.supplyAsync(() -> LocalStorageService.findUserById(userId));
        }
    }

    public CompletableFuture<List<User>> getUsersList() {
        if (NetworkService.isOnline()) {
            return fetchUsersListFromApi();
        } else {
            // On enveloppe l'appel local dans un Future pour garder la cohérence async
            return CompletableFuture.supplyAsync(LocalStorageService::loadUsers);
        }
    }

    // Récupérer les notes (Online ou Offline)
    public CompletableFuture<List<Note>> getUserNotes(int userId) {
        if (NetworkService.isOnline()) {
            return fetchUserNotesFromApi(userId);
        } else {
            return CompletableFuture.supplyAsync(() -> {
                // Logique de filtrage local déplacée ici
                return LocalStorageService.loadNotes().stream()
                        .filter(note -> note.getEleve().getId() == userId)
                        .collect(Collectors.toList());
            });
        }
    }

    // --- ÉCRITURE (CREATE, UPDATE, DELETE) ---

    /**
     * Ajoute un utilisateur (API ou Local)
     * @return true si succès, false sinon
     */
    public CompletableFuture<Boolean> createUser(User user) {
        if (NetworkService.isOnline()) {
            try {
                String json = mapper.writeValueAsString(user); // Conversion automatique Objet -> JSON

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(APIConstants.USERS))
                        .header("Authorization", BEARER_TOKEN)
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();

                return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                        .thenApply(response -> response.statusCode() == 200 || response.statusCode() == 201);

            } catch (JsonProcessingException e) {
                e.printStackTrace();
                return CompletableFuture.failedFuture(e);
            }
        } else {
            return CompletableFuture.supplyAsync(() -> {
                LocalStorageService.save(user);
                return true;
            });
        }
    }

    /**
     * Met à jour un utilisateur
     */
    public CompletableFuture<Boolean> updateUser(User user) {
        if (NetworkService.isOnline()) {
            try {
                String json = mapper.writeValueAsString(user);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(APIConstants.formatUrl(APIConstants.USER_BY_ID, user.getId())))
                        .header("Authorization", BEARER_TOKEN)
                        .header("Content-Type", "application/json")
                        .PUT(HttpRequest.BodyPublishers.ofString(json))
                        .build();

                return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                        .thenApply(response -> response.statusCode() == 200);

            } catch (JsonProcessingException e) {
                e.printStackTrace();
                return CompletableFuture.failedFuture(e);
            }
        } else {
            return CompletableFuture.supplyAsync(() -> {
                LocalStorageService.update(user);
                return true;
            });
        }
    }

    /**
     * Supprime un utilisateur
     */
    public CompletableFuture<Boolean> deleteUser(int userId) {
        if (NetworkService.isOnline()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(APIConstants.formatUrl(APIConstants.USER_BY_ID, userId)))
                    .header("Authorization", BEARER_TOKEN)
                    .DELETE()
                    .build();

            return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> response.statusCode() == 204 || response.statusCode() == 200);
        } else {
            return CompletableFuture.supplyAsync(() -> {
                // Logique locale simplifiée
                List<User> users = LocalStorageService.loadUsers();
                Optional<User> target = users.stream().filter(u -> u.getId() == userId).findFirst();
                target.ifPresent(LocalStorageService::remove);
                return target.isPresent();
            });
        }
    }


    // --- Méthodes privées pour l'API ---
    public CompletableFuture<User> fetchUserFromApi(int userId) {
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(APIConstants.formatUrl(APIConstants.USER_BY_ID, userId)))
                .header("Authorization", BEARER_TOKEN)
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(this::parseUserJson);

    }

    public CompletableFuture<List<User>> fetchUsersListFromApi() {
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(APIConstants.USERS))
                .header("Authorization", BEARER_TOKEN)
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(this::parseUsersListJson);

    }

    private CompletableFuture<List<Note>> fetchUserNotesFromApi(int userId) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(APIConstants.formatUrl(APIConstants.USER_NOTES, userId)))
                .header("Authorization", BEARER_TOKEN)
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(this::parseUserNotesJson);
    }


    // --- Méthodes de Parsing ---
    private User parseUserJson(String response) {
        try {
            return mapper.readValue(response, User.class);
        } catch (IOException e) {
            e.printStackTrace(); // Gérer l'erreur de parsing
        }
        return null;
    }

    private List<User> parseUsersListJson(String json) {
        try {
            return mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, User.class));
        } catch (IOException e) {
            e.printStackTrace(); // Gérer l'erreur de parsing
        }
        return null;
    }

    private List<Note> parseUserNotesJson(String json) {
        try {
            return mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, Note.class));
        } catch (IOException e) {
            throw new RuntimeException("Erreur parsing Notes", e);
        }
    }

}
