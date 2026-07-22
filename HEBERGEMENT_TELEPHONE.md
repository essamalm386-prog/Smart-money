# 📲 Mettre le serveur en ligne — guide pour téléphone

Objectif : avoir un serveur en ligne qui va chercher les **vraies données**
(cours Yahoo Finance, actualités géopolitiques GDELT, entreprises Pappers) pour
que votre téléphone les affiche.

Deux méthodes. La **Méthode A** est la plus fiable (serveur permanent). La
**Méthode B** se fait entièrement au téléphone mais le serveur n'est actif que
tant que la page tourne (bien pour tester).

---

## ✅ Méthode A — Render (permanent) · ~15 min sur un ordinateur, UNE fois

C'est la meilleure : après ça, tout se passe sur le téléphone pour toujours.

### 1. Créer un compte GitHub (gratuit)
- Sur l'ordinateur, allez sur https://github.com → **Sign up**.
- Choisissez un identifiant, un email, un mot de passe. Validez l'email.

### 2. Déposer le code
- Décompressez `smart-money.zip` (clic droit → Extraire).
- Sur GitHub : bouton **+** (en haut à droite) → **New repository**.
- Nom : `smart-money` → cochez **Public** → **Create repository**.
- Sur la page du dépôt vide, cliquez **« uploading an existing file »**.
- **Glissez-déposez le dossier `smart-money` décompressé** dans la page
  (GitHub garde la structure des dossiers). Attendez la fin.
- Cliquez **Commit changes**.

### 3. Déployer sur Render (gratuit)
- Allez sur https://render.com → **Get Started** → connectez-vous avec GitHub.
- **New +** → **Blueprint** → choisissez votre dépôt `smart-money` → **Apply**.
- Render lit le fichier `render.yaml` et crée le serveur. Attendez le vert (~4 min).
- En haut, copiez l'**URL** du service, ex. `https://smart-money-api.onrender.com`.

### 4. Brancher le téléphone
- Dans l'app : **Configuration → Se connecter au serveur** → collez l'URL → Enregistrer.
- ✅ Le badge passe au vert « Données réelles ». Terminé !

> Vérif : ouvrez `https://VOTRE-URL/api/health` dans un navigateur → vous devez
> voir `{"status":"ok",...}`.

---

## 📱 Méthode B — Replit (100 % au téléphone) · pour tester

Sur l'offre gratuite, le serveur tourne tant que l'onglet est actif. Pratique
pour essayer les vraies données tout de suite, sans ordinateur.

### 1. Créer un compte
- Sur le téléphone, allez sur https://replit.com → **Sign up** (email ou Google).

### 2. Créer un projet Python
- Bouton **+ Create Repl** → Template **Python** → **Create Repl**.

### 3. Envoyer le code
- Dans la liste des fichiers (à gauche), touchez les **trois points (⋮)** →
  **Upload file** → choisissez `smart-money.zip`.
- Ouvrez l'onglet **Shell** (console) et tapez ces lignes, une par une :
  ```bash
  unzip smart-money.zip
  cd smart-money/backend
  pip install -r requirements.txt
  uvicorn app.main:app --host 0.0.0.0 --port 8080
  ```
- Au bout d'un moment, une petite fenêtre de navigateur (Webview) s'ouvre avec
  une **adresse** (ex. `https://xxxx.replit.dev`). **Copiez cette adresse.**

### 4. Brancher le téléphone
- Dans l'app Smart Money : **Configuration → Se connecter au serveur** → collez
  l'adresse → Enregistrer.
- ✅ Vraies données affichées.
- ⚠️ Si vous fermez Replit, relancez la dernière commande `uvicorn ...` pour
  réactiver le serveur.

---

## Options (les deux méthodes)

- **Actualités géopolitiques (GDELT)** : automatique, gratuit, rien à configurer.
- **Analyses rédigées par l'IA (Claude)** : ajoutez `ANTHROPIC_API_KEY` (votre
  nouvelle clé) dans les variables d'environnement du serveur, et `USE_LLM=true`.
- **Entreprises françaises (Pappers)** : ajoutez `PAPPERS_API_KEY` (clé gratuite
  sur pappers.fr/api).
- **Cours plus fiables (Finnhub)** : ajoutez `FINNHUB_API_KEY` (finnhub.io) si
  Yahoo est limité.

---

## Mon conseil

Si vous pouvez emprunter un ordinateur 15 minutes, faites la **Méthode A** : le
serveur restera en ligne tout seul et vous n'aurez plus jamais à y toucher. Sinon
la **Méthode B** vous permet d'essayer les vraies données immédiatement.

Dites-moi à quelle étape vous êtes et je vous débloque en temps réel. 🙂
