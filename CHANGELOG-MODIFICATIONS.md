# Modifications appliquées

## 1. Correction de l'inscription

### Web — `frontend-web/src/pages/RegisterPage.tsx`
- Validation Yup du champ `login` alignée sur le `LOGIN_REGEX` du serveur
  (`^[_.@A-Za-z0-9-]+$`), avec `.trim()`.
- Normalisation avant envoi : suppression des accents, passage en minuscules,
  suppression des espaces, et garde-fou si le résultat est vide.
  C'était la cause du `rejected value []` renvoyé par Spring Validation.
- `firstName`, `lastName` et `email` sont également nettoyés.

> Après modification : relancer `npm run dev` ou `npm run build`
> (l'ancien bundle en cache reproduisait l'erreur).

### Mobile — `data/repository/AuthRepository.kt`
- L'ancien code traduisait **tout** code HTTP 400 par
  « Données invalides (mot de passe trop court ?) », ce qui masquait la vraie
  cause. Le corps `ProblemDetail` de JHipster est désormais lu et traduit :
  identifiant déjà pris, email déjà utilisé, mot de passe refusé…
- Nouvelle extension `String.normaliserLogin()` : accents retirés, minuscules,
  caractères interdits supprimés.

### Mobile — `ui/auth/RegisterViewModel.kt`
- `onLogin()` normalise la saisie au fil de la frappe.
- `valider()` contrôle la longueur et le format de l'identifiant, le format de
  l'email, et remonte des messages explicites.

## 2. Nouvelles fonctionnalités mobiles

| Fonction | Fichiers |
|---|---|
| Publier une annonce | `ui/publication/PublierAnnonceViewModel.kt`, `ui/publication/PublierAnnonceScreen.kt`, `data/repository/PublicationRepository.kt` |
| Créer / supprimer une alerte | `ui/alertes/AlertesViewModel.kt`, `ui/alertes/AlertesScreen.kt`, `data/repository/AlerteRepository.kt` |
| Paiement des frais de visite | `ui/paiements/PaiementsScreen.kt` (réutilise `RendezVousViewModel`) |
| Rendez-vous | `ui/rendezvous/*` — déjà présent, inchangé |
| Référentiel villes/quartiers/types | `data/repository/ReferentielRepository.kt` |
| Nouveaux DTO | `data/remote/dto/PublicationDtos.kt` |
| Endpoints ajoutés | `data/remote/ApiService.kt` |

### Publication : enchaînement respecté
1. `POST /api/immobiliers` (ou réutilisation d'un bien existant) ;
2. `POST /api/annonces` en statut `BROUILLON` ;
3. `PATCH /api/annonces/{id}/publier` pour appliquer les règles métier.

Court-circuiter l'étape 3 laisserait l'annonce invisible du catalogue.

### Paiements
`GET /api/paiements` est réservé aux administrateurs : côté utilisateur, l'écran
liste les rendez-vous et interroge `GET /api/paiements/rendez-vous/{id}`, puis
`initier` + `simuler-reglement`. Le reçu (référence) est affiché après règlement.

## 3. Navigation remaniée

La barre inférieure ne tient que cinq entrées. Nouvelle répartition :

`Accueil` · `Annonces` · `Publier` · `Services` · `Profil`

L'onglet **Services** (`ui/navigation/ServicesScreen.kt`) regroupe : mes visites,
mes paiements, mes favoris, mes alertes, notifications.

Le raccourci « Alertes » du tableau de bord pointe désormais sur les vraies
alertes de recherche et non plus sur l'historique des notifications.

## 4. Points à vérifier côté serveur

- La table `authority` doit contenir `ROLE_PROPRIETAIRE` et `ROLE_DEMARCHEUR`,
  sinon l'utilisateur n'obtient que `ROLE_USER` et ne peut pas publier.
- Les référentiels doivent être alimentés par `seed-01-referentiel-ouaga.sql`,
  faute de quoi les listes déroulantes (ville, quartier, type de bien) sont vides.

## 5. Compilation

```bash
cd backend && ./mvnw spring-boot:run
cd frontend-web && npm install && npm run dev
cd RoomMateBF-Android && ./gradlew :app:assembleDebug
```
