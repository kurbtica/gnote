package org.openjfx.sio2E4.repository;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openjfx.sio2E4.constants.APIConstants;
import org.openjfx.sio2E4.model.NoteType;
import org.openjfx.sio2E4.service.AuthService;
import org.openjfx.sio2E4.service.LocalStorageService;
import org.openjfx.sio2E4.service.NetworkService;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class NoteTypeRepository {
    private final String BEARER_TOKEN = "Bearer " + AuthService.getToken();
    private final ObjectMapper mapper;
    private final HttpClient client;

    public NoteTypeRepository(ObjectMapper mapper, HttpClient client) {
        this.mapper = mapper;
        this.client = client;
    }

    public NoteTypeRepository() {
        this.mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.client = HttpClient.newHttpClient();
    }

    // --- LECTURE (READ) ---

    // Récupérer une noteType (Online ou Offline)
    public CompletableFuture<NoteType> getNoteType(int noteTypeID) {
        if (NetworkService.isOnline()) {
            return fetchNoteTypeFromApi(noteTypeID);
        } else {
            // On enveloppe l'appel local dans un Future pour garder la cohérence async
            return CompletableFuture.supplyAsync(() -> LocalStorageService.findNoteTypeById(noteTypeID));
        }
    }

    public CompletableFuture<List<NoteType>> getNoteTypesList() {
        if (NetworkService.isOnline()) {
            return fetchNoteTypesListFromApi();
        } else {
            // On enveloppe l'appel local dans un Future pour garder la cohérence async
            return CompletableFuture.supplyAsync(LocalStorageService::loadNoteTypes);
        }
    }

    // --- Méthodes privées pour l'API ---

    public CompletableFuture<NoteType> fetchNoteTypeFromApi(int noteTypeID) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(APIConstants.formatUrl(APIConstants.NOTE_BY_ID, noteTypeID)))
                .header("Authorization", BEARER_TOKEN)
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(this::parseNoteTypeJson);
    }

    public CompletableFuture<List<NoteType>> fetchNoteTypesListFromApi() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(APIConstants.NOTES))
                .header("Authorization", BEARER_TOKEN)
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(this::parseNoteTypesListJson);
    }

    // --- Méthodes de Parsing ---

    private NoteType parseNoteTypeJson(String response) {
        try {
            return mapper.readValue(response, NoteType.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<NoteType> parseNoteTypesListJson(String json) {
        try {
            // Utilisation de TypeFactory pour construire une List<NoteType> proprement
            return mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, NoteType.class));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
