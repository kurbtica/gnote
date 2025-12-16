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
import java.util.List;

public class LocalStorageService {

    private static Path filePath = Paths.get("user_data.json");

    private static final ObjectMapper mapper = new ObjectMapper();

    private static final ArrayList<Etudiant> etudiants = new ArrayList<Etudiant>();
    private static final ArrayList<Matiere> matieres = new ArrayList<Matiere>();
    private static final ArrayList<Note> notes = new ArrayList<Note>();
    private static final ArrayList<NoteType> noteTypes = new ArrayList<NoteType>();
    private static final ArrayList<Role> roles = new ArrayList<Role>();
    private static final ArrayList<User> users = new ArrayList<User>();

    public static void setup() throws IOException {
        if (!Files.exists(filePath)) {
            String initialJson = "{\n" +
                    "  \"Etudiant\": [],\n" +
                    "  \"Matiere\": [],\n" +
                    "  \"Note\": [],\n" +
                    "  \"NoteType\": [],\n" +
                    "  \"Appreciation\" : [], \n" +
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

            int newId = -(array.size() + 1); // on met des identifiants négatifs quand on est en mode local
            // Vérifier si l'objet a déjà un ID
            Integer existingId = null;
            try {
                java.lang.reflect.Method getIdMethod = obj.getClass().getMethod("getId");
                existingId = (Integer) getIdMethod.invoke(obj);
            } catch (Exception e) {
                System.err.println("Erreur lors de la récupération de l'ID : " + e.getMessage());
            }

            if (existingId == null || existingId == 0) {

                // Modifier l'ID de l'objet en utilisant la réflexion
                try {
                    java.lang.reflect.Method setIdMethod = obj.getClass().getMethod("setId", int.class);
                    setIdMethod.invoke(obj, newId);
                } catch (Exception e) {
                    System.err.println("Erreur lors de la modification de l'ID : " + e.getMessage());
                }
            }

            // Petite sécurité pour ne pas ajouter deux fois le même objet (doublon JSON)
            boolean alreadyExists = false;
            if (existingId != null) {
                for (JsonNode node : array) {
                    if (node.has("id") && node.get("id").asInt() == existingId) {
                        alreadyExists = true;
                        break;
                    }
                }
            }

            if (!alreadyExists) {
                // Ajouter l'objet sérialisé (avec le nouvel ID)
                array.addPOJO(obj);

                // Réécrire le fichier
                mapper.writerWithDefaultPrettyPrinter().writeValue(filePath.toFile(), root);
            } else {
                // Si l'objet existe déjà, on appelle updateObject pour être sûr d'avoir la dernière version
                updateObject(obj, existingId, key);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Méthodes spécifiques
    public static void save(Etudiant etudiant) {
        saveObject(etudiant, "Etudiant");
    }

    public static void save(Matiere matiere) {
        saveObject(matiere, "Matiere");
    }

    public static void save(Evaluation evaluation) {
        // 1. Pré-calculer l'ID de l'évaluation
        // C'est nécessaire car les notes ont besoin de connaître l'ID de leur parent (Clé étrangère)
        if (evaluation.getId() == null || evaluation.getId() == 0) {
            int nextId = -(getNextObjectId("Evaluation") + 1);
            evaluation.setId(nextId);
        }

        // 2. Sauvegarder les enfants (Les Notes)
        if (evaluation.getNotes() != null) {
            for (Note note : evaluation.getNotes()) {
                // On lie la note à son parent (maintenant qu'il a un ID)
                note.setEvaluation(evaluation);

                // On sauvegarde la note individuellement
                // Cela va lui attribuer son propre ID (ex: -10) et l'écrire dans le tableau "Note"
                save(note);
            }
        }

        // 3. Sauvegarder le parent (L'Évaluation)
        // Maintenant, l'objet evaluation contient une liste de notes qui ont TOUTES des IDs valides.
        saveObject(evaluation, "Evaluation");
    }

    public static void save(Note note) {
        saveObject(note, "Note");
    }

    public static void save(NoteType noteType) {
        saveObject(noteType, "NoteType");
    }

    public static void save(Appreciation appreciation) {
        saveObject(appreciation, "Appreciation");
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

    public static Matiere findMatiereById(int id) {
        return findObjectById(id, "Matiere", Matiere.class);
    }

    public static Evaluation findEvaluationById(int id) {
        return findObjectById(id, "Evaluation", Evaluation.class);
    }

    public static Note findNoteById(int id) {
        return findObjectById(id, "Note", Note.class);
    }

    public static NoteType findNoteTypeById(int id) {
        return findObjectById(id, "NoteType", NoteType.class);
    }

    public static Appreciation findAppreciationById(int id) {
        return findObjectById(id, "Appreciation", Appreciation.class);
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

    public static ArrayList<Matiere> loadMatieres() {
        return loadObjects("Matiere", Matiere.class);
    }

    public static ArrayList<Evaluation> loadEvaluations() {
        return loadObjects("Evaluation", Evaluation.class);
    }

    public static ArrayList<Note> loadNotes() {
        return loadObjects("Note", Note.class);
    }

    public static ArrayList<NoteType> loadNoteTypes() {
        return loadObjects("NoteType", NoteType.class);
    }

    public static ArrayList<Appreciation> loadAppreciations() {
        return loadObjects("Appreciation", Appreciation.class);
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

    public static void remove(Matiere matiere) {
        removeObject(matiere.getId(), "Matiere");
    }

    public static void remove(Evaluation evaluation) {
        removeObject(evaluation.getId(), "Evaluation");
    }

    public static void remove(Note note) {
        removeObject(note.getId(), "Note");
    }

    public static void remove(NoteType noteType) {
        removeObject(noteType.getId(), "NoteType");
    }

    public static void remove(Appreciation appreciation) {
        removeObject(appreciation.getId(), "Appreciation");
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

    public static void update(Matiere matiere) {
        updateObject(matiere, matiere.getId(), "Matiere");
    }

    public static void update(Evaluation evaluation) {
        updateObject(evaluation, evaluation.getId(), "Evaluation");
    }

    public static void update(Note note) {
        updateObject(note, note.getId(), "Note");
    }

    public static void update(NoteType noteType) {
        updateObject(noteType, noteType.getId(), "NoteType");
    }

    public static void update(Appreciation appreciation) {
        updateObject(appreciation, appreciation.getId(), "Appreciation");
    }

    public static void update(Role role) {
        updateObject(role, role.getId(), "Role");
    }

    public static void update(User user) {
        updateObject(user, user.getId(), "User");
    }


    private static int getNextObjectId(String key) {
        try {
            JsonNode root = mapper.readTree(Files.readString(filePath));
            JsonNode arrayNode = root.get(key);

            if (arrayNode != null && arrayNode.isArray()) {
                return arrayNode.size();
            }
            return 0; // Si le tableau n'existe pas encore

        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    // Méthodes spécifiques
    public static int getNextID(Etudiant etudiant) {
        return getNextObjectId("Etudiant");
    }

    public static int getNextID(Matiere matiere) {
        return getNextObjectId("Matiere");
    }

    public static int getNextID(Evaluation evaluation) {
        return getNextObjectId("Evaluation");
    }

    public static int getNextID(Note note) {
        return getNextObjectId("Note");
    }

    public static int getNextID(NoteType noteType) {
        return getNextObjectId("NoteType");
    }

    public static int getNextID(Appreciation appreciation) {
        return getNextObjectId("Appreciation");
    }

    public static int getNextID(Role role) {
        return getNextObjectId("Role");
    }

    public static int getNextID(User user) {
        return getNextObjectId("User");
    }

    /*
     * Méthode pour ÉCRASER une liste complète (Utilisé lors du Pull serveur)
     */
    public static synchronized <T> void replaceAllObject(List<T> objects, String key) {
        try {
            // Lire le fichier actuel
            ObjectNode root;
            if (Files.exists(filePath)) {
                root = (ObjectNode) mapper.readTree(Files.readString(filePath));
            } else {
                root = mapper.createObjectNode();
            }

            // Créer un nouveau tableau vide et le remplir avec les données venant du serveur
            ArrayNode newArray = mapper.createArrayNode();

            for (T obj : objects) {
                newArray.addPOJO(obj);
            }

            // Remplacer l'ancienne liste par la nouvelle dans le JSON et sauvegarder
            root.set(key, newArray);

            mapper.writerWithDefaultPrettyPrinter().writeValue(filePath.toFile(), root);
            System.out.println("✅ Liste '" + key + "' mise à jour localement (" + objects.size() + " éléments).");

        } catch (IOException e) {
            System.err.println("❌ Erreur lors du remplacement de la liste " + key);
            e.printStackTrace();
        }
    }

    // Méthodes spécifiques
    public static void replaceEtudiants(List<Etudiant> etudiant) {
        replaceAllObject(etudiant, "Etudiant");
    }

    public static void replaceMatieres(List<Matiere> matiere) {
        replaceAllObject(matiere, "Matiere");
    }

    public static void replaceEvaluations(List<Evaluation> evaluation) {
        replaceAllObject(evaluation, "Evaluation");
    }

    public static void replaceNotes(List<Note> note) {
        replaceAllObject(note, "Note");
    }

    public static void replaceNoteTypes(List<NoteType> noteType) {
        replaceAllObject(noteType, "NoteType");
    }

    public static void replaceAppreciations(List<Appreciation> appreciation) {
        replaceAllObject(appreciation, "Appreciation");
    }

    public static void replaceRoles(List<Role> role) {
        replaceAllObject(role, "Role");
    }

    public static void replaceUsers(List<User> user) {
        replaceAllObject(user, "User");
    }

    // Getter / Setter filePath

    public static Path getFilePath() {
        return filePath;
    }

    public static void setFilePath(Path filePath) {
        LocalStorageService.filePath = filePath;
    }
}
