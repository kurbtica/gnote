# Gestion des Matières

## Description
Le module Matières est la fondation indispensable pour organiser les étudiants (qui les suivent), les professeurs (qui les enseignent) et les évaluations (qui portent sur ces matières).

## Composants impliqués
- **Modèle** : `Matiere.java`, `MatiereRow.java` (Pour l'interface du tableau)
- **Service** : `MatiereService.java`
- **Repository** : `MatiereRepository.java`
- **Contrôleur** : `MatieresController.java` s'occupant de réagir aux actions de la vue `MatieresView.fxml`.

## Cas d'utilisation
1. **Référentiel des modules** : Ce système va centraliser la liste des matières, leurs coefficients initiaux ou la spécialité associée.
2. **Liaison des entités** : Ce module sert de pivot dans la création d'évaluations qui ne peuvent jamais être en roue libre, mais doivent pointer précisément vers un object "Matière".
3. **Présentation sous forme de grille** : L'objet métier complexe est factorisé par le `MatiereRow.java` pour convenir aux TableView de JavaFX et faciliter le tri, la recherche temporelle d'affichage ou la pagination.
