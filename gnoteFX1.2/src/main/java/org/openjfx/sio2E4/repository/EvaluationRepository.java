package org.openjfx.sio2E4.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openjfx.sio2E4.constants.APIConstants;
import org.openjfx.sio2E4.model.Evaluation;
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
import java.util.ArrayList;
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
            if (evaluationID < 0) {
                return CompletableFuture.supplyAsync(() -> LocalStorageService.findEvaluationById(evaluationID));
            }
            return fetchEvaluationFromApi(evaluationID);
        } else {
            // On enveloppe l'appel local dans un Future pour garder la cohérence async
            return CompletableFuture.supplyAsync(() -> LocalStorageService.findEvaluationById(evaluationID));
        }
    }

    public CompletableFuture<List<Evaluation>> getEvaluationsList() {
        if (NetworkService.isOnline()) {
            return fetchEvaluationsListFromApi().thenApply(list -> {
                if (list == null) list = new ArrayList<>();
                list.addAll(LocalStorageService.loadSyncObjects("Evaluation", Evaluation.class));
                return list;
            });
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
                // Si l'ID est négatif, on le met à null pour l'API (ID local temporaire)
                Integer originalId = evaluation.getId();
                if (originalId != null && originalId < 0) {
                    evaluation.setId(null);
                }

                // On fait de même pour les notes si elles ont des IDs négatifs
                List<Integer> originalNoteIds = new ArrayList<>();
                if (evaluation.getNotes() != null) {
                    for (Note note : evaluation.getNotes()) {
                        originalNoteIds.add(note.getId());
                        if (note.getId() != null && note.getId() < 0) {
                            note.setId(null);
                        }
                    }
                }

                String json = mapper.writeValueAsString(evaluation);

                // Restaurer les IDs originaux pour permettre la suppression locale après succès
                if (originalId != null && originalId < 0) {
                    evaluation.setId(originalId);
                }
                if (evaluation.getNotes() != null) {
                    for (int i = 0; i < evaluation.getNotes().size(); i++) {
                        evaluation.getNotes().get(i).setId(originalNoteIds.get(i));
                    }
                }

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
            Evaluation eval = mapper.readValue(response, Evaluation.class);

            // Rétablir le lien Parent -> Enfant
            if (eval != null && eval.getNotes() != null) {
                for (Note note : eval.getNotes()) {
                    note.setEvaluation(eval); // On injecte l'objet eval dans chaque note
                }
            }
            return eval;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<Evaluation> parseEvaluationsListJson(String json) {
        try {
            JsonNode rootNode = mapper.readTree(json);
            JsonNode evaluationsNode = rootNode.path("_embedded").path("evaluationList");

            if (!evaluationsNode.isMissingNode() && evaluationsNode.isArray()) {
                List<Evaluation> evaluations = mapper.readerFor(new TypeReference<List<Evaluation>>(){})
                        .readValue(evaluationsNode);

                // On parcourt chaque évaluation et chaque note pour recréer les liens
                if (evaluations != null) {
                    for (Evaluation eval : evaluations) {
                        if (eval.getNotes() != null) {
                            for (Note note : eval.getNotes()) {
                                note.setEvaluation(eval);
                            }
                        }
                    }
                }
                return evaluations;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
