## Voici une liste de refactoring à effectuer pour améliorer la structure de votre code
---

<br>

### 🎨 Extraire la logique UI des boutons d'action

- [ ] Créer une classe UserActionButtonsCell extends TableCell<User, Void>
- [ ] Déplacer toute la logique de création des boutons (SVG, styles, hover effects)
  - [ ] Méthode : createViewButton(), createEditButton(), createDeleteButton()
  - [ ] Méthode : setupButtonHoverEffects(Button, SVGPath, String normalBg, String hoverBg)

<br>

### 🎭 Créer une classe StyleConstants ou ButtonStyleBuilder

- [X] Classe ButtonStyleBuilder avec pattern Builder
  - [x] Méthode : buildPrimaryStyle(), buildDangerStyle(), buildSuccessStyle()
  - [X] Stocker les couleurs et styles dans des constantes
  - [X] Méthode : withHoverEffect(), withIconColor()

<br>

### 📝 Extraire la logique des dialogues

- [ ] Créer une classe UserDialogFactory
  - [ ] Méthode : createEditDialog(User user) → retourne Dialog<User>
  - [ ] Méthode : createCreateDialog() → retourne Dialog<User>
  - [ ] Méthode : buildUserFormGrid(User user, boolean isEdit) → retourne GridPane
  - [ ] Méthode : setupFormValidation(Dialog, TextField...)

<br>

### 🌐 Séparer la logique HTTP/API

- [ ] Créer une classe UserApiService
  - [ ] Méthode : fetchAllUsers() → retourne CompletableFuture<List<User>>
  - [ ] Méthode : createUser(User user) → retourne CompletableFuture<User>
  - [ ] Méthode : updateUser(User user) → retourne CompletableFuture<User>
  - [ ] Méthode : deleteUser(int userId) → retourne CompletableFuture<Void>
- [ ] Injecter BEARER_TOKEN et API_URL via constructeur

<br>

### 💾 Améliorer la gestion du mode offline

- [ ] Créer une classe UserRepository
  - [ ] Méthode : getUsers() → gère automatiquement online/offline
  - [ ] Méthode : saveUser(User user) → gère automatiquement online/offline
  - [ ] Méthode : updateUser(User user) → gère automatiquement online/offline
  - [ ] Méthode : deleteUser(int userId) → gère automatiquement online/offline
- [ ] Centraliser la logique NetworkService.isOnline()

<br>

### ✅ Extraire la validation

- [ ] Créer une classe UserValidator
  - [ ] Méthode : validateEmail(String email) → retourne Boolean
  - [ ] Méthode : validatePhone(String phone) → retourne Boolean
  - [ ] Méthode : validateUser(User user) → retourne Boolean
<br>

### 🔔 Centraliser les alertes

- [X] Créer une classe AlertHelper ou utiliser un pattern Observer
  - [X] Méthode : showSuccess(String message)
  - [X] Méthode : showError(String message)
  - [X] Méthode : showWarning(String message)
  - [X] Méthode : showConfirmation(String message, Runnable onConfirm)

<br>

### 🎯 Extraire la logique de rôles

- [ ] Créer une classe RoleMapper ou enum RoleType
  - [ ] Méthode : getRoleById(int id) → retourne Role
  - [ ] Méthode : getRoleByName(String name) → retourne Role
  - [ ] Supprimer la méthode getRoleId() et utiliser directement Role.getId()

<br>

### 🗂️ Améliorer l'organisation du TableView

- [ ] Créer une classe UserTableConfigurator
  - [ ] Méthode : configureColumns(TableView<User> table)
  - [ ] Méthode : setupCellValueFactories()
  - [ ] Méthode : setupColumnWidths()
  - [ ] Méthode : setupActionColumn()

<br>

### 🔄 Améliorer le parsing JSON

- [ ] Créer une classe JsonMapper (wrapper autour d'ObjectMapper)
  - [ ] Méthode : parseUsers(String json) → retourne List<User>
  - [ ] Méthode : toJson(User user) → retourne String
- [ ] Gérer les exceptions de manière centralisée

<br>

### 🏗️ Structure générale recommandée
```
controller/
  └── UsersController.java (simplifié, orchestrateur)
service/
  ├── UserApiService.java
  ├── UserRepository.java
  └── UserValidator.java
ui/
  ├── UserDialogFactory.java
  ├── UserTableConfigurator.java
  ├── UserActionButtonsCell.java
  └── ButtonStyleBuilder.java
util/
  ├── AlertHelper.java
  ├── RoleMapper.java
  └── JsonMapper.java
```

<br>

### 🎯 Priorités de refactoring

1. **Haute priorité :** UserApiService, UserRepository, UserValidator
2. **Moyenne priorité :** UserDialogFactory, AlertHelper, ButtonStyleBuilder
3. **Basse priorité :** UserTableConfigurator, UserActionButtonsCell

<br>

### 📌 Bonus - Améliorations supplémentaires

- [ ] Utiliser l'injection de dépendances (ex: avec Spring ou manuel)
- [ ] Implémenter un pattern MVC/MVP plus strict
- [ ] Ajouter des tests unitaires pour chaque service
- [ ] Utiliser des Optional au lieu de null checks
- [ ] Implémenter un système de logging (SLF4J)
- [ ] Ajouter des constantes pour les magic strings (messages, styles)