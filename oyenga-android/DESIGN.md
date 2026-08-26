# Application de la charte

Comment le **Google Design Kit** a été appliqué à OYENGA, et pourquoi la refonte
ne ressemble pas à la PWA d'origine.

---

## Le point de départ

La PWA codait ses couleurs à la main : un rouge `#9B221B`, des beiges, dix-huit
teintes de moments liturgiques choisies une par une. C'est ce qui l'empêchait
d'avoir un mode sombre et ce qui rendait ses contrastes irréguliers.

La refonte ne choisit plus aucune couleur d'interface. Elle donne **une** couleur
de marque à l'algorithme, et lit des rôles.

---

## 1. Couleur

`#9B221B` — le rouge liturgique du bandeau de la PWA — passé dans l'algorithme
HCT officiel de Google (`material-color-utilities`, celui du générateur du kit)
produit les 36 rôles Material 3, en clair et en sombre, contrastes garantis par
construction.

Le résultat est figé dans `ui/theme/Color.kt`. Aucune couleur n'est écrite en
dur ailleurs : on passe toujours par `MaterialTheme.colorScheme.*`.

| Rôle | Usage dans l'application |
|---|---|
| `primary` | Une action forte par écran : le bandeau des lectures, le bouton de lecture, le bouton d'enregistrement |
| `secondaryContainer` | Chips actifs, indicateur de navigation, boutons secondaires |
| `tertiaryContainer` | Encarts sponsorisés — visiblement distincts du contenu liturgique |
| `surfaceContainer*` | Cartes, feuilles modales, mini-lecteur |
| `error` | Suppressions uniquement, jamais décoratif |

**Mode sombre.** Il n'y a rien à écrire : les deux schémas viennent du même
algorithme et l'application suit la préférence système.

### Les moments liturgiques

Les dix-huit teintes de la PWA (entrée, Kyrie, Gloria, communion…) sont
conservées comme **couleurs de contenu** : elles catégorisent un chant, au même
titre qu'une palette de série sur un graphique. Chaque teinte d'origine est
passée dans une palette tonale HCT pour produire un couple accent / conteneur
lisible en clair comme en sombre (`ui/theme/Moments.kt`).

L'information n'est jamais portée par la seule couleur : chaque moment a aussi
son **icône** et son **libellé**. Une vignette de communion reste distinguable
d'un Kyrie pour les ~8 % d'hommes qui ont une déficience de la vision des
couleurs.

---

## 2. Typographie

Deux familles, comme le prescrit la charte — plus une troisième réservée aux
contenus longs, parce que les paroles d'un chant et les lectures de la messe se
lisent, elles ne se parcourent pas.

| Famille | Rôle | Où |
|---|---|---|
| Roboto Flex | Titres, identité | Display, Headline, Title large |
| Roboto | Texte courant, interface | Body, Label, Title medium/small |
| Roboto Serif | Contenus longs | Paroles, lectures, introduction |

Les trois sont des polices **variables** : un seul fichier couvre toutes les
graisses, et l'axe `wght` est réglé par déclaration. L'échelle est celle de
Material 3, à quinze styles, sans taille intermédiaire inventée.

---

## 3. Espacement, formes, mouvement

**Grille de 4dp**, tokens `Space.s1` à `Space.s16`. Le défaut est **24dp** —
padding de carte et gouttière, la valeur Expressive.

**Formes** : cartes à 24dp, feuilles à 28dp, boutons entièrement arrondis. Un
petit élément dans un grand conteneur prend un rayon plus petit : les vignettes
de chant sont à 16dp, les chips à 8dp.

**Mouvement** : durées Expressive (200 / 350 / 500 ms), courbes
`emphasized-decelerate` à l'entrée et `emphasized-accelerate` à la sortie.
Rien n'est animé en `linear` sauf l'égaliseur du lecteur, qui est une boucle.
On n'anime que `transform` et `opacity`.

---

## 4. Iconographie

**Material Symbols Rounded**, un seul style dans tout le projet, cohérent avec
les rayons généreux de la charte. 91 icônes reprises des SVG officiels du kit et
converties en VectorDrawable.

Les tailles sont celles de la charte — 20dp dans un texte ou un chip, 24dp par
défaut, 40dp en tête de carte, 48dp dans un état vide. Passer par `OyIcon` évite
les tailles intermédiaires qui rendent les traits flous.

`ui/theme/OyengaIcons.kt` est la table de correspondance sens → icône : une icône
veut dire la même chose partout, et aucun `R.drawable.ic_*` n'est écrit ailleurs.

Les états actifs utilisent la variante **remplie** (`FILL 0 → 1`) : l'onglet
courant, un favori, un « j'aime ». L'état ne repose donc pas seulement sur la
couleur de l'indicateur.

---

## 5. Ce que la refonte a changé, au-delà du style

**Le mode sombre existe.** La PWA n'en avait pas.

**La lecture survit à l'application.** Une session média Media3 porte les
contrôles sur l'écran verrouillé et dans les notifications, et le son continue
en arrière-plan — le cas normal pendant une répétition, téléphone dans la poche.
La PWA s'arrêtait dès que l'onglet passait en arrière-plan.

**Les modifications sont réversibles.** Le corpus livré n'est plus réécrit sur
place : on enregistre l'écart avec l'original. Un chant modifié par erreur
revient à sa version imprimée d'un geste.

**La sauvegarde ne peut plus être coupée en deux.** L'état passe par un fichier
temporaire puis un renommage atomique. Sur mobile, c'est le scénario qui faisait
perdre le programme du dimanche.

**Quatre identifiants en double ont été corrigés** dans le corpus
(`KYRIE-013`, `MARIE-020`, `MARIE-025`, `MEDITATION-025` désignaient chacun deux
chants distincts). Latent sur le web, ce doublon fait planter une liste Compose
indexée par identifiant. Le chargement du corpus dédoublonne désormais aussi par
sécurité, et un test unitaire garde la porte fermée.

---

## Contrôles avant livraison

- [x] Aucune couleur d'interface codée en dur — que des rôles
- [x] Les deux schémas viennent du même algorithme ; le mode sombre suit le système
- [x] Un seul style d'icône dans tout le projet
- [x] Tailles d'icône limitées à celles de la charte
- [x] Espacements sur la grille de 4dp
- [x] Une seule action `primary` par écran
- [x] `error` réservé aux suppressions
- [x] Icônes décoratives sans description, icônes cliquables décrites
- [x] Aucune information portée par la seule couleur
- [x] Cibles tactiles ≥ 48dp (composants Material 3)
- [x] Le projet compile et les 23 tests unitaires passent (GitHub Actions)
- [ ] Rendu réel vérifié sur appareil — reste à faire une fois l'APK installé
