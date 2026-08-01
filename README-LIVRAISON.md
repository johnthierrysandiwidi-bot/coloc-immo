# ImmoColoc / ColocImmo — Projet complet (mise à jour de sécurité)

Plateforme web et mobile de colocation et de gestion de biens immobiliers.
Ce paquet contient l'intégralité du projet mis à jour, ainsi que le rapport de stage corrigé.

## Contenu

| Dossier / fichier | Description |
|---|---|
| `backend/` | API REST Spring Boot (JHipster) — **corrigée** (voir sécurité ci-dessous) |
| `frontend-web/` | Client web React + TypeScript (sources, sans `node_modules`) |
| `RoomMateBF-Android/` | Application mobile **Android native en Kotlin** (Jetpack Compose) |
| `Rapport_de_stage_2025-2026_corrige.docx` | Rapport corrigé (Flutter → Kotlin) |
| `Audit_Securite_ImmoColoc.docx` | Rapport d'audit de sécurité détaillé |

## Pile technologique réelle

- **Backend** : Spring Boot (Java), Spring Security + JWT, Spring Data JPA, MySQL.
- **Web** : React, TypeScript, Vite, Redux Toolkit, MUI.
- **Mobile** : **Kotlin + Jetpack Compose** (Android natif), Retrofit, Hilt, DataStore.
  Ce n'est PAS du Flutter — le rapport initial contenait cette erreur, désormais corrigée.

## Correctif de sécurité appliqué (faille IDOR)

Avant : tout utilisateur connecté pouvait accéder aux données d'un autre en changeant un
identifiant dans l'URL (rendez-vous, documents KYC, favoris, alertes, notifications, profils),
et les listes « personnelles » n'étaient pas filtrées côté serveur.

Après : un service central `AutorisationService` impose l'appartenance **côté serveur** :
- listes filtrées sur l'utilisateur courant (sauf administrateur) ;
- contrôle 403 sur chaque accès par identifiant (GET/PUT/PATCH/DELETE).

Ressources corrigées : RendezVous, Document, Favori, Alerte, Notification,
ProfilProprietaire, ProfilDemarcheur, Annonce (anti-énumération des brouillons).
Le web et le mobile bénéficient du correctif sans modification (même API).

Fichiers modifiés/créés (backend) :
- `service/security/AutorisationService.java` (nouveau)
- `web/rest/{RendezVous,Document,Favori,Alerte,Notification,ProfilProprietaire,ProfilDemarcheur,Annonce}Resource.java`
- `repository/RendezVousRepository.java`, `service/RendezVousService.java`, `service/impl/RendezVousServiceImpl.java`

## Lancer le projet

### Backend
```bash
cd backend
./mvnw compile           # compilation
./mvnw                   # démarre l'API (Tomcat intégré) sur http://localhost:8080
```
Base MySQL requise (voir `src/main/resources/config/application-*.yml`).

### Frontend web
```bash
cd frontend-web
npm install
npm run dev              # http://localhost:5173 (proxy /api vers le backend)
```

### Mobile Android (Kotlin)
Ouvrir `RoomMateBF-Android/` dans Android Studio, puis Run.
Adapter l'URL de l'API dans `app/src/main/java/bf/colocation/immo/core/Constants.kt` si besoin.

## À faire côté équipe

- Lancer `./mvnw compile` (l'environnement de génération n'avait pas accès à Maven Central).
- Adapter les tests `*ResourceIT` qui affirmaient l'ancien comportement non cloisonné des listes.
- Optionnel : retirer côté client les paramètres `userId` devenus inutiles (portée imposée par le serveur).
