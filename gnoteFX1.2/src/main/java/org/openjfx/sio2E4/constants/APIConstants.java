package org.openjfx.sio2E4.constants;

public class APIConstants {
    // Base
    public static  String BASE_URL = "http://localhost:8080/api";

    // Auth endpoints
    public static final String AUTH_LOGIN = BASE_URL + "/login";
    public static final String AUTH_LOGOUT = BASE_URL + "/auth/logout";

    // Users endpoints
    public static final String USERS = BASE_URL + "/users";
    public static final String USER_BY_ID = BASE_URL + "/users/%d";
    public static final String USER_NOTES = BASE_URL + "/users/%d/notes";

    // Evaluations endpoints
    public static final String EVALUATIONS = BASE_URL + "/evaluations";
    public static final String EVALUATION_BY_ID = BASE_URL + "/evaluations/%d";
    // public static final String EVALUATION_TYPES = BASE_URL + "/evaluations/type";

    // Notes endpoints
    public static final String NOTES = BASE_URL + "/notes";
    public static final String NOTE_BY_ID = BASE_URL + "/notes/%d";
    public static final String NOTE_TYPES = BASE_URL + "/notes/type";
    public static final String NOTE_TYPES_BY_ID = BASE_URL + "/notes/type/%d";

    // Matieres endpoints
    public static final String MATIERES = BASE_URL + "/matieres";
    public static final String MATIERE_BY_ID = BASE_URL + "/matieres/%d";

    // Etudiants endpoint
    public static final String ETUDIANTS = BASE_URL + "/etudiants";

    // Méthode helper pour formater les URLs avec paramètres
    public static String formatUrl(String template, Object... params) {
        return String.format(template, params);
    }

    public static String getBaseUrl() {
        return BASE_URL;
    }

    public static void setBaseUrl(String baseUrl) {
        BASE_URL = baseUrl;
    }
}

// Utilisation :
// String url = ApiConstants.formatUrl(ApiConstants.USER_BY_ID, userId);
// String url = ApiConstants.formatUrl(ApiConstants.USER_NOTES, userId);
