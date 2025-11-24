## Endpoints de l'API

**Base** : http://localhost:8080/api

**Auth endpoints** :
 - /auth/login
 - /auth/logout

**Users endpoints** :
- /users
- /users/%d
- /users/%d/notes

**Evaluations endpoints** :
- /evaluations
- /evaluations/%d
- /evaluations/type

**Notes endpoints** :
- /notes
- /notes/%d

**Matieres endpoints**
- /matieres
- /matieres/%d

**Etudiants endpoint**
- /etudiants
- /etudiants/%d        ->  /users/%d
- /etudiants/%d/notes  ->  /users/%d/notes