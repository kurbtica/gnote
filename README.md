# Projet Gnotes

Gnotes est un projet complet de gestion de notes dans un environnement scolaire ou académique. Il est composé de deux parties principales :
1. **L'application cliente** construite en JavaFX (`gnoteFX1.2`)
2. **L'API** (backend) avec laquelle l'application communique (`API_Gnotes`)

---

## 🖥️ Application Client (JavaFX) - Gnotes

> **Note importante :** Lors du lancement ou de la configuration de l'application cliente, utilisez l'argument VM suivant pour éviter une erreur :
> `--enable-native-access=javafx.graphics`

### Gnotes - Application JavaFX de Gestion de Notes

Gnotes est une application de bureau métier développée en **JavaFX** permettant la gestion des notes, des évaluations, des matières et des utilisateurs dans un environnement scolaire ou académique. L'application intègre notamment une gestion des rôles (Administrateur, Enseignant, Étudiant) et un système de synchronisation pour le fonctionnement en mode déconnecté.

### 🌟 Fonctionnalités Principales

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

### ⚙️ Architecture Technique & Technologies Utilisées

*   **Langage :** Java 11 (ou supérieur)
*   **Interface Graphique :** JavaFX 21 (vues construites avec FXML)
*   **Gestionnaire de Dépendances :** Maven (`pom.xml`)
*   **Manipulation de données JSON :** Jackson Databind 2.15.x
*   **Configuration :** SnakeYAML 2.5
*   **Tests Unitaires et UI :** JUnit 5 & TestFX 4

### 📂 Structure du Projet

*   `/gnoteFX1.2/src/main/java/org/openjfx/sio2E4/` : Code source Java de l'application.
    *   `/controller` : Contrôleurs faisant le pont entre les vues (FXML) et les données.
    *   `/model` : Classes métiers principales (User, Etudiant, Evaluation, Note, Matiere).
    *   `/repository` & `/service` : Accès aux données locales et gestion de la synchronisation (API REST, Jackson).
*   `/gnoteFX1.2/src/main/resources/org/openjfx/sio2E4/` : Fichiers FXML (interface graphique) et ressources visuelles (logo).
*   `gnoteFX1.2/user_data.json` : Base de données locale générée au format JSON.
*   `gnoteFX1.2/IpApi.yml` : Fichier de configuration spécifiant l'URL de base de l'API.

## 🚀 Guide d'Installation

### 📋 Prérequis

Avant de commencer, assurez-vous d'avoir installé les outils suivants sur votre machine :

- **WAMP** (ou XAMPP) : Serveur local pour la base de données MySQL.
- **Java (JDK 21)** : Version recommandée pour le support de JavaFX 21.
- **Maven** : Gestionnaire de dépendances pour les projets Java.
- **Git** : Pour cloner le dépôt (optionnel).

### Installation de WAMP

1. Téléchargez WAMP depuis le site officiel : [https://www.wampserver.com/](https://www.wampserver.com/)
2. Installez WAMP en suivant les instructions d'installation.
3. Lancez WAMP et assurez-vous que le service **MySQL** est démarré (icône verte).

### Installation du JDK & Maven (Méthode Propre)

Pour que le projet fonctionne sans erreur (notamment l'erreur `--add-exports`), vous **devez** utiliser le **JDK 21**.

#### 1. Installer le JDK 21
Le plus simple est d'utiliser `winget` (le gestionnaire de paquets Windows) ou de télécharger l'installeur :
*   **Via Terminal (Recommandé) :** Ouvrez un terminal et tapez :
    ```powershell
    winget install Microsoft.OpenJDK.21
    ```
*   **Via Téléchargement :** [Microsoft OpenJDK 21](https://learn.microsoft.com/fr-fr/java/openjdk/download#openjdk-21) (choisir l'installeur `.msi` pour Windows x64).

#### 2. Configurer JAVA_HOME
1. Cherchez "Modifier les variables d'environnement système" dans Windows.
2. Cliquez sur **Variables d'environnement**.
3. Dans **Variables système**, cliquez sur **Nouvelle** :
    *   Nom : `JAVA_HOME`
    *   Valeur : Le chemin d'installation (ex: `C:\Program Files\Microsoft\jdk-21.x.x-hotspot`)
4. Dans la variable **Path**, ajoutez une nouvelle ligne : `%JAVA_HOME%\bin`.

#### 3. Installer Maven
*   **Via Terminal :**
    ```powershell
    winget install Apache.Maven
    ```
*   **Alternative (Wrapper) :** Dans le dossier `API_Gnotes`, vous pouvez utiliser directement `./mvnw` au lieu de `mvn`. Cela utilisera la version de Maven intégrée au projet.

#### 4. Vérification
Ouvrez un **nouveau** terminal et tapez :
```powershell
java -version  # Doit afficher version "21" (ou supérieur)
mvn -version   # Doit s'exécuter sans erreur
```

---

## 💡 Dépannage (Erreurs fréquentes)

### Erreur : `Unrecognized option: --add-exports`
Si vous voyez cette erreur, c'est que votre terminal utilise **Java 8**.
*   **Cause :** Le projet utilise des modules Java modernes. Java 8 ne comprend pas ces options et s'arrête immédiatement.
*   **Solution :** Vérifiez votre `JAVA_HOME`. Tapez `echo $env:JAVA_HOME` dans PowerShell. Si cela pointe vers Java 8 (ou est vide), suivez l'étape **2. Configurer JAVA_HOME** ci-dessus.

### Utilisation avec IntelliJ IDEA (Recommandé)
Pour éviter de gérer les variables d'environnement manuellement :
1. Ouvrez le projet dans IntelliJ.
2. Allez dans `File` > `Project Structure` > `Project`.
3. Assurez-vous que le **SDK** est réglé sur **21** (ou plus). Si vous ne l'avez pas, IntelliJ peut le télécharger pour vous via `Add SDK` > `Download JDK`.

---

## 🛠️ Installation du projet

1. **Cloner ou copier le projet** :
   - Placez le dossier du projet dans votre répertoire de travail habituel.

2. **Configuration de la Base de Données** :
   - Créez une base de données MySQL nommée `gnotes_bdd` via phpMyAdmin (accessible via WAMP).
   - L'API est configurée pour mettre à jour le schéma automatiquement, mais vous pouvez importer le script initial si nécessaire :
     `API_Gnotes/src/main/java/com/stsau/slam2/API_Gnotes/gnote_bdd.sql`

3. **Configuration de l'Environnement** :
   - **Backend (API)** : Éditez `API_Gnotes/src/main/resources/application.properties` pour ajuster vos paramètres (URL, utilisateur, mot de passe).
   - **Frontend (Client)** : Vérifiez le fichier `gnoteFX1.2/IpApi.yml` pour l'adresse de l'API :
     ```yaml
     api:
       base_url: http://localhost:8080/api
     ```

---

## 💻 Commandes à exécuter

Voici les étapes pour lancer les deux composants du projet. Ouvrez **2 terminaux séparés** dans les dossiers respectifs :

### 1. Lancer l'API (Backend)
```bash
cd API_Gnotes
mvn spring-boot:run
```
L'API sera accessible sur `http://localhost:8080`.

### 2. Lancer l'Application Client (Frontend)
```bash
cd gnoteFX1.2
mvn javafx:run
```
> **Note :** Si vous lancez l'application depuis un IDE (IntelliJ/Eclipse), n'oubliez pas d'ajouter l'argument VM suivant :
> `--enable-native-access=javafx.graphics`

### 3. Compiler et Packager (Génération des JAR : Optionnel)
```bash
mvn clean package
```
Les fichiers générés se trouveront dans les dossiers `target/` de chaque module.

---

## 🧪 Tests

- Exécutez les tests unitaires et d'interface avec :
  ```bash
  mvn test
  ```
- Pour les tests JavaFX (TestFX), assurez-vous que votre environnement graphique permet le focus sur les fenêtres lors de l'exécution.

---

## ⚠️ À savoir

* L'application cliente utilise le port local `9999` pour garantir une instance unique.
* Si l'API est injoignable au démarrage, l'application s'arrêtera avec un message d'erreur.
* Les données locales sont stockées dans `user_data.json` et `sync_data.json` pour le mode déconnecté.
