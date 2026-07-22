# 🌐 Mettre l'app en ligne pour avoir les DONNÉES RÉELLES

Rappel important : les vraies données (prix, bilans, actualités) sont récupérées
par le **serveur** (backend). Pour que votre téléphone affiche de vrais chiffres,
il faut donc **héberger le serveur en ligne**, puis dire à l'app de s'y connecter.

C'est **gratuit** et ça prend environ 10 minutes. Voici la méthode la plus simple.

---

## Étape 1 — Mettre le code sur GitHub

1. Créez un compte sur https://github.com (gratuit) si vous n'en avez pas.
2. Créez un nouveau dépôt (bouton **New**), par exemple `smart-money`.
3. Envoyez-y le contenu du projet (depuis un ordinateur) :
   ```bash
   cd smart-money
   git init && git add . && git commit -m "Smart Money"
   git branch -M main
   git remote add origin https://github.com/VOTRE_COMPTE/smart-money.git
   git push -u origin main
   ```

> Pas d'ordinateur sous la main ? Vous pouvez aussi importer le dossier
> directement sur GitHub via **Add file → Upload files** dans le navigateur.

---

## Étape 2 — Héberger le serveur sur Render (gratuit)

1. Créez un compte sur https://render.com (connexion avec GitHub, gratuit).
2. Cliquez sur **New ➜ Blueprint**.
3. Choisissez votre dépôt `smart-money`. Render lit le fichier **`render.yaml`**
   déjà inclus et prépare le service **`smart-money-api`** automatiquement.
4. Cliquez **Apply / Create**. Attendez que le statut passe au vert (~3-5 min).
5. Render vous donne une **URL publique**, par exemple :
   ```
   https://smart-money-api.onrender.com
   ```
   👉 **Copiez cette URL**, c'est l'adresse de votre serveur.

> Vérification : ouvrez `https://VOTRE-URL/api/health` dans un navigateur. Vous
> devez voir `{"status":"ok",...}`. Et `https://VOTRE-URL/docs` montre l'API.

---

## Étape 3 — Connecter votre téléphone au serveur

Dans l'application Smart Money (PWA installée ou APK) :

1. Ouvrez **Configuration**.
2. Section **Source de données** → choisissez **« Se connecter au serveur »**.
3. Collez l'URL du serveur (ex. `https://smart-money-api.onrender.com`).
4. Enregistrez.

À partir de là, l'app affiche les **données réelles** (Yahoo Finance) et le badge
passe au **vert « Données réelles · Yahoo Finance »**. Lancez un scan (par
symboles, par **secteur** ou par **thématique**) depuis « Découvrir ».

---

## Options utiles

- **Analyses rédigées par l'IA (Claude)** : dans Render, ouvrez le service
  `smart-money-api` → **Environment** → ajoutez `ANTHROPIC_API_KEY` = votre
  nouvelle clé, et passez `USE_LLM` à `true`. Les 4 agents rédigeront alors
  thèse, risques et catalyseurs. (Sans clé, les scores restent calculés
  normalement.)

- **Si Yahoo Finance est limité** (les hébergeurs gratuits partagent des adresses
  IP parfois bloquées par Yahoo) : créez une clé gratuite sur https://finnhub.io
  et ajoutez `FINNHUB_API_KEY` dans **Environment**. L'app basculera
  automatiquement sur cette source réelle.

- **Entreprises françaises (Pappers)** : pour activer la page « Entreprises »
  (recherche de sociétés françaises, dirigeants, chiffre d'affaires, résultat),
  créez une clé gratuite sur https://www.pappers.fr/api et ajoutez
  `PAPPERS_API_KEY` dans **Environment** du service Render. Sans cette clé, la
  page indique simplement que la source n'est pas configurée (aucune donnée
  inventée).

- **Mise en veille** : sur l'offre gratuite Render, le serveur s'endort après
  15 min d'inactivité ; le premier accès suivant prend ~30-50 s à se réveiller,
  puis c'est rapide. Normal.

---

## Autres hébergeurs (équivalents)

- **Railway** (https://railway.app) : New Project → Deploy from GitHub → dossier
  `backend` → commande de démarrage
  `alembic upgrade head && uvicorn app.main:app --host 0.0.0.0 --port $PORT`.
- **Fly.io**, **Koyeb** : même principe (un service web Python).

Dans tous les cas, vous obtenez une URL à coller dans l'app (Étape 3).

---

## ⚠️ Rappel honnête

Les données gratuites de Yahoo Finance sont fiables mais **légèrement différées**
(pas du temps réel professionnel). Et cet outil est une **aide à la décision**,
pas un conseil financier : les choix d'investissement restent les vôtres.
