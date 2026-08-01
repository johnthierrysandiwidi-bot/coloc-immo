# Mise en place des changements — ColocImmo

Ce guide reprend, dans l'ordre, tout ce qu'il faut faire pour appliquer les
modifications. Comptez une trentaine de minutes la première fois.

---

## 1. Remplacer les sources

Décompressez `ImmoColoc-projet-complet.zip`. Il contient trois dossiers :
`backend`, `frontend-web`, `RoomMateBF-Android`.

**Ne remplacez pas aveuglément votre dossier de travail.** Un fichier vous
appartient et n'est pas dans l'archive :

- `seed-referentiel-ouaga.sql` — votre jeu de données de Ouagadougou. Mettez-le
  de côté avant de copier, puis remettez-le.

Vérifiez également votre adresse de backend dans
`RoomMateBF-Android/app/build.gradle.kts` ligne 25 : l'archive contient
`http://10.0.2.2:8080/`, adaptée à l'émulateur. Sur téléphone physique, mettez
l'adresse IP de votre PC — **sans espace** (c'est ce qui faisait planter l'app).

---

## 2. Backend

Aucune migration de base n'est nécessaire : le schéma n'a pas changé.

```
cd backend
mvnw clean compile
mvnw
```

Sous Linux ou Mac, utilisez `./mvnw`.

Le `clean` importe ici : il évite qu'Hibernate réutilise des métadonnées mises en
cache dans `target/`, notamment après la correction de la génération des
identifiants de paiement.

**Vérification** : l'API répond sur `http://localhost:8080`.

---

## 3. Frontend web

```
cd frontend-web
npm install
npm run dev
```

Ouvrez `http://localhost:5173`.

**Vérification rapide** : le hero affiche une barre de recherche, et la page
« Mes rendez-vous » montre une colonne « Frais de visite ».

---

## 4. Corriger les images incohérentes

Le script `backend/src/main/resources/config/liquibase/images-coherentes-ouaga.sql`
remplace les visuels sans rapport avec les biens.

Ouvrez-le et exécutez ses sections **dans l'ordre** :

1. **Section 1 — diagnostic.** Ne modifie rien. Elle vous montre quelles URL
   posent problème. Lisez le résultat avant de continuer.
2. **Section 2 — sauvegarde.** Crée la table `image_sauvegarde`. Obligatoire.
3. **Section 3 — correction.** Remplace les images incohérentes par
   l'illustration correspondant au type d'annonce.
4. **Section 4 — contrôle.** Vérifie le résultat.

Les photographies que vous avez réellement téléversées ne sont jamais touchées.

Pour revenir en arrière :

```sql
DELETE FROM image;
INSERT INTO image SELECT * FROM image_sauvegarde;
```

---

## 5. Application mobile

Ouvrez `RoomMateBF-Android/` dans Android Studio.

1. Laissez la synchronisation Gradle se terminer. Elle télécharge une nouvelle
   dépendance (`desugar_jdk_libs`), qui rend les dates utilisables sur Android 7.
2. **Build → Clean Project**, puis **Build → Rebuild Project**.
3. Désinstallez l'ancienne version depuis le téléphone ou l'émulateur, pour
   éviter qu'un APK incomplet subsiste.
4. Lancez.

**Vérification** : l'application s'ouvre sur l'accueil ColocImmo, avec le bouton
« Parcourir les annonces ». La barre du bas affiche cinq onglets dont « Visites »,
et « Annonces » tient sur une seule ligne.

---

## 6. Si le mobile n'atteint pas le serveur

Les messages sont désormais explicites, et chacun oriente vers une cause
différente.

### « Connexion au serveur impossible »

Rien n'écoute à l'adresse indiquée.

- Le backend est-il démarré ?
- L'adresse IP est-elle la bonne ? Lancez `ipconfig` sur le PC et comparez avec
  `build.gradle.kts`. Elle change à chaque changement de réseau.

### « Délai dépassé »

Les paquets partent mais rien ne revient : quelque chose les bloque en silence.
C'est presque toujours le **pare-feu Windows**.

Pour ouvrir le port 8080 :

1. Menu Démarrer → « Pare-feu Windows Defender avec fonctions avancées de sécurité ».
2. **Règles de trafic entrant** → **Nouvelle règle**.
3. Type **Port** → **TCP** → port spécifique **8080**.
4. **Autoriser la connexion**.
5. Cochez au minimum **Privé** (votre réseau Wi-Fi domestique).
6. Nommez-la « ColocImmo backend ».

Vérifiez aussi que le téléphone et le PC sont sur **le même Wi-Fi** — pas le PC
en Ethernet et le téléphone en données mobiles.

**Test décisif** : ouvrez `http://VOTRE_IP:8080/api/annonces` dans le navigateur
**du téléphone**. Si du texte JSON s'affiche, le réseau fonctionne et le problème
est ailleurs. Si rien ne vient, c'est bien le pare-feu ou le réseau.

---

## 7. Contrôler que la sécurité fonctionne

C'était la demande initiale : empêcher un utilisateur d'accéder au compte d'un
autre. Pour le vérifier concrètement :

1. Connectez-vous avec un compte A, notez l'identifiant d'un de ses rendez-vous
   (visible dans l'URL ou via l'API).
2. Déconnectez-vous, connectez-vous avec un compte B.
3. Appelez `GET /api/rendez-vous/{identifiant-de-A}`.

**Résultat attendu : 403 Forbidden.** Auparavant, les données de A s'affichaient.

Le même test vaut pour `/api/documents/{id}`, `/api/favoris/{id}`,
`/api/alertes/{id}` et `/api/profil-proprietaires/{id}`.

---

## 8. Points restants

- Les tests d'intégration générés (`*ResourceIT`) vérifiaient l'ancien
  comportement des listes, non cloisonnées. Ils échoueront tant qu'ils n'auront
  pas été adaptés : c'est la conséquence attendue du correctif, pas une
  régression.
- Le rapport doit être relu sur un point : la partie mobile est en **Kotlin**,
  pas en Flutter. La version corrigée est fournie.
- Les illustrations livrées sont des dessins, pas des photographies. Pour la
  soutenance, de vraies photos de Ouagadougou seront plus convaincantes ; la
  section 5 du script SQL explique comment les intégrer sans rien casser.
