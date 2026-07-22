# 📗 Smart Money — Guide en français (à lire en premier)

Bonjour Luis. Ce guide résume **tout** en français : ce que fait l'app, comment
l'installer sur votre téléphone Android, et comment avoir les **données réelles**.

---

## 1. Ce que fait l'application

Smart Money analyse des actions et repère les **opportunités d'investissement**
avant qu'elles ne deviennent médiatiques. Pour chaque valeur, 5 agents donnent une
note, et un **score de conviction de 0 à 100** est calculé :

- **Analyste Quantitatif** — finances (valorisation, croissance, dette…) → 25 %
- **Analyste Business** — solidité de l'entreprise (marché, marges…) → 20 %
- **Potentiel Futur** — croissance à venir → 20 %
- **Analyste Contrarien** — repère ce qui n'est pas encore sur-médiatisé → 15 %
- **Analyste Géopolitique** — risque pays, exposition géostratégique, tensions
  (sanctions, conflits, chaînes d'approvisionnement) → 20 %. En ligne, les
  tensions proviennent de **GDELT** (surveillance mondiale de l'actualité,
  gratuite, sans clé), avec repli DuckDuckGo.

Chaque opportunité affiche aussi une **prévision de fluctuation** : tendance
(Haussière / Neutre / Prudente), volatilité attendue et niveau de risque
géopolitique — pour anticiper les mouvements futurs.

> Bloqué ? L'app ne peut plus rester coincée : un bouton **« Réinitialiser »**
> est disponible dans **Configuration → Dépannage**, et ouvrir l'adresse avec
> **`?reset=1`** à la fin remet tout à zéro.

Vous pouvez chercher **par symbole**, **par secteur** (Technologie, Santé,
Énergie…) ou **par thématique** (Intelligence Artificielle, Semi-conducteurs,
Cybersécurité, Énergie propre…) depuis la page **« Découvrir »**.

La page **« Entreprises »** ajoute une recherche des **sociétés françaises** via
**Pappers** (registre légal + comptes annuels) : cherchez par nom ou SIREN et
consultez forme juridique, dirigeants, chiffre d'affaires et résultat par année.
Cette fonction nécessite le **mode serveur** et une clé Pappers gratuite (voir
`DEPLOIEMENT.md`).

L'app est **entièrement en français** et affiche un **badge de source** :
🟢 « Données réelles » ou 🟠 « Données simulées », pour que vous sachiez toujours
à quoi vous avez affaire.

---

## 2. Deux modes — c'est important à comprendre

| Mode | Données | Besoin d'internet ? |
|------|---------|---------------------|
| **Sur l'appareil (hors-ligne)** | **Simulées** (pour tester l'app) | Non |
| **Se connecter au serveur** | **Réelles et à jour** (Yahoo Finance) | Oui, serveur en ligne |

➡️ Pour de **vraies** décisions, utilisez le mode serveur (voir §4).

---

## 3. Installer l'app sur le téléphone (Android)

Détails complets dans **`MOBILE_INSTALL.md`**. En résumé, deux options :

- **Option rapide (PWA)** : hébergez le dossier `smart-money-pwa.zip` (une fois
  décompressé) sur https://app.netlify.com/drop, ouvrez l'URL dans Chrome sur le
  téléphone, puis menu ⋮ → **« Installer l'application »**.
- **Vrai fichier `.apk`** : poussez le projet sur GitHub ; le fichier
  `.github/workflows/android.yml` compile l'APK automatiquement dans le cloud.
  Vous le téléchargez ensuite depuis l'onglet *Releases* directement sur le
  téléphone.

---

## 4. Avoir les DONNÉES RÉELLES (mise en ligne du serveur)

Détails complets dans **`DEPLOIEMENT.md`**. En résumé :

1. Mettez le projet sur **GitHub**.
2. Sur **Render.com** : New → **Blueprint** → votre dépôt. Le fichier
   `render.yaml` fait tout. Vous obtenez une URL, ex.
   `https://smart-money-api.onrender.com`.
3. Dans l'app : **Configuration → Se connecter au serveur** → collez l'URL.

✅ Le badge passe au vert « Données réelles · Yahoo Finance » et les scans
utilisent les vrais chiffres.

Options : ajoutez `ANTHROPIC_API_KEY` (votre **nouvelle** clé) sur Render pour les
analyses rédigées par Claude ; ajoutez une clé gratuite `FINNHUB_API_KEY`
(finnhub.io) si Yahoo est temporairement limité.

---

## 5. Rappels honnêtes

- Les données Yahoo gratuites sont fiables mais **légèrement différées** (pas du
  temps réel professionnel).
- Cet outil est une **aide à la recherche**, **pas un conseil financier**. Les
  décisions restent les vôtres.
- Pensez à **régénérer votre clé API Anthropic** puisque l'ancienne a été
  partagée en clair.

---

## 6. Où est quoi (dans le projet)

```
GUIDE_FR.md          ← ce guide
MOBILE_INSTALL.md    ← installer sur Android (PWA + APK)
DEPLOIEMENT.md       ← mettre le serveur en ligne (données réelles)
backend/             ← serveur Python (FastAPI, agents, vraies données)
frontend/            ← application (React, français, mode hors-ligne + serveur)
frontend/android/    ← projet Android (Capacitor) pour l'APK
.github/workflows/   ← compilation automatique de l'APK
render.yaml          ← configuration d'hébergement gratuit
```
