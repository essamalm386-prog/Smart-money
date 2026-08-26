# OYENGA — application Android

Refonte native de la PWA **OYENGA — Chants liturgiques**, écrite en Kotlin avec
Jetpack Compose et habillée avec le **Google Design Kit** (Material Design 3
Expressive).

Le répertoire complet — 254 chants BETI, paroles incluses — est embarqué dans
l'APK : l'application s'ouvre, se cherche et se lit sans réseau.

---

## Construire l'application

Prérequis : JDK 17, Android SDK 35 (platform + build-tools), Android Studio
Ladybug ou plus récent.

```bash
cd oyenga-android
./gradlew assembleDebug          # APK de debug
./gradlew test                   # tests unitaires (logique liturgique et corpus)
./gradlew installDebug           # installer sur un appareil branché
./gradlew assembleRelease        # APK de production (signature à configurer)
```

L'APK de debug sort dans `app/build/outputs/apk/debug/`.

## Récupérer l'APK sans rien installer

Le workflow **OYENGA — APK Android** compile le projet sur les runners GitHub et
publie l'APK de debug en artefact :

1. [Onglet Actions](https://github.com/essamalm386-prog/Smart-money/actions/workflows/oyenga-android.yml)
2. Ouvre la dernière exécution verte → section **Artifacts** → `oyenga-debug-apk`
3. Décompresse le `.zip`, transfère le `.apk` sur le téléphone, autorise
   l'installation depuis cette source, installe.

C'est un APK de **debug** : il s'installe à côté d'une éventuelle version de
production (identifiant `cm.oyenga.app.debug`) et n'a pas besoin de signature de
publication.

---

## Ce que fait l'application

**Accueil.** Salutation, temps liturgique du jour, bandeau des textes de la
prochaine célébration, aperçu du programme de chants, fil de la communauté.

**Répertoire.** Recherche plein texte (titre, chorale, moment de la messe),
filtres par moment, langue, temps liturgique et thème, 254 chants.

**Lecteur.** Lecture réelle par ExoPlayer quand le chant a un lien audio,
défilement simulé sinon — le corpus livré n'a pas encore d'enregistrements, et
sans ce second régime l'application paraîtrait cassée sur ses propres données.
Mini-lecteur permanent, file d'attente, paroles, partition, favoris, playlists.
La session média expose les contrôles sur l'écran verrouillé, dans les
notifications et sur les casques Bluetooth ; le son continue en arrière-plan.

**Communauté.** Fil trié (communautés suivies d'abord), publications,
commentaires, abonnements, groupes publics et privés, encarts sponsorisés
signalés — uniquement dans cet espace, jamais dans les écrans liturgiques.

**Administration** (comptes responsables) — six onglets :

| Onglet | Ce qu'on y fait |
|---|---|
| Chants | Ajouter, modifier, étiqueter, rattacher audio et partition |
| Programmes | Préparer l'ordinaire de la messe, partie par partie, avec suggestion automatique |
| Lectures | Récupérer les textes AELF, les vérifier, rédiger l'introduction |
| Publications | Publier et modérer le fil |
| Publicités | Gérer les encarts de l'espace communauté |
| Compte | Comptes, clé de rédaction assistée |

---

## Deux mécanismes qui méritent une explication

### La suggestion de chants

`SuggestionEngine` classe le répertoire pour une partie de la messe donnée. Le
score additionne des correspondances factuelles et l'ordre des poids suit
l'ordre de contrainte de la liturgie :

| Correspondance | Poids |
|---|---|
| Le chant convient à la partie de la messe | +1 (socle) |
| Il est étiqueté pour la fête célébrée | +4 |
| Il est étiqueté pour le temps liturgique | +3 |
| Il porte le thème dominant retenu | +2 |
| Il est étiqueté pour un **autre** temps | −1 |

Un chant étiqueté pour un autre temps est rétrogradé, jamais exclu : la chorale
garde le dernier mot. La qualité des propositions dépend directement de
l'étiquetage fait dans l'onglet Chants.

### L'introduction aux lectures

`IntroductionBuilder` compose localement le texte que le diacre proclame avant
la liturgie de la Parole : salutation, contexte, fil conducteur, une explication
par lecture, prière finale. Aucun réseau, aucune clé — ce qui compte pour un
office : le texte existe même sans connexion.

La rédaction assistée (onglet Compte → clé API Anthropic) ne remplace pas ce
socle, elle le développe quand le responsable le demande. La clé appartient au
responsable, reste dans les données privées de l'application sur son téléphone,
et n'est transmise qu'à l'API Anthropic au moment d'une rédaction.

---

## Architecture

```
cm.oyenga.app
├── data
│   ├── model/          Chants, liturgie, communauté — types sérialisables
│   ├── local/          Corpus embarqué, persistance JSON atomique, préférences
│   ├── remote/         Client AELF, rédaction assistée (SDK Anthropic)
│   ├── liturgy/        Introduction et algorithme de suggestion
│   ├── Seed.kt         Données livrées avec l'application
│   └── OyengaRepository.kt   Source de vérité unique
├── player/             Service de session média + moteur de lecture
└── ui
    ├── theme/          Thème M3 généré, typographie, formes, icônes, moments
    ├── components/     Briques partagées
    ├── screens/        Accueil, Répertoire, Lecteur, Communauté
    ├── sheets/         Feuilles modales
    └── admin/          Espace d'administration
```

Toute modification passe par `OyengaRepository.update` : l'état change en mémoire
puis est persisté. Aucun écran n'écrit sur disque — c'est ce qui garantit qu'un
chant modifié en administration apparaît immédiatement dans le répertoire, dans
le programme et dans le lecteur.

Le corpus livré (`assets/songs.json`) n'est jamais réécrit. Les modifications
sont stockées comme des **écarts** avec l'original, ce qui permet deux choses :
revenir à la version imprimée d'un geste, et recevoir les corrections du recueil
dans une mise à jour de l'application sans écraser le travail de la chorale.

---

## Ressources

Le dossier `licences/` porte les licences des ressources reprises du Google
Design Kit :

- **Material Symbols Rounded** (icônes, Apache 2.0) — converties en
  VectorDrawable ; un seul style dans toute l'application.
- **Roboto Flex**, **Roboto**, **Roboto Serif** (SIL Open Font License 1.1) —
  polices variables converties en TTF.

Le détail de l'application de la charte est dans [DESIGN.md](DESIGN.md).
