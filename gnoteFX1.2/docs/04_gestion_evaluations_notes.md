# Gestion des Évaluations et des Notes

## Description
C'est le composant métier principal pour les enseignants. Il leur permet de créer des évaluations liées à une matière et d'y enregistrer les notes et appréciations individuelles des élèves.

## Composants impliqués
- **Modèles** : `Evaluation.java`, `Note.java`, `Appreciation.java`, `NoteType.java` regroupant les structures de la base de données.
- **Repository** : `EvaluationRepository`, `NoteRepository`, `NoteTypeRepository` gérant l'interrogation de l'API.
- **Vues** : 
  - `EvaluationListView.fxml` & `EvaluationListController` : Affichage sous forme de tableau (via `EvaluationRow.java` pour le TableView) des différentes évaluations configurées.
  - `EvaluationFormView.fxml` & `EvaluationFormController` : Interface de création d'une nouvelle évaluation.
  - `editNoteDialog.fxml` : Modale (Popup) pour saisir facilement la note ou l'appréciation d'un étudiant.

## Fonctionnalités
1. **Création d'évaluation** : Permet au professeur de définir une session (nom, date, coefficient, type via `NoteType`).
2. **Saisie des notes** : Par le biais d'un mécanisme de dialogue, l'enseignant peut ajouter la note chiffrée ou l'appréciation de l'élève.
3. **Intégration hors ligne** : Chaque ajout/édition est intercepté par le `SyncService`. Si le serveur est injoignable, la note enregistrée par le prof est sauvée en interne et envoyée à l'API lors du prochain retour en ligne.
