package org.openjfx.sio2E4.service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class NetworkService {
    private static final String API_URL = "http://localhost:8080/api";

    /**
     * Vérifie si l'accès à l'API est disponible.
     *
     * @return true si l'accès à l'API est possible (code 200), false sinon.
     */
    public static boolean isOnline() {
        try {
            URL url = new URL(API_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(3000); // Timeout de connexion : 3 secondes
            connection.setReadTimeout(3000);    // Timeout de lecture : 3 secondes

            int responseCode = connection.getResponseCode();
            return (responseCode == HttpURLConnection.HTTP_OK); // 200

        } catch (IOException e) {
            // En cas d'erreur de connexion (ex: API non disponible)
            return false;
        }
    }
}
