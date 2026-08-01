# RoomMate BF — Application Android

Application mobile native (Kotlin + Jetpack Compose) pour la plateforme de colocation/immobilier
**RoomMate BF**, connectée au backend Spring Boot (JHipster).

## Stack technique
- **Kotlin** + **Jetpack Compose** (Material 3)
- Architecture **MVVM** + **StateFlow**
- **Hilt** (injection de dépendances)
- **Retrofit** + **OkHttp** + **Moshi** (réseau/JSON)
- **DataStore** (stockage du token JWT)
- **Coil** (chargement d'images)
- **Navigation Compose** (+ barre de navigation basse)

## Fonctionnalités
- Connexion / inscription (JWT + **refresh token automatique** sur expiration)
- Liste des annonces : recherche par titre, filtres par type (Location / Colocation / Vente),
  **pagination au défilement**
- Détail d'annonce : carrousel de photos, caractéristiques du bien, prix
- **Ajout aux favoris**
- **Demande de visite** (sélecteur de date + heure)
- Mes favoris (avec suppression)
- Notifications
- Profil + déconnexion

## Prérequis
- Android Studio (Ladybug ou plus récent)
- JDK 17
- Un émulateur Android **ou** un appareil physique
- Le backend Spring Boot qui tourne (par défaut sur le port 8080)

## Installation
1. Ouvrir le dossier `RoomMateBF-Android` dans Android Studio (**File → Open**).
2. Laisser Android Studio synchroniser Gradle (télécharge les dépendances).
3. Lancer le backend Spring Boot (`./mvnw` ou depuis ton IDE) sur `http://localhost:8080`.
4. Cliquer sur **Run ▶** (choisir l'émulateur ou l'appareil).

## Configuration de l'URL du backend — IMPORTANT
L'URL est définie dans `app/build.gradle.kts` (champ `BASE_URL`).

- **Émulateur Android** : `http://10.0.2.2:8080/`
  (`10.0.2.2` = « localhost » du PC vu depuis l'émulateur). C'est la valeur par défaut ✅
- **Appareil physique** (téléphone réel sur le même Wi-Fi) : remplace par l'IP locale de ton PC,
  par ex. `http://192.168.1.15:8080/`. Trouve l'IP avec `ipconfig` (Windows).
  Pense à autoriser le port 8080 dans le pare-feu Windows.

Après modification, relance une synchro Gradle.

## Note sur les images
Le backend renvoie `photoUrl` / `photos` sous forme de chaînes. La fonction `String?.toImageUrl()`
(dans `core/Constants.kt`) gère 3 cas : URL absolue, chemin `/api/files/...`, ou simple nom de
fichier (supposé dans `/api/files/images/`). **Si tes images ne s'affichent pas**, regarde la vraie
valeur renvoyée par l'API et ajuste la dernière branche de `toImageUrl()`.

## Structure du projet
```
app/src/main/java/bf/colocation/immo/
├── App.kt                       # Application Hilt
├── MainActivity.kt              # Point d'entrée Compose
├── core/                        # Constantes, formatage dates, helpers
├── data/
│   ├── local/TokenManager.kt    # Stockage du token (DataStore)
│   ├── remote/                  # ApiService, intercepteurs JWT, refresh, DTO
│   └── repository/              # Repositories (auth, annonces, favoris, notifs, RDV)
├── di/NetworkModule.kt          # Module Hilt (Moshi/OkHttp/Retrofit)
└── ui/
    ├── theme/                   # Thème Material 3 (couleurs BF)
    ├── navigation/              # Routes + navigation + porte d'authentification
    ├── components/              # Composants réutilisables
    ├── auth/                    # Login + Register
    ├── annonces/                # Liste + détail + carte + dialogue visite
    ├── favoris/                 # Favoris
    ├── notifications/           # Notifications
    └── profil/                  # Profil + déconnexion
```

## Pistes d'amélioration (non incluses)
- Notifications push Firebase (l'endpoint `device-tokens` est déjà câblé côté API)
- Écran « Mes rendez-vous » (l'appel API existe déjà dans `ApiService`)
- Publication d'annonces pour propriétaires/démarcheurs + upload d'images
- Détection « déjà en favori » sur l'écran de détail
