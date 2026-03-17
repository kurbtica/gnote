package org.openjfx.sio2E4.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Deprecated
public class JsonMapper {

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Parse un JSON en liste d'objets du type spécifié.
     *
     * @param json  Le contenu JSON à parser
     * @param clazz La classe du modèle (ex: User.class)
     * @param <T>   Le type de l’objet attendu
     * @return Une liste d’objets du type T
     * @throws IOException En cas d’erreur de parsing
     */
    public static <T> List<T> parseList(String json, Class<T[]> clazz) throws IOException {
        return Arrays.asList(mapper.readValue(json, clazz));
    }

    /**
     * Parse un JSON en un seul objet.
     *
     * @param json  Le contenu JSON à parser
     * @param clazz La classe du modèle (ex: User.class)
     * @param <T>   Le type de l’objet attendu
     * @return L’objet du type T
     * @throws IOException En cas d’erreur de parsing
     */
    public static <T> T parseObject(String json, Class<T> clazz) throws IOException {
        return mapper.readValue(json, clazz);
    }

    /**
     * Convertit un objet Java en chaîne JSON.
     *
     * @param object L’objet à convertir
     * @return Le JSON correspondant en String
     */
    public static String toJson(Object object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }
}
