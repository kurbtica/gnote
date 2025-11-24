package org.openjfx.sio2E4.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openjfx.sio2E4.constants.APIConstants;
import org.openjfx.sio2E4.model.Note;
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

public class NoteRepository {

    private final String BEARER_TOKEN = "Bearer " + AuthService.getToken();
    private final ObjectMapper mapper;
    private final HttpClient client;

    public NoteRepository(ObjectMapper mapper, HttpClient client) {
        this.mapper = mapper;
        this.client = client;
    }

    public NoteRepository() {
        this.mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.client = HttpClient.newHttpClient();
    }

    // --- LECTURE (READ) ---

    // Récupérer une note (Online ou Offline)
    public CompletableFuture<Note> getNote(int noteID) {
        if (NetworkService.isOnline()) {
            return fetchNoteFromApi(noteID);
        } else {
            // On enveloppe l'appel local dans un Future pour garder la cohérence async
            return CompletableFuture.supplyAsync(() -> LocalStorageService.findNoteById(noteID));
        }
    }

    public CompletableFuture<List<Note>> getNotesList() {
        if (NetworkService.isOnline()) {
            return fetchNotesListFromApi();
        } else {
            // On enveloppe l'appel local dans un Future pour garder la cohérence async
            return CompletableFuture.supplyAsync(LocalStorageService::loadNotes);
        }
    }

    // --- ÉCRITURE (CREATE, UPDATE, DELETE) ---

    /**
     * Ajoute une note (API ou Local)
     * @return true si succès, false sinon
     */
    public CompletableFuture<Boolean> createNote(Note note) {
        if (NetworkService.isOnline()) {
            try {
                String json = mapper.writeValueAsString(note); // Conversion automatique Objet -> JSON

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(APIConstants.NOTES))
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
                LocalStorageService.save(note);
                return true;
            });
        }
    }

    /**
     * Met à jour une note
     */
    public CompletableFuture<Boolean> updateNote(Note note) {
        if (NetworkService.isOnline()) {
            try {
                String json = mapper.writeValueAsString(note);

                HttpRequest request = HttpRequest.newBuilder()
                        // Suppose que vous avez une constante NOTES_BY_ID
                        .uri(URI.create(APIConstants.formatUrl(APIConstants.NOTE_BY_ID, note.getId())))
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
                LocalStorageService.update(note);
                return true;
            });
        }
    }

    /**
     * Supprime une note
     */
    public CompletableFuture<Boolean> deleteNote(int noteID) {
        if (NetworkService.isOnline()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(APIConstants.formatUrl(APIConstants.NOTE_BY_ID, noteID)))
                    .header("Authorization", BEARER_TOKEN)
                    .DELETE()
                    .build();

            return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> response.statusCode() == 204 || response.statusCode() == 200);
        } else {
            return CompletableFuture.supplyAsync(() -> {
                // Logique locale simplifiée
                List<Note> notes = LocalStorageService.loadNotes();
                Optional<Note> target = notes.stream().filter(n -> n.getId() == noteID).findFirst();
                target.ifPresent(LocalStorageService::remove);
                return target.isPresent();
            });
        }
    }


    // --- Méthodes privées pour l'API ---

    public CompletableFuture<Note> fetchNoteFromApi(int noteID) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(APIConstants.formatUrl(APIConstants.NOTE_BY_ID, noteID)))
                .header("Authorization", BEARER_TOKEN)
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(this::parseNoteJson);
    }

    public CompletableFuture<List<Note>> fetchNotesListFromApi() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(APIConstants.NOTES))
                .header("Authorization", BEARER_TOKEN)
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(this::parseNotesListJson);
    }

    // --- Méthodes de Parsing ---

    private Note parseNoteJson(String response) {
        try {
            return mapper.readValue(response, Note.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<Note> parseNotesListJson(String json) {
        try {
            // Utilisation de TypeFactory pour construire une List<Note> proprement
            return mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, Note.class));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
