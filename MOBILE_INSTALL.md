# 📱 Installer Smart Money sur Android

L'app fonctionne **100 % hors-ligne, sans serveur** : le moteur de score tourne
directement dans l'application. (Vous pouvez aussi la connecter au backend
FastAPI depuis l'écran *Configuration* → *Source de données*.)

Il y a **deux façons** de l'avoir sur votre téléphone. La première est immédiate.

---

## ✅ Route 1 — Installer la PWA (la plus rapide, aucune compilation)

Une PWA s'installe comme une vraie app (icône sur l'écran d'accueil, plein écran,
hors-ligne). Il faut juste servir le dossier `frontend/dist/` en HTTPS une fois.

### Étape A — Publier le dossier `dist/` (30 secondes, gratuit)

Le dossier déjà compilé est fourni : **`smart-money-pwa.zip`** (décompressez-le).

Choisissez UN hébergeur gratuit :

- **Netlify Drop** — allez sur https://app.netlify.com/drop et glissez le dossier
  `dist` décompressé. Vous obtenez une URL `https://...netlify.app` instantanée.
- **Cloudflare Pages** / **Vercel** / **GitHub Pages** — même principe.

> Astuce : il faut du **HTTPS** pour que l'installation PWA soit proposée. Les
> hébergeurs ci-dessus le fournissent automatiquement.

### Étape B — Installer sur le téléphone

1. Ouvrez l'URL dans **Chrome** sur votre Android.
2. Menu **⋮** → **« Ajouter à l'écran d'accueil »** / **« Installer l'application »**.
3. L'icône Smart Money apparaît. Ouverte, elle est en plein écran et marche
   hors-ligne.

---

## 📦 Route 2 — Obtenir un vrai fichier `.apk` à installer

Le projet **Android (Capacitor) est déjà configuré** dans `frontend/android/`.
Un `.apk` ne peut pas être compilé dans l'environnement où j'ai travaillé (accès
bloqué aux serveurs Android de Google), mais il se compile normalement ailleurs.
Le plus simple : le laisser se construire **dans le cloud via GitHub Actions**.

### Option 2A — Compilation automatique dans le cloud (recommandé, aucun PC requis)

1. Créez un dépôt GitHub et poussez le contenu du projet :
   ```bash
   cd smart-money
   git init && git add . && git commit -m "Smart Money"
   git branch -M main
   git remote add origin https://github.com/VOTRE_COMPTE/smart-money.git
   git push -u origin main
   ```
2. Le workflow **`.github/workflows/android.yml`** se lance tout seul. Sinon,
   onglet **Actions** → *Build Android APK* → **Run workflow**.
3. Quand c'est vert (~5 min) : l'APK est disponible
   - dans **Actions → le run → Artifacts → `smart-money-apk`**, et
   - dans **Releases → `mobile-latest` → `smart-money.apk`**.
4. Ouvrez la page **Release** depuis Chrome **sur votre téléphone**, téléchargez
   `smart-money.apk`, ouvrez-le. Autorisez « Installer des applis inconnues »
   pour Chrome si demandé. → Installé. 🎉

### Option 2B — Compiler localement (si vous avez un PC)

Nécessite Node 20+, JDK 21, Android SDK (ou Android Studio) :
```bash
cd frontend
npm ci
npm run build
npx cap sync android
cd android
./gradlew assembleDebug
# APK ici : frontend/android/app/build/outputs/apk/debug/app-debug.apk
```

### Option 2C — Sans GitHub ni PC : PWABuilder

Après avoir hébergé la PWA (Route 1, étape A), allez sur https://www.pwabuilder.com,
collez votre URL, section **Android → Generate Package**. Il produit un `.apk` /
`.aab` téléchargeable.

---

## Résumé

| Ce que vous voulez | Route | Effort |
|--------------------|-------|--------|
| L'app maintenant, comme une app | Route 1 (PWA) | Héberger `dist/` + « Installer » |
| Un vrai fichier `.apk` | Route 2A (GitHub Actions) | Pousser sur GitHub, télécharger l'APK |
| `.apk` avec un PC | Route 2B | `gradlew assembleDebug` |

Dans tous les cas l'app est **autonome et hors-ligne** par défaut.
