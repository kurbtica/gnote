package org.openjfx.sio2E4.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openjfx.sio2E4.constants.APIConstants;
import org.openjfx.sio2E4.model.Evaluation;
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

public class EvaluationRepository {

    private final String BEARER_TOKEN = "Bearer " + AuthService.getToken();
    private final ObjectMapper mapper;
    private final HttpClient client;

    public EvaluationRepository(ObjectMapper mapper, HttpClient client) {
        this.mapper = mapper;
        this.client = client;
    }

    public EvaluationRepository() {
        this.mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.client = HttpClient.newHttpClient();
    }

    // --- LECTURE (READ) ---

    // Récupérer une evaluation (Online ou Offline)
    public CompletableFuture<Evaluation> getEvaluation(int evaluationID) {
        if (NetworkService.isOnline()) {
            return fetchEvaluationFromApi(evaluationID);
        } else {
            // On enveloppe l'appel local dans un Future pour garder la cohérence async
            return CompletableFuture.supplyAsync(() -> LocalStorageService.findEvaluationById(evaluationID));
        }
    }

    public CompletableFuture<List<Evaluation>> getEvaluationsList() {
        if (NetworkService.isOnline()) {
            return fetchEvaluationsListFromApi();
        } else {
            // On enveloppe l'appel local dans un Future pour garder la cohérence async
            return CompletableFuture.supplyAsync(LocalStorageService::loadEvaluations);
        }
    }

    // --- ÉCRITURE (CREATE, UPDATE, DELETE) ---

    /**
     * Ajoute une evaluation (API ou Local)
     * @return true si succès, false sinon
     */
    public CompletableFuture<Boolean> createEvaluation(Evaluation evaluation) {
        if (NetworkService.isOnline()) {
            try {
                String json = mapper.writeValueAsString(evaluation); // Conversion automatique Objet -> JSON

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(APIConstants.EVALUATIONS))
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
                LocalStorageService.save(evaluation);
                return true;
            });
        }
    }

    /**
     * Met à jour une evaluation
     */
    public CompletableFuture<Boolean> updateEvaluation(Evaluation evaluation) {
        if (NetworkService.isOnline()) {
            try {
                String json = mapper.writeValueAsString(evaluation);

                HttpRequest request = HttpRequest.newBuilder()
                        // Suppose que vous avez une constante NOTES_BY_ID
                        .uri(URI.create(APIConstants.formatUrl(APIConstants.EVALUATION_BY_ID, evaluation.getId())))
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
                LocalStorageService.update(evaluation);
                return true;
            });
        }
    }

    /**
     * Supprime une evaluation
     */
    public CompletableFuture<Boolean> deleteEvaluation(int evaluationID) {
        if (NetworkService.isOnline()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(APIConstants.formatUrl(APIConstants.EVALUATION_BY_ID, evaluationID)))
                    .header("Authorization", BEARER_TOKEN)
                    .DELETE()
                    .build();

            return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> response.statusCode() == 204 || response.statusCode() == 200);
        } else {
            return CompletableFuture.supplyAsync(() -> {
                // Logique locale simplifiée
                List<Evaluation> evaluations = LocalStorageService.loadEvaluations();
                Optional<Evaluation> target = evaluations.stream().filter(n -> n.getId() == evaluationID).findFirst();
                target.ifPresent(LocalStorageService::remove);
                return target.isPresent();
            });
        }
    }


    // --- Méthodes privées pour l'API ---

    public CompletableFuture<Evaluation> fetchEvaluationFromApi(int evaluationID) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(APIConstants.formatUrl(APIConstants.EVALUATION_BY_ID, evaluationID)))
                .header("Authorization", BEARER_TOKEN)
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(this::parseEvaluationJson);
    }

    public CompletableFuture<List<Evaluation>> fetchEvaluationsListFromApi() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(APIConstants.EVALUATIONS))
                .header("Authorization", BEARER_TOKEN)
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(this::parseEvaluationsListJson);
    }

    // --- Méthodes de Parsing ---

    private Evaluation parseEvaluationJson(String response) {
        try {
            return mapper.readValue(response, Evaluation.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<Evaluation> parseEvaluationsListJson(String json) {
        try {
            // Utilisation de TypeFactory pour construire une List<Evaluation> proprement
            return mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, Evaluation.class));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
