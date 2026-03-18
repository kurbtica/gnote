# 🔐 Sécurité — Authentification Hors-Ligne (Mode Offline)

> **Contexte BTS SIO** : Ce document explique la stratégie mise en place pour sécuriser  
> la connexion en mode hors-ligne dans l'application Gnotes, sans jamais stocker de mot de passe en clair.

---

## Le problème de départ

En mode hors-ligne, l'application ne peut pas contacter le serveur API pour vérifier si un mot de passe est correct.  
Il faut donc pouvoir **vérifier le mot de passe localement**, en s'appuyant uniquement sur des fichiers enregistrés sur le poste.

### ❌ Solution naïve (à ne pas faire)

Stocker le mot de passe directement dans un fichier JSON :

```json
{
  "email": "prof@lycee.fr",
  "password": "MonMotDePasseSecret"
}
```

**Problème :** Si un attaquant accède au fichier, il obtient le mot de passe en clair immédiatement. C'est une faille critique.

---

## Étape 1 — Le Hachage (Hash)

Un **hash** est une **empreinte numérique** d'un mot de passe.  
C'est une transformation à **sens unique** :

- ✅ On PEUT calculer l'empreinte d'un mot de passe : `SHA-256("azerty")` → `"a3f9c2..."`
- ❌ On ne PEUT PAS retrouver le mot de passe à partir de l'empreinte (mathématiquement impossible)

> On stocke l'**empreinte** dans le fichier, et non le mot de passe lui-même.  
> Pour vérifier, on refait l'empreinte de ce que l'utilisateur tape et on compare les deux.

### ❌ Problème du hash seul — Les Rainbow Tables

Un attaquant peut **pré-calculer à l'avance** les empreintes de millions de mots de passe courants et les stocker dans un tableau appelé **Rainbow Table** :

| Mot de passe   | Hash SHA-256   |
|----------------|----------------|
| `123456`       | `8d969ef...`   |
| `password`     | `5e88489...`   |
| `azerty`       | `a3f9c2...`    |

Si l'attaquant trouve notre fichier et voit l'empreinte `a3f9c2...`, il trouve `azerty` **instantanément** dans son tableau.  
Le hash seul n'est donc **pas suffisant**.

---

## Étape 2 — Le Sel (Salt)

La solution est d'ajouter un **sel** : une chaîne de caractères aléatoire générée à chaque connexion, que l'on mélange au mot de passe **avant** de le hacher.

```
Hash = SHA-256( mot_de_passe + sel )
```

Le **sel est stocké en clair** à côté du hash dans `auth_cache.json`.  
Il n'est **pas secret**, ce n'est pas un problème.

> L'effet : même si un attaquant connaît le sel, ses Rainbow Tables pré-calculées  
> sont **inutiles** car elles n'incluent pas ce sel spécifique.  
> Il devrait recalculer des millions de hashs avec **ce sel unique**, ce qui est très coûteux en temps.

### Résultat dans `auth_cache.json`

```json
{
  "email": "prof@lycee.fr",
  "salt": "xK9p2mZ4rT8vQj1n...",
  "hash": "7f4ab2c9e1d3f6a8..."
}
```

Le mot de passe **n'est nulle part**. Seuls son empreinte et le sel sont présents.

---

## Fonctionnement complet

### 🟢 Lors d'une connexion en LIGNE (serveur joignable)

```
1. L'utilisateur saisit son email + mot de passe
2. L'application envoie les identifiants au serveur via HTTP
3. Le serveur vérifie et répond : ✅ OK + données utilisateur + token
4. L'application génère un SEL aléatoire               → "xK9p2mZ4..."
5. L'application calcule : SHA-256("MonMDP" + sel)     → "7f4ab2c9..."
6. L'application sauvegarde dans auth_cache.json :
       { "email": "prof@lycee.fr", "salt": "xK9p2mZ4...", "hash": "7f4ab2c9..." }
```

### 🔴 Lors d'une connexion HORS-LIGNE (serveur injoignable)

```
1. L'utilisateur saisit son email + mot de passe
2. L'application lit auth_cache.json et récupère : email, salt, hash
3. Elle vérifie que l'email saisi = l'email du cache
4. Elle recalcule : SHA-256(mot_de_passe_saisi + salt_stocké)
5. Elle compare le résultat avec le hash stocké :
       ✅ Identique  → C'est le bon mot de passe → Accès autorisé
       ❌ Différent  → Mauvais mot de passe      → Accès refusé
```

> **Sécurité supplémentaire :** Seul le **dernier utilisateur ayant réussi une connexion en ligne**  
> sur ce poste peut se connecter en mode hors-ligne. On ne peut pas se connecter  
> avec n'importe quel compte, contrairement à l'ancienne version (email seul).

---

## Fichiers concernés

| Fichier | Rôle |
|---|---|
| `util/SecurityUtils.java` | Génération du sel (`generateSalt`) et hachage SHA-256 (`hashPassword`) |
| `service/AuthService.java` | Logique de connexion Online/Offline et appel à SecurityUtils |
| `service/LocalStorageService.java` | Lecture/écriture du fichier `auth_cache.json` |
| `auth_cache.json` | Cache local contenant l'email, le sel et le hash (jamais le mot de passe) |

---

## Résumé — Pourquoi cette approche est correcte

| Menace | Protection mise en place |
|---|---|
| Vol du fichier `auth_cache.json` | Le mot de passe n'est pas dedans, seulement un hash irréversible |
| Attaque Rainbow Table | Le sel aléatoire rend les tables pré-calculées inutilisables |
| Connexion avec n'importe quel email | Seul l'email du cache (dernier utilisateur online) est accepté |
| Mot de passe en clair quelque part | Jamais stocké ni en mémoire persistante ni dans aucun fichier |
