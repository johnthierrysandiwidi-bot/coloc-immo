# Déploiement de ColocImmo en ligne

Objectif : obtenir un lien web public à partager à des amis pour qu'ils utilisent l'application.

Architecture (3 morceaux) :

| Morceau | Dossier | Plateforme | Résultat |
|---|---|---|---|
| Base de données MySQL | — | Railway | base de données |
| API Spring Boot | `backend/` | Railway | `https://xxx.up.railway.app` |
| Site React | `frontend-web/` | Vercel | **le lien à partager** |

Les fichiers nécessaires sont déjà inclus : `backend/Dockerfile`, `backend/.dockerignore`,
le bloc CORS dans `backend/src/main/resources/config/application-prod.yml`,
`frontend-web/vercel.json` et `frontend-web/.env.production.example`.

---

## 0. Pousser le projet sur GitHub

Depuis la racine du projet :

```bash
git init
git add .
git commit -m "ColocImmo - deploiement"
git branch -M main
git remote add origin https://github.com/zongo337/coloc-immo.git
git push -u origin main
```

(Crée d'abord un dépôt vide `coloc-immo` sur github.com.)

---

## 1. Base MySQL + API sur Railway

1. Sur https://railway.app, connecte-toi avec GitHub, puis **New Project**.
2. **New → Database → Add MySQL** (elle se crée automatiquement).
3. **New → GitHub Repo → `coloc-immo`**. Dans les *Settings* du service :
   **Root Directory = `backend`** (pour utiliser le Dockerfile).
4. Onglet **Variables** du service backend — ajoute :

   ```
   SPRING_PROFILES_ACTIVE = prod
   SPRING_DATASOURCE_URL = jdbc:mysql://${{MySQL.MYSQLHOST}}:${{MySQL.MYSQLPORT}}/${{MySQL.MYSQLDATABASE}}?useUnicode=true&characterEncoding=utf8&useSSL=false&allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true
   SPRING_DATASOURCE_USERNAME = ${{MySQL.MYSQLUSER}}
   SPRING_DATASOURCE_PASSWORD = ${{MySQL.MYSQLPASSWORD}}
   JHIPSTER_SECURITY_AUTHENTICATION_JWT_BASE64_SECRET = NGZhM2QxYmM3ZTk4MjJhNTBkM2YxYzRiNmU3ODkwYWJjZGVmMTIzNDU2Nzg5MGFiY2RlZjEyMzQ1Njc4OTBhYmNkZWYxMjM0NTY3ODkw
   CORS_ALLOWED_ORIGINS = *
   ```

   `${{MySQL.…}}` = références vers ton service MySQL (adapte `MySQL` au nom exact du service).

5. **Settings → Networking → Generate Domain**. Note l'URL de l'API,
   ex. `https://coloc-immo-production.up.railway.app`.

---

## 2. Site React sur Vercel

1. Sur https://vercel.com, connecte-toi avec GitHub → **Add New Project** → importe `coloc-immo`.
2. **Root Directory = `frontend-web`**, Framework = **Vite** (détecté automatiquement).
3. **Environment Variables** :

   ```
   VITE_API_URL = https://coloc-immo-production.up.railway.app/api
   ```

   (URL de ton API Railway suivie de `/api`.)

4. **Deploy**. Tu obtiens `https://coloc-immo.vercel.app` → **c'est le lien à partager.**

---

## 3. Verrouiller le CORS (recommandé)

Une fois l'URL Vercel connue, dans les Variables Railway du backend, remplace :

```
CORS_ALLOWED_ORIGINS = https://coloc-immo.vercel.app
```

Le backend redéploie automatiquement.

---

## Notes

- **Coût** : Vercel gratuit en permanence. Railway = 5 $ de crédit d'essai (suffisant
  pour une démo / soutenance), puis ~1 $/mois.
- **Sécurité** : change la valeur de `JHIPSTER_SECURITY_AUTHENTICATION_JWT_BASE64_SECRET`
  par ta propre chaîne base64 aléatoire pour la production réelle.
- **Appli Android RoomMateBF** : ne fonctionnera à distance que si son URL de base
  pointe vers l'API Railway (au lieu de `localhost`).
