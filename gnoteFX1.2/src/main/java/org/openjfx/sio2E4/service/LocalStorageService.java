package org.openjfx.sio2E4.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.openjfx.sio2E4.model.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class LocalStorageService {

    private static Path filePath = Paths.get("user_data.json");

    private static final ObjectMapper mapper = new ObjectMapper();

    public static void setup() throws IOException {
        if (!Files.exists(filePath)) {
            String initialJson = "{\n" +
                    "  \"Etudiant\": [],\n" +
                    "  \"LocalUser\": [],\n" +
                    "  \"Matiere\": [],\n" +
                    "  \"Note\": [],\n" +
                    "  \"NoteType\": [],\n" +
                    "  \"Role\": [],\n" +
                    "  \"User\": []\n" +
                    "}";
            Files.writeString(filePath, initialJson);
            System.out.println("Fichier 'user_data.json' créé car il n'existait pas.");
        }
    }

    /*
     *   Méthodes pour sauvegarder les objets dans un fichier json pour les utiliser en mode hors ligne
     */

    private static <T> void saveObject(T obj, String key) {
        try {
            // Lire le JSON existant
            ObjectNode root = (ObjectNode) mapper.readTree(Files.readString(filePath));

            // Récupérer le tableau correspondant
            ArrayNode array = (ArrayNode) root.get(key);
            if (array == null) {
                array = mapper.createArrayNode();
                root.set(key, array);
            }

            // Ajouter l'objet sérialisé
            array.addPOJO(obj);

            // Réécrire le fichier
            mapper.writerWithDefaultPrettyPrinter().writeValue(filePath.toFile(), root);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Méthodes spécifiques
    public static void save(Etudiant etudiant) {
        saveObject(etudiant, "Etudiant");
    }

    public static void save(LocalUser localUser) {
        saveObject(localUser, "LocalUser");
    }

    public static void save(Matiere matiere) {
        saveObject(matiere, "Matiere");
    }

    public static void save(Note note) {
        saveObject(note, "Note");
    }

    public static void save(NoteType noteType) {
        saveObject(noteType, "NoteType");
    }

    public static void save(Role role) {
        saveObject(role, "Role");
    }

    public static void save(User user) {
        saveObject(user, "User");
    }

    /*
    *   Méthodes pour charger les objets sauvegardés en mode hors ligne
    */

    private static <T> ArrayList<T> loadObjects(String key, Class<T> clazz) {
        ArrayList<T> result = new ArrayList<>();
        try {
            JsonNode root = mapper.readTree(Files.readString(filePath));
            JsonNode arrayNode = root.get(key);

            if (arrayNode != null && arrayNode.isArray()) {
                for (JsonNode node : arrayNode) {
                    T obj = mapper.treeToValue(node, clazz);
                    result.add(obj);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    // Méthodes spécifiques pour récupérer les objets
    public static ArrayList<Etudiant> loadEtudiants() {
        return loadObjects("Etudiant", Etudiant.class);
    }

    public static ArrayList<LocalUser> loadLocalUsers() {
        return loadObjects("LocalUser", LocalUser.class);
    }

    public static ArrayList<Matiere> loadMatieres() {
        return loadObjects("Matiere", Matiere.class);
    }

    public static ArrayList<Note> loadNotes() {
        return loadObjects("Note", Note.class);
    }

    public static ArrayList<NoteType> loadNoteTypes() {
        return loadObjects("NoteType", NoteType.class);
    }

    public static ArrayList<Role> loadRoles() {
        return loadObjects("Role", Role.class);
    }

    public static ArrayList<User> loadUsers() {
        return loadObjects("User", User.class);
    }

    /*
     *   Méthodes pour supprimer des objets sauvegardés en mode hors ligne
     */

    private static <T> void removeObject(T obj, String key) {
        try {
            // Lire le JSON existant
            ObjectNode root = (ObjectNode) mapper.readTree(Files.readString(filePath));
            ArrayNode array = (ArrayNode) root.get(key);

            if (array != null) {
                // On parcourt les éléments pour trouver l'objet à supprimer
                for (int i = 0; i < array.size(); i++) {
                    T current = mapper.treeToValue(array.get(i), (Class<T>) obj.getClass());
                    if (current.equals(obj)) { // <- Il faut que equals() soit bien implémenté
                        array.remove(i);
                        break;
                    }
                }
            }

            // Réécrire le JSON
            mapper.writerWithDefaultPrettyPrinter().writeValue(filePath.toFile(), root);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // Méthodes spécifiques pour la supprimer des objets
    public static void remove(Etudiant etudiant) {
        removeObject(etudiant, "Etudiant");
    }

    public static void remove(LocalUser localUser) {
        removeObject(localUser, "LocalUser");
    }

    public static void remove(Matiere matiere) {
        removeObject(matiere, "Matiere");
    }

    public static void remove(Note note) {
        removeObject(note, "Note");
    }

    public static void remove(NoteType noteType) {
        removeObject(noteType, "NoteType");
    }

    public static void remove(Role role) {
        removeObject(role, "Role");
    }

    public static void remove(User user) {
        removeObject(user, "User");
    }

    // Getter / Setter filePath

    public static Path getFilePath() {
        return filePath;
    }

    public static void setFilePath(Path filePath) {
        LocalStorageService.filePath = filePath;
    }
}
