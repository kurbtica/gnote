# 🚑 Guide de secours Git & GitHub

> Résoudre les problèmes les plus fréquents sans paniquer.  
> Garder un historique propre, éviter les merges foireux, et reprendre le contrôle.

---

### 🧹 1. Annuler un commit local (non poussé)
➤ Objectif : retirer un commit que tu viens de faire

## Supprime le dernier commit mais garde les fichiers modifiés
> git reset --soft HEAD~1

## Supprime le dernier commit et les modifications
> git reset --hard HEAD~1

## 🔁 2. Rejouer un ancien commit après un autre (ex: ordre des commits)
➤ Objectif : replacer ton commit après celui de quelqu’un d’autre

> git reflog                     # Trouve le hash de ton ancien commit  
> git cherry-pick <hash_commit>  # Le rejoue sur la branche actuelle

## 🧩 3. Supprimer un commit de merge foireux
➤ Objectif : garder les bons commits, virer un merge mal fait

> git log --oneline              # Trouve le hash juste avant le merge  
> git rebase -i <hash_avant_merge>
### Supprime la ligne du merge dans l'éditeur (ou marque "drop")

    🔒 Termine avec :

    git push origin <branche> --force-with-lease

## 🕵️‍♂️ 4. Retrouver un commit perdu après un reset
➤ Objectif : récupérer ton travail disparu

> git reflog             # Trouve le commit perdu  
> git checkout <hash>    # Vérifie le contenu  
> git cherry-pick <hash> # Le récupère dans ta branche actuelle

## 🧭 5. Revenir à un état propre (annuler tous les changements locaux)

> git reset --hard origin/<branche>  
> git clean -fd

## 🧠 6. Résoudre un conflit de merge

> Ouvre le fichier en conflit  
> Garde uniquement le bon code  
> Marque comme résolu :  
> git add <fichier>  
> git commit

> IntelliJ : clique sur Resolve Conflicts → Accept Yours/Theirs → Apply.

## 🚀 7. Récupérer les changements distants avant de pousser

> git fetch origin  
> git rebase origin/<branche>  
> puis  
> git push

✅ Avantage du rebase : pas de merge inutile dans l’historique.

## 🧱 8. Supprimer le dernier commit sur GitHub (déjà poussé)
⚠️ Attention : modifie l’historique distant !

> git reset --hard HEAD~1  
> git push origin <branche> --force-with-lease

## 🌿 9. Repartir d’une base propre sans casser la branche principale

> git checkout -b fix/clean  
> git cherry-pick <hash_des_commits_importants>  
> git push origin fix/clean

## 💡 Tips rapides

> git stash → sauvegarde temporaire des modifs  
> git restore . → annule tous les fichiers modifiés  
> git log --graph --oneline --decorate --all → visualise les branches  
> git blame <fichier> → voir qui a modifié quoi

## 🧰 Bonus : alias utiles

Ajoute-les dans ton .gitconfig

```[alias]
  st = status
  lg = log --oneline --graph --decorate --all
  co = checkout
  br = branch
  cm = commit -m
  amend = commit --amend --no-edit
  ```