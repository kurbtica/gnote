# Authentification et Sécurité

## Description
Le module d'authentification gère la connexion, la déconnexion et les sessions des utilisateurs de l'application Gnotes. Il adapte également l'interface et les droits selon les rôles définis pour chaque profil (Admin, Enseignant, etc.).

## Composants impliqués
- **`AuthService.java`** : Classe métier centrale s'occupant d'envoyer la requête de connexion (`/auth/login`) à l'API et de traiter le jeton JWT renvoyé.
- **`loginPageController.java` & `loginPage.fxml`** : Interface de connexion pour l'utilisateur.
- **`logoutPageController.java`** : Gère la demande de déconnexion volontaire de l'utilisateur.

## Fonctionnement
1. **Connexion En Ligne** : 
   - L'utilisateur entre son adresse e-mail et son mot de passe.
   - Les données sont transmises à l'API via une requête POST.
   - En cas de succès, un token de session est enregistré temporairement en mémoire et l'utilisateur est redirigé vers son interface (Admin ou Enseignant) selon le rôle défini dans son objet `Role`.
2. **Connexion Hors Ligne** : 
   - L'application vérifie si l'API est accessible via `NetworkService`. Si elle ne l'est pas, le processus se bascule sur le `LocalStorageService` qui charge les utilisateurs trouvés dans `user_data.json`.
   - Cela permet aux professeurs de se connecter même sans connexion internet, si leur compte a déjà été synchronisé au préalable.
3. **Déconnexion** : 
   - Une requête de logout supprime la session côté API et nettoie les identifiants locaux (`sessionToken = null`).

## Rôles Supportés
* `ADMIN` : Accès aux interfaces d'administration (`Admin.fxml`).
* `ENSEIGNANT` : Accès aux interfaces dédiées aux professeurs (`Enseignant.fxml`).
* `USER` (par défaut).
