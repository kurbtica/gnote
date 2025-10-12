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
     *   Méthodes pour rechercher un objet par ID
     */

    private static <T> T findObjectById(Number id, String key, Class<T> clazz) {
        try {
            JsonNode root = mapper.readTree(Files.readString(filePath));
            JsonNode arrayNode = root.get(key);

            if (arrayNode != null && arrayNode.isArray()) {
                for (JsonNode node : arrayNode) {
                    JsonNode idNode = node.get("id");

                    if (idNode != null) {
                        long nodeId = idNode.asLong();
                        long searchId = id.longValue();

                        if (nodeId == searchId) {
                            return mapper.treeToValue(node, clazz);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Méthodes spécifiques pour rechercher par ID
    public static Etudiant findEtudiantById(int id) {
        return findObjectById(id, "Etudiant", Etudiant.class);
    }

    public static LocalUser findLocalUserById(int id) {
        return findObjectById(id, "LocalUser", LocalUser.class);
    }

    public static Matiere findMatiereById(int id) {
        return findObjectById(id, "Matiere", Matiere.class);
    }

    public static Note findNoteById(int id) {
        return findObjectById(id, "Note", Note.class);
    }

    public static NoteType findNoteTypeById(int id) {
        return findObjectById(id, "NoteType", NoteType.class);
    }

    public static Role findRoleById(int id) {
        return findObjectById(id, "Role", Role.class);
    }

    public static User findUserById(int id) {
        return findObjectById(id, "User", User.class);
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

    private static <T> void removeObject(Number id, String key) {
        try {
            ObjectNode root = (ObjectNode) mapper.readTree(Files.readString(filePath));
            ArrayNode array = (ArrayNode) root.get(key);
            if (array == null) return;

            for (int i = 0; i < array.size(); i++) {
                JsonNode node = array.get(i);
                JsonNode idNode = node.get("id");

                if (idNode != null && idNode.isNumber() && idNode.asLong() == id.longValue()) {
                    array.remove(i);
                    break;
                }
            }

            mapper.writerWithDefaultPrettyPrinter().writeValue(filePath.toFile(), root);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // Méthodes spécifiques pour la supprimer des objets
    public static void remove(Etudiant etudiant) {
        removeObject(etudiant.getId(), "Etudiant");
    }

    public static void remove(LocalUser localUser) {
        removeObject(localUser.getId(), "LocalUser");
    }

    public static void remove(Matiere matiere) {
        removeObject(matiere.getId(), "Matiere");
    }

    public static void remove(Note note) {
        removeObject(note.getId(), "Note");
    }

    public static void remove(NoteType noteType) {
        removeObject(noteType.getId(), "NoteType");
    }

    public static void remove(Role role) {
        removeObject(role.getId(), "Role");
    }

    public static void remove(User user) {
        removeObject(user.getId(), "User");
    }

    /*
     *   Méthodes pour modifier des objets sauvegardés en mode hors ligne
     */

    private static <T> void updateObject(T obj, Number id, String key) {
        try {
            // Lire le JSON existant
            ObjectNode root = (ObjectNode) mapper.readTree(Files.readString(filePath));
            ArrayNode array = (ArrayNode) root.get(key);
            if (array == null) return;

            boolean updated = false;

            // Parcourir le tableau et remplacer l'objet correspondant
            for (int i = 0; i < array.size(); i++) {
                JsonNode node = array.get(i);
                JsonNode idNode = node.get("id");

                if (idNode != null && idNode.isNumber() && idNode.asLong() == id.longValue()) {
                    // Remplacement : on sérialise l'objet et on écrase l'ancien
                    JsonNode newNode = mapper.valueToTree(obj);
                    array.set(i, newNode);
                    updated = true;
                    break;
                }
            }

            if (updated) {
                mapper.writerWithDefaultPrettyPrinter().writeValue(filePath.toFile(), root);
            } else {
                System.out.println("⚠️ Aucun objet trouvé avec l'ID " + id + " dans " + key);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Méthodes spécifiques pour la modifer des objets
    public static void update(Etudiant etudiant) {
        updateObject(etudiant, etudiant.getId(), "Etudiant");
    }

    public static void update(LocalUser localUser) {
        updateObject(localUser, localUser.getId(), "LocalUser");
    }

    public static void update(Matiere matiere) {
        updateObject(matiere, matiere.getId(), "Matiere");
    }

    public static void update(Note note) {
        updateObject(note, note.getId(), "Note");
    }

    public static void update(NoteType noteType) {
        updateObject(noteType, noteType.getId(), "NoteType");
    }

    public static void update(Role role) {
        updateObject(role, role.getId(), "Role");
    }

    public static void update(User user) {
        updateObject(user, user.getId(), "User");
    }

    // Getter / Setter filePath

    public static Path getFilePath() {
        return filePath;
    }

    public static void setFilePath(Path filePath) {
        LocalStorageService.filePath = filePath;
    }
}
