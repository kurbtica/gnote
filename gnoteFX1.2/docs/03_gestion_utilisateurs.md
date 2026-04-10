# Gestion des Utilisateurs

## Description
Le module Utilisateurs permet à l'administration de piloter la base de données des étudiants et des professeurs.

## Composants impliqués
- **Vues & Layouts** : `UsersController.java`, `EtudiantsController.java`, `AdminPageController.java`
- **Composants d'interface (UI Cards)** : `UserCardController`, `EtudiantCardController`, `TeacherCardController` qui pilotent les `.fxml` respectifs, permettant un affichage très modulaire et élégant en cartes pour chaque utilisateur (Avatar, nom, informations de base).
- **Service & Données** : `UserRepository.java`, `User.java`, `Etudiant.java`

## Fonctionnalités Clés
1. **Liste des Etudiants** : L'interface (`EtudiantsView.fxml`) affiche sous format de liste/grille tous les étudiants inscrits, possiblement filtrables.
2. **Liste des Enseignants** : Géré via les vues d'administration associées, où chaque enseignant est présenté via une `TeacherCardView`.
3. **Modification et Assignation** : L'API ou l'accès SQLite/Local permet d'aller modifier les adresses, e-mails, rôles ou numéros de téléphones pour les entités métier gérées.
4. **Héritage Objet** : Le modèle sépare de façon intelligente l'entité mère `User` de l'entité fille `Etudiant` pour structurer logiquement les habilitations dans le traitement métier propre aux élèves (qui ont des notes, contrairement au User classique).
