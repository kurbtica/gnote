# Gnotes - Application JavaFX de Gestion de Notes

Gnotes est une application de bureau métier développée en **JavaFX** permettant la gestion des notes, des évaluations, des matières et des utilisateurs dans un environnement scolaire ou académique. L'application intègre notamment une gestion des rôles (Administrateur, Enseignant, Étudiant) et un système de synchronisation pour le fonctionnement en mode déconnecté.

## 🌟 Fonctionnalités Principales

*   **Gestion des Utilisateurs & Rôles :**
    *   **Administrateur :** Gestion globale, création et supervision des utilisateurs.
    *   **Enseignant :** Création des évaluations, saisie des notes et ajout d'appréciations.
    *   **Étudiant :** Consultation de ses notes et évaluations.
*   **Gestion des Évaluations & Notes :**
    *   Saisie des notes par matière, type d'évaluation (Devoir Surveillé, TP, Exposé, etc.) et coefficient.
    *   Attribution des notes et des appréciations aux étudiants.
    *   Mise en valeur du major de promotion et indicateurs colorés pour les notes insuffisantes.
*   **Mode Hors-Ligne & Sécurité Renforcée :**
    *   Sauvegarde locale des données au format JSON (`user_data.json` / `sync_data.json`).
    *   Le système fonctionne de manière sécurisée hors-ligne grâce à une mise en cache des utilisateurs avec mots de passe hachés et salés.
    *   Authentification auprès de l'API via la méthode globale de gestion des tokens, et protection par clé aléatoire contre le vol lors des déconnexions.
*   **Synchronisation API :**
    *   Synchronisation manuelle contrôlée via un bouton dédié, permettant la gestion intelligente des évaluations créées hors ligne et des identifiants temporaires locaux.
*   **Configuration Dynamique :**
    *   L'adresse de l'API distante est facilement configurable via le fichier `IpApi.yml`.
*   **Monoposte (Instance Unique) :**
    *   L'application garantit qu'une seule instance graphique peut tourner simultanément pour éviter tout conflit de données.

## ⚙️ Architecture Technique & Technologies Utilisées

*   **Langage :** Java 11 (ou supérieur)
*   **Interface Graphique :** JavaFX 21 (vues construites avec FXML)
*   **Gestionnaire de Dépendances :** Maven (`pom.xml`)
*   **Manipulation de données JSON :** Jackson Databind 2.15.x
*   **Configuration :** SnakeYAML 2.5
*   **Tests Unitaires et UI :** JUnit 5 & TestFX 4

## 📂 Structure du Projet

*   `/src/main/java/org/openjfx/sio2E4/` : Code source Java de l'application.
    *   `/controller` : Contrôleurs faisant le pont entre les vues (FXML) et les données.
    *   `/model` : Classes métiers principales (User, Etudiant, Evaluation, Note, Matiere).
    *   `/repository` & `/service` : Accès aux données locales et gestion de la synchronisation (API REST, Jackson).
*   `/src/main/resources/org/openjfx/sio2E4/` : Fichiers FXML (interface graphique) et ressources visuelles (logo).
*   `user_data.json` : Base de données locale générée au format JSON.
*   `IpApi.yml` : Fichier de configuration spécifiant l'URL de base de l'API.

## 🚀 Installation & Exécution

### Prérequis
*   Avoir **Java 11** (ou plus récent) installé et configuré (JDK).
*   Avoir **Maven** installé.

### Compiler et exécuter le projet

Cloner (ou ouvrir) ce projet et ouvrir un terminal à la racine `gnoteFX1.2` :

1. **Compiler l'application :**
   ```bash
   mvn clean compile
   ```

2. **Lancer l'application :**
   ```bash
   mvn javafx:run
   ```

### Construire un exécutable (JAR)

Un plugin Assembly est configuré pour packager l'application avec toutes ses dépendances dans un seul fichier :

```bash
mvn clean package
```
*Le fichier `.jar` généré se trouvera ensuite dans le dossier `target/`.*

## 🧪 Tests

Les tests unitaires ainsi que les tests de validation de l'interface graphique (via TestFX) peuvent être exécutés avec la commande suivante :

```bash
mvn test
```

## ⚠️ À savoir
* Par défaut, l'application écoute sur le port local `9999` pour éviter l'ouverture multiple de l'interface simultanément.
* Si le chargement de l'API échoue (`IpApi.yml` invalide ou absent), l'application s'arrêtera au lancement avec une erreur console.
