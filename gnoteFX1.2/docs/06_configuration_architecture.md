# Structure Globale et Configuration

## Description
Ce module détaille comment est lancé et configuré le projet Gnote globalement, permettant l'instanciation de base et la prévention de plantages ou de surcharges.

## Composants impliqués
- **`App.java`** : Contient le point d'entrée principal (`public static void main`).
- **`ConfIpApi.java` & `ConfigLoader.java`** : Chargent et interrogent les variables de déploiements.
- **`APIConstants.java`** : Base des différentes URI finales vers où les appels HTTP doivent être forgés.
- **`IpApi.yml`** : Fichier de configuration plat.

## Mécanismes Clés
1. **Configuration API dynamique** : 
   - Au lancement, le projet lit le fichier `IpApi.yml` (via la librairie SnakeYAML) pour récupérer la base URL (`base_url: http://localhost:8080/api`). L'information est injectée dans `APIConstants`. Cela rend la production d'un `.JAR` unique possible et paramétrable via une simple édition de ce fichier au même endroit que l'exécutable client, évitant la recompilation obligatoire de toute l'App à chaque changement de serveur.
2. **Verrou Multi-Instances (Single-Instance)** :
   - Pour éviter de compromettre l'intégrité de la base locale JSON générée par les actions hors linges, l'application crée un `ServerSocket` temporaire sur le port réseau local `9999`. 
   - Si un utilisateur double-clique 5 fois sur le programme, l’application empêchera l'ouverture des autres fenêtres via une interception transparente `IOException` ("Déjà en cours d'exécution").
3. **Rattrapage Global d'Erreurs (UncaughtExceptionHandler)** :
   - Un processus de thread de la JVM est altéré lors du `getStart()`. Toute erreur bloquante ignorée par les `catch` de l'application va être attrapée, et un `AlertHelper.showError()` rendra un affichage propre à l'utilisateur de l'exception, au lieu de rendre la fenêtre client sans réponse sans aucun indice visuel.
