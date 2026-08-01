# Installer ColocImmo sur un nouveau poste

Ce guide part d'un ordinateur vierge et va jusqu'à l'application qui tourne.
Comptez une heure la première fois, dont une bonne moitié en téléchargements.

---

## 1. Ce qu'il faut installer

| Outil | Version | Pour quoi | Vérifier avec |
|---|---|---|---|
| JDK | 17 ou plus | Compiler et lancer le backend | `java -version` |
| Node.js | 18 ou plus | Construire le site web | `node -v` |
| MySQL | 8 | Base de données | `mysql --version` |
| Android Studio | récent | Application mobile | — |

Sur Windows, **Laragon** installe MySQL en une fois et évite bien des réglages.

Point d'attention sur le JDK : si `java -version` affiche 8 ou 11, la compilation
échouera avec des erreurs de syntaxe incompréhensibles. C'est la cause la plus
fréquente d'échec sur un poste neuf.

---

## 2. Copier le projet

Décompressez `ImmoColoc-projet-complet.zip`. Vous obtenez trois dossiers :

```
backend/              API Spring Boot
frontend-web/         Site React
RoomMateBF-Android/   Application Android (Kotlin)
```

**Un fichier ne s'y trouve pas** : `seed-referentiel-ouaga.sql`, votre jeu de
données de Ouagadougou. Copiez-le depuis votre poste actuel, il est indispensable
pour retrouver vos annonces.

---

## 3. Base de données

La base se crée toute seule au premier démarrage : l'URL de connexion contient
`createDatabaseIfNotExist=true`. Vous n'avez rien à créer à la main.

En revanche, si votre MySQL exige un mot de passe root, ouvrez
`backend/src/main/resources/config/application-dev.yml` et ajoutez deux lignes
sous `datasource:`, juste après `url:` :

```yaml
    username: root
    password: votre_mot_de_passe
```

Une fois le backend démarré au moins une fois, chargez vos données :

```
mysql -u root -p colocationImmo < seed-referentiel-ouaga.sql
```

---

## 4. Backend

```
cd backend
mvnw clean compile
mvnw
```

Sous Linux ou Mac : `./mvnw`.

Le premier lancement télécharge les dépendances Maven — plusieurs minutes, c'est
normal. Liquibase crée ensuite les tables automatiquement.

L'API est prête quand le bandeau JHipster affiche `http://localhost:8080`.

Laissez ce terminal ouvert : il fait tourner le serveur.

---

## 5. Site web

Dans un **second terminal** :

```
cd frontend-web
npm install
npm run dev
```

Ouvrez `http://localhost:5173`.

Le proxy est déjà configuré : les appels `/api` partent vers le port 8080, rien
à régler.

Comptes de départ : `admin` / `admin` et `user` / `user`.

---

## 6. Application mobile

1. Ouvrez le dossier `RoomMateBF-Android/` dans Android Studio.
2. Laissez Gradle synchroniser (long la première fois).
3. **Build → Clean Project**, puis **Rebuild Project**.
4. Lancez sur un émulateur.

Sur émulateur, l'adresse `http://10.0.2.2:8080/` fonctionne telle quelle : c'est
ainsi que l'émulateur désigne le `localhost` du PC.

Sur **téléphone physique**, modifiez `app/build.gradle.kts` ligne 25 avec l'adresse
IP du PC :

```kotlin
buildConfigField("String", "BASE_URL", "\"http://192.168.1.42:8080/\"")
```

Trouvez cette adresse avec `ipconfig`. **Aucun espace après `http://`** — une
espace glissée là fait planter l'application au démarrage.

---

## 7. Quand le mobile ne joint pas le serveur

Les messages d'erreur sont explicites et orientent chacun vers une cause distincte.

**« Connexion au serveur impossible »** — rien n'écoute à cette adresse. Le backend
tourne-t-il ? L'adresse IP est-elle à jour ? Elle change à chaque changement de
réseau.

**« Délai dépassé »** — les paquets partent mais rien ne revient. C'est le
**pare-feu Windows** dans la quasi-totalité des cas. Ouvrez le port 8080 :

1. « Pare-feu Windows Defender avec fonctions avancées de sécurité »
2. Règles de trafic entrant → Nouvelle règle
3. Port → TCP → 8080 → Autoriser la connexion
4. Cochez au moins « Privé »

Vérifiez aussi que téléphone et PC sont sur **le même Wi-Fi**.

**Test décisif** : ouvrez `http://VOTRE_IP:8080/api/annonces` dans le navigateur
**du téléphone**. Du texte JSON s'affiche ? Le réseau va bien. Rien ? C'est le
pare-feu.

---

## 8. Vérifier que tout fonctionne

Dans l'ordre, en cinq minutes :

1. **Backend** — `http://localhost:8080/management/health` répond `{"status":"UP"}`.
2. **Web** — la page d'accueil affiche la barre de recherche et des annonces.
3. **Connexion** — `admin` / `admin` ouvre le tableau de bord.
4. **Sécurité** — la commande suivante doit renvoyer **401** :
   ```
   curl -i -X POST http://localhost:8080/api/rendez-vous/demander -H "Content-Type: application/json" -d "{}"
   ```
5. **Mobile** — l'application s'ouvre sur l'accueil ColocImmo, et « Parcourir les
   annonces » affiche la liste.

---

## 9. Les pièges déjà rencontrés

Ces erreurs ont toutes été vécues sur ce projet ; autant les éviter.

**`'.' n'est pas reconnu`** — vous avez tapé `./mvnw` sous Windows. C'est `mvnw`.

**`mvnw` introuvable** — vous êtes dans `frontend-web`. Maven vit dans `backend`.

**`Access denied for user`** — le mot de passe MySQL de l'étape 3.

**`Table 'sequence_generator' doesn't exist`** — ne devrait plus arriver, mais si
c'est le cas, un `mvnw clean compile` élimine les métadonnées Hibernate en cache.

**L'app mobile se ferme aussitôt** — une espace dans `BASE_URL`, ou un APK
incomplet d'un build échoué. Désinstallez puis reconstruisez.

**Port 8080 déjà pris** — changez-le dans `application-dev.yml` ligne 57, et
répercutez dans `vite.config.ts` et `build.gradle.kts`.
