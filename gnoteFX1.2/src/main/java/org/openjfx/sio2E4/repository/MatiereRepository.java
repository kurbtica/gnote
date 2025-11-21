package org.openjfx.sio2E4.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openjfx.sio2E4.constants.APIConstants;
import org.openjfx.sio2E4.model.Matiere;
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

public class MatiereRepository {

    private final String BEARER_TOKEN = "Bearer " + AuthService.getToken();
    private final ObjectMapper mapper;
    private final HttpClient client;

    public MatiereRepository(ObjectMapper mapper, HttpClient client) {
        this.mapper = mapper;
        this.client = client;
    }

    public MatiereRepository() {
        this.mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.client = HttpClient.newHttpClient();
    }

    // Récupérer une matière (Online ou Offline)
    public CompletableFuture<Matiere> getMatiere(int matiereID) {
        if (NetworkService.isOnline()) {
            return fetchMatiereFromApi(matiereID);
        } else {
            // On enveloppe l'appel local dans un Future pour garder la cohérence async
            return CompletableFuture.supplyAsync(() -> LocalStorageService.findMatiereById(matiereID));
        }
    }

    public CompletableFuture<List<Matiere>> getMatieresList() {
        if (NetworkService.isOnline()) {
            return fetchMatieresListFromApi();
        } else {
            // On enveloppe l'appel local dans un Future pour garder la cohérence async
            return CompletableFuture.supplyAsync(LocalStorageService::loadMatieres);
        }
    }

    // --- ÉCRITURE (CREATE, UPDATE, DELETE) ---

    /**
     * Ajoute une matière (API ou Local)
     * @return true si succès, false sinon
     */
    public CompletableFuture<Boolean> createMatiere(Matiere matiere) {
        if (NetworkService.isOnline()) {
            try {
                String json = mapper.writeValueAsString(matiere); // Conversion automatique Objet -> JSON

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(APIConstants.MATIERES))
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
                LocalStorageService.save(matiere);
                return true;
            });
        }
    }

    /**
     * Met à jour une matière
     */
    public CompletableFuture<Boolean> updateMatiere(Matiere matiere) {
        if (NetworkService.isOnline()) {
            try {
                String json = mapper.writeValueAsString(matiere);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(APIConstants.formatUrl(APIConstants.MATIERE_BY_ID, matiere.getId())))
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
                LocalStorageService.update(matiere);
                return true;
            });
        }
    }

    /**
     * Supprime une matière
     */
    public CompletableFuture<Boolean> deleteMatiere(int matiereId) {
        if (NetworkService.isOnline()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(APIConstants.formatUrl(APIConstants.MATIERE_BY_ID, matiereId)))
                    .header("Authorization", BEARER_TOKEN)
                    .DELETE()
                    .build();

            return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> response.statusCode() == 204 || response.statusCode() == 200);
        } else {
            return CompletableFuture.supplyAsync(() -> {
                // Logique locale simplifiée
                List<Matiere> matieres = LocalStorageService.loadMatieres();
                Optional<Matiere> target = matieres.stream().filter(u -> u.getId() == matiereId).findFirst();
                target.ifPresent(LocalStorageService::remove);
                return target.isPresent();
            });
        }
    }


    // --- Méthodes privées pour l'API ---
    public CompletableFuture<Matiere> fetchMatiereFromApi(int matiereID) {
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(APIConstants.formatUrl(APIConstants.MATIERE_BY_ID, matiereID)))
                .header("Authorization", BEARER_TOKEN)
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(this::parseMatiereJson);

    }

    public CompletableFuture<List<Matiere>> fetchMatieresListFromApi() {
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(APIConstants.MATIERES))
                .header("Authorization", BEARER_TOKEN)
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(this::parseMatieresListJson);

    }

    // --- Méthodes de Parsing ---
    private Matiere parseMatiereJson(String response) {
        try {
            return mapper.readValue(response, Matiere.class);
        } catch (IOException e) {
            e.printStackTrace(); // Gérer l'erreur de parsing
        }
        return null;
    }

    private List<Matiere> parseMatieresListJson(String json) {
        try {
            return mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, Matiere.class));
        } catch (IOException e) {
            e.printStackTrace(); // Gérer l'erreur de parsing
        }
        return null;
    }

}
