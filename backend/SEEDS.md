# Jeux de données

## Ordre d'exécution

Les deux scripts se lancent **dans l'ordre**, depuis HeidiSQL
(clic droit Laragon → Database → Manage), base `colocationImmo` sélectionnée,
`Ctrl+O` → F9.

| # | Fichier | Contenu |
|---|---------|---------|
| 1 | `seed-01-referentiel-ouaga.sql` | 1 localité, **57 quartiers**, 24 types de biens |
| 2 | `seed-02-donnees-demo.sql` | 70 comptes, 100 biens, 309 photos, 250 annonces, 100 RDV, 80 favoris, 50 alertes, 100 notifications |

Prérequis : le backend a démarré **au moins une fois** (Liquibase crée les tables)
et le faker est désactivé (`application-dev.yml` → `contexts: dev`).

## Ce qui a changé dans le référentiel

Les **55 « Secteur N »** ont été retirés. Ils noyaient les vrais quartiers dans une
liste déroulante de 112 entrées dont la moitié n'avait aucun intérêt pour une
démonstration. Restent les 57 quartiers nommés : Ouaga 2000, Gounghin, Tampouy,
Somgandé, Patte d'Oie, Zone du Bois…

## Comptes créés

| Login | Rôle | Mot de passe |
|-------|------|--------------|
| `proprio01` … `proprio10` | Propriétaire | `Passer@123` |
| `demarcheur01` … `demarcheur10` | Démarcheur | `Passer@123` |
| `user01` … `user50` | Utilisateur | `Passer@123` |
| `admin` | Administrateur | `admin` (inchangé) |

Les mots de passe sont hachés en BCrypt `$2a$10$`, le format exact de JHipster.

## Le verrou du démarcheur reste démontrable

Les 10 démarcheurs ne sont **pas tous validés** — c'est délibéré :

- **7 validés** (`demarcheur01` … `demarcheur07`) : publient normalement.
- **2 en attente** (`demarcheur08`, `demarcheur09`) : documents déposés, publication **bloquée (403)**.
- **1 refusé** (`demarcheur10`) : motif de refus visible, doit redéposer une pièce.

Vous pouvez donc montrer le verrou en direct : connectez-vous en `demarcheur08`,
tentez de publier → 403. Validez son document en admin → la publication passe.

## Cohérence des données

- Les **terrains** n'ont ni chambres ni salles de bain, et ne sont qu'en **vente**.
- Les **colocations** ne portent que sur des studios, chambres ou logements de 3 pièces et plus.
- Les prix suivent le marché de Ouagadougou : location d'un studio 30–60 000 F,
  vente d'une parcelle 15–45 000 F/m², colocation 25–60 000 F/mois.
- Seuls les **démarcheurs validés** gèrent des biens (40 % du parc).
- Les annonces se répartissent entre PUBLIEE, BROUILLON, EXPIREE, SUSPENDUE, CLOTUREE :
  vous pouvez donc tester **renouveler** une annonce expirée.

## Photos

Les images pointent vers `picsum.photos` (libre de droits, sans clé d'API).
Chaque bien a 2 à 4 photos, la première marquée comme principale.
Une connexion internet est nécessaire pour les afficher.
