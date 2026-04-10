# Synchronisation et Mode Hors Ligne

## Description
Gnotes est conçu pour être résilient face aux pertes de connexion internet. L'application possède un mode hors ligne complet et un système de synchronisation asynchrone ("Local First").

## Composants impliqués
- **`NetworkService.java`** : Vérifie l'état de la connexion internet en temps réel pour décider du comportement de l'application.
- **`LocalStorageService.java`** : Gère la sauvegarde et la lecture des données sous format local (sur disques via des fichiers JSON comme `user_data.json`).
- **`SyncService.java`** : Centralise la logique de "Pending Actions" (actions en attente).
- **`model/PendingAction.java`** : Modèle de donnée représentant une action qui n'a pas pu être envoyée à l'API.
- **`pending_sync.json`** : Le fichier où sont stockées les listes d'attentes de synchronisation.

## Fonctionnement
1. **Sauvegarde Locale Transparente** : 
   - À chaque récupération de données réussie via l'API, les informations sont mises en cache dans les fichiers JSON gérés par `LocalStorageService`. Cela permet un accès read-only (et certaines actions d'écriture) si le réseau venait à tomber.
2. **File d'Attente des Requêtes (Pending Sync)** : 
   - Si un enseignant ajoute une note ou une évaluation en mode OFFLINE, l'action métier n'échoue pas. Elle est interceptée, formatée en objet de type `PendingAction` (contenant le format JSON, la méthode HTTP, et le endpoint cible), puis sauvegardée de façon sécurisée localement.
3. **Resynchronisation Automatique** : 
   - Dès que le `NetworkService` détecte que l'application retrouve un accès internet fiable, le `SyncService` est notifié et parcourt séquentiellement la queue de `pending_sync.json`.
   - Il rejoue toutes les requêtes en attente vers l'API, vidant petit à petit la file locale. L'état global est ainsi de nouveau aligné avec le serveur distant.
