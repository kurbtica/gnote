-- Création de l'utilisateur on va
-- on utilise % pour autoriser la connexion depuis n’importe où, réseau local ou Internet
CREATE USER 'gnote'@'%' IDENTIFIED BY 'gnote';

-- Donner l'accès uniquement à la table gnote_bdd
CREATE DATABASE IF NOT EXISTS gnotes_bdd;
GRANT ALL PRIVILEGES ON gnotes_bdd.* TO 'gnote'@'%';

-- Appliquer les permissions
FLUSH PRIVILEGES;
